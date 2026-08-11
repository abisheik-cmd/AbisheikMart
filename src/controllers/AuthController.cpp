#include "controllers/AuthController.h"
#include "json/json.hpp"
#include "db/Database.h"
#include "utils/Crypto.h"
#include "utils/SessionManager.h"
#include "models/User.h"
#include <iostream>
#include <regex>

using json = nlohmann::json;

void AuthController::registerRoutes(httplib::Server& svr) {
    svr.Post("/api/auth/register", handleRegister);
    svr.Post("/api/auth/login", handleLogin);
    svr.Get("/api/auth/me", handleMe);
}

bool AuthController::isValidEmail(const std::string& email) {
    // Basic regex validation for email
    const std::regex pattern("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    return std::regex_match(email, pattern);
}

void AuthController::handleRegister(const httplib::Request& req, httplib::Response& res) {
    try {
        json body = json::parse(req.body);
        std::string name = body.value("name", "");
        std::string email = body.value("email", "");
        std::string password = body.value("password", "");
        std::string role = body.value("role", "BUYER");

        // Input Validation
        if (name.empty() || email.empty() || password.empty()) {
            res.status = 400;
            res.set_content(json({{"error", "Name, email/username, and password are required fields."}}).dump(), "application/json");
            return;
        }

        if (password.length() < 6) {
            res.status = 400;
            res.set_content(json({{"error", "Password must be at least 6 characters in length."}}).dump(), "application/json");
            return;
        }

        // Validate role: public registration can only register BUYER or SELLER
        if (role != "BUYER" && role != "SELLER") {
            res.status = 400;
            res.set_content(json({{"error", "Invalid role selected. Public registration allows BUYER or SELLER roles only."}}).dump(), "application/json");
            return;
        }

        // Check if email format is valid (if contains '@')
        if (email.find('@') != std::string::npos && !isValidEmail(email)) {
            res.status = 400;
            res.set_content(json({{"error", "Invalid email address format."}}).dump(), "application/json");
            return;
        }

        sqlite3* db = Database::getInstance().getDbHandle();

        // Check duplicate email
        std::string checkSql = "SELECT COUNT(*) FROM users WHERE email = ?;";
        sqlite3_stmt* checkStmt = nullptr;
        if (sqlite3_prepare_v2(db, checkSql.c_str(), -1, &checkStmt, nullptr) != SQLITE_OK) {
            res.status = 500;
            res.set_content(json({{"error", "Database error checking account existence."}}).dump(), "application/json");
            return;
        }

        sqlite3_bind_text(checkStmt, 1, email.c_str(), -1, SQLITE_TRANSIENT);
        int count = 0;
        if (sqlite3_step(checkStmt) == SQLITE_ROW) {
            count = sqlite3_column_int(checkStmt, 0);
        }
        sqlite3_finalize(checkStmt);

        if (count > 0) {
            res.status = 409; // Conflict
            res.set_content(json({{"error", "An account with this email/username already exists."}}).dump(), "application/json");
            return;
        }

        // Generate salt & PBKDF2 password hash
        std::string salt = Crypto::generateSalt(16);
        std::string hash = Crypto::hashPassword(password, salt);

        // Insert new user
        std::string insertSql = "INSERT INTO users (name, email, password_hash, salt, role) VALUES (?, ?, ?, ?, ?);";
        sqlite3_stmt* insertStmt = nullptr;
        if (sqlite3_prepare_v2(db, insertSql.c_str(), -1, &insertStmt, nullptr) != SQLITE_OK) {
            res.status = 500;
            res.set_content(json({{"error", "Failed to prepare user registration query."}}).dump(), "application/json");
            return;
        }

        sqlite3_bind_text(insertStmt, 1, name.c_str(), -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(insertStmt, 2, email.c_str(), -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(insertStmt, 3, hash.c_str(), -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(insertStmt, 4, salt.c_str(), -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(insertStmt, 5, role.c_str(), -1, SQLITE_TRANSIENT);

        int rc = sqlite3_step(insertStmt);
        sqlite3_finalize(insertStmt);

        if (rc != SQLITE_DONE) {
            res.status = 500;
            res.set_content(json({{"error", "Failed to register user."}}).dump(), "application/json");
            return;
        }

        int newUserId = static_cast<int>(sqlite3_last_insert_rowid(db));
        std::string token = SessionManager::getInstance().createSession(newUserId, name, email, role);

        json response = {
            {"message", "Registration successful"},
            {"token", token},
            {"user", {
                {"id", newUserId},
                {"name", name},
                {"email", email},
                {"role", role}
            }}
        };

        res.status = 201; // Created
        res.set_content(response.dump(), "application/json");
    } catch (const std::exception& e) {
        res.status = 400;
        res.set_content(json({{"error", std::string("Malformed JSON request: ") + e.what()}}).dump(), "application/json");
    }
}

void AuthController::handleLogin(const httplib::Request& req, httplib::Response& res) {
    try {
        json body = json::parse(req.body);
        std::string email = body.value("email", "");
        std::string password = body.value("password", "");

        if (email.empty() || password.empty()) {
            res.status = 400;
            res.set_content(json({{"error", "Email/username and password are required."}}).dump(), "application/json");
            return;
        }

        sqlite3* db = Database::getInstance().getDbHandle();

        // Support login by email OR username 'admin' (where email = 'admin@abishmart.com')
        std::string querySql = "SELECT id, name, email, password_hash, salt, role FROM users WHERE email = ? OR (email = 'admin@abishmart.com' AND ? = 'admin');";
        sqlite3_stmt* stmt = nullptr;
        if (sqlite3_prepare_v2(db, querySql.c_str(), -1, &stmt, nullptr) != SQLITE_OK) {
            res.status = 500;
            res.set_content(json({{"error", "Database error during login."}}).dump(), "application/json");
            return;
        }

        sqlite3_bind_text(stmt, 1, email.c_str(), -1, SQLITE_TRANSIENT);
        sqlite3_bind_text(stmt, 2, email.c_str(), -1, SQLITE_TRANSIENT);

        bool found = false;
        User user;

        if (sqlite3_step(stmt) == SQLITE_ROW) {
            found = true;
            user.id = sqlite3_column_int(stmt, 0);
            user.name = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 1));
            user.email = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 2));
            user.passwordHash = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 3));
            user.salt = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 4));
            user.role = reinterpret_cast<const char*>(sqlite3_column_text(stmt, 5));
        }
        sqlite3_finalize(stmt);

        if (!found) {
            res.status = 401; // Unauthorized
            res.set_content(json({{"error", "Invalid email/username or password."}}).dump(), "application/json");
            return;
        }

        // Verify password using PBKDF2
        if (!Crypto::verifyPassword(password, user.salt, user.passwordHash)) {
            res.status = 401; // Unauthorized
            res.set_content(json({{"error", "Invalid email/username or password."}}).dump(), "application/json");
            return;
        }

        // Password verified, create session token
        std::string token = SessionManager::getInstance().createSession(user.id, user.name, user.email, user.role);

        json response = {
            {"message", "Login successful"},
            {"token", token},
            {"user", {
                {"id", user.id},
                {"name", user.name},
                {"email", user.email},
                {"role", user.role}
            }}
        };

        res.status = 200;
        res.set_content(response.dump(), "application/json");
    } catch (const std::exception& e) {
        res.status = 400;
        res.set_content(json({{"error", std::string("Malformed JSON request: ") + e.what()}}).dump(), "application/json");
    }
}

void AuthController::handleMe(const httplib::Request& req, httplib::Response& res) {
    std::string token = SessionManager::extractToken(req);
    UserSession session;

    if (!SessionManager::getInstance().getSession(token, session)) {
        res.status = 401;
        res.set_content(json({{"error", "Unauthenticated or session expired."}}).dump(), "application/json");
        return;
    }

    json response = {
        {"user", {
            {"id", session.userId},
            {"name", session.name},
            {"email", session.email},
            {"role", session.role}
        }}
    };

    res.status = 200;
    res.set_content(response.dump(), "application/json");
}
