package adesso.it.awesomepizza;

import adesso.it.awesomepizza.dto.OrderDTO;
import adesso.it.awesomepizza.entity.OutboxEvent;
import adesso.it.awesomepizza.kafka.KafkaProducer;
import adesso.it.awesomepizza.repository.OutboxRepository;
import adesso.it.awesomepizza.service.OutboxProcessingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.annotation.SchedulingConfigurer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OutboxProcessingServiceTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxProcessingService outboxProcessingService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessOutboxEvents() throws JsonProcessingException {
        // Mocking
        OutboxEvent outboxEvent1 = new OutboxEvent();
        outboxEvent1.setId(1L);
        outboxEvent1.setAggregateId(1L);
        outboxEvent1.setPayload("{\"orderId\":1,\"pizza\":\"Margherita\"}");
        outboxEvent1.setProcessed(false);

        List<OutboxEvent> events = new ArrayList<>();
        events.add(outboxEvent1);

        when(outboxRepository.findAllByProcessed(false)).thenReturn(events);

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setPizzaType("Margherita");

        when(objectMapper.readValue(outboxEvent1.getPayload(), OrderDTO.class)).thenReturn(orderDTO);

        // Calling the method under test
        outboxProcessingService.processOutboxEvents();

        // Verify that sendMessage() is called with the correct orderDTO
        verify(kafkaProducer, times(1)).sendMessage(orderDTO);

        // Verify that outboxEvent1 is saved with processed=true
        verify(outboxRepository, times(1)).save(outboxEvent1);
        assertTrue(outboxEvent1.isProcessed(), "outboxEvent1 should be processed after calling processOutboxEvents()");
    }
}
