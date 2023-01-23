package org.camunda.runtime.facade;

import java.io.File;
import java.util.List;
import org.camunda.runtime.exception.TechnicalException;
import org.camunda.runtime.jsonmodel.Connector;
import org.camunda.runtime.security.annotation.IsAdmin;
import org.camunda.runtime.service.ConnectorExecutionService;
import org.camunda.runtime.service.ConnectorStorageService;
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

  @IsAdmin
  @PostMapping
  public ResponseEntity<Connector> save(@RequestBody Connector connector)
      throws TechnicalException {
    connectorStorageService.save(connector);
    return new ResponseEntity<>(connector, HttpStatus.CREATED);
  }

  @IsAdmin
  @GetMapping("/{name}")
  @ResponseBody
  public Connector get(@PathVariable String name) throws TechnicalException {
    return connectorStorageService.findByName(name);
  }

  @IsAdmin
  @DeleteMapping("/{name}")
  public void delete(@PathVariable String name) throws TechnicalException {
    connectorStorageService.deleteByName(name);
  }

  @IsAdmin
  @GetMapping()
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
    return connectorStorageService.save(connector);
  }

  @IsAdmin
  @GetMapping("/stop/{name}")
  @ResponseBody
  public Connector stop(@PathVariable String name) throws TechnicalException {
    Connector connector = connectorStorageService.findByName(name);
    connectorExecutionService.stop(connector);
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

      return connector;
    }
    return null;
  }

  @Override
  public Logger getLogger() {
    return logger;
  }
}
