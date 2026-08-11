#include "controllers/ProductController.h"
#include "json/json.hpp"
#include "db/Database.h"
#include "utils/SessionManager.h"
#include "models/Product.h"
#include <iostream>

using json = nlohmann::json;

void ProductController::registerRoutes(httplib::Server& svr) {
    svr.Get("/api/products", handleGetAllProducts);
    svr.Get(R"(/api/products/(\d+))", handleGetProductById);
    svr.Get("/api/seller/products", handleGetSellerProducts);
    svr.Post("/api/seller/products", handleAddProduct);
    svr.Put(R"(/api/seller/products/(\d+))", handleUpdateProduct);
    svr.Delete(R"(/api/seller/products/(\d+))", handleDeleteProduct);
}

void ProductController::handleGetAllProducts(const httplib::Request& req, httplib::Response& res) {
    sqlite3* db = Database::getInstance().getDbHandle();
    std::string sql = 
        "SELECT p.id, p.seller_id, u.name AS seller_name, p.name, p.description, p.price, p.stock, p.category, COALESCE(p.image_url, '') AS image_url, p.created_at "
        "FROM products p JOIN users u ON p.seller_id = u.id "
        "ORDER BY p.id DESC;";

    sqlite3_stmt* stmt = nullptr;
    if (sqlite3_prepare_v2(db, sql.c_str(), -1, &stmt, nullptr) != SQLITE_OK) {
        res.status = 500;
        res.set_content(json({{"error", "Database error fetching product catalog."}}).dump(), "application/json");
        return;
    }

    json productArray = json::array();
    while (sqlite3_step(stmt) == SQLITE_ROW) {
        Product p;
        p.id = sqlite3_column_int(stmt, 0);
        p.sellerId = sqlite3_column_int(stmt, 1);
        p.sellerName = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 2));
        p.name = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 3));
        p.description = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 4) ? sqlite3_column_text(stmt, 4) : reinterpret_cast<const unsigned char*>(""));
        p.price = sqlite3_column_double(stmt, 5);
        p.stock = sqlite3_column_int(stmt, 6);
        p.category = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 7));
        p.imageUrl = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 8) ? sqlite3_column_text(stmt, 8) : reinterpret_cast<const unsigned char*>(""));
        p.createdAt = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 9) ? sqlite3_column_text(stmt, 9) : reinterpret_cast<const unsigned char*>(""));

        productArray.push_back(p.toJson());
    }
    sqlite3_finalize(stmt);

    res.status = 200;
    res.set_content(json({{"products", productArray}}).dump(), "application/json");
}

void ProductController::handleGetProductById(const httplib::Request& req, httplib::Response& res) {
    int productId = std::stoi(req.matches[1]);
    sqlite3* db = Database::getInstance().getDbHandle();

    std::string sql = 
        "SELECT p.id, p.seller_id, u.name AS seller_name, p.name, p.description, p.price, p.stock, p.category, COALESCE(p.image_url, '') AS image_url, p.created_at "
        "FROM products p JOIN users u ON p.seller_id = u.id WHERE p.id = ?;";

    sqlite3_stmt* stmt = nullptr;
    if (sqlite3_prepare_v2(db, sql.c_str(), -1, &stmt, nullptr) != SQLITE_OK) {
        res.status = 500;
        res.set_content(json({{"error", "Database error fetching product detail."}}).dump(), "application/json");
        return;
    }

    sqlite3_bind_int(stmt, 1, productId);

    if (sqlite3_step(stmt) == SQLITE_ROW) {
        Product p;
        p.id = sqlite3_column_int(stmt, 0);
        p.sellerId = sqlite3_column_int(stmt, 1);
        p.sellerName = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 2));
        p.name = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 3));
        p.description = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 4) ? sqlite3_column_text(stmt, 4) : reinterpret_cast<const unsigned char*>(""));
        p.price = sqlite3_column_double(stmt, 5);
        p.stock = sqlite3_column_int(stmt, 6);
        p.category = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 7));
        p.imageUrl = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 8) ? sqlite3_column_text(stmt, 8) : reinterpret_cast<const unsigned char*>(""));
        p.createdAt = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 9) ? sqlite3_column_text(stmt, 9) : reinterpret_cast<const unsigned char*>(""));
        sqlite3_finalize(stmt);

        res.status = 200;
        res.set_content(json({{"product", p.toJson()}}).dump(), "application/json");
        return;
    }

    sqlite3_finalize(stmt);
    res.status = 404;
    res.set_content(json({{"error", "Product not found."}}).dump(), "application/json");
}

