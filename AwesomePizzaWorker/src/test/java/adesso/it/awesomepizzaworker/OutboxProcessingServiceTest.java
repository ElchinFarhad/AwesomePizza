package adesso.it.awesomepizzaworker;

import adesso.it.awesomepizzaworker.dto.PizzaDTO;
import adesso.it.awesomepizzaworker.entity.OutboxEvent;
import adesso.it.awesomepizzaworker.kafka.KafkaProducer;
import adesso.it.awesomepizzaworker.repository.OutboxRepository;
import adesso.it.awesomepizzaworker.service.OutboxProcessingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OutboxProcessingServiceTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxProcessingService outboxProcessingService;

    @Test
    public void testProcessOutboxEvents() throws JsonProcessingException {
        PizzaDTO pizzaDTO = new PizzaDTO();
        pizzaDTO.setOrderId(1L);
        OutboxEvent event = new OutboxEvent();
        event.setId(1L);
        event.setPayload("{\"orderId\":\"1L\"}");
        event.setProcessed(false);
        List<OutboxEvent> events = Collections.singletonList(event);

        when(outboxRepository.findAllByProcessed(false)).thenReturn(events);
        when(objectMapper.readValue(event.getPayload(), PizzaDTO.class)).thenReturn(pizzaDTO);

        outboxProcessingService.processOutboxEvents();

        verify(outboxRepository, times(1)).findAllByProcessed(false);
        verify(objectMapper, times(1)).readValue(event.getPayload(), PizzaDTO.class);
        verify(kafkaProducer, times(1)).sendMessage(eq(pizzaDTO));
        verify(outboxRepository, times(1)).save(argThat(e -> e.getId().equals(event.getId()) && e.isProcessed()));
    }

    @Test
    public void testProcessOutboxEvents_withJsonProcessingException() throws JsonProcessingException {
        OutboxEvent event = new OutboxEvent();
        event.setId(1L);
        event.setPayload("{\"orderId\":\"ORD-123\"}");
        event.setProcessed(false);
        List<OutboxEvent> events = Collections.singletonList(event);

        when(outboxRepository.findAllByProcessed(false)).thenReturn(events);
        when(objectMapper.readValue(event.getPayload(), PizzaDTO.class)).thenThrow(new JsonProcessingException("Test exception") {});

        outboxProcessingService.processOutboxEvents();

        verify(outboxRepository, times(1)).findAllByProcessed(false);
        verify(objectMapper, times(1)).readValue(event.getPayload(), PizzaDTO.class);
        verify(kafkaProducer, never()).sendMessage(any(PizzaDTO.class));
        verify(outboxRepository, never()).save(any(OutboxEvent.class));
    }
}
