package adesso.it.awesomepizzaworker.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pizza_preparation")
public class Pizza {

    @Id
    @SequenceGenerator(name = "worker_id_seq", sequenceName = "worker_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true, nullable = false)
    private Long orderId;
    @Column(nullable = false)
    private String pizzaType;
    private String note;
    private String status;
    private LocalDateTime receivedTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getPizzaType() {
        return pizzaType;
    }

    public void setPizzaType(String pizzaType) {
        this.pizzaType = pizzaType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(LocalDateTime receivedTime) {
        this.receivedTime = receivedTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Pizza{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", pizzaType='" + pizzaType + '\'' +
                ", note='" + note + '\'' +
                ", status='" + status + '\'' +
                ", receivedTime=" + receivedTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
