package adesso.it.awesomepizzaworker.service;

import adesso.it.awesomepizzaworker.dto.PizzaDTO;
import adesso.it.awesomepizzaworker.entity.OutboxEvent;
import adesso.it.awesomepizzaworker.entity.Pizza;
import adesso.it.awesomepizzaworker.entity.OrderStatus;
import adesso.it.awesomepizzaworker.error.DatabaseException;
import adesso.it.awesomepizzaworker.error.OrderNotFoundException;
import adesso.it.awesomepizzaworker.error.ServiceException;
import adesso.it.awesomepizzaworker.repository.OutboxRepository;
import adesso.it.awesomepizzaworker.repository.PizzaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PizzaService {

    private static final Logger logger = LoggerFactory.getLogger(PizzaService.class);

    private final PizzaRepository pizzaRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public PizzaService(PizzaRepository pizzaRepository, OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.pizzaRepository = pizzaRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Processes pending pizza orders.
     *
     * @param pizzaDTO The pizza DTO containing order information.
     * @throws ServiceException       If an error occurs while processing pending pizza orders.
     * @throws DatabaseException      If a database error occurs.
     * @throws OrderNotFoundException If the order with the given ID is not found.
     */
    public void processPendingPizzaOrder(PizzaDTO pizzaDTO) {
        try {
            Optional<Pizza> optionalExistingOrder = pizzaRepository.findByOrderId(pizzaDTO.getOrderId());
            if (optionalExistingOrder.isPresent()) {
                logger.info("Order already exists with id: {}", pizzaDTO.getId());
            } else {
                handleNewOrder(pizzaDTO);
            }
        } catch (JsonProcessingException | DatabaseException e) {
            logger.error("Error processing pending pizza order", e);
            throw new ServiceException("Error processing pending pizza order", e);
        }
    }

    @Transactional
    public void handleNewOrder(PizzaDTO pizzaDTO) throws JsonProcessingException, DatabaseException {
        Pizza newOrder = createPizzaFromDTO(pizzaDTO);
        savePizza(newOrder);
        PizzaDTO newPizzaDTO = mapPizzaEntityToDTO(newOrder);
        saveOutboxEvent(newPizzaDTO.getOrderId(), objectMapper.writeValueAsString(newPizzaDTO));
        processOrder();
        updateOrderStatusInNewTransaction(newOrder.getOrderId(), OrderStatus.COMPLETED.name());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateOrderStatusInNewTransaction(Long orderId, String status) throws JsonProcessingException {
        Pizza existingOrder = pizzaRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        existingOrder.setStatus(status);
        existingOrder.setUpdateTime(LocalDateTime.now());
        savePizza(existingOrder);
        PizzaDTO updatedPizzaDTO = mapPizzaEntityToDTO(existingOrder);
        saveOutboxEvent(updatedPizzaDTO.getOrderId(), objectMapper.writeValueAsString(updatedPizzaDTO));
    }

    @Transactional(rollbackFor = DatabaseException.class)
    public void savePizza(Pizza pizza) throws DatabaseException {
        try {
            pizzaRepository.save(pizza);
        } catch (Exception e) {
            throw new DatabaseException("Error saving pizza order", e);
        }
    }

    public Pizza createPizzaFromDTO(PizzaDTO pizzaDTO) {
        Pizza newOrder = new Pizza();
        newOrder.setOrderId(pizzaDTO.getOrderId());
        newOrder.setPizzaType(pizzaDTO.getPizzaType());
        newOrder.setNote(pizzaDTO.getNote());
        newOrder.setStatus(OrderStatus.IN_PROGRESS.name());
        newOrder.setReceivedTime(LocalDateTime.now());
        newOrder.setUpdateTime(LocalDateTime.now());
        return newOrder;
    }

    public void saveOutboxEvent(Long aggregateId, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateId(aggregateId);
        event.setPayload(payload);
        event.setCreatedAt(LocalDateTime.now());
        outboxRepository.save(event);
    }

    private PizzaDTO mapPizzaEntityToDTO(Pizza pizza) {
        PizzaDTO newPizzaDTO = new PizzaDTO();
        newPizzaDTO.setId(pizza.getId());
        newPizzaDTO.setOrderId(pizza.getOrderId());
        newPizzaDTO.setPizzaType(pizza.getPizzaType());
        newPizzaDTO.setNote(pizza.getNote());
        newPizzaDTO.setStatus(pizza.getStatus());
        newPizzaDTO.setOrderTime(pizza.getReceivedTime());
        newPizzaDTO.setUpdateTime(pizza.getUpdateTime());
        return newPizzaDTO;
    }

    public void processOrder() {
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
