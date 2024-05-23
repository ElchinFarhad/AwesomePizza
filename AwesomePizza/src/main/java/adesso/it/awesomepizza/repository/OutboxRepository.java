package adesso.it.awesomepizza.repository;

import adesso.it.awesomepizza.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findAllByProcessed(boolean processed);
}
