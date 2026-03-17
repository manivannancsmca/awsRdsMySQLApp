package com.awsRdsMySQLApp.componetns;

import org.springframework.stereotype.Component;

import com.awsRdsMySQLApp.dto.OrderResponse;
import com.awsRdsMySQLApp.entity.Order;

@Component
public class OrderMapper {
    
    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getCustomerName(),
            order.getCustomerEmail(),
            order.getDeliveryAddress(),
            order.getTotalAmount(),
            order.getCreatedAt(),
            order.getStatus()
        );
    }
}
