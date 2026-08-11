#ifndef PRODUCT_H
#define PRODUCT_H

#include <string>
#include "json/json.hpp"

struct Product {
    int id = 0;
    int sellerId = 0;
    std::string sellerName;
    std::string name;
    std::string description;
    double price = 0.0;
    int stock = 0;
    std::string category;
    std::string imageUrl;
    std::string createdAt;

    nlohmann::json toJson() const {
        return {
            {"id", id},
            {"seller_id", sellerId},
            {"seller_name", sellerName},
            {"name", name},
            {"description", description},
            {"price", price},
            {"stock", stock},
            {"category", category},
            {"image_url", imageUrl},
            {"created_at", createdAt}
        };
    }
};

#endif // PRODUCT_H
