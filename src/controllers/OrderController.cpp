#include "controllers/OrderController.h"
#include "json/json.hpp"
#include "db/Database.h"
#include "utils/SessionManager.h"
#include "models/Order.h"
#include <iostream>

using json = nlohmann::json;

void OrderController::registerRoutes(httplib::Server& svr) {
    svr.Post("/api/checkout", handleCheckout);
    svr.Get("/api/orders", handleGetOrders);
}

void OrderController::handleCheckout(const httplib::Request& req, httplib::Response& res) {
    std::string token = SessionManager::extractToken(req);
    UserSession session;

    if (!SessionManager::getInstance().getSession(token, session)) {
        res.status = 401;
        res.set_content(json({{"error", "Authentication required to checkout."}}).dump(), "application/json");
        return;
    }

    sqlite3* db = Database::getInstance().getDbHandle();

    // Begin SQLite Transaction
    Database::getInstance().execute("BEGIN TRANSACTION;");

    struct TempCartItem {
        int cartItemId;
        int productId;
        std::string productName;
        int sellerId;
        double unitPrice;
        int currentStock;
        int quantity;
        double subtotal;
    };

    std::vector<TempCartItem> cartItems;
    double grandTotal = 0.0;

    std::string cartSql = 
        "SELECT c.id, c.product_id, p.name, p.seller_id, p.price, p.stock, c.quantity "
        "FROM cart_items c JOIN products p ON c.product_id = p.id "
        "WHERE c.buyer_id = ?;";

    sqlite3_stmt* cartStmt = nullptr;
    if (sqlite3_prepare_v2(db, cartSql.c_str(), -1, &cartStmt, nullptr) != SQLITE_OK) {
        Database::getInstance().execute("ROLLBACK;");
        res.status = 500;
        res.set_content(json({{"error", "Database error fetching cart for checkout."}}).dump(), "application/json");
        return;
    }

    sqlite3_bind_int(cartStmt, 1, session.userId);

    while (sqlite3_step(cartStmt) == SQLITE_ROW) {
        TempCartItem item;
        item.cartItemId = sqlite3_column_int(cartStmt, 0);
        item.productId = sqlite3_column_int(cartStmt, 1);
        item.productName = reinterpret_cast<const char*>(sqlite3_column_text(cartStmt, 2));
        item.sellerId = sqlite3_column_int(cartStmt, 3);
        item.unitPrice = sqlite3_column_double(cartStmt, 4);
        item.currentStock = sqlite3_column_int(cartStmt, 5);
        item.quantity = sqlite3_column_int(cartStmt, 6);
        item.subtotal = item.unitPrice * item.quantity;

        grandTotal += item.subtotal;
        cartItems.push_back(item);
    }
    sqlite3_finalize(cartStmt);

    // 1. Verify Cart Is Not Empty
    if (cartItems.empty()) {
        Database::getInstance().execute("ROLLBACK;");
        res.status = 400;
        res.set_content(json({{"error", "Cart is empty. Add products to cart before checkout."}}).dump(), "application/json");
        return;
    }

    // 2. Re-verify Stock for every item in cart to prevent over-ordering
    for (const auto& item : cartItems) {
        if (item.quantity > item.currentStock) {
            Database::getInstance().execute("ROLLBACK;");
            res.status = 400;
            res.set_content(json({{"error", "Insufficient stock for product '" + item.productName + "'. Available stock: " + std::to_string(item.currentStock) + ", requested: " + std::to_string(item.quantity)}}).dump(), "application/json");
            return;
        }
    }

    // 3. Create Order in 'orders' table
    std::string orderInsertSql = "INSERT INTO orders (buyer_id, total_amount, status) VALUES (?, ?, 'PLACED');";
    sqlite3_stmt* orderStmt = nullptr;
    if (sqlite3_prepare_v2(db, orderInsertSql.c_str(), -1, &orderStmt, nullptr) != SQLITE_OK) {
        Database::getInstance().execute("ROLLBACK;");
        res.status = 500;
        res.set_content(json({{"error", "Failed to create order record."}}).dump(), "application/json");
        return;
    }

    sqlite3_bind_int(orderStmt, 1, session.userId);
    sqlite3_bind_double(orderStmt, 2, grandTotal);

    if (sqlite3_step(orderStmt) != SQLITE_DONE) {
        sqlite3_finalize(orderStmt);
        Database::getInstance().execute("ROLLBACK;");
        res.status = 500;
        res.set_content(json({{"error", "Failed to insert order."}}).dump(), "application/json");
        return;
    }
    sqlite3_finalize(orderStmt);

    int orderId = static_cast<int>(sqlite3_last_insert_rowid(db));

    // 4. Create Order Items & Update Product Stock
    std::string orderItemInsertSql = 
        "INSERT INTO order_items (order_id, product_id, seller_id, product_name, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?);";
    std::string stockUpdateSql = "UPDATE products SET stock = stock - ? WHERE id = ?;";

    json orderItemArray = json::array();

    for (const auto& item : cartItems) {
        // Insert order_item
        sqlite3_stmt* oiStmt = nullptr;
        if (sqlite3_prepare_v2(db, orderItemInsertSql.c_str(), -1, &oiStmt, nullptr) == SQLITE_OK) {
            sqlite3_bind_int(oiStmt, 1, orderId);
            sqlite3_bind_int(oiStmt, 2, item.productId);
            sqlite3_bind_int(oiStmt, 3, item.sellerId);
            sqlite3_bind_text(oiStmt, 4, item.productName.c_str(), -1, SQLITE_TRANSIENT);
            sqlite3_bind_int(oiStmt, 5, item.quantity);
            sqlite3_bind_double(oiStmt, 6, item.unitPrice);
            sqlite3_bind_double(oiStmt, 7, item.subtotal);
            sqlite3_step(oiStmt);
            sqlite3_finalize(oiStmt);
        }

        // Update product stock
        sqlite3_stmt* stockStmt = nullptr;
        if (sqlite3_prepare_v2(db, stockUpdateSql.c_str(), -1, &stockStmt, nullptr) == SQLITE_OK) {
            sqlite3_bind_int(stockStmt, 1, item.quantity);
            sqlite3_bind_int(stockStmt, 2, item.productId);
            sqlite3_step(stockStmt);
            sqlite3_finalize(stockStmt);
        }

        orderItemArray.push_back({
            {"product_id", item.productId},
            {"product_name", item.productName},
            {"quantity", item.quantity},
            {"unit_price", item.unitPrice},
            {"subtotal", item.subtotal}
        });
    }

    // 5. Clear Buyer's Cart Items
    std::string clearCartSql = "DELETE FROM cart_items WHERE buyer_id = ?;";
    sqlite3_stmt* clearStmt = nullptr;
    if (sqlite3_prepare_v2(db, clearCartSql.c_str(), -1, &clearStmt, nullptr) == SQLITE_OK) {
        sqlite3_bind_int(clearStmt, 1, session.userId);
        sqlite3_step(clearStmt);
        sqlite3_finalize(clearStmt);
    }

    // Commit Transaction
    Database::getInstance().execute("COMMIT;");

    json response = {
        {"message", "Order placed successfully"},
        {"order", {
            {"order_id", orderId},
            {"buyer_id", session.userId},
            {"total_amount", grandTotal},
            {"status", "PLACED"},
            {"items", orderItemArray}
        }}
    };

    res.status = 201; // Created
    res.set_content(response.dump(), "application/json");
}

