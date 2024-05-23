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
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setPizzaType("Margherita");
        orderDTO.setNote(null);

        Order order = new Order();
        order.setOrderId("ORD-000");
        order.setPizzaType(orderDTO.getPizzaType());
        order.setNote(orderDTO.getNote());
        order.setStatus(OrderStatus.PENDING.name());
        order.setOrderTime(LocalDateTime.now());
        order.setUpdateTime(null);

        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(objectMapper.writeValueAsString(any(OrderDTO.class))).thenReturn("orderPayload");

        OrderDTO result = orderService.placeOrder(orderDTO);

        assertEquals(order.getPizzaType(), result.getPizzaType());
        assertEquals(order.getNote(), result.getNote());
        assertEquals(order.getStatus(), result.getStatus());
        assertEquals(order.getOrderTime(), result.getOrderTime());
        assertEquals(order.getUpdateTime(), result.getUpdateTime());

        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository, times(1)).save(outboxCaptor.capture());
        OutboxEvent outboxEvent = outboxCaptor.getValue();
        assertEquals(order.getOrderId(), outboxEvent.getAggregateId());
        assertEquals("orderPayload", outboxEvent.getPayload());
    }

    @Test
    public void testGetOrderStatusById() {
        String orderId = "ORD-001";
        Order order = new Order();
        order.setOrderId(orderId);
        order.setPizzaType("Margherita");
        order.setStatus("PENDING");

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));

        OrderDTO result = orderService.getOrderStatusById(orderId);

        assertEquals(orderId, result.getOrderId());
        assertEquals("Margherita", result.getPizzaType());
        assertEquals("PENDING", result.getStatus());

        verify(orderRepository, times(1)).findByOrderId(orderId);
    }

    @Test
    public void testGetOrderStatusById_NotFound() {
        String orderId = "ORD-001";

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(OrderNotFoundException.class, () -> orderService.getOrderStatusById(orderId));
        assertEquals("Order not found with id: " + orderId, exception.getMessage());
        verify(orderRepository, times(1)).findByOrderId(orderId);
    }

    @Test
    public void testGetAllOrders() {
        Order order1 = new Order();
        order1.setOrderId("ORD-001");
        order1.setPizzaType("Margherita");
        order1.setStatus("PENDING");

        Order order2 = new Order();
        order2.setOrderId("ORD-002");
        order2.setPizzaType("Pepperoni");
        order2.setStatus("COMPLETED");

        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

        List<OrderDTO> orders = orderService.getAllOrders();

        assertEquals(2, orders.size());
        assertEquals("ORD-001", orders.get(0).getOrderId());
        assertEquals("ORD-002", orders.get(1).getOrderId());

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    public void testProcessPizzaOrderMessage() {
        String orderId = "ORD-001";
        Order existingOrder = new Order();
        existingOrder.setOrderId(orderId);
        existingOrder.setStatus("PENDING");

        Order updatedOrder = new Order();
        updatedOrder.setOrderId(orderId);
        updatedOrder.setStatus("COMPLETED");

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingOrder));

        orderService.processPizzaOrderMessage(updatedOrder);

        verify(orderRepository, times(1)).findByOrderId(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
        assertEquals("COMPLETED", existingOrder.getStatus());
    }
}
