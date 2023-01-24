[![Community Extension](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)

# Friendly connector runtime for Camunda Platform 8 using React, Java and Spring Boot

This project is made to provide a connector runtime with a simple UI to deploy, start, stop and delete connector implementations and manage secrets.

## Repository content

This repository contains a Java application built with Spring Boot and Zeebe Spring client to act as a connector runtime for Camunda Platform 8.

It also contains a [React front-end](src/main/front/) that you can execute independently (npm run start) or serve from the spring boot application (you should run a `mvnw package` at the project root).

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
