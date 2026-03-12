# Axoniq Livestream application

This application is a sample project created for the Axoniq Livestream on March 12, 2024. It demonstrates how to build a simple application using the Axon Framework 5 and the DCB technology. This application has been generated with [Axoniq Platform](https://platform.axoniq.io/).

The slides are available at [Google Slides](https://docs.google.com/presentation/d/17GVUhXzyHA74bl4y1DxQrXEx2knXKFBZ5Idolct9KjA/edit?usp=sharing)


A demo.http call is included within the project, which contains sample HTTP requests to interact with the application. You can use this file with tools like Postman or IntelliJ's HTTP client to test the API endpoints.

Be sure to reach out to the Axoniq team if you have any questions or want to share your experience with the application!

## Prerequisites
- Java 21
- Maven 3.9 or higher
- Docker Compose
- An IDE like IntelliJ IDEA (optional)

## Getting Started

Run docker compose to start all the dependencies, including Postgres and Axon Server:

```bash
docker compose up -d
```

Now, you can run the application using Maven:
```bash
mvn spring-boot:run
```


Alternatively, you can run the application from your IDE by running the main class.

## Using the Application
The application also exposes a REST API, which you can explore by navigating to: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
The events are stored in Axon Server, which you can access at [http://localhost:8024](http://localhost:8024).

## Useful resources

You can access a lot of resources through the [Axoniq Platform](https://platform.axoniq.io/). This includes:

- Extensive Documentation
- Axoniq Academy with free courses
- AI-powered Development Agent to create your applications faster
- Community Forum to ask questions and share knowledge
- Monitor your applications
- And much more!
