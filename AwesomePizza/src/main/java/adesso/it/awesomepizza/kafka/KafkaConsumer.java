package adesso.it.awesomepizza.kafka;

import adesso.it.awesomepizza.entity.Order;
import adesso.it.awesomepizza.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    public KafkaConsumer(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    /**
     * Listens to the 'pizza-topic' Kafka topic for new messages.
     *
     * @param message The incoming message as a JSON string.
     * @throws JsonProcessingException if the message cannot be parsed.
     */
    @KafkaListener(topics = "preparation-topic", groupId = "awesome-pizza-group")
    public void receiveMessage(String message) throws JsonProcessingException {

        // ObjectMapper for converting JSON messages to Order objects
        objectMapper.registerModule(new JavaTimeModule());
        Order order = objectMapper.readValue(message, Order.class);
        orderService.processPizzaOrderMessage(order);

    }

}