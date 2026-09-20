package com.abisheikmart.listener;

import com.abisheikmart.util.DBUtil;
import com.abisheikmart.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

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

            overrideFromEnvironment(props, "DB_URL", "db.url");
            overrideFromEnvironment(props, "DB_DRIVER", "db.driver");
            overrideFromEnvironment(props, "DB_USERNAME", "db.username");
            overrideFromEnvironment(props, "DB_PASSWORD", "db.password");

            DBUtil.initDataSource(props);
            initDatabaseSchemaAndSeed();
            logger.info("AbisheikMart 2.0 Application Context initialized successfully.");
        } catch (Exception e) {
            logger.error("Error during application context initialization", e);
            throw new RuntimeException("Application startup failed", e);
        }
    }

    private void overrideFromEnvironment(Properties props, String environmentKey, String propertyKey) {
        String value = System.getenv(environmentKey);
        if (value != null && !value.isBlank()) {
            props.setProperty(propertyKey, value);
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

            // Add the expanded catalog after seller and category bootstrap. The name check
            // keeps this migration safe to rerun on existing and freshly initialized databases.
            if (sellerId > 0) {
                record AdditionalSeedItem(String name, String desc, double origPrice, double price, int stock, String categoryName, String img) {}
                AdditionalSeedItem[] additionalItems = new AdditionalSeedItem[] {
                    new AdditionalSeedItem("USB-C GaN Fast Charger 65W", "Compact dual-port GaN wall charger for phones, tablets, and laptops with fast USB-C power delivery.", 2999.00, 1899.00, 24, "Electronics", "images/usb-c-hub.jpg"),
                    new AdditionalSeedItem("Portable SSD 1TB", "Slim external solid-state drive with fast USB-C transfer speeds for backups and creative work.", 8999.00, 6499.00, 12, "Electronics", "images/keyboard.jpg"),
                    new AdditionalSeedItem("Full HD Streaming Webcam", "1080p webcam with autofocus, privacy shutter, and built-in microphone for classes and calls.", 3999.00, 2499.00, 18, "Electronics", "images/camera.jpg"),
                    new AdditionalSeedItem("Ergonomic Wireless Keyboard and Mouse", "Low-profile wireless keyboard and mouse combo with comfortable typing and quiet clicks.", 2999.00, 1999.00, 20, "Electronics", "images/wireless-mouse.jpg"),
                    new AdditionalSeedItem("Noise-Isolating Neckband", "Lightweight Bluetooth neckband with magnetic earbuds and a long-lasting battery for daily commuting.", 1999.00, 1199.00, 28, "Electronics", "images/wireless-earbuds.jpg"),

                    new AdditionalSeedItem("The Pragmatic Programmer", "Practical software craftsmanship lessons for building adaptable and dependable applications.", 999.00, 699.00, 22, "Books", "images/clean-code.jpg"),
                    new AdditionalSeedItem("Computer Networking Fundamentals", "Clear introduction to protocols, networks, internet architecture, and troubleshooting concepts.", 1299.00, 899.00, 18, "Books", "images/python-data.jpg"),
                    new AdditionalSeedItem("Atomic Productivity Planner", "Guided planner with weekly layouts, habit tracking, and focused daily planning pages.", 799.00, 499.00, 30, "Books", "images/deep-work.jpg"),
                    new AdditionalSeedItem("The Indian Startup Handbook", "A practical overview of validating ideas, serving customers, and growing an Indian business.", 899.00, 599.00, 20, "Books", "images/startup-playbook.jpg"),

                    new AdditionalSeedItem("Slim Silicone Phone Case", "Flexible shock-absorbing phone case with raised camera protection and a soft-touch finish.", 999.00, 499.00, 40, "Accessories", "images/phone-stand.jpg"),
                    new AdditionalSeedItem("Tempered Glass Screen Protector", "Clear tempered glass screen protector with alignment frame and smudge-resistant coating.", 699.00, 299.00, 38, "Accessories", "images/galaxy-a55.jpg"),
                    new AdditionalSeedItem("Magnetic Cable Organizer Set", "Reusable magnetic cable clips that keep charging leads tidy on desks and bedside tables.", 499.00, 249.00, 34, "Accessories", "images/braided-cable.jpg"),
                    new AdditionalSeedItem("Foldable Travel Packing Cubes", "Lightweight set of packing cubes with breathable mesh panels for organized travel luggage.", 1999.00, 1199.00, 16, "Accessories", "images/canvas-backpack.jpg"),

                    new AdditionalSeedItem("Regular Fit Linen Blend Shirt", "Breathable linen-blend casual shirt with a relaxed silhouette for warm Indian weather.", 2499.00, 1599.00, 20, "Fashion", "images/jacket.jpg"),
                    new AdditionalSeedItem("Everyday Cotton Chinos", "Comfort-stretch cotton chinos with a clean tapered fit for office and weekend wear.", 2999.00, 1899.00, 18, "Fashion", "images/sneakers.jpg"),
                    new AdditionalSeedItem("Lightweight Puffer Jacket", "Lightly insulated quilted jacket with zip pockets for cool mornings and travel.", 5999.00, 3999.00, 13, "Fashion", "images/jacket.jpg"),
                    new AdditionalSeedItem("Breathable Casual Slip-On Shoes", "Cushioned knit slip-on shoes with flexible soles for comfortable everyday walking.", 2999.00, 1999.00, 22, "Fashion", "images/sneakers.jpg"),

                    new AdditionalSeedItem("Hexagonal Rubber Dumbbells 5kg", "Pair of compact rubber-coated dumbbells with comfortable handles for home strength training.", 2499.00, 1699.00, 15, "Fitness", "images/dumbbell.jpg"),
                    new AdditionalSeedItem("High-Density Foam Roller", "Firm textured foam roller for warm-ups, mobility work, and post-workout recovery.", 1799.00, 1099.00, 18, "Fitness", "images/yoga-mat.jpg"),
                    new AdditionalSeedItem("Adjustable Kettlebell 12kg", "Space-saving adjustable kettlebell with secure weight plates for versatile home workouts.", 3499.00, 2499.00, 12, "Fitness", "images/dumbbell.jpg"),
                    new AdditionalSeedItem("Breathable Training Gloves", "Padded gym gloves with breathable mesh and adjustable wrist closure for lifting sessions.", 999.00, 599.00, 25, "Fitness", "images/resistance-bands.jpg"),

                    new AdditionalSeedItem("Minimal LED Study Lamp", "Adjustable LED study lamp with touch controls and warm, neutral, and cool light modes.", 2499.00, 1399.00, 21, "Home", "images/extension-board.jpg"),
                    new AdditionalSeedItem("Borosilicate Glass Food Container Set", "Leak-resistant glass storage containers with secure lids for meal prep and kitchen storage.", 1999.00, 1299.00, 20, "Home", "images/storage-organizer.jpg"),
                    new AdditionalSeedItem("Cotton Cushion Cover Set", "Set of textured cotton cushion covers with concealed zips for a simple living-room refresh.", 1299.00, 799.00, 27, "Home", "images/cotton-bedsheet.jpg"),
                    new AdditionalSeedItem("Stainless Steel Cookware Organizer", "Countertop organizer with sections for pans, lids, and everyday cooking utensils.", 1799.00, 1099.00, 19, "Home", "images/kitchen-organizer.jpg"),

                    new AdditionalSeedItem("Samsung Galaxy M35 5G", "Mid-range 5G smartphone with a vivid display, capable cameras, and a large everyday battery.", 24999.00, 19999.00, 9, "Mobiles", "images/galaxy-a55.jpg"),
                    new AdditionalSeedItem("OnePlus 12R", "Performance-focused smartphone with a smooth high-refresh display and fast wired charging.", 45999.00, 38999.00, 6, "Mobiles", "images/oneplus-nord.jpg"),
                    new AdditionalSeedItem("Google Pixel 8", "Compact smartphone with clean Android software and computational photography features.", 75999.00, 64999.00, 5, "Mobiles", "images/pixel-8a.jpg"),
                    new AdditionalSeedItem("Redmi Note 13 5G", "Value-focused 5G smartphone with a bright display, reliable cameras, and all-day battery life.", 22999.00, 17999.00, 12, "Mobiles", "images/redmi-note-13-pro.jpg"),
                    new AdditionalSeedItem("Vivo Y200 5G", "Slim 5G smartphone with a colorful display, portrait camera, and fast charging support.", 26999.00, 21999.00, 10, "Mobiles", "images/vivo-v30.jpg")
                };

                for (AdditionalSeedItem item : additionalItems) {
                    long categoryId = 0;
                    try (PreparedStatement psCategory = conn.prepareStatement("SELECT id FROM categories WHERE name = ?;")) {
                        psCategory.setString(1, item.categoryName());
                        ResultSet categoryResult = psCategory.executeQuery();
                        if (categoryResult.next()) categoryId = categoryResult.getLong("id");
                    }
                    if (categoryId == 0) continue;

                    boolean exists = false;
                    try (PreparedStatement psExisting = conn.prepareStatement("SELECT id FROM products WHERE name = ? LIMIT 1;")) {
                        psExisting.setString(1, item.name());
                        ResultSet existingResult = psExisting.executeQuery();
                        exists = existingResult.next();
                    }
                    if (exists) continue;

                    try (PreparedStatement psProduct = conn.prepareStatement(
                            "INSERT INTO products (seller_id, category_id, name, description, original_price, price, stock, image_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?);")) {
                        psProduct.setLong(1, sellerId);
                        psProduct.setLong(2, categoryId);
                        psProduct.setString(3, item.name());
                        psProduct.setString(4, item.desc());
                        psProduct.setDouble(5, item.origPrice());
                        psProduct.setDouble(6, item.price());
                        psProduct.setInt(7, item.stock());
                        psProduct.setString(8, item.img());
                        psProduct.executeUpdate();
                    }
                }
                logger.info("Seeded additional idempotent product catalog records.");
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
