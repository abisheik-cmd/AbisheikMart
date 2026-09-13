# AbisheikMart — Java Multi-Seller E-Commerce Platform

A modern, full-stack **Java Multi-Seller E-Commerce web application** developed as an Academic Capstone Project.

AbisheikMart provides a complete marketplace experience with customer authentication, product discovery, shopping cart, checkout, order management, seller tools, reviews, search and filtering, and database integration.

The application is built using a traditional **Java Servlet + JSP + Maven + WAR + Apache Tomcat** architecture with a responsive HTML, CSS, and JavaScript frontend.

---

## 🚀 Features

### 🔐 Authentication

- User registration
- User login
- Secure password hashing using BCrypt
- Session management
- Role-based application functionality
- Logout functionality

### 🛍️ Customer Marketplace

- Product browsing
- Product details
- Product search
- Product filtering
- Product categories
- Shopping cart
- Quantity management
- Checkout
- Order management
- Order history
- Product reviews and ratings

### 🏪 Seller Platform

- Seller dashboard
- Product management
- Add products
- Edit products
- Manage product listings
- Seller-specific marketplace functionality

### ⚡ Application

- AJAX-powered interactions
- Responsive web interface
- Database persistence
- Error handling
- Modular JSP views
- Session-based authentication

---

## 🧠 Technical Stack & Architecture

### Backend

- **Java 25**
- **Java Servlets**
- **JSP**
- **Maven**
- **WAR Packaging**
- **Apache Tomcat**

### Database

- **H2 Database**
- **HikariCP** for database connection pooling

### Data & Security

- **Gson** for JSON serialization
- **jBCrypt** for password hashing

### Frontend

- **HTML5**
- **CSS3**
- **JavaScript**
- **JSP**

### Development Tools

- **Git**
- **GitHub**
- **Visual Studio Code**
- **Antigravity**
- **Maven Wrapper**

---

## 🏗️ Application Architecture

