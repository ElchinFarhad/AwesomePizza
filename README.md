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

## How to Run

### Awesome Pizza Application

1. **Clone the Repository**:

```
 git clone <repository_url>
```

2. **Build the Application**:

```
 cd awesome-pizza
 mvn clean install
```

3. **Run Kafka with Docker Compose**:

- Ensure you have Docker and Docker Compose installed on your system.
- Navigate to the directory containing the `docker-compose.yaml` file.
- Run the following command:
 ```
  docker-compose up -d
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


6. **Access H2 Database**:
   - You can access the H2 database for the Awesome Pizza application at: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
   - Username: sa (no password)
   

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


3. **Access H2 Database**:
   - You can access the H2 database for the Awesome Worker application at: [http://localhost:8081/h2-console](http://localhost:8081/h2-console)
   - Username: sa (no password)

## Testing

To run the tests for both applications, execute the following command:

```
mvn test
```
