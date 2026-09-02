#include "db/Database.h"
#include "utils/Crypto.h"
#include <iostream>

Database& Database::getInstance() {
    static Database instance;
    return instance;
}

Database::Database() : db(nullptr), initialized(false) {}

Database::~Database() {
    close();
}

bool Database::init(const std::string& dbPath) {
    if (initialized) {
        return true;
    }

    int rc = sqlite3_open(dbPath.c_str(), &db);
    if (rc != SQLITE_OK) {
        std::cerr << "[Database Error] Cannot open database: " << sqlite3_errmsg(db) << std::endl;
        return false;
    }

    // Enable Foreign Key support in SQLite
    execute("PRAGMA foreign_keys = ON;");

    if (!createTables()) {
        std::cerr << "[Database Error] Failed to create database tables." << std::endl;
        return false;
    }

    if (!seedDefaultAdmin()) {
        std::cerr << "[Database Error] Failed to seed default administrator." << std::endl;
        return false;
    }

    if (!seedDefaultCatalog()) {
        std::cerr << "[Database Error] Failed to seed default catalog products." << std::endl;
        return false;
    }

    initialized = true;
    std::cout << "[Database] Successfully initialized SQLite database at: " << dbPath << std::endl;
    return true;
}

void Database::close() {
    if (db) {
        sqlite3_close(db);
        db = nullptr;
    }
    initialized = false;
}

sqlite3* Database::getDbHandle() const {
    return db;
}

bool Database::execute(const std::string& sql) {
    char* errMsg = nullptr;
    int rc = sqlite3_exec(db, sql.c_str(), nullptr, nullptr, &errMsg);
    if (rc != SQLITE_OK) {
        std::cerr << "[Database SQL Error]: " << (errMsg ? errMsg : "Unknown error") << std::endl;
        if (errMsg) sqlite3_free(errMsg);
        return false;
    }
    return true;
}

bool Database::executeParam(const std::string& sql, const std::vector<std::string>& params) {
    sqlite3_stmt* stmt = nullptr;
    if (sqlite3_prepare_v2(db, sql.c_str(), -1, &stmt, nullptr) != SQLITE_OK) {
        std::cerr << "[Database Prepare Error]: " << sqlite3_errmsg(db) << std::endl;
        return false;
    }

    for (size_t i = 0; i < params.size(); ++i) {
        sqlite3_bind_text(stmt, static_cast<int>(i + 1), params[i].c_str(), -1, SQLITE_TRANSIENT);
    }

    int rc = sqlite3_step(stmt);
    sqlite3_finalize(stmt);

    return (rc == SQLITE_DONE || rc == SQLITE_ROW);
}

bool Database::createTables() {
    std::string createUsersTable = 
        "CREATE TABLE IF NOT EXISTS users ("
        "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        "name TEXT NOT NULL,"
        "email TEXT UNIQUE NOT NULL,"
        "password_hash TEXT NOT NULL,"
        "salt TEXT NOT NULL,"
        "role TEXT NOT NULL CHECK(role IN ('BUYER', 'SELLER', 'ADMIN')),"
        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP"
        ");";

    std::string createProductsTable = 
        "CREATE TABLE IF NOT EXISTS products ("
        "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        "seller_id INTEGER NOT NULL,"
        "name TEXT NOT NULL,"
        "description TEXT,"
        "price REAL NOT NULL CHECK(price >= 0),"
        "stock INTEGER NOT NULL CHECK(stock >= 0),"
        "category TEXT NOT NULL,"
        "image_url TEXT DEFAULT '',"
        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
        "FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE"
        ");";

    std::string createCartItemsTable = 
        "CREATE TABLE IF NOT EXISTS cart_items ("
        "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        "buyer_id INTEGER NOT NULL,"
        "product_id INTEGER NOT NULL,"
        "quantity INTEGER NOT NULL CHECK(quantity > 0),"
        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
        "FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,"
        "FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,"
        "UNIQUE(buyer_id, product_id)"
        ");";

    std::string createOrdersTable = 
        "CREATE TABLE IF NOT EXISTS orders ("
        "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        "buyer_id INTEGER NOT NULL,"
        "total_amount REAL NOT NULL CHECK(total_amount >= 0),"
        "status TEXT NOT NULL DEFAULT 'PLACED',"
        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
        "FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE"
        ");";

    std::string createOrderItemsTable = 
        "CREATE TABLE IF NOT EXISTS order_items ("
        "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        "order_id INTEGER NOT NULL,"
        "product_id INTEGER,"
        "seller_id INTEGER NOT NULL,"
        "product_name TEXT NOT NULL,"
        "quantity INTEGER NOT NULL CHECK(quantity > 0),"
        "unit_price REAL NOT NULL,"
        "subtotal REAL NOT NULL,"
        "FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,"
        "FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL"
        ");";

    bool success = execute(createUsersTable) &&
                   execute(createProductsTable) &&
                   execute(createCartItemsTable) &&
                   execute(createOrdersTable) &&
                   execute(createOrderItemsTable);

    // Non-destructive safe migration for existing databases
    execute("ALTER TABLE products ADD COLUMN image_url TEXT DEFAULT '';");

    return success;
}

