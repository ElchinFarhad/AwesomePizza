package com.example.awesomepizzaworker.kafka;

import com.example.awesomepizzaworker.dto.PizzaDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final String topicName = "preparation-topic";

    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Sends the order message to the Kafka topic.
     *
     * @param pizzaDTO The pizza to be sent.
     * @throws JsonProcessingException if the order cannot be converted to JSON.
     */
    public void sendMessage(PizzaDTO pizzaDTO) throws JsonProcessingException {
        objectMapper.registerModule(new JavaTimeModule());
        String jsonString = objectMapper.writeValueAsString(pizzaDTO);
        kafkaTemplate.send(topicName, jsonString);
    }
}