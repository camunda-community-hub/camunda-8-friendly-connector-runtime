package org.camunda.runtime.facade;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.camunda.runtime.exception.TechnicalException;
import org.camunda.runtime.facade.dto.OutOfTheBoxConnector;
import org.camunda.runtime.facade.dto.dashboard.AuditLog;
import org.camunda.runtime.jsonmodel.Connector;
import org.camunda.runtime.security.annotation.IsAdmin;
import org.camunda.runtime.security.annotation.IsAuthenticated;
import org.camunda.runtime.service.ConnectorExecutionService;
import org.camunda.runtime.service.ConnectorStorageService;
import org.camunda.runtime.service.MonitoringService;
import org.camunda.runtime.service.OutOfTheBoxConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
@RequestMapping("/api/connectors")
public class ConnectorController extends AbstractController {

  private final Logger logger = LoggerFactory.getLogger(ConnectorController.class);

  @Autowired private ConnectorExecutionService connectorExecutionService;
  @Autowired private ConnectorStorageService connectorStorageService;
  @Autowired private OutOfTheBoxConnectorService outOfTheBoxConnectorService;
  @Autowired private MonitoringService monitoringService;

  @IsAdmin
  @PostMapping
  public ResponseEntity<Connector> save(@RequestBody Connector connector)
      throws TechnicalException {
    connectorStorageService.save(connector);
    monitoringService.addAuditLog(
        new AuditLog("CONNECTOR SAVED", connector.getName(), getAuthenticatedUsername()));

    return new ResponseEntity<>(connector, HttpStatus.CREATED);
  }

  @IsAuthenticated
  @GetMapping("/{name}")
  @ResponseBody
  public Connector get(@PathVariable String name) throws TechnicalException {
    return connectorStorageService.findByName(name);
  }

  @IsAdmin
  @DeleteMapping("/{name}")
  public void delete(@PathVariable String name) throws TechnicalException {
    connectorStorageService.deleteByName(name);
    monitoringService.addAuditLog(
        new AuditLog("CONNECTOR DELETION", name, getAuthenticatedUsername()));
  }

  @IsAuthenticated
  @GetMapping
  @ResponseBody
  public List<Connector> all() throws TechnicalException {
    return connectorStorageService.all();
  }

  @IsAdmin
  @GetMapping("/start/{name}")
  @ResponseBody
  public Connector start(@PathVariable String name) throws TechnicalException {
    Connector connector = connectorStorageService.findByName(name);
    connectorExecutionService.start(connector);
    monitoringService.addAuditLog(
        new AuditLog("CONNECTOR STARTS", name, getAuthenticatedUsername()));
    return connectorStorageService.save(connector);
  }

  @IsAdmin
  @GetMapping("/stop/{name}")
  @ResponseBody
  public Connector stop(@PathVariable String name) throws TechnicalException {
    Connector connector = connectorStorageService.findByName(name);
    connectorExecutionService.stop(connector);
    monitoringService.addAuditLog(
        new AuditLog("CONNECTOR STOPS", name, getAuthenticatedUsername()));
    return connectorStorageService.save(connector);
  }

  @PostMapping(
      value = "upload",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Connector upload(@RequestPart("File") List<MultipartFile> uploadedfiles)
      throws TechnicalException {
    if (uploadedfiles.size() > 0) {
      MultipartFile file = uploadedfiles.get(0);
      File target = connectorStorageService.storeJarFile(file);

      Connector connector = new Connector(file.getOriginalFilename(), target.getName());
      monitoringService.addAuditLog(
          new AuditLog(
              "CONNECTOR JAR UPDATED", file.getOriginalFilename(), getAuthenticatedUsername()));
      return connector;
    }
    return null;
  }

  @IsAuthenticated
  @GetMapping("/ootb/lastrelease")
  public String outOfTheBoxLastRelease() {
    return outOfTheBoxConnectorService.getLatestRelease();
  }

  @IsAuthenticated
  @GetMapping("/ootb/{release}")
  public List<OutOfTheBoxConnector> outOfTheBox(@PathVariable String release) {

    Map<String, String> connectors = outOfTheBoxConnectorService.listConnectors(release);
    List<OutOfTheBoxConnector> result = new ArrayList<>();
    for (String name : connectors.keySet()) {
      result.add(new OutOfTheBoxConnector(name, release));
    }
    return result;
  }

  @IsAdmin
  @PostMapping("/ootb/install")
  public Connector installOotb(@RequestBody OutOfTheBoxConnector outOfTheBoxConnector) {

    Connector connector =
        outOfTheBoxConnectorService.getConnector(
            outOfTheBoxConnector.getName(), outOfTheBoxConnector.getRelease());
    monitoringService.addAuditLog(
        new AuditLog("CONNECTOR INSTALLED", connector.getName(), getAuthenticatedUsername()));

    return connectorStorageService.save(connector);
  }

  @Override
  public Logger getLogger() {
    return logger;
  }
}
