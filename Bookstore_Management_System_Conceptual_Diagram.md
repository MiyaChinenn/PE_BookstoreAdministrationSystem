# Bookstore Management System - Conceptual Diagram

## System Architecture Overview

```mermaid
graph TB
    subgraph "Client Layer"
        UI[Web Browser]
        Admin[Admin Interface]
        Customer[Customer Interface]
    end
    
    subgraph "Presentation Layer"
        Controllers[Spring Controllers]
        Templates[Thymeleaf Templates]
    end
    
    subgraph "Business Logic Layer"
        Services[Business Services]
        DAO[Data Access Objects]
        Repositories[JPA Repositories]
    end
    
    subgraph "Data Layer"
        MySQL[(MySQL Database)]
    end
    
    subgraph "External Systems"
        Payment[Payment Gateway]
    end
    
    UI --> Controllers
    Admin --> Controllers
    Customer --> Controllers
    Controllers --> Templates
    Controllers --> Services
    Services --> DAO
    Services --> Repositories
    DAO --> MySQL
    Repositories --> MySQL
    Services --> Payment
```

## Entity Relationship Diagram

```mermaid
erDiagram
    USERS {
        string username PK
        string firstName
        string lastName
        string phoneNumber
        string password
        string role
    }
    
    BOOKS {
        int bookId PK
        string type
        string name
        double price
        string publisher
        int quantity
    }
    
    ORDERS {
        int orderId PK
        string username FK
        datetime orderDate
        string status
        decimal totalAmount
        int totalItems
    }
    
    ORDERDETAILS {
        int orderDetailId PK
        int orderId FK
        int bookId FK
        int quantity
        double price
        decimal subtotal
    }
    
    SHOPPINGCART {
        string username FK
        int bookId FK
        int quantity
        double price
        timestamp dateAdded
    }
    
    USERS ||--o{ ORDERS : "places"
    USERS ||--o{ SHOPPINGCART : "has"
    ORDERS ||--o{ ORDERDETAILS : "contains"
    BOOKS ||--o{ ORDERDETAILS : "included_in"
    BOOKS ||--o{ SHOPPINGCART : "added_to"
```

## Use Case Diagram

```mermaid
graph TB
    subgraph "Actors"
        Admin[Admin User]
        Customer[Customer User]
        Guest[Guest User]
    end
    
    subgraph "Authentication & User Management"
        UC1[Login]
        UC2[Register]
        UC3[Logout]
        UC4[Manage Users]
    end
    
    subgraph "Book Management"
        UC5[View Books]
        UC6[Search Books]
        UC7[Add Book]
        UC8[Edit Book]
        UC9[Delete Book]
        UC10[Update Inventory]
    end
    
    subgraph "Shopping Cart"
        UC11[Add to Cart]
        UC12[Remove from Cart]
        UC13[Update Quantity]
        UC14[View Cart]
    end
    
    subgraph "Order Management"
        UC15[Place Order]
        UC16[View Orders]
        UC17[Order Details]
        UC18[Process Orders]
        UC19[Update Order Status]
    end
    
    subgraph "Checkout Process"
        UC20[Checkout]
        UC21[Payment Processing]
        UC22[Order Confirmation]
    end
    
    Admin --> UC1
    Admin --> UC3
    Admin --> UC4
    Admin --> UC7
    Admin --> UC8
    Admin --> UC9
    Admin --> UC10
    Admin --> UC18
    Admin --> UC19
    
    Customer --> UC1
    Customer --> UC2
    Customer --> UC3
    Customer --> UC5
    Customer --> UC6
    Customer --> UC11
    Customer --> UC12
    Customer --> UC13
    Customer --> UC14
    Customer --> UC15
    Customer --> UC16
    Customer --> UC17
    Customer --> UC20
    Customer --> UC21
    Customer --> UC22
    
    Guest --> UC5
    Guest --> UC6
    Guest --> UC2
```

## Component Diagram

