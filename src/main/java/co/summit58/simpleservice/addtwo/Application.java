package co.summit58.simpleservice.addtwo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Iterator;
import java.util.stream.StreamSupport;

@SpringBootApplication
public class Application {

	private final Logger logger = LoggerFactory.getLogger(Application.class);

	@Value(value = "${request.topic.name}")
	private String reqTopicName;

	@Value(value = "${response.topic.name}")
	private String resTopicName;

	private final KafkaTemplate<String, Object> template;

	public Application(KafkaTemplate<String, Object> template) {
		this.template = template;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	/**
	 * Listen to the "addtwo-request" topic on Kafka.
	 * Pass the payload to the processPayload() method.
	 * Send the response to the resTopicName topic.
	 *
	 * @param cr Consumer record from Kafka for the incoming message.
	 * @param payload The incoming message.
	 */
	@KafkaListener(topics = "addtwo-request", clientIdPrefix = "string",
			containerFactory = "kafkaListenerStringContainerFactory")
	public void listenAsString(ConsumerRecord<String, String> cr,
							   @Payload String payload) {
		logger.info("Logger 2 [String] received key {}: Type [{}] | Payload: {} | Record: {}", cr.key(),
				typeIdHeader(cr.headers()), payload, cr.toString());
		try {
			ObjectNode newNode = processPayload(payload);
			logger.info("++ payload = " + newNode);
			template.send(resTopicName, newNode);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method to parse the Kafka header.
	 *
	 * @param headers
	 * @return
	 */
	private static String typeIdHeader(Headers headers) {
		return StreamSupport.stream(headers.spliterator(), false)
				.filter(header -> header.key().equals("__TypeId__"))
				.findFirst().map(header -> new String(header.value())).orElse("N/A");
	}

	/**
	 * Method used to parse the payload.  In this case, add two numbers that appear in the request, and return the sum.
	 *
	 * @param payload
	 * @return
	 * @throws JsonProcessingException
	 * @throws RuntimeException
	 */
	private ObjectNode processPayload(String payload) throws JsonProcessingException, RuntimeException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(payload);
		if (actualObj.get("command") == null ||
				!actualObj.get("command").asText().equalsIgnoreCase("addtwo")) {
			throw new RuntimeException("improper command");
		}
		int i = 0;
		for (Iterator<JsonNode> it = actualObj.get("input").elements(); it.hasNext(); ) {
			JsonNode node = it.next();
			if (!node.get("value").isInt()) throw new RuntimeException("value not Int.");
			i = i + node.get("value").asInt();
		}

		logger.info("++ sum = " + i);
		ArrayNode outputArrayNode = mapper.createArrayNode();
		ObjectNode outputNode = mapper.createObjectNode();
		outputNode.put("key", "sum");
		outputNode.put("value", i);
		outputNode.put("type", "Integer");
		outputArrayNode.add(outputNode);

		return ((ObjectNode) actualObj).putPOJO("output", outputArrayNode);
	}
//	{
//		"command": "addtwo",
//			"input": [
//		{
//			"key": "val1",
//				"value": 5,
//				"type": "Integer"
//		},
//		{
//			"key": "val2",
//				"value": 7,
//				"type": "Integer"
//		}
//  ],
//		"output": [
//		{
//			"key": "sum",
//				"value": 12,
//				"type": "Integer"
//		}
//  ],
//		"camunda": {
//		"processInstance": 123412341234,
//				"messageCorrelationKey": "addTwoResult",
//				"outputMappings": [
//		{
//			"variableName": "result",
//				"mapsTo": "sum"
//		}
//    ]
//	}
//	}
}