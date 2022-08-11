package co.summit58.simpleservice.addtwo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SimpleProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleProducer.class);

    @Value(value = "${response.topic.name}")
    private String resTopicName;

    private final KafkaTemplate<String, Object> template;

    public SimpleProducer(KafkaTemplate<String, Object> template) {
        this.template = template;
    }

    public void send(ObjectNode data) {
        LOGGER.info("sending payload='{}' to topic='{}'", data, resTopicName);
        template.send(resTopicName, data);
    }
}
