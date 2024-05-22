package adesso.it.awesomepizza.kafka;
import adesso.it.awesomepizza.dto.OrderDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
public class KafkaProducer {

    @Autowired
    ObjectMapper objectMapper;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topicName = "order-topic";

    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Sends the order message to the Kafka topic.
     *
     * @param orderDto The order to be sent.
     * @throws JsonProcessingException if the order cannot be converted to JSON.
     */
    public void sendMessage(OrderDTO orderDto) throws JsonProcessingException {
        objectMapper.registerModule(new JavaTimeModule());
        String jsonString = objectMapper.writeValueAsString(orderDto);
        kafkaTemplate.send(topicName, jsonString);
    }
}