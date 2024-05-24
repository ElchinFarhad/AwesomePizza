package adesso.it.awesomepizza.controller;

import adesso.it.awesomepizza.dto.OrderDTO;
import adesso.it.awesomepizza.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("api/orders")
public class OrderController {

    OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService=orderService;
    }

    @PostMapping("/place-order")
    public ResponseEntity<OrderDTO> placeOrder(@Valid @RequestBody OrderDTO orderDTO, BindingResult result) {
        OrderDTO placedOrder = orderService.placeOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(placedOrder);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<OrderDTO> getOrderStatusById(@Valid @PathVariable String id) {
        OrderDTO orderDTO = orderService.getOrderStatusById(id);
        return ResponseEntity.ok(orderDTO);
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderDTO>> getOrders() {
        List<OrderDTO> orderDTOs = orderService.getAllOrders();
        return ResponseEntity.ok(orderDTOs);
    }
}
