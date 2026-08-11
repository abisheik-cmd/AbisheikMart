#include <iostream>
#include <string>
#include "httplib/httplib.h"
#include "json/json.hpp"
#include "db/Database.h"
#include "controllers/AuthController.h"
#include "controllers/ProductController.h"
#include "controllers/CartController.h"
#include "controllers/OrderController.h"

using json = nlohmann::json;

int main() {
    std::cout << "==================================================" << std::endl;
    std::cout << "  Starting Abisheikmart C++ E-Commerce Server...  " << std::endl;
    std::cout << "==================================================" << std::endl;

    // Initialize SQLite Database and seed default admin account
    if (!Database::getInstance().init("ecommerce.db")) {
        std::cerr << "[Fatal Error] Failed to initialize database. Server shutting down." << std::endl;
        return 1;
    }

    httplib::Server svr;

    // Middleware to set CORS headers for REST API requests
    svr.set_post_routing_handler([](const httplib::Request& req, httplib::Response& res) {
        res.set_header("Access-Control-Allow-Origin", "*");
        res.set_header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Auth-Token");
        res.set_header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    });

    // Options handler for preflight requests
    svr.Options(R"(.*)", [](const httplib::Request& req, httplib::Response& res) {
        res.set_header("Access-Control-Allow-Origin", "*");
        res.set_header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Auth-Token");
        res.set_header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        res.status = 204;
    });

    // Serve static frontend files from 'public' directory
    if (!svr.set_mount_point("/", "./public")) {
        std::cout << "[Warning] Could not set static mount point to './public'. Checking fallback." << std::endl;
    }

    // Health Check Endpoint
    svr.Get("/api/health", [](const httplib::Request& req, httplib::Response& res) {
        json response = {
            {"status", "ok"},
            {"message", "Abisheikmart C++ E-Commerce Server is running"},
            {"version", "1.0.0"}
        };
        res.set_content(response.dump(), "application/json");
    });

    // Register API Controller Routes
    AuthController::registerRoutes(svr);
    ProductController::registerRoutes(svr);
    CartController::registerRoutes(svr);
    OrderController::registerRoutes(svr);

    int port = 8080;
    std::cout << "[Server] Listening on http://localhost:" << port << std::endl;
    std::cout << "[Server] Press Ctrl+C to stop the server." << std::endl;

    if (!svr.listen("0.0.0.0", port)) {
        std::cerr << "[Error] Server failed to start on port " << port << std::endl;
        return 1;
    }

    return 0;
}
