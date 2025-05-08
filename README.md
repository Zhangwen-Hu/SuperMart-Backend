# SuperMart Backend

A Spring Boot-based backend system for an e-commerce platform.

## Technologies

- **Java 8**
- **Spring Boot 2.7.14**
- **Spring Security** with JWT authentication
- **Spring Data JPA** for database access
- **MySQL** for data storage
- **Hibernate** as the ORM tool
- **Maven** for dependency management
- **Lombok** for reducing boilerplate code

## Features

- User authentication and authorization (JWT)
- Product management
- Order processing
- Watchlist functionality
- Role-based access control

## API Endpoints

The system provides RESTful APIs for:

### Authentication

- **POST /signup** - Register a new user
  - Request Body: SignupRequest (username, email, password)
  - Response: Success message

- **POST /login** - Authenticate a user
  - Request Body: LoginRequest (username, password)
  - Response: JwtResponse (token, id, username, email, roles)

### Products

- **GET /products/all** - Get all products
  - For Admin: Complete product information including wholesale price
  - For Users: Only in-stock products with limited information
  - Response: List of ProductResponse

- **GET /products/{id}** - Get a specific product by ID
  - For Admin: Complete product information
  - For Users: Limited product information
  - Response: ProductResponse

- **POST /products** - Add a new product (Admin only)
  - Request Body: ProductRequest
  - Response: ProductResponse

- **PATCH /products/{id}** - Update an existing product (Admin only)
  - Request Body: ProductRequest
  - Response: ProductResponse

- **GET /products/profit/{count}** - Get top profitable products (Admin only)
  - Response: List of ProductResponse

- **GET /products/popular/{count}** - Get top popular products (Admin only)
  - Response: List of ProductResponse

- **GET /products/recent/{count}** - Get recently purchased products (User only)
  - Response: List of ProductResponse

- **GET /products/frequent/{count}** - Get frequently purchased products (User only)
  - Response: List of ProductResponse

### Orders

- **POST /orders** - Place a new order
  - Request Body: OrderRequest
  - Response: OrderResponse

- **GET /orders/all** - Get all orders
  - Response: List of OrderResponse

- **GET /orders/{id}** - Get a specific order by ID
  - Response: OrderResponse

- **PATCH /orders/{id}/cancel** - Cancel an order
  - Response: OrderResponse

- **PATCH /orders/{id}/complete** - Mark an order as completed
  - Response: OrderResponse

### Watchlist

- **GET /watchlist/products/all** - Get all products in user's watchlist
  - Response: List of ProductResponse

- **POST /watchlist/product/{productId}** - Add a product to watchlist
  - Response: Success message

- **DELETE /watchlist/product/{productId}** - Remove a product from watchlist
  - Response: Success message

## Database Configuration

The system uses MySQL with the following default configuration:
```
URL: jdbc:mysql://localhost:3306/supermart
Username: root
Password: root
```

## Getting Started

### Prerequisites

- JDK 1.8 or later
- Maven 3.6+
- MySQL 8.0+

### Installation

1. Clone the repository
2. Configure your MySQL database (create a database named 'supermart')
3. Adjust `application.properties` if needed
4. Run the application:
   ```
   mvn spring-boot:run
   ```

### Default Admin Account

A default admin account is created with the following credentials:
- Username: admin
- Email: admin@example.com
- Password: admin123

## Security

The application implements Spring Security with JWT token authentication. Authentication tokens are valid for 24 hours by default.

## Contributing

Please follow the standard Git workflow:
1. Fork the repository
2. Create a feature branch
3. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 