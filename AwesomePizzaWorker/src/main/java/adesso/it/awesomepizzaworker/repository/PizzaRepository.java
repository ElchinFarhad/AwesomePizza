package adesso.it.awesomepizzaworker.repository;

import adesso.it.awesomepizzaworker.entity.Pizza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PizzaRepository extends JpaRepository<Pizza, Long> {
    Optional<Pizza> findByOrderId (Long orderId);
}
