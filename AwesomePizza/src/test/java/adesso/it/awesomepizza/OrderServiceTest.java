package adesso.it.awesomepizza;


import adesso.it.awesomepizza.dto.OrderDTO;
import adesso.it.awesomepizza.entity.Order;
import adesso.it.awesomepizza.entity.OrderStatus;
import adesso.it.awesomepizza.error.OrderNotFoundException;
import adesso.it.awesomepizza.kafka.KafkaProducer;
import adesso.it.awesomepizza.repository.OrderRepository;
import adesso.it.awesomepizza.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Optional;

public class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    KafkaProducer kafkaProducer;

    @InjectMocks
    OrderService orderService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPlaceOrder() throws JsonProcessingException {

        ArgumentCaptor<OrderDTO> orderCaptor = ArgumentCaptor.forClass(OrderDTO.class);

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setPizzaType("Margherita");
        orderDTO.setNote(null);

        Order order=new Order();
        order.setPizzaType(orderDTO.getPizzaType());
        order.setNote(orderDTO.getNote());
        order.setStatus(OrderStatus.PENDING.name());
        order.setOrderTime(LocalDateTime.now());
        order.setUpdateTime(null);

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDTO result = orderService.placeOrder(orderDTO);

        assertEquals(order.getPizzaType(), result.getPizzaType());
        assertEquals(order.getNote(), result.getNote());
        assertEquals(order.getStatus(), result.getStatus());
        assertEquals(order.getOrderTime(), result.getOrderTime());
        assertEquals(order.getUpdateTime(), result.getUpdateTime());
        verify(kafkaProducer, times(1)).sendMessage(orderCaptor.capture());
    }

    @Test
    public void testGetOrderStatusById() {
        String orderId = "1";
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
        String orderId = "1";

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(OrderNotFoundException.class, () -> {
            orderService.getOrderStatusById(orderId);
        });
        assertEquals("Order not found with id: " + orderId, exception.getMessage());
        verify(orderRepository, times(1)).findByOrderId(orderId);
    }


    @Test
    public void testProcessPizzaOrderMessage() {
        String orderId = "1";
        Order existingOrder = new Order();
        existingOrder.setOrderId(orderId);
        existingOrder.setStatus("PENDING");

        Order updatedOrder = new Order();
        updatedOrder.setOrderId(orderId);
        updatedOrder.setStatus("COMPLETED");

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

        orderService.processPizzaOrderMessage(updatedOrder);

        verify(orderRepository, times(1)).findByOrderId(orderId);
        verify(orderRepository, times(1)).save(existingOrder);
    }

}