void ProductController::handleGetSellerProducts(const httplib::Request& req, httplib::Response& res) {
    std::string token = SessionManager::extractToken(req);
    UserSession session;

    if (!SessionManager::getInstance().getSession(token, session)) {
        res.status = 401;
        res.set_content(json({{"error", "Authentication required to access seller dashboard."}}).dump(), "application/json");
        return;
    }

    if (session.role != "SELLER" && session.role != "ADMIN") {
        res.status = 403;
        res.set_content(json({{"error", "Access denied. Only sellers can view seller dashboard products."}}).dump(), "application/json");
        return;
    }

    sqlite3* db = Database::getInstance().getDbHandle();
    std::string sql = 
        "SELECT p.id, p.seller_id, u.name AS seller_name, p.name, p.description, p.price, p.stock, p.category, COALESCE(p.image_url, '') AS image_url, p.created_at "
        "FROM products p JOIN users u ON p.seller_id = u.id "
        "WHERE p.seller_id = ? ORDER BY p.id DESC;";

    sqlite3_stmt* stmt = nullptr;
    if (sqlite3_prepare_v2(db, sql.c_str(), -1, &stmt, nullptr) != SQLITE_OK) {
        res.status = 500;
        res.set_content(json({{"error", "Database error fetching seller products."}}).dump(), "application/json");
        return;
    }

    sqlite3_bind_int(stmt, 1, session.userId);

    json productArray = json::array();
    while (sqlite3_step(stmt) == SQLITE_ROW) {
        Product p;
        p.id = sqlite3_column_int(stmt, 0);
        p.sellerId = sqlite3_column_int(stmt, 1);
        p.sellerName = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 2));
        p.name = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 3));
        p.description = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 4) ? sqlite3_column_text(stmt, 4) : reinterpret_cast<const unsigned char*>(""));
        p.price = sqlite3_column_double(stmt, 5);
        p.stock = sqlite3_column_int(stmt, 6);
        p.category = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 7));
        p.imageUrl = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 8) ? sqlite3_column_text(stmt, 8) : reinterpret_cast<const unsigned char*>(""));
        p.createdAt = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 9) ? sqlite3_column_text(stmt, 9) : reinterpret_cast<const unsigned char*>(""));

        productArray.push_back(p.toJson());
    }
    sqlite3_finalize(stmt);

    res.status = 200;
    res.set_content(json({{"products", productArray}}).dump(), "application/json");
}

