package adesso.it.awesomepizza.kafka;

import adesso.it.awesomepizza.dto.KafkaOrderDTO;
import adesso.it.awesomepizza.entity.Order;
import adesso.it.awesomepizza.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private OrderService orderService;
    private ObjectMapper objectMapper;

    public KafkaConsumer(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    /**
     * Listens to the 'pizza-topic' Kafka topic for new messages.
     *
     * @objectMapper for converting JSON messages to Order objects
     * @param message The incoming message as a JSON string.
     * @throws JsonProcessingException if the message cannot be parsed.
     */
    @KafkaListener(topics = "preparation-topic", groupId = "awesome-pizza-group")
    public void receiveMessage(String message) throws JsonProcessingException {
        objectMapper.registerModule(new JavaTimeModule());
        KafkaOrderDTO kafkaOrderDTO = objectMapper.readValue(message, KafkaOrderDTO.class);

        Order order = new Order();
        order.setId(kafkaOrderDTO.getOrderId());
        order.setPizzaType(kafkaOrderDTO.getPizzaType());
        order.setNote(kafkaOrderDTO.getNote());
        order.setStatus(kafkaOrderDTO.getStatus());
        order.setOrderTime(kafkaOrderDTO.getOrderTime());
        order.setUpdateTime(kafkaOrderDTO.getUpdateTime());
        orderService.processPizzaOrderMessage(order);
    }
}