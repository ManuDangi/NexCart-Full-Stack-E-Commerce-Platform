🚀 NexCart: Full-Stack E-Commerce Platform
NexCart is a modern, feature-rich e-commerce web application built with a powerful Spring Boot backend and a dynamic React frontend. It provides a seamless shopping experience for users and a robust management dashboard for administrators.


✨ Features
User/Customer Features
User Authentication: Secure user registration and login with JWT.
Product Catalog: Browse products with categories, search, and pagination.
Dynamic Search: Real-time product search functionality.
Shopping Cart: Add, update, and remove products from the cart.
Secure Checkout: Integrated with Stripe API for secure payment processing.
Order History: View past orders and their status.



Admin Features
Admin Dashboard: Centralized dashboard for managing the entire store.
Product Management: Full CRUD (Create, Read, Update, Delete) operations for products.
Order Management: View and update the status of customer orders.
User Management: View and manage registered users.
Inventory Control: Update product stock levels.


💻 Tech Stack
Category	Technology
Backend	Java, Spring Boot, Spring Security, Spring Data JPA (Hibernate), JWT, Lombok
Frontend	React, Material-UI, Axios
Database	MySQL / PostgreSQL
Payment Gateway	Stripe API
Tools & DevOps	Git, Maven, Postman, Docker


Prerequisites
Java JDK 17 or later
Maven 3.2+
Node.js 18.x or later
MySQL Server 8.0 or later

# 1. Repository 
git clone https://github.com/ManuDangi/NexCart-Full-Stack-E-Commerce-Platform.git

# 2. Backend directory 
cd nexcart/backend

# 3. application.properties file configure 
# src/main/resources/application.properties file open 


Backend Setup
spring.datasource.url=jdbc:mysql://localhost:3306/your_database_name
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

jwt.secret=your_strong_jwt_secret_key




cd nexcart/frontend

# . Dependencies install 
# . Application  start 
Run AS :- Java Application 


📄 API Endpoints
A brief overview of some key API endpoints:

POST /api/auth/register - User registration
POST /api/auth/login - User login
GET /api/products - Fetch all products
GET /api/products/{id} - Fetch a single product
POST /api/orders/ - Place a new order
GET /api/admin/orders - (Admin only) Fetch all orders

👨‍💻 Author
Manu Dangi
GitHub:   https://github.com/ManuDangi
LinkedIn: https://www.linkedin.com/in/manu-dangi-75321123a/
