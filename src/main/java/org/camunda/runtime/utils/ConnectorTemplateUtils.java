package org.camunda.runtime.utils;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.UUID;
import org.camunda.runtime.exception.TechnicalException;
import org.camunda.runtime.jsonmodel.Connector;

public class ConnectorTemplateUtils {
  private ConnectorTemplateUtils() {}

  public static JsonNode generateElementTemplate(Connector connector) {
    try {
      return JsonUtils.toJsonNode(generateElementTemplateAsString(connector));
    } catch (IOException e) {
      throw new TechnicalException("Error generating an element template", e);
    }
  }

  public static String generateElementTemplateAsString(Connector connector) {
    StringBuilder sb =
        new StringBuilder(
                "{\"$schema\": \"https://unpkg.com/@camunda/zeebe-element-templates-json-schema/resources/schema.json\",")
            .append("\"id\": \"")
            .append(UUID.randomUUID().toString())
            .append("\",")
            .append("\"name\": \"")
            .append(connector.getName())
            .append("\",")
            .append("\"appliesTo\": [\"bpmn:Task\"],")
            .append("\"elementType\": {\"value\": \"bpmn:ServiceTask\"},")
            .append("\"properties\": [")
            .append("{\"type\": \"Hidden\",\"value\": \"")
            .append(connector.getJobType())
            .append("\",\"binding\": {\"type\": \"zeebe:taskDefinition:type\"}}");
    for (String variable : connector.getFetchVariables()) {
      sb.append(",{\"label\": \"")
          .append(variable)
          .append("\",\"description\": \"")
          .append(variable)
          .append("\",\"value\": \"=")
          .append(variable)
          .append("\",\"type\": \"String\",\"feel\": \"optional\",")
          .append("\"binding\": {\"type\": \"zeebe:input\",\"name\": \"")
          .append(variable)
          .append("\"},\"constraints\": {\"notEmpty\": true}}");
    }
    sb.append("]}");
    return sb.toString();
  }
}
