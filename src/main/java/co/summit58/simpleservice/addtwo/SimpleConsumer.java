package co.summit58.simpleservice.addtwo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

@Component
public class SimpleConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleConsumer.class);

    private CountDownLatch latch = new CountDownLatch(1);
    private String payload;

    private final SimpleProducer producer;

    public SimpleConsumer(SimpleProducer producer) {
        this.producer = producer;
    }

    @KafkaListener(topics = "${request.topic.name}", clientIdPrefix = "string",
            containerFactory = "kafkaListenerStringContainerFactory")
    public void receive(ConsumerRecord<String, String> cr, @Payload String payload) {
        LOGGER.info("received payload='{}'", payload);
        try {
            ObjectNode newNode = processPayload(payload);
            LOGGER.info("++ payload = " + newNode);
            producer.send(newNode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        latch.countDown();
    }

    public void resetLatch() {
        latch = new CountDownLatch(1);
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

        LOGGER.info("++ sum = " + i);
        ArrayNode outputArrayNode = mapper.createArrayNode();
        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("key", "sum");
        outputNode.put("value", i);
        outputNode.put("type", "Integer");
        outputArrayNode.add(outputNode);

        return ((ObjectNode) actualObj).putPOJO("output", outputArrayNode);
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}
