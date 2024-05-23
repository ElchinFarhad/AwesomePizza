package adesso.it.awesomepizzaworker.kafka;

import adesso.it.awesomepizzaworker.service.PizzaService;
import adesso.it.awesomepizzaworker.dto.PizzaDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class KafkaConsumer {

    private final PizzaService pizzaService;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaConsumer(PizzaService pizzaService, ObjectMapper objectMapper) {
        this.pizzaService = pizzaService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-topic", groupId = "awesome-pizza-group")
    public void receiveMessage(String message) throws JsonProcessingException {
        PizzaDTO pizzaDTO = objectMapper.readValue(message, PizzaDTO.class);
        pizzaService.processPendingPizzaOrder(pizzaDTO);
    }
}