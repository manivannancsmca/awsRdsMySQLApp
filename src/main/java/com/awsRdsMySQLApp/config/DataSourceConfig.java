package com.awsRdsMySQLApp.config;

import com.awsRdsMySQLApp.enums.DataSourceType;
import com.awsRdsMySQLApp.utils.RoundRobinReadDataSource;
import com.awsRdsMySQLApp.utils.RoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.write")
    public HikariDataSource writeDataSource() {
        return new HikariDataSource();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.reads")
    public List<HikariDataSource> readHikariDataSources() {
        return List.of(new HikariDataSource(), new HikariDataSource());
    }

    @Bean
    public List<DataSource> readDataSources(List<HikariDataSource> readHikariDataSources) {
        // ✅ Cast to DataSource interface (matches RoundRobinReadDataSource constructor)
        return readHikariDataSources.stream()
                .peek(ds -> ds.setReadOnly(true))
                .map(ds -> (DataSource) ds)
                .toList();
    }

    @Primary
@Bean
public DataSource routingDataSource(
        @Qualifier("writeDataSource") DataSource writeDataSource,
        @Qualifier("readDataSources") List<DataSource> readDataSources) {

    RoutingDataSource routingDataSource = new RoutingDataSource();
    
    Map<Object, Object> targetDataSources = new HashMap<>();
    targetDataSources.put("WRITE", writeDataSource);  // Master
    targetDataSources.put("READ", new RoundRobinReadDataSource(readDataSources));  // Replicas
    
    routingDataSource.setTargetDataSources(targetDataSources);
    routingDataSource.setDefaultTargetDataSource(writeDataSource);
    
    return routingDataSource;
}
}
