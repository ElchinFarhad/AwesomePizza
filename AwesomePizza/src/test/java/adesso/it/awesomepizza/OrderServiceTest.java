package adesso.it.awesomepizza;

import adesso.it.awesomepizza.dto.OrderDTO;
import adesso.it.awesomepizza.entity.Order;
import adesso.it.awesomepizza.entity.OrderStatus;
import adesso.it.awesomepizza.entity.OutboxEvent;
import adesso.it.awesomepizza.error.DatabaseException;
import adesso.it.awesomepizza.error.OrderNotFoundException;
import adesso.it.awesomepizza.repository.OrderRepository;
import adesso.it.awesomepizza.repository.OutboxRepository;
import adesso.it.awesomepizza.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPlaceOrder() throws JsonProcessingException {
        // Mocking ObjectMapper behavior
        when(objectMapper.writeValueAsString(any(OrderDTO.class))).thenReturn("orderPayload");

        // Creating OrderDTO
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setPizzaType("Margherita");
        orderDTO.setNote(null);

        // Creating expected Order object
        Order expectedOrder = new Order();
        expectedOrder.setId(1L);
        expectedOrder.setPizzaType(orderDTO.getPizzaType());
        expectedOrder.setNote(orderDTO.getNote());
        expectedOrder.setStatus(OrderStatus.PENDING.name());
        expectedOrder.setOrderTime(LocalDateTime.now());
        expectedOrder.setUpdateTime(null);

        // Mocking OrderRepository behavior
        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);

        // Calling the method under test
        OrderDTO result = orderService.placeOrder(orderDTO);

        // Verifying the result
        assertEquals(expectedOrder.getPizzaType(), result.getPizzaType());
        assertEquals(expectedOrder.getNote(), result.getNote());
        assertEquals(expectedOrder.getStatus(), result.getStatus());
        assertEquals(expectedOrder.getOrderTime(), result.getOrderTime());
        assertEquals(expectedOrder.getUpdateTime(), result.getUpdateTime());

        // Verifying that saveOutboxEvent() is called with the correct parameters
        verify(outboxRepository, times(1)).save(any(OutboxEvent.class));
    }


    @Test
    public void testGetOrderStatusById() {
        Long id = 1L;
        Order order = new Order();
        order.setId(id);
        order.setPizzaType("Margherita");
        order.setStatus("PENDING");

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        OrderDTO result = orderService.getOrderStatusById(id);

        assertEquals(id, result.getId());
        assertEquals("Margherita", result.getPizzaType());
        assertEquals("PENDING", result.getStatus());

        verify(orderRepository, times(1)).findById(id);
    }

    @Test
    public void testGetOrderStatusById_NotFound() {
        Long id = 1L;

        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(OrderNotFoundException.class, () -> orderService.getOrderStatusById(id));
        assertEquals("Order not found with id: " + id, exception.getMessage());
        verify(orderRepository, times(1)).findById(id);
    }

    @Test
    public void testGetAllOrders() {
        Order order1 = new Order();
        order1.setId(1L);
        order1.setPizzaType("Margherita");
        order1.setStatus("PENDING");

        Order order2 = new Order();
        order2.setId(2L);
        order2.setPizzaType("Pepperoni");
        order2.setStatus("COMPLETED");

        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

        List<OrderDTO> orders = orderService.getAllOrders();

        assertEquals(2, orders.size());
        assertEquals(1L, orders.get(0).getId());
        assertEquals(2L, orders.get(1).getId());

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    public void testProcessPizzaOrderMessage() {
        Long orderId = 1L;
        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus("PENDING");

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus("COMPLETED");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));

        orderService.processPizzaOrderMessage(updatedOrder);

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
        assertEquals("COMPLETED", existingOrder.getStatus());
    }
}
