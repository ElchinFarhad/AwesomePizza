package adesso.it.awesomepizza.service;

import adesso.it.awesomepizza.dto.OrderDTO;
import adesso.it.awesomepizza.entity.OutboxEvent;
import adesso.it.awesomepizza.kafka.KafkaProducer;
import adesso.it.awesomepizza.repository.OutboxRepository;
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
        List<OutboxEvent> events = outboxRepository.findAllByProcessed(false);
        for (OutboxEvent event : events) {
            try {
                OrderDTO orderDTO = objectMapper.readValue(event.getPayload(), OrderDTO.class);
                kafkaProducer.sendMessage(orderDTO);
                event.setProcessed(true);
                outboxRepository.save(event);
                logger.info("Processed outbox event for aggregateId: {} and type: {}", event.getAggregateId(), event.getEventType());
            } catch (JsonProcessingException e) {
                logger.error("Error sending outbox event to Kafka", e);
            }
        }
    }
}
