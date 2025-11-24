# Trip Service

A Spring Boot microservice for managing trip lifecycle in the UIT-GO ride-sharing platform. This service handles trip creation, status updates, driver assignment through Kafka events, and provides REST APIs for trip management.

## Architecture Overview

This service is part of a microservices architecture that includes:

- **Trip Management**: Core trip CRUD operations and status management
- **Event-Driven Integration**: Uses Kafka for asynchronous communication with driver services
- **External Service Integration**: Communicates with user-service via Feign client
- **Security**: JWT-based authentication with custom security filters
- **Real-time Updates**: WebSocket support for live trip updates
- **Data Persistence**: MongoDB for trip data storage

## Key Features

- ✅ Create trips with geographical coordinates
- ✅ Query trip details and status tracking
- ✅ Update trip status through REST API
- ✅ Automatic driver assignment via Kafka events
- ✅ JWT authentication and authorization
- ✅ Integration with user service for driver/user names
- ✅ Real-time WebSocket notifications
- ✅ MongoDB persistence

## Technology Stack

- **Framework**: Spring Boot 3.4.6
- **Language**: Java 17
- **Database**: MongoDB
- **Message Queue**: Apache Kafka
- **Security**: Spring Security + JWT (jjwt 0.11.5)
- **HTTP Client**: Spring Cloud OpenFeign
- **WebSocket**: Spring WebSocket
- **Build Tool**: Maven
- **Additional**: Lombok for boilerplate reduction

## Prerequisites

Before running this service, ensure you have:

- **Java 17+** installed
- **Maven 3.6+** (or use included wrapper)
- **MongoDB** running on `localhost:27017`
- **Kafka** running on `localhost:29092`
- **User Service** running on `localhost:3030` (for Feign client)

## Quick Start (Windows PowerShell)

### 1. Build the project

```powershell
.\mvnw.cmd clean package -DskipTests
```

### 2. Run with tests

```powershell
.\mvnw.cmd clean package
```

### 3. Start the service

Run the packaged JAR:
```powershell
java -jar .\target\trip-service-0.0.1-SNAPSHOT.jar
```

Or run directly (development):
```powershell
.\mvnw.cmd spring-boot:run
```

The service will start on **port 3032**.

## Configuration

### Required Infrastructure

Update `src/main/resources/application.properties` for your environment:

```properties
# Application
spring.application.name=trip-service
server.port=3032

# MongoDB Configuration
spring.data.mongodb.uri=mongodb://admin:admin123@localhost:27017/trip-db?authSource=admin&retryWrites=true&w=majority

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:29092
auto.create.topics.enable=true

# JWT Security
jwt.secretKey=MySuperSecretKey12345678901234567890
jwt.header=Authorization
```

### Environment Variables (Production)

For production deployment, override sensitive values:

```powershell
$env:MONGODB_URI="mongodb://your-mongo-cluster/trip-db"
$env:KAFKA_SERVERS="your-kafka-cluster:9092"
$env:JWT_SECRET="your-secure-jwt-secret-key"
$env:USER_SERVICE_URL="https://your-user-service.com"
```

## API Documentation

### Base URL
```
http://localhost:3032/api/trips
```

### Authentication
All endpoints require JWT authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

### Endpoints

#### 1. Create Trip
**POST** `/api/trips`

Creates a new trip and publishes a Kafka event for driver matching.

**Request Body** (`TripRequest`):
```json
{
  "userId": "user123",
  "origin": "Downtown Plaza",
  "destination": "Airport Terminal 1",
  "latitude": "10.762622",
  "longitude": "106.660172"
}
```

**Response**: 
```json
"Waiting for driver to accept the trip"
```

**Kafka Event**: Publishes `CreateTripEvent` to topic `trip_create_wait_driver`

---

#### 2. Get Trip Status
**GET** `/api/trips/{tripId}/status`

**Response**:
```json
"PENDING"
```

**Trip Status Values**:
- `PENDING` - Waiting for driver
- `ACCEPTED` - Driver assigned
- `IN_PROGRESS` - Trip in progress  
- `COMPLETED` - Trip finished
- `CANCELLED` - Trip cancelled

---

#### 3. Get Trip Details
**GET** `/api/trips/{tripId}`

**Response** (`TripResponse`):
```json
{
  "id": "64f8a1b2c3d4e5f678901234",
  "userName": "John Doe",
  "driverName": "Jane Smith",
  "origin": "Downtown Plaza",
  "destination": "Airport Terminal 1", 
  "status": "ACCEPTED"
}
```

---

#### 4. Update Trip Status
**PUT** `/api/trips/{tripId}/status?status={TripStatus}`

**Query Parameters**:
- `status`: One of `PENDING`, `ACCEPTED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`

**Response**:
```json
"Trip status updated to ACCEPTED"
```

### Example API Usage

```powershell
# Create a trip
curl -X POST "http://localhost:3032/api/trips" `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_JWT_TOKEN" `
  -d '{
    "userId": "user123",
    "origin": "Downtown Plaza",
    "destination": "Airport Terminal 1",
    "latitude": "10.762622",
    "longitude": "106.660172"
  }'

# Get trip status
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" `
  "http://localhost:3032/api/trips/TRIP_ID/status"

# Update trip status
curl -X PUT -H "Authorization: Bearer YOUR_JWT_TOKEN" `
  "http://localhost:3032/api/trips/TRIP_ID/status?status=ACCEPTED"
```

## Event-Driven Architecture

### Published Events