```text
                         ABISHEIKMART
                              │
                              ▼
                    ┌───────────────────┐
                    │      Browser      │
                    └─────────┬─────────┘
                              │
                              ▼
                    ┌───────────────────┐
                    │    JSP / HTML     │
                    │    CSS / JS       │
                    └─────────┬─────────┘
                              │
                              ▼
                    ┌───────────────────┐
                    │   Java Servlets   │
                    └─────────┬─────────┘
                              │
                              ▼
                    ┌───────────────────┐
                    │ Business / Data   │
                    │      Layer        │
                    └─────────┬─────────┘
                              │
                              ▼
                    ┌───────────────────┐
                    │    H2 Database    │
                    └───────────────────┘
                              │
                              ▼
                       Apache Tomcat
📂 Project Structure
AbisheikMart/
│
├── .github/
│   └── workflows/
│
├── .mvn/
│   └── wrapper/
│
├── .vscode/
│
├── build/
├── data/
├── public/
├── third_party/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── abisheikmart/
│   │   │           ├── controllers/
│   │   │           ├── models/
│   │   │           ├── services/
│   │   │           ├── dao/
│   │   │           └── utils/
│   │   │
│   │   ├── resources/
│   │   │
│   │   └── webapp/
│   │       │
│   │       ├── css/
│   │       │   └── style.css
│   │       │
│   │       ├── images/
│   │       │   ├── logo.jpg
│   │       │   ├── airfryer.jpg
│   │       │   ├── books.jpg
│   │       │   ├── camera.jpg
│   │       │   ├── coffeemaker.jpg
│   │       │   ├── dumbbell.jpg
│   │       │   ├── headphones.jpg
│   │       │   ├── jacket.jpg
│   │       │   ├── keyboard.jpg
│   │       │   ├── smartwatch.jpg
│   │       │   └── sneakers.jpg
│   │       │
│   │       ├── js/
│   │       │   ├── app.js
│   │       │   ├── cart-ajax.js
│   │       │   ├── features.js
│   │       │   └── seller-ajax.js
│   │       │
│   │       ├── WEB-INF/
│   │       │   ├── web.xml
│   │       │   └── views/
│   │       │       ├── auth/
│   │       │       ├── cart/
│   │       │       ├── catalog/
│   │       │       ├── common/
│   │       │       ├── error/
│   │       │       ├── order/
│   │       │       └── seller/
│   │       │
│   │       └── index.jsp
│   │
│   └── test/
│
├── .env.example
├── .gitignore
├── ecommerce.db
├── mvnw
├── mvnw.cmd
├── pom.xml
├── run_server.bat
└── README.md
🎨 User Interface

AbisheikMart uses a modern, dark and cinematic UI designed around a strong blue accent system.

Design Direction
Dark Background
       +
Glass-inspired Surfaces
       +
Electric Blue Accents
       +
Cyan / Purple Glow
       +
Rounded Components
       +
Soft Shadows
       +
Responsive Layout

The interface focuses on creating a polished marketplace experience while keeping navigation and functionality straightforward.

UI Components
Modern navigation
Responsive product grids
Product cards
Authentication forms
Glass-style cards
Rounded input fields
Modern buttons
Search and filtering interfaces
Shopping cart interface
Seller dashboard
Responsive tables and forms
Smooth UI interactions
📱 Responsive Design

The frontend is designed to adapt across:

Desktop
   ↓
Laptop
   ↓
Tablet
   ↓
Mobile

The application should maintain:

Responsive layouts
Flexible product grids
Mobile-friendly navigation
Responsive forms
Touch-friendly controls
No unnecessary horizontal scrolling
🗄️ Database

AbisheikMart uses H2 Database for application data persistence.

Database connectivity is managed using HikariCP.

The application maintains data related to areas such as:

Users
Products
Cart Items
Orders
Order Items
Reviews

Database configuration is maintained separately from the presentation layer.

🔒 Security

Security is considered throughout the application.

Security Features
BCrypt password hashing
Session-based authentication
Input validation
Protected authenticated functionality
Environment-based configuration
No hard-coded production secrets

Never commit production credentials, API keys, passwords, or other sensitive configuration to the repository.

⚙️ Requirements

Before running AbisheikMart, make sure the following are installed:

Java JDK 25+
Git
Maven Wrapper
Apache Tomcat-compatible environment

Verify Java:

java -version
🚀 Building & Running
1. Clone the Repository
git clone https://github.com/abisheik-cmd/AbisheikMart.git
cd AbisheikMart
2. Configure Java

Set JAVA_HOME to your installed JDK.

Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Verify:

java -version
3. Build the Application
Windows
.\mvnw.cmd package
Linux / macOS
./mvnw package
4. Start the Application
Windows
.\mvnw.cmd tomcat7:run
Linux / macOS
./mvnw tomcat7:run
🌐 Open the Application

Once Tomcat starts, open:

http://localhost:8080
🧪 Testing

Run the Maven test suite:

Windows
.\mvnw.cmd test
Linux / macOS
./mvnw test

Testing should cover:

Authentication
Registration
Product browsing
Product management
Shopping cart
Checkout
Orders
Seller functionality
Database operations
Servlet behavior
🐛 Troubleshooting
CSS Not Loading

Verify that:

src/main/webapp/css/style.css

exists.

JSP pages should reference CSS using the application context path:

<link
    rel="stylesheet"
    href="${pageContext.request.contextPath}/css/style.css">
Images Not Loading

Verify that images exist inside:

src/main/webapp/images/

Use:

<img
    src="${pageContext.request.contextPath}/images/logo.jpg"
    alt="AbisheikMart Logo">
JavaScript Not Loading

Verify:

src/main/webapp/js/

and use context-aware paths:

<script
    src="${pageContext.request.contextPath}/js/app.js">
</script>
Port 8080 Already in Use

Stop the process using port 8080 or terminate the existing Java/Tomcat process.

On Windows:

taskkill /F /IM java.exe

Then restart:

.\mvnw.cmd tomcat7:run
Build Files Are Locked

Stop Tomcat first.

Then remove the generated target directory:

Remove-Item -Recurse -Force ".\target"

Rebuild:

.\mvnw.cmd package
🧩 Development Guidelines

When contributing to AbisheikMart:

Preserve the existing Java architecture.
Keep frontend and backend responsibilities separated.
Avoid unnecessary dependencies.
Test changes before committing.
Do not commit secrets.
Preserve working functionality.
Keep JSP views organized.
Keep CSS and JavaScript modular.
Maintain responsive behavior.
Document major architectural changes.
🚫 Architecture Restrictions

AbisheikMart is currently a Java Servlet/JSP web application.

The project should NOT be migrated or incorrectly documented as:

❌ C++
❌ CMake
❌ Spring Boot
❌ Node.js Backend
❌ React-only Application
❌ Static-only Website

The current architecture is:

✅ Java 25
✅ Java Servlets
✅ JSP
✅ Maven
✅ WAR
✅ Apache Tomcat
✅ H2
✅ HikariCP
✅ Gson
✅ BCrypt
✅ HTML
✅ CSS
✅ JavaScript
🗺️ Development Roadmap
Phase 1 — Core Platform
 Java migration
 Maven configuration
 WAR packaging
 Servlet architecture
 JSP structure
 H2 database integration
 Database connection pooling
Phase 2 — Frontend
 Restore original UI
 Refine authentication interface
 Refine marketplace interface
 Refine product cards
 Refine cart and checkout
 Refine seller dashboard
 Fix asset loading
 Improve responsive behavior
 Add polished micro-interactions
Phase 3 — Testing
 Authentication testing
 Product testing
 Cart testing
 Checkout testing
 Order testing
 Seller testing
 Database testing
 Responsive testing
 Browser compatibility testing
Phase 4 — Production
 Production database
 Environment configuration
 Security review
 Performance optimization
 Java-compatible hosting
 HTTPS
 Production testing
 Domain configuration
📊 Project Status
Component	Status
☕ Java Architecture	🟢 Active
🌐 Servlet/JSP Application	🟢 Active
📦 Maven / WAR	🟢 Active
🗄️ Database	🟢 Active
🔐 Authentication	🟢 Active
🛍️ Marketplace	🟢 Active
🏪 Seller System	🟢 Active
🎨 UI Restoration	🟡 In Progress
🧪 Testing	🟡 In Progress
🌍 Production Deployment	🔴 Pending
💡 Project Vision

AbisheikMart aims to become a complete, modern multi-seller e-commerce platform that combines:

Software Engineering
        +
Backend Development
        +
Frontend Design
        +
Database Systems
        +
Security
        +
Modern User Experience

The goal is not simply to create an online store, but to build a complete marketplace application while continuously improving its architecture, functionality, design, and reliability.

👨‍💻 Developer
Abisheik J

CSE Student • Developer • Designer • Creative Technologist

Built as an Academic Capstone Project.

🔗 Repository

GitHub:
https://github.com/abisheik-cmd/AbisheikMart

🚀 AbisheikMart
Next-Gen Multi-Seller E-Commerce Platform

Built with Java. Designed to evolve.

© Abisheik J
