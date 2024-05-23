package adesso.it.awesomepizzaworker;

import adesso.it.awesomepizzaworker.dto.PizzaDTO;
import adesso.it.awesomepizzaworker.entity.OrderStatus;
import adesso.it.awesomepizzaworker.entity.Pizza;
import adesso.it.awesomepizzaworker.error.DatabaseException;
import adesso.it.awesomepizzaworker.kafka.KafkaProducer;
import adesso.it.awesomepizzaworker.repository.PizzaRepository;
import adesso.it.awesomepizzaworker.service.PizzaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PizzaServiceTest {

    @Mock
    PizzaRepository pizzaRepository;

    @Mock
    KafkaProducer kafkaProducer;

    @Spy
    @InjectMocks
    PizzaService pizzaService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessPendingPizzaOrder_NewOrder() throws JsonProcessingException {
        PizzaDTO pizzaDTO = new PizzaDTO();
        pizzaDTO.setOrderId("order123");
        pizzaDTO.setStatus(OrderStatus.PENDING.name());

        when(pizzaRepository.findByOrderId("order123")).thenReturn(Optional.empty());

        pizzaService.processPendingPizzaOrder(pizzaDTO);

        verify(pizzaService, times(1)).handleNewOrder(pizzaDTO);
        verify(pizzaService, times(1)).handleInProgressOrder(any());
    }

    @Test
    public void testProcessPendingPizzaOrder_ExistingOrder() throws JsonProcessingException {
        PizzaDTO pizzaDTO = new PizzaDTO();
        pizzaDTO.setOrderId("order123");
        pizzaDTO.setStatus(OrderStatus.PENDING.name());

        Pizza existingOrder = new Pizza();
        existingOrder.setOrderId("order123");

        when(pizzaRepository.findByOrderId("order123")).thenReturn(Optional.of(existingOrder));

        pizzaService.processPendingPizzaOrder(pizzaDTO);

        verify(pizzaService, never()).handleNewOrder(any());
        verify(pizzaService, never()).handleInProgressOrder(any());
    }

    @Test
    public void testProcessPendingPizzaOrder_InProgressOrder() throws JsonProcessingException {
        PizzaDTO pizzaDTO = new PizzaDTO();
        pizzaDTO.setOrderId("order123");
        pizzaDTO.setStatus(OrderStatus.IN_PROGRESS.name());

        pizzaService.processPendingPizzaOrder(pizzaDTO);

        verify(pizzaService, never()).handleNewOrder(any());
        verify(pizzaService, times(1)).handleInProgressOrder(pizzaDTO);
    }

    @Test
    public void testHandleNewOrder() throws JsonProcessingException, DatabaseException {
        PizzaDTO pizzaDTO = new PizzaDTO();
        pizzaDTO.setOrderId("order123");
        pizzaDTO.setStatus(OrderStatus.PENDING.name());

        Pizza newOrder = new Pizza();
        newOrder.setOrderId("order123");

        doReturn(newOrder).when(pizzaService).createPizzaFromDTO(pizzaDTO);

        pizzaService.handleNewOrder(pizzaDTO);

        verify(pizzaService, times(1)).createPizzaFromDTO(pizzaDTO);
        verify(pizzaService, times(1)).savePizza(newOrder);
        verify(pizzaService, times(2)).sendMessage(any(PizzaDTO.class));
        verify(pizzaService, times(1)).processOrder();
        verify(pizzaService, times(1)).handleInProgressOrder(any());
    }
}