bool Database::seedDefaultAdmin() {
    // Check if an admin user or 'admin@abishmart.com' exists
    std::string checkSql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN' OR email = 'admin@abishmart.com' OR email = 'admin';";
    sqlite3_stmt* stmt = nullptr;
    if (sqlite3_prepare_v2(db, checkSql.c_str(), -1, &stmt, nullptr) != SQLITE_OK) {
        return false;
    }

    int count = 0;
    if (sqlite3_step(stmt) == SQLITE_ROW) {
        count = sqlite3_column_int(stmt, 0);
    }
    sqlite3_finalize(stmt);

    if (count > 0) {
        // Admin already exists
        return true;
    }

    // Seed Admin Account: admin / admin@abishmart.com / Admin@123
    std::string salt = Crypto::generateSalt(16);
    std::string hash = Crypto::hashPassword("Admin@123", salt);

    std::string insertSql = "INSERT INTO users (name, email, password_hash, salt, role) VALUES ('System Administrator', 'admin@abishmart.com', ?, ?, 'ADMIN');";
    if (sqlite3_prepare_v2(db, insertSql.c_str(), -1, &stmt, nullptr) != SQLITE_OK) {
        return false;
    }

    sqlite3_bind_text(stmt, 1, hash.c_str(), -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(stmt, 2, salt.c_str(), -1, SQLITE_TRANSIENT);

    int rc = sqlite3_step(stmt);
    sqlite3_finalize(stmt);

    if (rc == SQLITE_DONE) {
        std::cout << "[Database Seed] Created default admin account (email: admin@abishmart.com, username: admin, password: Admin@123)" << std::endl;
        return true;
    }
    return false;
}

bool Database::seedDefaultCatalog() {
    sqlite3_stmt* stmt = nullptr;
    int sellerId = 0;

    // 1. Check if seller account exists
    std::string checkSeller = "SELECT id FROM users WHERE email = 'seller@abishmart.com';";
    if (sqlite3_prepare_v2(db, checkSeller.c_str(), -1, &stmt, nullptr) == SQLITE_OK) {
        if (sqlite3_step(stmt) == SQLITE_ROW) {
            sellerId = sqlite3_column_int(stmt, 0);
        }
        sqlite3_finalize(stmt);
    }

    if (sellerId == 0) {
        std::string salt = Crypto::generateSalt(16);
        std::string hash = Crypto::hashPassword("Seller@123", salt);
        std::string insertSeller = "INSERT INTO users (name, email, password_hash, salt, role) VALUES ('Verified Tech Seller', 'seller@abishmart.com', ?, ?, 'SELLER');";
        
        if (sqlite3_prepare_v2(db, insertSeller.c_str(), -1, &stmt, nullptr) == SQLITE_OK) {
            sqlite3_bind_text(stmt, 1, hash.c_str(), -1, SQLITE_TRANSIENT);
            sqlite3_bind_text(stmt, 2, salt.c_str(), -1, SQLITE_TRANSIENT);
            if (sqlite3_step(stmt) == SQLITE_DONE) {
                sellerId = static_cast<int>(sqlite3_last_insert_rowid(db));
                std::cout << "[Database Seed] Created default seller account (email: seller@abishmart.com, password: Seller@123)" << std::endl;
            }
            sqlite3_finalize(stmt);
        }
    }

    if (sellerId == 0) {
        std::cerr << "[Database Seed Error] Could not find or create default seller ID." << std::endl;
        return false;
    }

    // 2. Check product count
    std::string checkProd = "SELECT COUNT(*) FROM products;";
    int count = 0;
    if (sqlite3_prepare_v2(db, checkProd.c_str(), -1, &stmt, nullptr) == SQLITE_OK) {
        if (sqlite3_step(stmt) == SQLITE_ROW) {
            count = sqlite3_column_int(stmt, 0);
        }
        sqlite3_finalize(stmt);
    }

    // Convert any old USD prices (< 500) to INR in existing database
    execute("UPDATE products SET price = 4499.00 WHERE name LIKE '%Keyboard%' AND price < 500;");
    execute("UPDATE products SET price = 12999.00 WHERE name LIKE '%Headphones%' AND price < 500;");
    execute("UPDATE products SET price = 6999.00 WHERE name LIKE '%Smartwatch%' AND price < 500;");
    execute("UPDATE products SET price = 14999.00 WHERE name LIKE '%Espresso%' AND price < 500;");
    execute("UPDATE products SET price = 3499.00 WHERE name LIKE '%Sneakers%' AND price < 500;");

    if (count >= 10) {
        return true; // Catalog already fully populated
    }

    struct SeedProd {
        std::string name;
        std::string desc;
        double price;
        int stock;
        std::string cat;
        std::string img;
    };

    std::vector<SeedProd> seedItems = {
        {"Wireless Mechanical Keyboard", "RGB backlit tactile mechanical switches with multi-device Bluetooth connectivity.", 4499.00, 15, "Electronics", "assets/products/keyboard.jpg"},
        {"Noise-Cancelling Headphones", "Active noise cancelling with 40-hour battery life and spatial audio.", 12999.00, 10, "Electronics", "assets/products/headphones.jpg"},
        {"Pro Fitness Smartwatch", "Heart rate monitor, GPS tracking, and AMOLED display.", 6999.00, 12, "Electronics", "assets/products/smartwatch.jpg"},
        {"4K Ultra-HD Vlog Camera", "Compact vlog camera with 4K recording, flip LCD screen, and directional microphone.", 42500.00, 6, "Electronics", "assets/products/camera.jpg"},
        {"Italian Espresso Maker", "Premium 15-bar pump espresso & cappuccino machine for home kitchen.", 14999.00, 8, "Home", "assets/products/coffeemaker.jpg"},
        {"Digital Touchscreen Air Fryer 5.5L", "Rapid hot air circulation 5.5L digital air fryer with 8 preset cooking modes.", 7499.00, 14, "Home", "assets/products/airfryer.jpg"},
        {"Ultra Cushion Athletic Sneakers", "Lightweight breathable mesh running shoes for maximum daily comfort.", 3499.00, 20, "Fashion", "assets/products/sneakers.jpg"},
        {"Waterproof All-Weather Hiking Jacket", "Windproof and waterproof outdoor hooded shell jacket for trekking and winter wear.", 5999.00, 11, "Fashion", "assets/products/jacket.jpg"},
        {"Mastering Modern Software Engineering", "Complete guide to cloud architecture, system design, microservices, and clean code.", 1850.00, 25, "Books", "assets/products/books.jpg"},
        {"Adjustable Dumbbell Set (20kg)", "Solid iron weight plates with non-slip chrome handles for home gym workouts.", 8999.00, 9, "Fitness", "assets/products/dumbbell.jpg"}
    };

    std::string insertSql = "INSERT INTO products (seller_id, name, description, price, stock, category, image_url) VALUES (?, ?, ?, ?, ?, ?, ?);";
    for (const auto& item : seedItems) {
        // Skip if product already exists by name
        std::string dupCheck = "SELECT COUNT(*) FROM products WHERE name = ?;";
        sqlite3_stmt* dupStmt = nullptr;
        bool exists = false;
        if (sqlite3_prepare_v2(db, dupCheck.c_str(), -1, &dupStmt, nullptr) == SQLITE_OK) {
            sqlite3_bind_text(dupStmt, 1, item.name.c_str(), -1, SQLITE_TRANSIENT);
            if (sqlite3_step(dupStmt) == SQLITE_ROW && sqlite3_column_int(dupStmt, 0) > 0) {
                exists = true;
            }
            sqlite3_finalize(dupStmt);
        }

        if (!exists && sqlite3_prepare_v2(db, insertSql.c_str(), -1, &stmt, nullptr) == SQLITE_OK) {
            sqlite3_bind_int(stmt, 1, sellerId);
            sqlite3_bind_text(stmt, 2, item.name.c_str(), -1, SQLITE_TRANSIENT);
            sqlite3_bind_text(stmt, 3, item.desc.c_str(), -1, SQLITE_TRANSIENT);
            sqlite3_bind_double(stmt, 4, item.price);
            sqlite3_bind_int(stmt, 5, item.stock);
            sqlite3_bind_text(stmt, 6, item.cat.c_str(), -1, SQLITE_TRANSIENT);
            sqlite3_bind_text(stmt, 7, item.img.c_str(), -1, SQLITE_TRANSIENT);
            sqlite3_step(stmt);
            sqlite3_finalize(stmt);
        }
    }

    std::cout << "[Database Seed] Successfully seeded default product catalog (" << seedItems.size() << " products)." << std::endl;
    return true;
}

