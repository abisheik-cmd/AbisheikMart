# 🛒 AbisheikMart

**AbisheikMart** is a Java-based multi-vendor e-commerce platform built as an academic capstone project. It provides complete shopping workflows for **buyers, sellers, and administrators**, from product discovery and cart management to checkout and order management.

### 🌐 Live Demo

**[https://abisheikmart-production.up.railway.app](https://abisheikmart-production.up.railway.app)**

---

## ✨ Features

* 🛍️ Product catalog with multiple categories
* 🔍 Product search and filtering
* 👤 User registration and authentication
* 🛒 Shopping cart management
* 📦 Checkout and order processing
* 📋 Order history
* 🏪 Seller dashboard and product management
* ⚙️ Admin dashboard and platform management
* ⭐ Product reviews and ratings
* 🔐 Role-based access control
* 🔒 Secure password hashing
* 📱 Responsive modern e-commerce interface
* ☁️ Cloud deployment on Railway

---

## 🧑‍💻 Technology Stack

| Layer           | Technology                       |
| --------------- | -------------------------------- |
| Language        | Java 17                          |
| Backend         | Java Servlets                    |
| Frontend        | JSP, JSTL, HTML, CSS, JavaScript |
| Database        | H2                               |
| Build Tool      | Maven                            |
| Server          | Apache Tomcat 9                  |
| Connection Pool | HikariCP                         |
| Security        | jBCrypt                          |
| JSON            | Gson                             |
| Testing         | JUnit 5, Mockito                 |
| Logging         | SLF4J, Logback                   |
| Deployment      | Railway                          |

---

## 🏗️ Architecture

```text
                    ┌──────────────────┐
                    │      Browser     │
                    │   HTML / JSP /   │
                    │  CSS / JavaScript│
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │  Java Servlets  │
                    │   Controllers    │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │    Services      │
                    │ Business Logic   │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │      DAO Layer   │
                    │ Data Access      │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │   H2 Database    │
                    └──────────────────┘
```

---

## 👥 User Roles

### 🛍️ Buyer

* Browse products
* Search and filter products
* View product details
* Add products to cart
* Checkout
* View order history
* Submit product reviews

### 🏪 Seller

* Manage products
* View seller dashboard
* Manage product inventory
* View customer orders
* Update order status

### ⚙️ Administrator

* Manage users
* Manage products
* Manage orders
* View platform statistics
* Control platform operations

---

## 🧪 Testing

The project includes automated testing using **JUnit 5 and Mockito**.

Current verification:

```text
44 Tests
44 Passed
0 Failed
```

The application has also been packaged successfully as a **WAR** and deployed to Apache Tomcat 9 on Railway.

---

## ☁️ Deployment

AbisheikMart is deployed using:

```text
Railway
   ↓
Java 17
   ↓
Apache Tomcat 9
   ↓
WAR Application
   ↓
H2 Database
```

### Live Application

**[https://abisheikmart-production.up.railway.app](https://abisheikmart-production.up.railway.app)**

---

## 📁 Project Structure

```text
AbisheikMart/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/abisheikmart/
│   │   ├── resources/
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       ├── css/
│   │       ├── images/
│   │       └── js/
│   └── test/
├── pom.xml
├── Dockerfile
└── README.md
```

---

## 🚀 Running Locally

### Prerequisites

* Java 17
* Maven
* Apache Tomcat 9

### Build

```bash
mvn clean package
```

### Run Tests

```bash
mvn test
```

The generated WAR file can be deployed to a Tomcat 9 server.

---

## 🎯 Project Objective

The primary objective of AbisheikMart is to demonstrate the development of a complete **Java web-based e-commerce system** using Servlets, JSP, Maven, Tomcat, and H2 while implementing real-world concepts such as authentication, role-based authorization, database operations, cart management, order processing, testing, and cloud deployment.

---

## 📌 Project Status

**Production-ready academic capstone project**

> Built with Java, tested with JUnit, packaged as a WAR, and deployed to the cloud.

---

### 👨‍💻 Developer

**Abisheik J**

**GitHub:** `https://github.com/abisheik-cmd`

**Live Project:** [AbisheikMart Live Demo](https://abisheikmart-production.up.railway.app?utm_source=chatgpt.com)
                            
