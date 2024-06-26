package adesso.it.awesomepizza.dto;

import java.time.LocalDateTime;

public class OrderDTO {

    private Long id;
    private String orderId;
    private String pizzaType;
    private String note;
    private String status;
    private LocalDateTime orderTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
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

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "OrderDTO{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", pizzaType='" + pizzaType + '\'' +
                ", note='" + note + '\'' +
                ", status='" + status + '\'' +
                ", orderTime=" + orderTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
