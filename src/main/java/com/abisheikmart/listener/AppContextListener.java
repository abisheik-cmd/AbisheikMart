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
                        if (!statement.trim().isEmpty()) {
                            stmt.execute(statement);
                        }
                    }
                    logger.info("Database schema applied successfully.");
                }
            }

            // Seed default categories
            String[] categories = {"Electronics", "Fashion", "Home", "Books", "Fitness"};
            for (String cat : categories) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO categories (name, description) VALUES (?, ?) ON CONFLICT DO NOTHING;")) {
                    ps.setString(1, cat);
                    ps.setString(2, cat + " category products");
                    ps.executeUpdate();
                } catch (Exception ignored) {
                    // Category already exists
                }
            }

            // Seed default Admin
            try (PreparedStatement psCheck = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE email = ?;")) {
                psCheck.setString(1, "admin@abishmart.com");
                ResultSet rs = psCheck.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement psInsert = conn.prepareStatement(
                            "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, 'ADMIN');")) {
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
                            "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, 'SELLER');", Statement.RETURN_GENERATED_KEYS)) {
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

            // Seed default Product Catalog
            if (sellerId > 0) {
                try (PreparedStatement psCheck = conn.prepareStatement("SELECT COUNT(*) FROM products;")) {
                    ResultSet rs = psCheck.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        record SeedItem(String name, String desc, double price, int stock, String categoryName, String img) {}

                        SeedItem[] items = new SeedItem[] {
                            new SeedItem("Wireless Mechanical Keyboard", "RGB backlit tactile mechanical switches with multi-device Bluetooth connectivity.", 4499.00, 15, "Electronics", "images/keyboard.jpg"),
                            new SeedItem("Noise-Cancelling Headphones", "Active noise cancelling with 40-hour battery life and spatial audio.", 12999.00, 10, "Electronics", "images/headphones.jpg"),
                            new SeedItem("Pro Fitness Smartwatch", "Heart rate monitor, GPS tracking, and AMOLED display.", 6999.00, 12, "Electronics", "images/smartwatch.jpg"),
                            new SeedItem("4K Ultra-HD Vlog Camera", "Compact vlog camera with 4K recording, flip LCD screen, and directional microphone.", 42500.00, 6, "Electronics", "images/camera.jpg"),
                            new SeedItem("Italian Espresso Maker", "Premium 15-bar pump espresso & cappuccino machine for home kitchen.", 14999.00, 8, "Home", "images/coffeemaker.jpg"),
                            new SeedItem("Digital Touchscreen Air Fryer 5.5L", "Rapid hot air circulation 5.5L digital air fryer with 8 preset cooking modes.", 7499.00, 14, "Home", "images/airfryer.jpg"),
                            new SeedItem("Ultra Cushion Athletic Sneakers", "Lightweight breathable mesh running shoes for maximum daily comfort.", 3499.00, 20, "Fashion", "images/sneakers.jpg"),
                            new SeedItem("Waterproof All-Weather Hiking Jacket", "Windproof and waterproof outdoor hooded shell jacket for trekking and winter wear.", 5999.00, 11, "Fashion", "images/jacket.jpg"),
                            new SeedItem("Mastering Modern Software Engineering", "Complete guide to cloud architecture, system design, microservices, and clean code.", 1850.00, 25, "Books", "images/books.jpg"),
                            new SeedItem("Adjustable Dumbbell Set (20kg)", "Solid iron weight plates with non-slip chrome handles for home gym workouts.", 8999.00, 9, "Fitness", "images/dumbbell.jpg")
                        };

                        for (SeedItem item : items) {
                            long catId = 1;
                            try (PreparedStatement psCat = conn.prepareStatement("SELECT id FROM categories WHERE name = ?;")) {
                                psCat.setString(1, item.categoryName());
                                ResultSet rsCat = psCat.executeQuery();
                                if (rsCat.next()) catId = rsCat.getLong("id");
                            }

                            try (PreparedStatement psProd = conn.prepareStatement(
                                    "INSERT INTO products (seller_id, category_id, name, description, price, stock, image_url) VALUES (?, ?, ?, ?, ?, ?, ?);")) {
                                psProd.setLong(1, sellerId);
                                psProd.setLong(2, catId);
                                psProd.setString(3, item.name());
                                psProd.setString(4, item.desc());
                                psProd.setDouble(5, item.price());
                                psProd.setInt(6, item.stock());
                                psProd.setString(7, item.img());
                                psProd.executeUpdate();
                            }
                        }
                        logger.info("Seeded 10 default products in Rupees INR.");
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
