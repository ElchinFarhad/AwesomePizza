package com.example.awesomepizzaworker.service;

import com.example.awesomepizzaworker.dto.PizzaDTO;
import com.example.awesomepizzaworker.entity.OutboxEvent;
import com.example.awesomepizzaworker.entity.Pizza;
import com.example.awesomepizzaworker.entity.OrderStatus;
import com.example.awesomepizzaworker.error.DatabaseException;
import com.example.awesomepizzaworker.error.OrderNotFoundException;
import com.example.awesomepizzaworker.error.ServiceException;
import com.example.awesomepizzaworker.kafka.KafkaProducer;
import com.example.awesomepizzaworker.repository.OutboxRepository;
import com.example.awesomepizzaworker.repository.PizzaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PizzaService {

    private static final Logger logger = LoggerFactory.getLogger(PizzaService.class);

    private final PizzaRepository pizzaRepository;
    private final OutboxRepository outboxRepository;
    private final KafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper;


    @Autowired
    public PizzaService(PizzaRepository pizzaRepository, OutboxRepository outboxRepository, KafkaProducer kafkaProducer, ObjectMapper objectMapper) {
        this.pizzaRepository = pizzaRepository;
        this.outboxRepository = outboxRepository;
        this.kafkaProducer = kafkaProducer;
        this.objectMapper = objectMapper;
    }

    /**
     * Processes with pizza orders.
     *
     * @param pizzaDTO The pizza DTO containing order information.
     * @throws ServiceException       If an error occurs while processing pending pizza orders.
     * @throws DatabaseException      If a database error occurs.
     * @throws OrderNotFoundException If the order with the given ID is not found.
     */
    public void processPendingPizzaOrder(PizzaDTO pizzaDTO) {
        try {
            if (OrderStatus.PENDING.name().equals(pizzaDTO.getStatus())) {
                Optional<Pizza> optionalExistingOrder = pizzaRepository.findByOrderId(pizzaDTO.getOrderId());
                if (optionalExistingOrder.isPresent()) {
                    logger.info("Order already exists with id: {}", pizzaDTO.getId());
                } else {
                    handleNewOrder(pizzaDTO);
                }
            } else if (OrderStatus.IN_PROGRESS.name().equals(pizzaDTO.getStatus())) {
                handleInProgressOrder(pizzaDTO);
            }
        } catch (JsonProcessingException e) {
            logger.error("Error processing pending pizza order", e);
            throw new ServiceException("Error processing pending pizza order", e);
        } catch (DatabaseException e) {
            logger.error("Database error occurred", e);
            throw new DatabaseException("Database error occurred", e);
        } catch (OrderNotFoundException e) {
            logger.error("Order not found with id: {}", pizzaDTO.getOrderId(), e);
            throw new OrderNotFoundException("Order not found with id: " + pizzaDTO.getOrderId(), e);
        }
    }
    @Transactional
    public void handleNewOrder(PizzaDTO pizzaDTO) throws JsonProcessingException, DatabaseException {
        Pizza newOrder = createPizzaFromDTO(pizzaDTO);
        savePizza(newOrder);
        PizzaDTO newPizzaDTO = mapPizzaEntityToDTO(newOrder);
        saveOutboxEvent("Pizza", newPizzaDTO.getOrderId(), "PizzaCreated", objectMapper.writeValueAsString(newPizzaDTO));
        processOrder();
        handleInProgressOrder(newPizzaDTO);
    }

    @Transactional
    public void handleInProgressOrder(PizzaDTO pizzaDTO) throws JsonProcessingException {
        pizzaDTO.setStatus(OrderStatus.COMPLETED.name());
        pizzaDTO.setUpdateTime(LocalDateTime.now());
        saveOutboxEvent("Pizza", pizzaDTO.getOrderId(), "PizzaCompleted", objectMapper.writeValueAsString(pizzaDTO));
    }

    public void sendMessage(PizzaDTO pizzaDTO) throws JsonProcessingException {
        kafkaProducer.sendMessage(pizzaDTO);
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

    @Transactional(rollbackFor = DatabaseException.class)
    public void savePizza(Pizza pizza) throws DatabaseException {
        try {
            pizzaRepository.save(pizza);
        } catch (Exception e) {
            throw new DatabaseException("Error saving pizza order", e);
        }
    }

    private void saveOutboxEvent(String aggregateType, String aggregateId, String eventType, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
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
