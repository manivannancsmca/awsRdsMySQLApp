package com.awsRdsMySQLApp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.awsRdsMySQLApp.enums.OrderStatus;

public record OrderResponse(
    Long id,
    String orderNumber,
    String customerName,
    String customerEmail,
    String deliveryAddress,
    BigDecimal totalAmount,
    LocalDateTime createdAt,
    OrderStatus status
) {}
