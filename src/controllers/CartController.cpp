#include "controllers/CartController.h"
#include "json/json.hpp"
#include "db/Database.h"
#include "utils/SessionManager.h"
#include "models/CartItem.h"
#include <iostream>

using json = nlohmann::json;

void CartController::registerRoutes(httplib::Server& svr) {
    svr.Post("/api/cart/add", handleAddToCart);
    svr.Get("/api/cart", handleGetCart);
    svr.Put("/api/cart/update", handleUpdateCartQuantity);
    svr.Delete(R"(/api/cart/remove/(\d+))", handleRemoveFromCart);
}

void CartController::handleAddToCart(const httplib::Request& req, httplib::Response& res) {
    std::string token = SessionManager::extractToken(req);
    UserSession session;

    if (!SessionManager::getInstance().getSession(token, session)) {
        res.status = 401;
        res.set_content(json({{"error", "Authentication required to add items to cart."}}).dump(), "application/json");
        return;
    }

    try {
        json body = json::parse(req.body);
        int productId = body.value("product_id", 0);
        int addQuantity = body.value("quantity", 1);

        if (productId <= 0 || addQuantity <= 0) {
            res.status = 400;
            res.set_content(json({{"error", "Valid product_id and positive quantity are required."}}).dump(), "application/json");
            return;
        }

        sqlite3* db = Database::getInstance().getDbHandle();

        // 1. Verify Product Existence and Available Stock
        std::string prodSql = "SELECT id, price, stock FROM products WHERE id = ?;";
        sqlite3_stmt* prodStmt = nullptr;
        if (sqlite3_prepare_v2(db, prodSql.c_str(), -1, &prodStmt, nullptr) != SQLITE_OK) {
            res.status = 500;
            res.set_content(json({{"error", "Database error checking product stock."}}).dump(), "application/json");
            return;
        }

        sqlite3_bind_int(prodStmt, 1, productId);
        bool found = false;
        int currentStock = 0;

        if (sqlite3_step(prodStmt) == SQLITE_ROW) {
            found = true;
            currentStock = sqlite3_column_int(prodStmt, 2);
        }
        sqlite3_finalize(prodStmt);

        if (!found) {
            res.status = 404;
            res.set_content(json({{"error", "Product not found."}}).dump(), "application/json");
            return;
        }

        if (currentStock <= 0) {
            res.status = 400;
            res.set_content(json({{"error", "Product is out of stock."}}).dump(), "application/json");
            return;
        }

        // 2. Check if item already exists in buyer's cart
        std::string cartCheckSql = "SELECT id, quantity FROM cart_items WHERE buyer_id = ? AND product_id = ?;";
        sqlite3_stmt* cartStmt = nullptr;
        if (sqlite3_prepare_v2(db, cartCheckSql.c_str(), -1, &cartStmt, nullptr) != SQLITE_OK) {
            res.status = 500;
            res.set_content(json({{"error", "Database error checking cart items."}}).dump(), "application/json");
            return;
        }

        sqlite3_bind_int(cartStmt, 1, session.userId);
        sqlite3_bind_int(cartStmt, 2, productId);

        int existingCartId = 0;
        int existingQuantity = 0;
        bool inCart = false;

        if (sqlite3_step(cartStmt) == SQLITE_ROW) {
            inCart = true;
            existingCartId = sqlite3_column_int(cartStmt, 0);
            existingQuantity = sqlite3_column_int(cartStmt, 1);
        }
        sqlite3_finalize(cartStmt);

        int targetQuantity = inCart ? (existingQuantity + addQuantity) : addQuantity;

        if (targetQuantity > currentStock) {
            res.status = 400;
            res.set_content(json({{"error", "Cannot add item. Requested total quantity (" + std::to_string(targetQuantity) + ") exceeds available stock (" + std::to_string(currentStock) + ")."}}).dump(), "application/json");
            return;
        }

        if (inCart) {
            // Update existing cart item
            std::string updateSql = "UPDATE cart_items SET quantity = ? WHERE id = ?;";
            sqlite3_stmt* updateStmt = nullptr;
            if (sqlite3_prepare_v2(db, updateSql.c_str(), -1, &updateStmt, nullptr) == SQLITE_OK) {
                sqlite3_bind_int(updateStmt, 1, targetQuantity);
                sqlite3_bind_int(updateStmt, 2, existingCartId);
                sqlite3_step(updateStmt);
                sqlite3_finalize(updateStmt);
            }
        } else {
            // Insert new cart item
            std::string insertSql = "INSERT INTO cart_items (buyer_id, product_id, quantity) VALUES (?, ?, ?);";
            sqlite3_stmt* insertStmt = nullptr;
            if (sqlite3_prepare_v2(db, insertSql.c_str(), -1, &insertStmt, nullptr) == SQLITE_OK) {
                sqlite3_bind_int(insertStmt, 1, session.userId);
                sqlite3_bind_int(insertStmt, 2, productId);
                sqlite3_bind_int(insertStmt, 3, targetQuantity);
                sqlite3_step(insertStmt);
                sqlite3_finalize(insertStmt);
            }
        }

        res.status = 200;
        res.set_content(json({{"message", "Product added to cart successfully"}}).dump(), "application/json");
    } catch (const std::exception& e) {
        res.status = 400;
        res.set_content(json({{"error", std::string("Malformed JSON: ") + e.what()}}).dump(), "application/json");
    }
}

