package org.camunda.runtime.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.camunda.runtime.exception.TechnicalException;
import org.camunda.runtime.jsonmodel.Connector;
import org.camunda.runtime.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Service
public class OutOfTheBoxConnectorService {

  public static final String REPO = "camunda/connectors-bundle";
  public static final List<String> IGNORE = List.of("github");

  private Map<String, Map<String, String>> releaseConnectors = new HashMap<>();

  @Autowired private RestTemplate restTemplate;
  @Autowired private ConnectorStorageService connectorStorageService;

  public String getLatestRelease() {
    JsonNode response = get("https://api.github.com/repos/" + REPO + "/releases/latest");
    return response.get("name").asText();
  }

  private JsonNode get(String url) throws TechnicalException {
    try {
      return JsonUtils.toJsonNode(restTemplate.getForObject(url, String.class));
    } catch (RestClientException | IOException e) {
      throw new TechnicalException("Error reading " + url, e);
    }
  }

  private String getConnectorsUrl(String release) {
    JsonNode tree = get("https://api.github.com/repos/" + REPO + "/git/trees/" + release);
    JsonNode subtrees = tree.get("tree");
    for (JsonNode subtree : subtrees) {
      if ("connectors".equals(subtree.get("path").asText())) {
        return (String) subtree.get("url").asText();
      }
    }
    return null;
  }

  public Map<String, String> listConnectors(String release) {
    String connectorsUrl = getConnectorsUrl(release);
    JsonNode tree = get(connectorsUrl);
    JsonNode subtrees = tree.get("tree");
    Map<String, String> connectorUrls = new HashMap<>();
    for (JsonNode subtree : subtrees) {
      if ("tree".equals(subtree.get("type").asText())
          && !IGNORE.contains(subtree.get("path").asText())) {
        connectorUrls.put(subtree.get("path").asText(), subtree.get("url").asText());
      }
    }
    releaseConnectors.put(release, connectorUrls);
    return connectorUrls;
  }

  private String downloadMavenJar(String release, String name) {
    String mavenJarUrl = getMavenCentralUrl(release, name);
    String jarName = mavenJarUrl.substring(mavenJarUrl.lastIndexOf("/") + 1, mavenJarUrl.length());
    try {
      URL url = new URL(mavenJarUrl);
      URLConnection connection = url.openConnection();
      InputStream is = connection.getInputStream();

      File target = connectorStorageService.storeJarFile(jarName, is);

      return target.getName();
    } catch (IOException e) {
      throw new TechnicalException("Error downloading " + mavenJarUrl, e);
    }
  }

  public Connector getConnector(String name, String release) {
    try {
      Connector connector = new Connector();
      connector.setName(name + "-" + release);
      getVariablesAndJobType(connector, name, release);
      connector.setJarFile(downloadMavenJar(release, name));
      return connector;
    } catch (Exception e) {
      return null;
    }
  }

  private JsonNode getElementTemplate(String name, String release) {
    String connectorUrl = releaseConnectors.get(release).get(name);
    JsonNode tree = get(connectorUrl);
    JsonNode subtrees = tree.get("tree");
    for (JsonNode subtree : subtrees) {
      if ("element-templates".equals(subtree.get("path").asText())) {
        String elementTemplateUrl = subtree.get("url").asText();
        JsonNode elementTemplateTree = get(elementTemplateUrl);
        JsonNode elementTemplateSubtrees = elementTemplateTree.get("tree");
        for (JsonNode jsonFile : elementTemplateSubtrees) {
          String jsonEltTemplateRawRul =
              "https://raw.githubusercontent.com/"
                  + REPO
                  + "/"
                  + release
                  + "/connectors/"
                  + name
                  + "/element-templates/"
                  + jsonFile.get("path").asText();
          return get(jsonEltTemplateRawRul);
        }
      }
    }
    return null;
  }

  private void getVariablesAndJobType(Connector connector, String name, String release) {
    connector.setFetchVariables(new ArrayList<>());
    JsonNode template = getElementTemplate(name, release);
    JsonNode properties = template.get("properties");
    for (JsonNode prop : properties) {
      JsonNode binding = prop.get("binding");
      if ("zeebe:taskDefinition:type".equals(binding.get("type").asText())) {
        connector.setJobType(prop.get("value").asText());
      } else if ("zeebe:input".equals(binding.get("type").asText())) {
        connector.getFetchVariables().add(binding.get("name").asText());
      }
    }
  }

  private String getMavenCentralUrl(String release, String name) {
    String groupId = "";
    String artifactId = "";
    try {
      String pom =
          restTemplate.getForObject(
              "https://raw.githubusercontent.com/"
                  + REPO
                  + "/"
                  + release
                  + "/connectors/"
                  + name
                  + "/pom.xml",
              String.class);
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(new ByteArrayInputStream(pom.getBytes()));
      XPath xPath = XPathFactory.newInstance().newXPath();
      NodeList nodes =
          (NodeList) xPath.evaluate("/project/parent/groupId", doc, XPathConstants.NODESET);
      for (int i = 0; i < nodes.getLength(); ++i) {
        groupId = nodes.item(i).getTextContent();
      }
      nodes = (NodeList) xPath.evaluate("/project/artifactId", doc, XPathConstants.NODESET);
      for (int i = 0; i < nodes.getLength(); ++i) {
        artifactId = nodes.item(i).getTextContent();
      }
      return "https://repo.maven.apache.org/maven2/"
          + groupId.replaceAll("\\.", "/")
          + "/"
          + artifactId
          + "/"
          + release
          + "/"
          + artifactId
          + "-"
          + release
          + "-with-dependencies.jar";
    } catch (ParserConfigurationException
        | SAXException
        | IOException
        | XPathExpressionException e) {
      throw new TechnicalException("Error building the maven url from the pom", e);
    }
  }
}
