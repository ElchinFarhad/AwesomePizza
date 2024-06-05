# Awesome Pizza and Awesome Worker Applications

These are Spring Boot applications for managing pizza orders and processing order messages.

## Features

### Awesome Pizza Application

- Place new pizza orders
- Retrieve the status of an order by its ID
- Retrieve all orders

### Awesome Worker Application

- Process pizza order messages asynchronously
- Send order status messages

## Technologies Used

- Java
- Spring Boot
- Kafka
- Docker
- PostgreSQL

## How to Run

### Awesome Pizza Application

1. **Clone the Repository**:

```
 git clone <repository_url>
```

2.**Run Kafka and PostgreSQL with Docker Compose**:

- Ensure you have Docker and Docker Compose installed on your system.
- Navigate to the directory containing the `docker-compose.yaml` file.
- Run the following command:

```
 docker-compose up -d
```

3. **Build the Application**:

```
 cd awesome-pizza
 mvn clean install
```

4. **Run the Application**:

```
mvn spring-boot:run
```

5. **Access the Application**:
   Once the application is running, you can access the following endpoints:

- Place a new order: POST `http://localhost:8080/api/orders/place-order`
  - Sample Request Body:
    {
    "pizzaType": "Quattro formaggi",
    "note": "more cheese"
    }
- Get order status by ID: GET `http://localhost:8080/api/orders/{id}/status`
- Get all orders: GET `http://localhost:8080/api/orders/all`

6. **Access PostgreSQL Databases**:
   - You can access the PostgreSQL databases for both applications using tools like pgAdmin or any PostgreSQL client.
   - Awesome Pizza Application database URL: jdbc:postgresql://localhost:5432/awesome_pizza
   - Awesome Worker Application database URL: jdbc:postgresql://localhost:5433/awesome_worker
   - Username: test
   - Password: test

### Awesome Worker Application

1. **Build the Application**:

```
 cd awesome-worker
 mvn clean install
```

2. **Run the Application**:

```
mvn spring-boot:run
```

## Testing

To run the tests for both applications, execute the following command:

```
mvn test
```
