package com.abisheikmart.config;

import com.abisheikmart.util.CryptoUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final CryptoUtils cryptoUtils;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate, CryptoUtils cryptoUtils) {
        this.jdbcTemplate = jdbcTemplate;
        this.cryptoUtils = cryptoUtils;
    }

    @Override
    public void run(String... args) {
        createTables();
        seedDefaultAdmin();
        seedDefaultSellerAndCatalog();
    }

    private void createTables() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                role TEXT NOT NULL CHECK(role IN ('BUYER', 'SELLER', 'ADMIN')),
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                seller_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                price REAL NOT NULL CHECK(price >= 0),
                stock INTEGER NOT NULL CHECK(stock >= 0),
                category TEXT NOT NULL,
                image_url TEXT DEFAULT '',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
            );
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS cart_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                buyer_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL CHECK(quantity > 0),
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                UNIQUE(buyer_id, product_id)
            );
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                buyer_id INTEGER NOT NULL,
                total_amount REAL NOT NULL CHECK(total_amount >= 0),
                status TEXT NOT NULL DEFAULT 'PLACED',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE
            );
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS order_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL,
                product_id INTEGER,
                seller_id INTEGER NOT NULL,
                product_name TEXT NOT NULL,
                quantity INTEGER NOT NULL CHECK(quantity > 0),
                unit_price REAL NOT NULL,
                subtotal REAL NOT NULL,
                FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
            );
        """);
    }

    private void seedDefaultAdmin() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE email = 'admin@abishmart.com';", Integer.class);
        if (count == null || count == 0) {
            String salt = cryptoUtils.generateSalt();
            String hash = cryptoUtils.hashPassword("Admin@123", salt);
            jdbcTemplate.update("INSERT INTO users (name, email, password_hash, salt, role) VALUES (?, ?, ?, ?, 'ADMIN');",
                    "System Administrator", "admin@abishmart.com", hash, salt);
            System.out.println("[Database Init] Created default admin account (email: admin@abishmart.com, password: Admin@123)");
        }
    }

    private void seedDefaultSellerAndCatalog() {
        Integer sellerCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE email = 'seller@abishmart.com';", Integer.class);
        Long sellerId;
        if (sellerCount == null || sellerCount == 0) {
            String salt = cryptoUtils.generateSalt();
            String hash = cryptoUtils.hashPassword("Seller@123", salt);
            jdbcTemplate.update("INSERT INTO users (name, email, password_hash, salt, role) VALUES (?, ?, ?, ?, 'SELLER');",
                    "Verified Tech Seller", "seller@abishmart.com", hash, salt);
            sellerId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = 'seller@abishmart.com';", Long.class);
            System.out.println("[Database Init] Created default seller account (email: seller@abishmart.com, password: Seller@123)");
        } else {
            sellerId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = 'seller@abishmart.com';", Long.class);
        }

        Integer prodCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products;", Integer.class);
        if (prodCount == null || prodCount < 10) {
            record SeedItem(String name, String desc, double price, int stock, String cat, String img) {}

            SeedItem[] items = new SeedItem[] {
                new SeedItem("Wireless Mechanical Keyboard", "RGB backlit tactile mechanical switches with multi-device Bluetooth connectivity.", 4499.00, 15, "Electronics", "assets/products/keyboard.jpg"),
                new SeedItem("Noise-Cancelling Headphones", "Active noise cancelling with 40-hour battery life and spatial audio.", 12999.00, 10, "Electronics", "assets/products/headphones.jpg"),
                new SeedItem("Pro Fitness Smartwatch", "Heart rate monitor, GPS tracking, and AMOLED display.", 6999.00, 12, "Electronics", "assets/products/smartwatch.jpg"),
                new SeedItem("4K Ultra-HD Vlog Camera", "Compact vlog camera with 4K recording, flip LCD screen, and directional microphone.", 42500.00, 6, "Electronics", "assets/products/camera.jpg"),
                new SeedItem("Italian Espresso Maker", "Premium 15-bar pump espresso & cappuccino machine for home kitchen.", 14999.00, 8, "Home", "assets/products/coffeemaker.jpg"),
                new SeedItem("Digital Touchscreen Air Fryer 5.5L", "Rapid hot air circulation 5.5L digital air fryer with 8 preset cooking modes.", 7499.00, 14, "Home", "assets/products/airfryer.jpg"),
                new SeedItem("Ultra Cushion Athletic Sneakers", "Lightweight breathable mesh running shoes for maximum daily comfort.", 3499.00, 20, "Fashion", "assets/products/sneakers.jpg"),
                new SeedItem("Waterproof All-Weather Hiking Jacket", "Windproof and waterproof outdoor hooded shell jacket for trekking and winter wear.", 5999.00, 11, "Fashion", "assets/products/jacket.jpg"),
                new SeedItem("Mastering Modern Software Engineering", "Complete guide to cloud architecture, system design, microservices, and clean code.", 1850.00, 25, "Books", "assets/products/books.jpg"),
                new SeedItem("Adjustable Dumbbell Set (20kg)", "Solid iron weight plates with non-slip chrome handles for home gym workouts.", 8999.00, 9, "Fitness", "assets/products/dumbbell.jpg")
            };

            for (SeedItem item : items) {
                Integer exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products WHERE name = ?;", Integer.class, item.name());
                if (exists == null || exists == 0) {
                    jdbcTemplate.update("INSERT INTO products (seller_id, name, description, price, stock, category, image_url) VALUES (?, ?, ?, ?, ?, ?, ?);",
                            sellerId, item.name(), item.desc(), item.price(), item.stock(), item.cat(), item.img());
                }
            }
            System.out.println("[Database Init] Successfully seeded default product catalog with prices in Rupees INR.");
        }
    }
}