```mermaid
graph TB
    subgraph "Web Layer"
        LoginController[LoginController]
        RegisterController[RegisterController]
        AdminController[AdminController]
        BookApiController[BookApiController]
        CartController[CartController]
        OrderController[OrderController]
        CheckoutController[CheckoutController]
    end
    
    subgraph "Business Logic Layer"
        UserDAO[UserDAO]
        BookDAO[BookDAO]
        ShoppingCartDAO[ShoppingCartDAO]
        OrderDAO[OrderDAO]
        OrderDetailDAO[OrderDetailDAO]
        UserRepository[UserRepository]
    end
    
    subgraph "Data Models"
        User[User Model]
        Book[Book Model]
        ShoppingCart[ShoppingCart Model]
        Order[Order Model]
        OrderDetails[OrderDetails Model]
    end
    
    subgraph "Database"
        MySQL[(MySQL Database)]
    end
    
    subgraph "External Services"
        PaymentGateway[Payment Gateway]
    end
    
    LoginController --> UserDAO
    RegisterController --> UserDAO
    AdminController --> BookDAO
    AdminController --> OrderDAO
    BookApiController --> BookDAO
    CartController --> ShoppingCartDAO
    OrderController --> OrderDAO
    CheckoutController --> OrderDAO
    CheckoutController --> PaymentGateway
    
    UserDAO --> User
    BookDAO --> Book
    ShoppingCartDAO --> ShoppingCart
    OrderDAO --> Order
    OrderDetailDAO --> OrderDetails
    
    UserDAO --> MySQL
    BookDAO --> MySQL
    ShoppingCartDAO --> MySQL
    OrderDAO --> MySQL
    OrderDetailDAO --> MySQL
    UserRepository --> MySQL
```

## Sequence Diagram - Order Processing

```mermaid
sequenceDiagram
    participant C as Customer
    participant UI as Web Interface
    participant CC as CartController
    participant SC as ShoppingCartDAO
    participant OC as OrderController
    participant OD as OrderDAO
    participant OD2 as OrderDetailDAO
    participant DB as Database
    participant PG as Payment Gateway
    
    C->>UI: Add items to cart
    UI->>CC: Add to cart request
    CC->>SC: Save cart item
    SC->>DB: Insert cart record
    DB-->>SC: Confirmation
    SC-->>CC: Success
    CC-->>UI: Cart updated
    UI-->>C: Item added
    
    C->>UI: Proceed to checkout
    UI->>OC: Checkout request
    OC->>OD: Create order
    OD->>DB: Insert order
    DB-->>OD: Order ID
    OC->>OD2: Create order details
    OD2->>DB: Insert order details
    DB-->>OD2: Confirmation
    OC->>PG: Process payment
    PG-->>OC: Payment result
    OC->>SC: Clear cart
    SC->>DB: Delete cart items
    OC-->>UI: Order confirmation
    UI-->>C: Order success
```

## Technology Stack

```mermaid
graph TB
    subgraph "Frontend"
        HTML[HTML5]
        CSS[CSS3]
        JS[JavaScript]
        Thymeleaf[Thymeleaf Templates]
    end
    
    subgraph "Backend"
        SpringBoot[Spring Boot 3.2.0]
        SpringWeb[Spring Web]
        SpringJPA[Spring Data JPA]
        SpringSecurity[Spring Security]
    end
    
    subgraph "Database"
        MySQL[MySQL 8.0]
    end
    
    subgraph "Build & Deploy"
        Maven[Maven]
        Tomcat[Tomcat]
        Java21[Java 21]
    end
    
    HTML --> Thymeleaf
    CSS --> Thymeleaf
    JS --> Thymeleaf
    Thymeleaf --> SpringWeb
    SpringWeb --> SpringBoot
    SpringBoot --> SpringJPA
    SpringBoot --> SpringSecurity
    SpringJPA --> MySQL
    Maven --> SpringBoot
    SpringBoot --> Tomcat
    SpringBoot --> Java21
```

## Key Features

### For Customers:
- User registration and authentication
- Browse and search books by category
- Add books to shopping cart
- Manage shopping cart (update quantities, remove items)
- Place orders with checkout process
- View order history and details
- Real-time inventory checking

### For Administrators:
- Manage book inventory (add, edit, delete books)
- Update book quantities and prices
- View and process customer orders
- Manage user accounts
- Monitor system status
- Generate reports

### System Capabilities:
- Secure user authentication and authorization
- Shopping cart persistence
- Order management with status tracking
- Inventory management
- Payment processing integration
- Responsive web interface
- Database transaction management 