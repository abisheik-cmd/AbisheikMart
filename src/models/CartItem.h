#ifndef CART_ITEM_H
#define CART_ITEM_H

#include <string>
#include "json/json.hpp"

struct CartItem {
    int id = 0;
    int buyerId = 0;
    int productId = 0;
    std::string productName;
    std::string productDescription;
    double unitPrice = 0.0;
    int availableStock = 0;
    std::string category;
    int sellerId = 0;
    std::string sellerName;
    int quantity = 0;
    double subtotal = 0.0;

    nlohmann::json toJson() const {
        return {
            {"id", id},
            {"buyer_id", buyerId},
            {"product_id", productId},
            {"product_name", productName},
            {"product_description", productDescription},
            {"unit_price", unitPrice},
            {"available_stock", availableStock},
            {"category", category},
            {"seller_id", sellerId},
            {"seller_name", sellerName},
            {"quantity", quantity},
            {"subtotal", subtotal}
        };
    }
};

#endif // CART_ITEM_H
