package com.awsRdsMySQLApp.utils;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;


public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        // ✅ Spring's built-in transaction detection - NO CUSTOM CONTEXT NEEDED
        boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        return readOnly ? "READ" : "WRITE";
    
    }
}
