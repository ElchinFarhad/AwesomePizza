package adesso.it.awesomepizzaworker.service;

import adesso.it.awesomepizzaworker.dto.PizzaDTO;
import adesso.it.awesomepizzaworker.entity.OutboxEvent;
import adesso.it.awesomepizzaworker.kafka.KafkaProducer;
import adesso.it.awesomepizzaworker.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutboxProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(OutboxProcessingService.class);
    private final OutboxRepository outboxRepository;
    private final KafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper;

    public OutboxProcessingService(OutboxRepository outboxRepository, KafkaProducer kafkaProducer, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaProducer = kafkaProducer;
        this.objectMapper = objectMapper;
    }

    /**
     * Process outbox events periodically.
     * This method is scheduled to run every 4 seconds for sending message to kafka .
     */
    @Scheduled(fixedRate = 4000)
    public void processOutboxEvents() {
        logger.info("Starting to process outbox events");
        List<OutboxEvent> events = outboxRepository.findAllByProcessed(false);
        for (OutboxEvent event : events) {
            try {
                PizzaDTO pizzaDTO = objectMapper.readValue(event.getPayload(), PizzaDTO.class);
                kafkaProducer.sendMessage(pizzaDTO);
                event.setProcessed(true);
                outboxRepository.save(event);
                logger.info("Processed outbox event with id: {}", event.getId());
            } catch (JsonProcessingException e) {
                logger.error("Error processing outbox event", e);
            }
        }
    }
}