**Topic**: `trip_create_wait_driver`  
**Event**: `CreateTripEvent`
```json
{
  "userId": "user123",
  "origin": "Downtown Plaza", 
  "destination": "Airport Terminal 1",
  "latitude": "10.762622",
  "longitude": "106.660172"
}
```

### Consumed Events

**Topic**: `trip_created`  
**Event**: `AcceptTripEvent`  
**Consumer Group**: `driver-service-group`

When a driver accepts a trip, this service listens for the event and:
1. Updates trip status to `ACCEPTED`
2. Assigns the `driverId` to the trip
3. Sends WebSocket notifications (if configured)

## Data Model

### Trip Entity
```java
@Data
public class Trip {
    @Id
    private String id;           // MongoDB ObjectId
    private String driverId;     // Assigned driver ID
    private String userId;       // Trip requester ID
    private String origin;       // Pickup location
    private String destination;  // Drop-off location  
    private TripStatus status;   // Current trip status
}
```

## Testing

### Run Tests
```powershell
.\mvnw.cmd test
```

### Test Coverage
The project includes:
- Unit tests for service layer (`TripServiceImplTest`)
- Integration tests for application context loading
- Kafka and Security test dependencies

## Development

### Project Structure
```
src/main/java/com/example/trip_service/
├── TripServiceApplication.java     # Main application class
├── client/                         # Feign clients
│   └── UserClient.java            # User service integration
├── config/                        # Configuration classes  
│   ├── FeignConfig.java          # Feign client config
│   ├── SecurityConfig.java       # Security configuration
│   ├── SecurityContextFilter.java # JWT filter
│   └── WebSocketConfig.java      # WebSocket config
├── controller/                    # REST controllers
│   └── TripController.java       # Trip API endpoints
├── DTO/                          # Data Transfer Objects
├── ENUM/                         # Enumerations
│   └── TripStatus.java          # Trip status enum
├── event/                        # Event classes
├── eventListener/                # Kafka event listeners
├── model/                        # Domain entities
│   └── Trip.java                # Trip entity
├── repository/                   # Data repositories
├── request/                      # Request DTOs
├── response/                     # Response DTOs
└── service/                      # Business logic
    ├── TripService.java         # Service interface
    └── TripServiceImpl.java     # Service implementation
```

### Adding New Features

1. **New API Endpoints**: Add methods to `TripController`
2. **Business Logic**: Implement in `TripServiceImpl`
3. **Data Model Changes**: Update `Trip` entity and repository
4. **External Integration**: Add new Feign clients in `client/`
5. **Event Handling**: Create listeners in `eventListener/`

## CI/CD Pipeline

This project uses GitHub Actions for automated building and deployment. The workflow is triggered manually and includes:

### Workflow Features
- ✅ **Automated Build**: Maven compilation and packaging
- ✅ **Docker Integration**: Automatic Docker image creation
- ✅ **Registry Push**: Pushes images to Docker Hub
- ✅ **Java 17 Setup**: Ensures consistent build environment

### Pipeline Stages

1. **Code Checkout**: Gets latest source code
2. **Java Setup**: Configures JDK 17 (Temurin distribution)
3. **Maven Build**: Compiles and packages the application
4. **Docker Login**: Authenticates with Docker Hub
5. **Image Build**: Creates Docker image with latest tag
6. **Image Push**: Publishes to Docker Hub registry

### Triggering the Workflow

The CI/CD pipeline can be triggered manually:

1. Go to the **Actions** tab in your GitHub repository
2. Select the **build** workflow
3. Click **Run workflow** button
4. Choose the branch and click **Run workflow**

### Required Secrets

Configure these secrets in your GitHub repository settings:

```
DOCKERHUB_USERNAME - Your Docker Hub username
DOCKERHUB_TOKEN    - Your Docker Hub access token
```

### Accessing Built Images

After successful pipeline execution, Docker images are available at:
```
docker pull <dockerhub-username>/trip-service:latest
```

## Deployment

### Local Docker Deployment

The project includes a Dockerfile for containerization:

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/trip-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 3032
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run locally:
```powershell
docker build -t trip-service .
docker run -p 3032:3032 trip-service
```

### Production Deployment

Use the image from the CI/CD pipeline:
```powershell
# Pull the latest image
docker pull <dockerhub-username>/trip-service:latest

# Run with environment variables
docker run -p 3032:3032 `
  -e MONGODB_URI="mongodb://your-mongo-cluster/trip-db" `
  -e KAFKA_SERVERS="your-kafka-cluster:9092" `
  -e JWT_SECRET="your-secure-jwt-secret" `
  <dockerhub-username>/trip-service:latest
```

## Troubleshooting

### Common Issues

**MongoDB Connection Failed**
```
Check if MongoDB is running on localhost:27017
Verify credentials (admin/admin123)
Ensure database 'trip-db' exists
```

**Kafka Connection Failed** 
```
Verify Kafka is running on localhost:29092
Check if topics are created (auto.create.topics.enable=true)
```

**JWT Authentication Failed**
```
Verify JWT secret key matches other services
Check token format and expiration
Ensure Authorization header format: "Bearer <token>"
```

**User Service Integration Failed**
```
Check if user-service is running on localhost:3030
Verify Feign client configuration
Test user service endpoints manually
```

### Logging

Enable debug logging by adding to `application.properties`:
```properties
logging.level.com.example.trip_service=DEBUG
logging.level.org.springframework.kafka=DEBUG
logging.level.org.springframework.web=DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Commit changes: `git commit -am 'Add new feature'`
4. Push to branch: `git push origin feature/new-feature`  
5. Submit a Pull Request

## License

This project is part of the UIT-GO platform. See the main repository for license information.