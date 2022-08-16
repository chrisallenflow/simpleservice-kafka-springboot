# Introduction

This represents a simple example of a microservice utilizing the Spring Boot Framework, Spring for Apache Kafka, and Apache Kafka itself.  It also utilizes the combination of Micrometer and Prometheus for active monitoring of the microservice.

This logic of this microservice is extremely simple; to add two integers together and provide the sum in the response.

- Ingress:  Kafka topic "addtwo-request"
- Egress:  Kafka topic "addtwo-response"

This microservice could act as a template for utilizing Kafka for event commands to trigger code that "does something", then returning the result to an event log.

Changing the topic names and refactoring the Application.processPayload(payload) method would be all that is needed to suit it to your needs.

# Steps to Use

NOTE: JVM used in creation was Java 18.

1. Start Kafka and Prometheus server in docker containers.

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
7. Next, we look at how long it took to execute the consumption and parsing of the payload.  Open the following URLs.
- http://localhost:8080/actuator/prometheus : This is the scraping endpoint for Prometheus running in our Spring Boot microservice.  We annotated our Consumer method with the @Timed annotation in Micrometer.  This will allow us to see long it took for the consumption of the command to execute inside the microservice.  In this log search for "consumer_process_time".  You might see something like this for count and sum:
```logcatfilter
# HELP consumer_process_time_seconds Time taken to process payload.
# TYPE consumer_process_time_seconds summary
consumer_process_time_seconds_count{class="co.summit58.simpleservice.addtwo.SimpleConsumer",exception="none",method="receive",} 1.0
consumer_process_time_seconds_sum{class="co.summit58.simpleservice.addtwo.SimpleConsumer",exception="none",method="receive",} 0.027856606
# HELP consumer_process_time_seconds_max Time taken to process payload.
# TYPE consumer_process_time_seconds_max gauge
```
- http://localhost:9090 - This is the provided Prometheus front end.  On the top menu select "Graph", and between the subtabs of "Table" and "Graph" select "Graph".  For the query expression paste "consumer_process_time_seconds_max" and execute the query to view the graph.