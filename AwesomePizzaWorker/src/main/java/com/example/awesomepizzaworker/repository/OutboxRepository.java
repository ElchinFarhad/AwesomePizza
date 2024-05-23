package com.example.awesomepizzaworker.repository;

import com.example.awesomepizzaworker.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findAllByProcessed(boolean processed);
}
