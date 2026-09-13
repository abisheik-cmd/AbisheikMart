package com.abisheikmart.listener;

import com.abisheikmart.util.DBUtil;
import com.abisheikmart.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initializing AbisheikMart 2.0 Web Application Context...");
        try {
            Properties props = new Properties();
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties")) {
                if (is != null) {
                    props.load(is);
                } else {
                    logger.warn("db.properties not found on classpath, using defaults.");
                }
            }

            DBUtil.initDataSource(props);
            initDatabaseSchemaAndSeed();
            logger.info("AbisheikMart 2.0 Application Context initialized successfully.");
        } catch (Exception e) {
            logger.error("Error during application context initialization", e);
            throw new RuntimeException("Application startup failed", e);
        }
    }

    private void initDatabaseSchemaAndSeed() {
        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
            // Read and execute schema.sql
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("schema.sql")) {
                if (is != null) {
                    String sql = new String(is.readAllBytes());
                    for (String statement : sql.split(";")) {
                        String trimmed = statement.trim();
                        if (!trimmed.isEmpty()) {
                            try {
                                stmt.execute(trimmed);
                            } catch (Exception e) {
                                logger.warn("Schema statement warning [{}]: {}", trimmed, e.getMessage());
                            }
                        }
                    }
                    logger.info("Database schema applied successfully.");
                }
            }

            // Seed categories
            String[] categories = {"Electronics", "Mobiles", "Fashion", "Home", "Books", "Fitness", "Accessories"};
            for (String cat : categories) {
                try (PreparedStatement ps = conn.prepareStatement("MERGE INTO categories (name, description) KEY(name) VALUES (?, ?);")) {
                    ps.setString(1, cat);
                    ps.setString(2, cat + " products");
                    ps.executeUpdate();
                } catch (Exception ignored) {}
            }

            // Seed default Admin
            try (PreparedStatement psCheck = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE email = ?;")) {
                psCheck.setString(1, "admin@abishmart.com");
                ResultSet rs = psCheck.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement psInsert = conn.prepareStatement(
                            "INSERT INTO users (name, email, password_hash, role, phone) VALUES (?, ?, ?, 'ADMIN', '9876543210');")) {
                        psInsert.setString(1, "System Administrator");
                        psInsert.setString(2, "admin@abishmart.com");
                        psInsert.setString(3, PasswordUtil.hashPassword("Admin@123"));
                        psInsert.executeUpdate();
                        logger.info("Seeded default admin account (admin@abishmart.com / Admin@123)");
                    }
                }
            }

            // Seed default Seller
            long sellerId = 0;
            try (PreparedStatement psCheck = conn.prepareStatement("SELECT id FROM users WHERE email = ?;")) {
                psCheck.setString(1, "seller@abishmart.com");
                ResultSet rs = psCheck.executeQuery();
                if (rs.next()) {
                    sellerId = rs.getLong("id");
                } else {
                    try (PreparedStatement psInsert = conn.prepareStatement(
                            "INSERT INTO users (name, email, password_hash, role, phone) VALUES (?, ?, ?, 'SELLER', '9876500000');", Statement.RETURN_GENERATED_KEYS)) {
                        psInsert.setString(1, "Verified Tech Seller");
                        psInsert.setString(2, "seller@abishmart.com");
                        psInsert.setString(3, PasswordUtil.hashPassword("Seller@123"));
                        psInsert.executeUpdate();
                        ResultSet keys = psInsert.getGeneratedKeys();
                        if (keys.next()) {
                            sellerId = keys.getLong(1);
                        }
                        logger.info("Seeded default seller account (seller@abishmart.com / Seller@123)");
                    }
                }
            }

            // Seed Coupons
            record SeedCoupon(String code, String type, double val, double minOrder) {}
            SeedCoupon[] coupons = new SeedCoupon[] {
                new SeedCoupon("WELCOME10", "PERCENT", 10.0, 500.0),
                new SeedCoupon("FESTIVE20", "PERCENT", 20.0, 1500.0),
                new SeedCoupon("SUPER500", "FLAT", 500.0, 2999.0),
                new SeedCoupon("LUCKY15", "PERCENT", 15.0, 0.0)
            };
            for (SeedCoupon sc : coupons) {
                try (PreparedStatement ps = conn.prepareStatement("MERGE INTO coupons (code, discount_type, discount_value, min_order_amount) KEY(code) VALUES (?, ?, ?, ?);")) {
                    ps.setString(1, sc.code());
                    ps.setString(2, sc.type());
                    ps.setDouble(3, sc.val());
                    ps.setDouble(4, sc.minOrder());
                    ps.executeUpdate();
                } catch (Exception ignored) {}
            }

            // Seed Product Catalog
            if (sellerId > 0) {
                try (PreparedStatement psCheck = conn.prepareStatement("SELECT COUNT(*) FROM products;")) {
                    ResultSet rs = psCheck.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        record SeedItem(String name, String desc, double origPrice, double price, int stock, String categoryName, String img) {}

                        SeedItem[] items = new SeedItem[] {
                            new SeedItem("Wireless Mechanical Keyboard", "RGB backlit tactile mechanical switches with multi-device Bluetooth connectivity.", 5999.00, 4499.00, 15, "Electronics", "images/keyboard.jpg"),
                            new SeedItem("Noise-Cancelling Headphones", "Active noise cancelling with 40-hour battery life and spatial audio sound profile.", 15999.00, 12999.00, 10, "Electronics", "images/headphones.jpg"),
                            new SeedItem("Pro Fitness Smartwatch", "Heart rate monitor, GPS tracking, sleep tracking, and AMOLED color display.", 8999.00, 6999.00, 12, "Electronics", "images/smartwatch.jpg"),
                            new SeedItem("4K Ultra-HD Vlog Camera", "Compact vlog camera with 4K recording, flip LCD screen, and directional microphone.", 49999.00, 42500.00, 6, "Electronics", "images/camera.jpg"),
                            new SeedItem("Italian Espresso Maker", "Premium 15-bar pump espresso & cappuccino machine for home kitchen.", 18999.00, 14999.00, 8, "Home", "images/coffeemaker.jpg"),
                            new SeedItem("Digital Touchscreen Air Fryer 5.5L", "Rapid hot air circulation 5.5L digital air fryer with 8 preset cooking modes.", 9999.00, 7499.00, 14, "Home", "images/airfryer.jpg"),
                            new SeedItem("Ultra Cushion Athletic Sneakers", "Lightweight breathable mesh running shoes for maximum daily comfort.", 4999.00, 3499.00, 20, "Fashion", "images/sneakers.jpg"),
                            new SeedItem("Waterproof All-Weather Hiking Jacket", "Windproof and waterproof outdoor hooded shell jacket for trekking and winter wear.", 7999.00, 5999.00, 11, "Fashion", "images/jacket.jpg"),
                            new SeedItem("Mastering Modern Software Engineering", "Complete guide to cloud architecture, system design, microservices, and clean code.", 2499.00, 1850.00, 25, "Books", "images/books.jpg"),
                            new SeedItem("Adjustable Dumbbell Set (20kg)", "Solid iron weight plates with non-slip chrome handles for home gym workouts.", 11999.00, 8999.00, 9, "Fitness", "images/dumbbell.jpg")
                        };

                        for (SeedItem item : items) {
                            long catId = 1;
                            try (PreparedStatement psCat = conn.prepareStatement("SELECT id FROM categories WHERE name = ?;")) {
                                psCat.setString(1, item.categoryName());
                                ResultSet rsCat = psCat.executeQuery();
                                if (rsCat.next()) catId = rsCat.getLong("id");
                            }

                            long prodId = 0;
                            try (PreparedStatement psProd = conn.prepareStatement(
                                    "INSERT INTO products (seller_id, category_id, name, description, original_price, price, stock, image_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS)) {
                                psProd.setLong(1, sellerId);
                                psProd.setLong(2, catId);
                                psProd.setString(3, item.name());
                                psProd.setString(4, item.desc());
                                psProd.setDouble(5, item.origPrice());
                                psProd.setDouble(6, item.price());
                                psProd.setInt(7, item.stock());
                                psProd.setString(8, item.img());
                                psProd.executeUpdate();
                                ResultSet keys = psProd.getGeneratedKeys();
                                if (keys.next()) prodId = keys.getLong(1);
                            }

                            // Seed a sample customer review
                            if (prodId > 0) {
                                try (PreparedStatement psRev = conn.prepareStatement(
                                        "INSERT INTO reviews (product_id, user_id, user_name, rating, comment, image_url) VALUES (?, ?, 'Verified Buyer', 5, 'Exceptional quality product! Exactly as described in INR.', ?);")) {
                                    psRev.setLong(1, prodId);
                                    psRev.setLong(2, sellerId);
                                    psRev.setString(3, item.img());
                                    psRev.executeUpdate();
                                } catch (Exception ignored) {}
                            }
                        }
                        logger.info("Seeded default product catalog with MRP prices and reviews in Rupees INR.");
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error initializing database schema and seed data", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Shutting down AbisheikMart 2.0 Web Application Context...");
        DBUtil.closeDataSource();
        logger.info("Application Context destroyed.");
    }
}