void OrderController::handleGetOrders(const httplib::Request& req, httplib::Response& res) {
    std::string token = SessionManager::extractToken(req);
    UserSession session;

    if (!SessionManager::getInstance().getSession(token, session)) {
        res.status = 401;
        res.set_content(json({{"error", "Authentication required to view orders."}}).dump(), "application/json");
        return;
    }

    sqlite3* db = Database::getInstance().getDbHandle();
    std::string orderSql = "SELECT id, buyer_id, total_amount, status, created_at FROM orders WHERE buyer_id = ? ORDER BY id DESC;";

    sqlite3_stmt* orderStmt = nullptr;
    if (sqlite3_prepare_v2(db, orderSql.c_str(), -1, &orderStmt, nullptr) != SQLITE_OK) {
        res.status = 500;
        res.set_content(json({{"error", "Database error fetching orders."}}).dump(), "application/json");
        return;
    }

    sqlite3_bind_int(orderStmt, 1, session.userId);
    json orderList = json::array();

    while (sqlite3_step(orderStmt) == SQLITE_ROW) {
        Order o;
        o.id = sqlite3_column_int(orderStmt, 0);
        o.buyerId = sqlite3_column_int(orderStmt, 1);
        o.totalAmount = sqlite3_column_double(orderStmt, 2);
        o.status = reinterpret_cast<const char*>(sqlite3_column_text(orderStmt, 3));
        o.createdAt = reinterpret_cast<const char*>(sqlite3_column_text(orderStmt, 4));

        // Fetch order items for this order
        std::string itemSql = "SELECT id, product_id, seller_id, product_name, quantity, unit_price, subtotal FROM order_items WHERE order_id = ?;";
        sqlite3_stmt* itemStmt = nullptr;
        if (sqlite3_prepare_v2(db, itemSql.c_str(), -1, &itemStmt, nullptr) == SQLITE_OK) {
            sqlite3_bind_int(itemStmt, 1, o.id);
            while (sqlite3_step(itemStmt) == SQLITE_ROW) {
                OrderItem oi;
                oi.id = sqlite3_column_int(itemStmt, 0);
                oi.orderId = o.id;
                oi.productId = sqlite3_column_int(itemStmt, 1);
                oi.sellerId = sqlite3_column_int(itemStmt, 2);
                oi.productName = reinterpret_cast<const char*>(sqlite3_column_text(itemStmt, 3));
                oi.quantity = sqlite3_column_int(itemStmt, 4);
                oi.unitPrice = sqlite3_column_double(itemStmt, 5);
                oi.subtotal = sqlite3_column_double(itemStmt, 6);
                o.items.push_back(oi);
            }
            sqlite3_finalize(itemStmt);
        }

        orderList.push_back(o.toJson());
    }
    sqlite3_finalize(orderStmt);

    res.status = 200;
    res.set_content(json({{"orders", orderList}}).dump(), "application/json");
}
