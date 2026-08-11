#ifndef USER_H
#define USER_H

#include <string>
#include "json/json.hpp"

struct User {
    int id = 0;
    std::string name;
    std::string email;
    std::string passwordHash;
    std::string salt;
    std::string role; // BUYER, SELLER, ADMIN
    std::string createdAt;

    nlohmann::json toJson(bool includePrivate = false) const {
        nlohmann::json j = {
            {"id", id},
            {"name", name},
            {"email", email},
            {"role", role},
            {"created_at", createdAt}
        };
        if (includePrivate) {
            j["password_hash"] = passwordHash;
            j["salt"] = salt;
        }
        return j;
    }
};

#endif // USER_H
