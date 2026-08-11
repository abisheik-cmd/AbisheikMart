#ifndef ORDER_H
#define ORDER_H

#include <string>
#include <vector>
#include "json/json.hpp"

struct OrderItem {
    int id = 0;
    int orderId = 0;
    int productId = 0;
    int sellerId = 0;
    std::string productName;
    int quantity = 0;
    double unitPrice = 0.0;
    double subtotal = 0.0;

    nlohmann::json toJson() const {
        return {
            {"id", id},
            {"order_id", orderId},
            {"product_id", productId},
            {"seller_id", sellerId},
            {"product_name", productName},
            {"quantity", quantity},
            {"unit_price", unitPrice},
            {"subtotal", subtotal}
        };
    }
};

struct Order {
    int id = 0;
    int buyerId = 0;
    double totalAmount = 0.0;
    std::string status;
    std::string createdAt;
    std::vector<OrderItem> items;

    nlohmann::json toJson() const {
        nlohmann::json itemArray = nlohmann::json::array();
        for (const auto& item : items) {
            itemArray.push_back(item.toJson());
        }
        return {
            {"id", id},
            {"buyer_id", buyerId},
            {"total_amount", totalAmount},
            {"status", status},
            {"created_at", createdAt},
            {"items", itemArray}
        };
    }
};

#endif // ORDER_H
