package co.summit58.simpleservice.addtwo;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value(value = "${request.topic.name}")
    private String reqTopicName;

    @Value(value = "${response.topic.name}")
    private String resTopicName;

    @Bean
    public NewTopic topicRequest() {
        return TopicBuilder.name(reqTopicName).build();
    }

    @Bean
    public NewTopic topicResponse() {
        return TopicBuilder.name(resTopicName).build();
    }

}