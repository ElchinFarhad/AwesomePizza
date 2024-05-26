package adesso.it.awesomepizzaworker;

import adesso.it.awesomepizzaworker.dto.PizzaDTO;
import adesso.it.awesomepizzaworker.entity.Pizza;
import adesso.it.awesomepizzaworker.entity.OrderStatus;
import adesso.it.awesomepizzaworker.error.DatabaseException;
import adesso.it.awesomepizzaworker.repository.OutboxRepository;
import adesso.it.awesomepizzaworker.repository.PizzaRepository;
import adesso.it.awesomepizzaworker.service.PizzaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private PizzaRepository pizzaRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Spy
    @InjectMocks
    private PizzaService pizzaService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testHandleNewOrder_OutboxEventCreated_OrderSaved() throws JsonProcessingException, DatabaseException {
        // Create a PizzaDTO with the desired order ID, status, and pizza type
        PizzaDTO pizzaDTO = new PizzaDTO();
        pizzaDTO.setOrderId(1L);
        pizzaDTO.setStatus(OrderStatus.PENDING.name());
        pizzaDTO.setPizzaType("4Formaggio");

        // Create a Pizza entity to represent the existing order
        Pizza existingOrder = new Pizza();
        existingOrder.setId(1L);
        existingOrder.setOrderId(pizzaDTO.getOrderId()); // Use the order ID from the PizzaDTO
        existingOrder.setPizzaType(pizzaDTO.getPizzaType()); // Set other properties as needed

        // Mocking repository method to return the existing order
        when(pizzaRepository.findByOrderId(pizzaDTO.getOrderId())).thenReturn(Optional.of(existingOrder));

        // Call the method under test
        pizzaService.handleNewOrder(pizzaDTO);

        // Verify that the expected methods were called with the correct parameters and number of times
        verify(pizzaService, times(1)).createPizzaFromDTO(pizzaDTO);
        verify(pizzaRepository, times(1)).save(existingOrder);
        verify(pizzaService, times(1)).processOrder();
        verify(pizzaService, times(1)).updateOrderStatusInNewTransaction(pizzaDTO.getOrderId(), OrderStatus.COMPLETED.name());
        verify(outboxRepository, times(2)).save(any());
    }




    @Test
    public void testUpdateOrderStatusInNewTransaction_OutboxEventCreated() throws JsonProcessingException {
        PizzaDTO pizzaDTO = new PizzaDTO();
        pizzaDTO.setOrderId(1L);
        pizzaDTO.setStatus(OrderStatus.COMPLETED.name());

        Pizza existingOrder = new Pizza();
        existingOrder.setOrderId(1L);
        existingOrder.setStatus(OrderStatus.IN_PROGRESS.name());

        when(pizzaRepository.findByOrderId(pizzaDTO.getOrderId())).thenReturn(Optional.of(existingOrder));

        pizzaService.updateOrderStatusInNewTransaction(pizzaDTO.getOrderId(), pizzaDTO.getStatus());

        verify(pizzaRepository, times(1)).findByOrderId(pizzaDTO.getOrderId());
        verify(pizzaRepository, times(1)).save(existingOrder);
        verify(outboxRepository, times(1)).save(any());
    }

    @Test
    public void testProcessPendingPizzaOrder_NewOrderCreated() throws JsonProcessingException, DatabaseException {
        PizzaDTO pizzaDTO = new PizzaDTO();
        pizzaDTO.setOrderId(1L);

        when(pizzaRepository.findByOrderId(pizzaDTO.getOrderId())).thenReturn(Optional.empty());

        doNothing().when(pizzaService).handleNewOrder(pizzaDTO);

        pizzaService.processPendingPizzaOrder(pizzaDTO);

        verify(pizzaRepository, times(1)).findByOrderId(pizzaDTO.getOrderId());
        verify(pizzaService, times(1)).handleNewOrder(pizzaDTO);
    }

    @Test
    public void testProcessPendingPizzaOrder_OrderAlreadyExists() throws JsonProcessingException {
        PizzaDTO pizzaDTO = new PizzaDTO();
        pizzaDTO.setOrderId(1L);

        Pizza existingOrder = new Pizza();
        existingOrder.setOrderId(1L);

        when(pizzaRepository.findByOrderId(pizzaDTO.getOrderId())).thenReturn(Optional.of(existingOrder));

        pizzaService.processPendingPizzaOrder(pizzaDTO);

        verify(pizzaRepository, times(1)).findByOrderId(pizzaDTO.getOrderId());
        verify(pizzaService, never()).handleNewOrder(any(PizzaDTO.class));
    }
}