void CartController::handleGetCart(const httplib::Request& req, httplib::Response& res) {
    std::string token = SessionManager::extractToken(req);
    UserSession session;

    if (!SessionManager::getInstance().getSession(token, session)) {
        res.status = 401;
        res.set_content(json({{"error", "Authentication required to view cart."}}).dump(), "application/json");
        return;
    }

    sqlite3* db = Database::getInstance().getDbHandle();
    std::string sql = 
        "SELECT c.id, c.buyer_id, c.product_id, p.name AS product_name, p.description, p.price, p.stock, p.category, p.seller_id, u.name AS seller_name, c.quantity "
        "FROM cart_items c "
        "JOIN products p ON c.product_id = p.id "
        "JOIN users u ON p.seller_id = u.id "
        "WHERE c.buyer_id = ? ORDER BY c.id ASC;";

    sqlite3_stmt* stmt = nullptr;
    if (sqlite3_prepare_v2(db, sql.c_str(), -1, &stmt, nullptr) != SQLITE_OK) {
        res.status = 500;
        res.set_content(json({{"error", "Database error fetching cart contents."}}).dump(), "application/json");
        return;
    }

    sqlite3_bind_int(stmt, 1, session.userId);

    json itemArray = json::array();
    double grandTotal = 0.0;

    while (sqlite3_step(stmt) == SQLITE_ROW) {
        CartItem item;
        item.id = sqlite3_column_int(stmt, 0);
        item.buyerId = sqlite3_column_int(stmt, 1);
        item.productId = sqlite3_column_int(stmt, 2);
        item.productName = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 3));
        item.productDescription = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 4) ? sqlite3_column_text(stmt, 4) : reinterpret_cast<const unsigned char*>(""));
        item.unitPrice = sqlite3_column_double(stmt, 5);
        item.availableStock = sqlite3_column_int(stmt, 6);
        item.category = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 7));
        item.sellerId = sqlite3_column_int(stmt, 8);
        item.sellerName = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 9));
        item.quantity = sqlite3_column_int(stmt, 10);
        item.subtotal = item.unitPrice * item.quantity;

        grandTotal += item.subtotal;
        itemArray.push_back(item.toJson());
    }
    sqlite3_finalize(stmt);

    res.status = 200;
    res.set_content(json({
        {"items", itemArray},
        {"total", grandTotal}
    }).dump(), "application/json");
}

