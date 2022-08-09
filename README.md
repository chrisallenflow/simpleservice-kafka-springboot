# Introduction

This represents a simple example of a microservice utilizing the Spring Boot Framework, Spring for Apache Kafka, and Apache Kafka itself.

This logic of this microservice is extremely simple; to add two integers together and provide the sum in the response.

- Ingress:  Kafka topic "addtwo-request"
- Egress:  Kafka topic "addtwo-response"

This microservice could act as a template for utilizing Kafka for event commands to trigger code that "does something", then returning the result to an event log.

Changing the topic names and refactoring the Application.processPayload(payload) method would be all that is needed to suit it to your needs.

# Steps to Use

NOTE: JVM used in creation was Java 18.

1. Start kafka in a docker container.

```bash
docker-compose up -d
```

2. Once Kafka is up, check the admin console at http://localhost:9021.

3. Start Spring Boot

```bash
mvn spring-boot:run
```

4. In the Kafka admin console, navigate to Cluster -> Topics -> addtwo-request -> Messages.

5. Click "Produce a new message to this topic.".  Copy the contents of "payload-request-example.json" into this dialog box and send.

6. Now go look at the addtwo-response topic, enter Jump To Offset = -100 . You should see a response that now has an "output" section with the sum of the two numbers.