void ProductController::handleAddProduct(const httplib::Request& req, httplib::Response& res) {
    std::string token = SessionManager::extractToken(req);
    UserSession session;

    if (!SessionManager::getInstance().getSession(token, session)) {
        res.status = 401;
        res.set_content(json({{"error", "Authentication required to add products."}}).dump(), "application/json");
        return;
    }

    if (session.role != "SELLER" && session.role != "ADMIN") {
        res.status = 403;
        res.set_content(json({{"error", "Access denied. Only sellers can add products."}}).dump(), "application/json");
        return;
    }

    try {
        json body = json::parse(req.body);
        std::string name = body.value("name", "");
        std::string description = body.value("description", "");
        double price = body.value("price", 0.0);
        int stock = body.value("stock", 0);
        std::string category = body.value("category", "General");
        std::string imageUrl = body.value("image_url", "");

        if (name.empty()) {
            res.status = 400;
            res.set_content(json({{"error", "Product name is required."}}).dump(), "application/json");
            return;
        }

        if (price < 0.0) {
            res.status = 400;
            res.set_content(json({{"error", "Product price cannot be negative."}}).dump(), "application/json");
            return;
        }

        if (stock < 0) {
            res.status = 400;
            res.set_content(json({{"error", "Product stock cannot be negative."}}).dump(), "application/json");
            return;
        }

        sqlite3* db = Database::getInstance().getDbHandle();
        std::string insertSql = 
            "INSERT INTO products (seller_id, name, description, price, stock, category, image_url) VALUES (?, ?, ?, ?, ?, ?, ?);";

        sqlite3_stmt* stmt = nullptr;
        if (sqlite3_prepare_v2(db, insertSql.c_str(), -1, &stmt, nullptr) != SQLITE_OK) {
            res.status = 500;
            res.set_content(json({{"error", "Failed to prepare add product statement."}}).dump(), "application/json");
            return;
        }

        // CRITICAL: seller_id ALWAYS comes from session token, NEVER trusted from body
        sqlite3_bind_int(stmt, 1, session.userId);
        sqlite3_bind_text(stmt, 2, name.c_str(), -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(stmt, 3, description.c_str(), -1, SQLITE_TRANSIENT);
        sqlite3_bind_double(stmt, 4, price);
        sqlite3_bind_int(stmt, 5, stock);
        sqlite3_bind_text(stmt, 6, category.c_str(), -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(stmt, 7, imageUrl.c_str(), -1, SQLITE_TRANSIENT);

        int rc = sqlite3_step(stmt);
        sqlite3_finalize(stmt);

        if (rc != SQLITE_DONE) {
            res.status = 500;
            res.set_content(json({{"error", "Failed to create product."}}).dump(), "application/json");
            return;
        }

        int newProductId = static_cast<int>(sqlite3_last_insert_rowid(db));

        json response = {
            {"message", "Product added successfully"},
            {"product", {
                {"id", newProductId},
                {"seller_id", session.userId},
                {"seller_name", session.name},
                {"name", name},
                {"description", description},
                {"price", price},
                {"stock", stock},
                {"category", category},
                {"image_url", imageUrl}
            }}
        };

        res.status = 201; // Created
        res.set_content(response.dump(), "application/json");
    } catch (const std::exception& e) {
        res.status = 400;
        res.set_content(json({{"error", std::string("Malformed JSON: ") + e.what()}}).dump(), "application/json");
    }
}

void ProductController::handleUpdateProduct(const httplib::Request& req, httplib::Response& res) {
    int productId = std::stoi(req.matches[1]);
    std::string token = SessionManager::extractToken(req);
    UserSession session;

    if (!SessionManager::getInstance().getSession(token, session)) {
        res.status = 401;
        res.set_content(json({{"error", "Authentication required to update products."}}).dump(), "application/json");
        return;
    }

    if (session.role != "SELLER" && session.role != "ADMIN") {
        res.status = 403;
        res.set_content(json({{"error", "Access denied. Only sellers can update products."}}).dump(), "application/json");
        return;
    }

    sqlite3* db = Database::getInstance().getDbHandle();

    // 1. Fetch product to verify existence and check ownership
    std::string selectSql = "SELECT id, seller_id FROM products WHERE id = ?;";
    sqlite3_stmt* checkStmt = nullptr;
    if (sqlite3_prepare_v2(db, selectSql.c_str(), -1, &checkStmt, nullptr) != SQLITE_OK) {
        res.status = 500;
        res.set_content(json({{"error", "Database error checking product ownership."}}).dump(), "application/json");
        return;
    }

    sqlite3_bind_int(checkStmt, 1, productId);
    int productOwnerId = 0;
    bool exists = false;

    if (sqlite3_step(checkStmt) == SQLITE_ROW) {
        exists = true;
        productOwnerId = sqlite3_column_int(checkStmt, 1);
    }
    sqlite3_finalize(checkStmt);

    if (!exists) {
        res.status = 404;
        res.set_content(json({{"error", "Product not found."}}).dump(), "application/json");
        return;
    }

    // 2. CRITICAL OWNERSHIP SECURITY CHECK: Verify seller_id == authenticated_user.id
    if (productOwnerId != session.userId && session.role != "ADMIN") {
        res.status = 403; // Forbidden
        res.set_content(json({{"error", "Access denied. You can only modify your own products."}}).dump(), "application/json");
        return;
    }

    try {
        json body = json::parse(req.body);
        std::string name = body.value("name", "");
        std::string description = body.value("description", "");
        double price = body.value("price", 0.0);
        int stock = body.value("stock", 0);
        std::string category = body.value("category", "General");
        std::string imageUrl = body.value("image_url", "");

        if (name.empty()) {
            res.status = 400;
            res.set_content(json({{"error", "Product name cannot be empty."}}).dump(), "application/json");
            return;
        }

        if (price < 0.0 || stock < 0) {
            res.status = 400;
            res.set_content(json({{"error", "Price and stock cannot be negative."}}).dump(), "application/json");
            return;
        }

        std::string updateSql = "UPDATE products SET name = ?, description = ?, price = ?, stock = ?, category = ?, image_url = ? WHERE id = ?;";
        sqlite3_stmt* updateStmt = nullptr;
        if (sqlite3_prepare_v2(db, updateSql.c_str(), -1, &updateStmt, nullptr) != SQLITE_OK) {
            res.status = 500;
            res.set_content(json({{"error", "Failed to prepare product update query."}}).dump(), "application/json");
            return;
        }

        sqlite3_bind_text(updateStmt, 1, name.c_str(), -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(updateStmt, 2, description.c_str(), -1, SQLITE_TRANSIENT);
        sqlite3_bind_double(updateStmt, 3, price);
        sqlite3_bind_int(updateStmt, 4, stock);
        sqlite3_bind_text(updateStmt, 5, category.c_str(), -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(updateStmt, 6, imageUrl.c_str(), -1, SQLITE_TRANSIENT);
        sqlite3_bind_int(updateStmt, 7, productId);

        int rc = sqlite3_step(updateStmt);
        sqlite3_finalize(updateStmt);

        if (rc != SQLITE_DONE) {
            res.status = 500;
            res.set_content(json({{"error", "Failed to update product."}}).dump(), "application/json");
            return;
        }

        res.status = 200;
        res.set_content(json({
            {"message", "Product updated successfully"},
            {"product", {
                {"id", productId},
                {"seller_id", productOwnerId},
                {"name", name},
                {"description", description},
                {"price", price},
                {"stock", stock},
                {"category", category},
                {"image_url", imageUrl}
            }}
        }).dump(), "application/json");
    } catch (const std::exception& e) {
        res.status = 400;
        res.set_content(json({{"error", std::string("Malformed JSON: ") + e.what()}}).dump(), "application/json");
    }
}

void ProductController::handleDeleteProduct(const httplib::Request& req, httplib::Response& res) {
    int productId = std::stoi(req.matches[1]);
    std::string token = SessionManager::extractToken(req);
    UserSession session;

    if (!SessionManager::getInstance().getSession(token, session)) {
        res.status = 401;
        res.set_content(json({{"error", "Authentication required to delete products."}}).dump(), "application/json");
        return;
    }

    if (session.role != "SELLER" && session.role != "ADMIN") {
        res.status = 403;
        res.set_content(json({{"error", "Access denied. Only sellers can delete products."}}).dump(), "application/json");
        return;
    }

    sqlite3* db = Database::getInstance().getDbHandle();

    // 1. Fetch product to verify existence and check ownership
    std::string selectSql = "SELECT id, seller_id FROM products WHERE id = ?;";
    sqlite3_stmt* checkStmt = nullptr;
    if (sqlite3_prepare_v2(db, selectSql.c_str(), -1, &checkStmt, nullptr) != SQLITE_OK) {
        res.status = 500;
        res.set_content(json({{"error", "Database error checking product ownership."}}).dump(), "application/json");
        return;
    }

    sqlite3_bind_int(checkStmt, 1, productId);
    int productOwnerId = 0;
    bool exists = false;

    if (sqlite3_step(checkStmt) == SQLITE_ROW) {
        exists = true;
        productOwnerId = sqlite3_column_int(checkStmt, 1);
    }
    sqlite3_finalize(checkStmt);

    if (!exists) {
        res.status = 404;
        res.set_content(json({{"error", "Product not found."}}).dump(), "application/json");
        return;
    }

    // 2. CRITICAL OWNERSHIP SECURITY CHECK: Verify seller_id == authenticated_user.id
    if (productOwnerId != session.userId && session.role != "ADMIN") {
        res.status = 403; // Forbidden
        res.set_content(json({{"error", "Access denied. You can only delete your own products."}}).dump(), "application/json");
        return;
    }

    std::string deleteSql = "DELETE FROM products WHERE id = ?;";
    sqlite3_stmt* deleteStmt = nullptr;
    if (sqlite3_prepare_v2(db, deleteSql.c_str(), -1, &deleteStmt, nullptr) != SQLITE_OK) {
        res.status = 500;
        res.set_content(json({{"error", "Failed to prepare delete query."}}).dump(), "application/json");
        return;
    }

    sqlite3_bind_int(deleteStmt, 1, productId);
    int rc = sqlite3_step(deleteStmt);
    sqlite3_finalize(deleteStmt);

    if (rc != SQLITE_DONE) {
        res.status = 500;
        res.set_content(json({{"error", "Failed to delete product."}}).dump(), "application/json");
        return;
    }

    res.status = 200;
    res.set_content(json({{"message", "Product deleted successfully"}}).dump(), "application/json");
}