void CartController::handleUpdateCartQuantity(const httplib::Request& req, httplib::Response& res) {
    std::string token = SessionManager::extractToken(req);
    UserSession session;

    if (!SessionManager::getInstance().getSession(token, session)) {
        res.status = 401;
        res.set_content(json({{"error", "Authentication required to update cart."}}).dump(), "application/json");
        return;
    }

    try {
        json body = json::parse(req.body);
        int productId = body.value("product_id", 0);
        int newQuantity = body.value("quantity", 0);

        if (productId <= 0) {
            res.status = 400;
            res.set_content(json({{"error", "Valid product_id is required."}}).dump(), "application/json");
            return;
        }

        sqlite3* db = Database::getInstance().getDbHandle();

        if (newQuantity <= 0) {
            // Remove item from cart if quantity is set to 0
            std::string removeSql = "DELETE FROM cart_items WHERE buyer_id = ? AND product_id = ?;";
            sqlite3_stmt* rmStmt = nullptr;
            if (sqlite3_prepare_v2(db, removeSql.c_str(), -1, &rmStmt, nullptr) == SQLITE_OK) {
                sqlite3_bind_int(rmStmt, 1, session.userId);
                sqlite3_bind_int(rmStmt, 2, productId);
                sqlite3_step(rmStmt);
                sqlite3_finalize(rmStmt);
            }
            res.status = 200;
            res.set_content(json({{"message", "Item removed from cart"}}).dump(), "application/json");
            return;
        }

        // Validate available product stock
        std::string prodSql = "SELECT stock FROM products WHERE id = ?;";
        sqlite3_stmt* prodStmt = nullptr;
        if (sqlite3_prepare_v2(db, prodSql.c_str(), -1, &prodStmt, nullptr) != SQLITE_OK) {
            res.status = 500;
            res.set_content(json({{"error", "Database error checking product stock."}}).dump(), "application/json");
            return;
        }

        sqlite3_bind_int(prodStmt, 1, productId);
        int currentStock = 0;
        bool found = false;

        if (sqlite3_step(prodStmt) == SQLITE_ROW) {
            found = true;
            currentStock = sqlite3_column_int(prodStmt, 0);
        }
        sqlite3_finalize(prodStmt);

        if (!found) {
            res.status = 404;
            res.set_content(json({{"error", "Product not found."}}).dump(), "application/json");
            return;
        }

        if (newQuantity > currentStock) {
            res.status = 400;
            res.set_content(json({{"error", "Requested quantity (" + std::to_string(newQuantity) + ") exceeds available stock (" + std::to_string(currentStock) + ")."}}).dump(), "application/json");
            return;
        }

        std::string updateSql = "UPDATE cart_items SET quantity = ? WHERE buyer_id = ? AND product_id = ?;";
        sqlite3_stmt* updateStmt = nullptr;
        if (sqlite3_prepare_v2(db, updateSql.c_str(), -1, &updateStmt, nullptr) != SQLITE_OK) {
            res.status = 500;
            res.set_content(json({{"error", "Failed to prepare update cart statement."}}).dump(), "application/json");
            return;
        }

        sqlite3_bind_int(updateStmt, 1, newQuantity);
        sqlite3_bind_int(updateStmt, 2, session.userId);
        sqlite3_bind_int(updateStmt, 3, productId);

        sqlite3_step(updateStmt);
        sqlite3_finalize(updateStmt);

        res.status = 200;
        res.set_content(json({{"message", "Cart quantity updated successfully"}}).dump(), "application/json");
    } catch (const std::exception& e) {
        res.status = 400;
        res.set_content(json({{"error", std::string("Malformed JSON: ") + e.what()}}).dump(), "application/json");
    }
}

void CartController::handleRemoveFromCart(const httplib::Request& req, httplib::Response& res) {
    int productId = std::stoi(req.matches[1]);
    std::string token = SessionManager::extractToken(req);
    UserSession session;

    if (!SessionManager::getInstance().getSession(token, session)) {
        res.status = 401;
        res.set_content(json({{"error", "Authentication required to remove cart item."}}).dump(), "application/json");
        return;
    }

    sqlite3* db = Database::getInstance().getDbHandle();
    std::string removeSql = "DELETE FROM cart_items WHERE buyer_id = ? AND product_id = ?;";
    sqlite3_stmt* rmStmt = nullptr;

    if (sqlite3_prepare_v2(db, removeSql.c_str(), -1, &rmStmt, nullptr) != SQLITE_OK) {
        res.status = 500;
        res.set_content(json({{"error", "Database error removing cart item."}}).dump(), "application/json");
        return;
    }

    sqlite3_bind_int(rmStmt, 1, session.userId);
    sqlite3_bind_int(rmStmt, 2, productId);
    sqlite3_step(rmStmt);
    sqlite3_finalize(rmStmt);

    res.status = 200;
    res.set_content(json({{"message", "Product removed from cart successfully"}}).dump(), "application/json");
}
