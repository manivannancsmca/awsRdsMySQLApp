package com.awsRdsMySQLApp.aop;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.awsRdsMySQLApp.enums.DataSourceType;
import com.awsRdsMySQLApp.utils.DataSourceContextHolder;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class ReadWriteRoutingAspect {

    @Pointcut("@within(org.springframework.transaction.annotation.Transactional) || " +
              "@annotation(org.springframework.transaction.annotation.Transactional)")
    public void transactionalMethods() {}

    @Before("transactionalMethods() && @annotation(tx)")
    public void setDataSourceForMethod(Transactional tx) {
        if (tx.readOnly()) {
            DataSourceContextHolder.set(DataSourceType.READ);
        } else {
            DataSourceContextHolder.set(DataSourceType.WRITE);
        }
    }

    @Before("transactionalMethods() && @within(tx)")
    public void setDataSourceForClass(Transactional tx) {
        // Only set if not already set by method-level annotation
        if (DataSourceContextHolder.get() == null) {
            if (tx.readOnly()) {
                DataSourceContextHolder.set(DataSourceType.READ);
            } else {
                DataSourceContextHolder.set(DataSourceType.WRITE);
            }
        }
    }

    @After("transactionalMethods()")
    public void clearDataSource() {
        DataSourceContextHolder.clear();
    }
}
