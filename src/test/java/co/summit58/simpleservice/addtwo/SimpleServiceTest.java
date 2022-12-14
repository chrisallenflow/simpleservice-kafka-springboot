package co.summit58.simpleservice.addtwo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.test.annotation.DirtiesContext;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class SimpleServiceTest {

	@Value(value = "${request.topic.name}")
	private String requestTopic;

	@Autowired
	private SimpleConsumer consumer;

	@Autowired
	private KafkaTemplate<String, Object> template;

	JsonNode resultNode;

	CountDownLatch latch = new CountDownLatch(1);

	/**
	 * Test the microservice.
	 *
	 * @throws Exception
	 */
	@Test
	void addSum() throws Exception {

		// send the request to add two numbers
		template.send(requestTopic, getRequest());
		// confirm that the web service consumed the message
		boolean messageConsumed = consumer.getLatch().await(10, TimeUnit.SECONDS);
		assertTrue(messageConsumed);
		// after adding the two number, it should automatically be posting the result to the response topic.
		// check that here.  The result() method below will grab the final response in order to continue testing.
		boolean resultConsumed = this.latch.await(10, TimeUnit.SECONDS);
		assertTrue(resultConsumed);

		// in the final result, confirm that the values are added properly.
		int i = 0;
		for (Iterator<JsonNode> it = resultNode.get("input").elements(); it.hasNext(); ) {
			JsonNode node = it.next();
			if (!node.get("value").isInt()) throw new RuntimeException("value not Int.");
			i = i + node.get("value").asInt();
		}

		int sum = 0;
		for (Iterator<JsonNode> it2 = resultNode.get("output").elements(); it2.hasNext(); ) {
			JsonNode node = it2.next();
			sum = sum + node.get("value").asInt();
		}

		assertTrue(i == sum);

	}

	/**
	 * Read a sample request JSON payload from the root of this project and return a Jackson object.
	 *
	 * @return
	 * @throws Exception
	 */
	private Object getRequest() throws Exception {
		File file = new File("payload-request-example.json");
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readTree(file);
	}

	/**
	 * Listen for the final result and save a Jackson object as a member variable for use in the test method.
	 *
	 * @param cr
	 * @param payload
	 */
	@KafkaListener(topics = "${response.topic.name}", clientIdPrefix = "string",
			containerFactory = "kafkaListenerStringContainerFactory")
	public void receive(ConsumerRecord<String, String> cr, @Payload String payload) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			resultNode = mapper.readTree(payload);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		latch.countDown();
	}

}
