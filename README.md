# Abisheikmart — C++ Multi-Seller E-Commerce Platform

A portable, self-contained C++ Full-Stack Multi-Seller E-Commerce web application developed for an Academic Capstone Project. Built using standard C++17, an embedded C++ HTTP server (`cpp-httplib`), embedded SQLite3 database, and a clean, responsive single-page web UI.

---

## Technical Stack & Architecture

- **Backend**: C++17 Standard Code
- **HTTP / REST Server**: `cpp-httplib` (single-header C++ server)
- **Database Engine**: Embedded SQLite3 (`sqlite3.c` & `sqlite3.h`)
- **JSON Serialization**: `nlohmann/json` (`json.hpp`)
- **Password Security & KDF**: PBKDF2-HMAC-SHA256 (10,000 iterations + unique salt)
- **Build System**: CMake (Minimum Version 3.15)
- **IDE Support**: Visual Studio Code pre-configured tasks & launch configurations

---

## Project Structure

```
Abisheikmart/
├── CMakeLists.txt              # Primary CMake build configuration
├── README.md                   # Setup, build, run, default admin credentials & troubleshooting
├── .gitignore                  # Git ignore rules for build artifacts and database
├── .env.example                # Example environment configuration
├── .vscode/
│   ├── tasks.json              # VS Code build tasks configuration
│   └── launch.json             # VS Code debugger configuration
├── third_party/                # Self-contained third-party headers/sources
│   ├── httplib/httplib.h       # Header-only C++ HTTP server
│   ├── json/json.hpp           # Header-only nlohmann JSON library
│   └── sqlite/                 # SQLite3 embedded C database engine
│       ├── sqlite3.h
│       ├── sqlite3.c
│       └── sqlite3ext.h
├── src/                        # C++ Backend Source Code
│   ├── main.cpp                # Server entry point & API route setup
│   ├── db/
│   │   ├── Database.h          # SQLite database connection & schema manager
│   │   └── Database.cpp
│   ├── utils/                  # Password hashing & session utilities
│   ├── models/                 # User, Product, CartItem, Order entities
│   └── controllers/            # Auth, Product, Cart, Order controllers
└── public/                     # Static Web Frontend
    └── index.html              # Main Web Portal
```

---

## Default Administrator Credentials

> [!IMPORTANT]
> The database automatically seeds a default administrator account upon initial application launch if the database file does not exist:
>
> - **Username**: `admin`
> - **Email**: `admin@abishmart.com`
> - **Password**: `Admin@123`
> - **Role**: `ADMIN`

---

## Building and Running

### Prerequisites
- A modern C++ compiler supporting C++17 (e.g. `g++`, `clang++`, or MSVC `cl.exe`).
- `CMake` version 3.15 or higher.

### Command Line Instructions

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/Abisheikmart.git
   cd Abisheikmart
   ```

2. **Configure the build using CMake**:
   ```bash
   cmake -B build
   ```
   *(Note: On Windows with MinGW, run `cmake -B build -G "MinGW Makefiles"` if MinGW is your primary generator).*

3. **Build the executable**:
   ```bash
   cmake --build build
   ```

4. **Run the server**:
   - **Windows**:
     ```cmd
     .\build\AbisheikmartServer.exe
     ```
   - **Linux / macOS**:
     ```bash
     ./build/AbisheikmartServer
     ```

5. **Open in Web Browser**:
   Navigate to `http://localhost:8080` in your web browser.

---

## Visual Studio Code Integration

This project includes pre-configured VS Code tasks:
1. Open the project folder in VS Code (`code .`).
2. Press `Ctrl+Shift+B` (or `Cmd+Shift+B` on macOS) to execute the default **CMake Build** task.
3. Press `F5` to start debugging the C++ server using the configured launch target.

---

## Database Management

- The application uses a local SQLite database stored in `ecommerce.db` in the project root directory.
- Database tables (`users`, `products`, `cart_items`, `orders`, `order_items`) and initial admin seed data are automatically created when the server is launched for the first time.
- To reset the database to a clean default state, simply stop the server and delete `ecommerce.db`.

---

## Troubleshooting

- **Port 8080 already in use**:
  Ensure no other web server or process is running on port `8080`.
- **Missing WinSock libraries on Windows**:
  The `CMakeLists.txt` automatically links `ws2_32` and `wsock32` when compiling under Windows.
- **Clean Rebuild**:
  If build cache errors occur, delete the `build/` directory and re-run `cmake -B build`.
