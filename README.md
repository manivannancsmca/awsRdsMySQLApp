A production-ready Spring Boot microservice demonstrating read/write splitting with AWS RDS MySQL using RoundRobin read replicas and abstract routing datasource.

✨ Key Features
✅ Master/Slave Routing: @Transactional → Master, @Transactional(readOnly=true) → Replicas

✅ RoundRobinReadDataSource: Load balances across multiple read replicas

✅ Zero-downtime Deployment: HikariCP connection pooling optimized

✅ Flyway Migrations: Automatic schema management

✅ JPA/Hibernate: Full entity mapping with bidirectional relationships

✅ REST API: Complete Order CRUD with validation

✅ Observability Ready: Structured logging, health checks


┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   REST Client   │───▶│  Spring Boot     │───▶│  Master (Write) │
│                 │    │  OrderService    │    │  orderdb        │
└─────────────────┘    │  @Transactional  │    └─────────────────┘
                       │                   │         ▲
                       │ ┌─────────────────┐         │ Async Replication
                       │ │ RoutingDataSource│         │ (milliseconds lag)
                       │ │  READ → Replicas │         │
                       │ │ WRITE → Master   │         │
                       │ └─────────────────┘         │
                       │          ▲                  │
                       └──────────┼──────────────────┘
                                  │
                       ┌─────────────────┐    ┌─────────────────┐
                       │ @Transactional  │    │ Replica 1 (Read) │
                       │ (readOnly=true) │───▶│  orderdb        │
                       │ getOrder()      │    └─────────────────┘
                       └─────────────────┘           ▲
                                                    │
                                             ┌─────────────────┐
                                             │ Replica 2 (Read) │
                                             │  orderdb        │
                                             └─────────────────┘


🚀 Quick Start
1. AWS RDS Setup

   # Create Master DB (orderdb)
aws rds create-db-instance \
  --db-instance-identifier orderdb-master \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --master-username admin \
  --master-user-password test1234 \
  --backup-retention-period 7 \
  --allocated-storage 20

# Create Read Replicas (after master is ready)
aws rds create-db-instance-read-replica \
  --db-instance-identifier orderdb-replica1 \
  --source-db-instance-identifier orderdb-master

aws rds create-db-instance-read-replica \
  --db-instance-identifier orderdb-replica2 \
  --source-db-instance-identifier orderdb-master

2. Update application.yml

   spring:
  datasource:
    write:
      jdbc-url: jdbc:mysql://orderdb-master.xxxx.region.rds.amazonaws.com:3306/orderdb?..
    reads:
      - jdbc-url: jdbc:mysql://orderdb-replica1.xxxx.region.rds.amazonaws.com:3306/orderdb?..
      - jdbc-url: jdbc:mysql://orderdb-replica2.xxxx.region.rds.amazonaws.com:3306/orderdb?..

3. Run Application

   mvn spring-boot:run

4. Test API

   # Create Order (→ Master)
curl -X POST http://localhost:8085/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "John Doe",
    "customerEmail": "john@example.com", 
    "deliveryAddress": "123 Main St, Chennai",
    "items": [{"productId":1,"productName":"Laptop","price":999.99,"quantity":1}]
  }'

# Get Order (→ Replica - RoundRobin)
curl http://localhost:8085/api/orders/1


🔧 Core Components
1. RoundRobinReadDataSource

   public class RoundRobinReadDataSource implements DataSource {
    private final List<DataSource> delegates;
    private final AtomicInteger index = new AtomicInteger(0);
    
    private DataSource next() {
        int i = Math.abs(index.getAndIncrement() % delegates.size());
        return delegates.get(i);  // Cycles: replica1 → replica2 → replica1...
    }
}

2. RoutingDataSource

   public class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly() 
            ? "READ" : "WRITE";
    }
}

3. Service Layer Routing

   @Transactional        // → Master (WRITE)
public OrderResponse createOrder(CreateOrderRequest req) { ... }

@Transactional(readOnly = true)  // → Replica (READ)
public OrderResponse getOrder(Long id) { ... }


📊 Performance Benefits

| Operation  | Master   | Replica 1 | Replica 2 | RoundRobin      |
| ---------- | -------- | --------- | --------- | --------------- |
| Create     | ✅ 100%   | ❌         | ❌         | ❌               |
| Read       | ✅ 33%    | ✅ 33%     | ✅ 33%     | ✅ Load Balanced |
| 1000 req/s | 1000 r/s | 333 r/s   | 333 r/s   | 1000 r/s        |
