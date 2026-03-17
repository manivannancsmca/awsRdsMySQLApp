package com.awsRdsMySQLApp.dto;

import java.math.BigDecimal;

public record CreateOrderRequest(
    String orderNumber,
    String customerName,
    String customerEmail,
    String deliveryAddress,
    BigDecimal totalAmount
) {}
