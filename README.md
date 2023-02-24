[![Community Extension](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)

# Friendly connector runtime for Camunda Platform 8 using React, Java and Spring Boot

This project is made to provide a connector runtime with a simple UI to :
- deploy, start, stop and delete connectors.
- manage secrets.
- generate and update element templates.
- monitor executions.

:information_source: This is a community project that you can use during exploration phase, PoCs, trainings, etc. It's **not production ready** and you should carefully review it before using it in production.

## Repository content

This repository contains a Java application built with Spring Boot and Zeebe Spring client to act as a connector runtime for Camunda Platform 8.

It also contains a [React front-end](src/main/front/) that you can execute independently (npm run start) or serve from the spring boot application (you should first run a `mvnw package` at the project root).

## Features
- You can execute and stop custom or "out of the box" connectors built on top of the [Connector SDK](https://github.com/camunda/connector-sdk)
    <img src="/docs/installedConnectors.png" height="200">
  - To install a new custom connector, click on "New connector" and upload your JAR packaged with dependencies. It will automatically compute all required informations.
 
    <img src="/docs/installCustomConnector.png" height="200">
  - To install a new OOTB connector, click on "Camunda connector". In the popup, change the release version (latest is the default), refresh the list and click on the install button in front of the connector you need.
  
    <img src="/docs/installOotbConnector.png" height="200">
    
    :information_source: Remember to start/stop your connectors after installation.
- You can manage secrets for your connectors. These connectors can be kept or in memory. If you store them on disk, you can choose to encrypt them. A private key will be shared with you in a popup. If the application stops and restarts, you will need to provide this private key to restore the secrets.

    <img src="/docs/secrets.png" height="200">
- To mitigate that "issue", you can also run it as a cluster. The replicas will share any secret updates between each other via socket encrypted messages. To discover each other, cluster members register themselves in an embedded hazelcast cache. As long as a node is alive, new nodes will get updated on startup and it will not be required to provide the private key for decryption.
- You can monitor connector executions. In case of errors, you can retrieve connector context, fetched variables, duration, error message, etc. For successful executions, we measure execution times. You also have some "audit logs" : who installed, removed, started, stoped connectors, changed secrets, etc.

    <img src="/docs/monitoring.png" height="150">
    <img src="/docs/errors.png" height="150">
    <img src="/docs/duration.png" height="150">
- You can download connector element templates. You can also edit them. In case of custom connector, the element template is pre-generated for you and you can update it on demand.

    <img src="/docs/editElementTemplate.png" height="200">

## First steps with the application

The application requires a running Zeebe engine (SaaS or Self Managed).
You can run Zeebe locally using the instructions :
[recommended deployment options for Camunda Platform](https://docs.camunda.io/docs/self-managed/platform-deployment/#deployment-recommendation.).

Run the application via
```
./mvnw spring-boot:run
```

UI [http://localhost:8080/](http://localhost:8080/)
Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

The first time you use the project, you should be able to connect with demo/demo to create upload new connector implementations and manage secrets.

When you start the application for the first time, an "ACME" organization is created for you with a single user demo/demo with admin rights. When you access the landing page, you'll be able to access the "admin" section where you can [manage forms](https://github.com/camunda-community-hub/extended-form-js), [mail templates](https://github.com/camunda-community-hub/thymeleaf-feel) and your organization.

## Secure the app with keycloak
If you want to secure your app with keycloak, you can set the keycloak.enabled to true and uncomment the properties in the application.yaml file.

```yaml
keycloak:
  enabled: true
  auth-server-url: http://localhost:18080/auth
  realm: camunda-platform
  resource: ConnectorRuntime
  public-client: true
  principal-attribute: preferred_username
```

This application relies on 2 kind of users :
- Admin : has a role Admin
- User : is connected without Admin role.

> :information_source: To use the application with Keycloak, create the ConnectorRuntime client and Admin role and assign it to (at least) one user.

## Build and run the image

```
docker build -t camunda-community/friendly-connector-runtime .
```
```
docker run -p 8888:8080 camunda-community/friendly-connector-runtime
```
