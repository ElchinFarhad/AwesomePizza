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
        PizzaDTO pizzaDTO = new PizzaDTO();
        pizzaDTO.setOrderId("ORD-123");
        pizzaDTO.setStatus(OrderStatus.PENDING.name());
        pizzaDTO.setPizzaType("4Formaggio");
        Pizza newOrder = new Pizza();
        newOrder.setOrderId("ORD-123");

        doReturn(newOrder).when(pizzaService).createPizzaFromDTO(pizzaDTO);

        pizzaService.handleNewOrder(pizzaDTO);

        verify(pizzaService, times(1)).createPizzaFromDTO(pizzaDTO);
        verify(pizzaRepository, times(1)).save(newOrder);
        verify(pizzaService, times(1)).processOrder();
        verify(pizzaService, times(1)).handleInProgressOrder(any());
    }

    @Test
    public void testHandleInProgressOrder_OutboxEventCreated() throws JsonProcessingException {
        PizzaDTO pizzaDTO = new PizzaDTO();
        pizzaDTO.setOrderId("ORD-123");
        pizzaService.handleInProgressOrder(pizzaDTO);
        verify(outboxRepository, times(1)).save(any());
    }
}
