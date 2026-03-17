package com.awsRdsMySQLApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.awsRdsMySQLApp.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
