package adesso.it.awesomepizza.service;

import adesso.it.awesomepizza.dto.OrderDTO;
import adesso.it.awesomepizza.entity.Order;
import adesso.it.awesomepizza.entity.OrderStatus;
import adesso.it.awesomepizza.entity.OutboxEvent;
import adesso.it.awesomepizza.error.DatabaseException;
import adesso.it.awesomepizza.error.OrderNotFoundException;
import adesso.it.awesomepizza.error.ServiceException;
import adesso.it.awesomepizza.repository.OrderRepository;
import adesso.it.awesomepizza.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    private final AtomicInteger orderIdCounter = new AtomicInteger(0);


    public OrderService(OrderRepository orderRepository, OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Places new order.
     *
     * @param orderDTO The order DTO containing order information.
     * @throws DatabaseException If an error occurs while saving the order to the database.
     * @throws ServiceException If an error occurs during JSON processing or placing the order.
     */
    @Transactional
    public OrderDTO placeOrder(OrderDTO orderDTO) {
        try {
            Order order = createOrderFromDTO(orderDTO);
            Order savedOrder = orderRepository.save(order);
            OrderDTO updatedOrderDTO =  convertToDTO(order);
            saveOutboxEvent(updatedOrderDTO.getOrderId(), objectMapper.writeValueAsString(updatedOrderDTO));
            logger.info("Order placed successfully with id: {}", savedOrder.getOrderId());
            return convertToDTO(savedOrder);
        } catch (DataAccessException ex) {
            logger.error("Error saving the order to the database.", ex);
            throw new DatabaseException("Error saving the order to the database.", ex);
        } catch (JsonProcessingException ex) {
            logger.error("Error processing JSON data.", ex);
            throw new ServiceException("Error processing JSON data.", ex);
        } catch (Exception ex) {
            logger.error("An error occurred while placing the order.", ex);
            throw new ServiceException("An error occurred while placing the order.", ex);
        }
    }

    /**
     * Retrieves existing order status by ID.
     *
     * @param orderId The ID of the order to retrieve.
     * @throws OrderNotFoundException If the order with the given ID is not found.
     * @throws ServiceException       If an error occurs while retrieving the order status.
     */
    public OrderDTO getOrderStatusById(String orderId) {
        try {
            Order order = getOrderById(orderId);
            logger.info("Retrieved order status successfully for order with id: {}", orderId);
            return convertToDTO(order);
        } catch (OrderNotFoundException ex) {
            logger.error("Order not found with id: {}", orderId, ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("An error occurred while retrieving the order status.", ex);
            throw new ServiceException("An error occurred while retrieving the order status.", ex);
        }
    }


    /**
     * Retrieves all orders.
     *
     * @throws OrderNotFoundException If the order with the given ID is not found.
     * @throws ServiceException       If an error occurs while retrieving the order status.
     */
    public List<OrderDTO> getAllOrders() {
        try {
            List<Order> orders = orderRepository.findAll();
            logger.info("Retrieved all orders successfully.");
            return orders.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (DataAccessException ex) {
            logger.error("Error retrieving the orders from the database.", ex);
            throw new DatabaseException("Error retrieving the orders from the database.", ex);
        } catch (Exception ex) {
            logger.error("An error occurred while retrieving the orders.", ex);
            throw new ServiceException("An error occurred while retrieving the orders.", ex);
        }
    }

    /**
     * Processes pizza order message from Kafka.
     *
     * @param order The order to process.
     * @throws DatabaseException If an error occurs while updating the order in the database.
     * @throws ServiceException  If an error occurs while processing the order message.
     */
    @Transactional
    public void processPizzaOrderMessage(Order order) {
        try {
            Order existingOrder = getOrderById(order.getOrderId());
            updateOrderStatus(existingOrder, order.getStatus());
            logger.info("Processed pizza order message successfully for order with id: {}", order.getOrderId());
        } catch (DataAccessException ex) {
            logger.error("Error updating the order in the database.", ex);
            throw new DatabaseException("Error updating the order in the database.", ex);
        } catch (Exception ex) {
            logger.error("An error occurred while processing the order message.", ex);
            throw new ServiceException("An error occurred while processing the order message.", ex);
        }
    }

    /**
     * Generate Order ID
     */
    private String generateOrderId() {
        int orderIdSuffix = orderIdCounter.getAndIncrement();
        return "ORD-" + String.format("%03d", orderIdSuffix);
    }

    private Order getOrderById(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    logger.error("Order not found with id: {}", orderId);
                    return new OrderNotFoundException("Order not found with id: " + orderId);
                });
    }

    private void updateOrderStatus(Order order, String status) {
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        orderRepository.save(order);
        logger.info("Updated order status to {} for order with id: {}", status, order.getOrderId());
    }

    private Order createOrderFromDTO(OrderDTO orderDTO) {
        Order order = new Order();
        order.setOrderId(generateOrderId());
        order.setPizzaType(orderDTO.getPizzaType());
        order.setStatus(OrderStatus.PENDING.name());
        order.setNote(orderDTO.getNote());
        order.setOrderTime(LocalDateTime.now());
        order.setUpdateTime(null);
        return order;
    }
    private OrderDTO convertToDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setOrderId(order.getOrderId());
        orderDTO.setPizzaType(order.getPizzaType());
        orderDTO.setStatus(order.getStatus());
        orderDTO.setNote(order.getNote());
        orderDTO.setOrderTime(order.getOrderTime());
        orderDTO.setUpdateTime(order.getUpdateTime());
        return orderDTO;
    }

    /**
     * Create OutBoxEventObject and write into database
     */
    private void saveOutboxEvent(String aggregateId, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateId(aggregateId);
        event.setPayload(payload);
        event.setCreatedAt(LocalDateTime.now());
        event.setProcessed(false);
        outboxRepository.save(event);
    }
}