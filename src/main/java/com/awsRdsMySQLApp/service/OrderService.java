package com.awsRdsMySQLApp.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.awsRdsMySQLApp.componetns.OrderMapper;
import com.awsRdsMySQLApp.dto.CreateOrderRequest;
import com.awsRdsMySQLApp.dto.OrderResponse;
import com.awsRdsMySQLApp.entity.Order;
import com.awsRdsMySQLApp.enums.OrderStatus;
import com.awsRdsMySQLApp.repository.OrderRepository;

import jakarta.persistence.EntityNotFoundException;


@Service
@Transactional
public class OrderService {
    
    private final OrderRepository repo;
    private final OrderMapper orderMapper;
    
    public OrderService(OrderRepository repo, OrderMapper orderMapper) {
        this.repo = repo;
        this.orderMapper = orderMapper;
    }

    @Transactional
public OrderResponse createOrder(CreateOrderRequest req) {
    // ✅ Validate ALL required fields FIRST
    validateOrderRequest(req);
    
    // ✅ Build Order with ALL required fields
    Order order = Order.builder()
        .orderNumber(generateOrderNumber())
        .customerName(req.customerName())
        .customerEmail(req.customerEmail())
        .deliveryAddress(req.deliveryAddress())  // ← THIS WAS NULL!
        .status(OrderStatus.PENDING)
        .totalAmount(req.totalAmount())
        .build();
    
    // ← Critical bidirectional fix
    
    Order saved = repo.save(order);
    return orderMapper.toResponse(saved);
}

private void validateOrderRequest(CreateOrderRequest req) {
    if (req.customerName() == null || req.customerName().trim().isEmpty()) {
        throw new IllegalArgumentException("Customer name is required");
    }
    if (req.customerEmail() == null || !req.customerEmail().contains("@")) {
        throw new IllegalArgumentException("Valid customer email is required");
    }
    if (req.deliveryAddress() == null || req.deliveryAddress().trim().isEmpty()) {
        throw new IllegalArgumentException("Delivery address is required");
    }
   
}

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        Order order = repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));
        return orderMapper.toResponse(order);
    }

   

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

   
}