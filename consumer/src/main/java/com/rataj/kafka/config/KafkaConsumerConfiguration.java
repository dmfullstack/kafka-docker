package com.rataj.kafka.config;


import com.rataj.kafka.model.Person;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer2;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfiguration {

    private final KafkaProperties kafkaProperties;

    public KafkaConsumerConfiguration(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ConsumerFactory<String, Person> consumerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServer());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "group");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "60000");
        ErrorHandlingDeserializer2<String> headerErrorHandlingDeserializer
                = new ErrorHandlingDeserializer2<>(new StringDeserializer());
        ErrorHandlingDeserializer2<Person> errorHandlingDeserializer
                = new ErrorHandlingDeserializer2<>(new JsonDeserializer<>(Person.class));
        return new DefaultKafkaConsumerFactory<>(properties, headerErrorHandlingDeserializer, errorHandlingDeserializer);
    }

    @Bean
    public KafkaListenerContainerFactory<?> kafkaListenerContainerFactory(ConsumerFactory<String, Person> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Person> kafkaListenerContainerFactory
                = new ConcurrentKafkaListenerContainerFactory<>();
        kafkaListenerContainerFactory.setConcurrency(kafkaProperties.getConcurrentConsumers());
        kafkaListenerContainerFactory.setConsumerFactory(consumerFactory);
        kafkaListenerContainerFactory.setErrorHandler(new KafkaErrorHandler());
        return kafkaListenerContainerFactory;
    }

}