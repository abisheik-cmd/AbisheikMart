#include "utils/SessionManager.h"
#include "utils/Crypto.h"
#include <chrono>

SessionManager& SessionManager::getInstance() {
    static SessionManager instance;
    return instance;
}

std::string SessionManager::createSession(int userId, const std::string& name, const std::string& email, const std::string& role) {
    std::lock_guard<std::mutex> lock(sessionMutex);
    
    // Generate token: hash of userId + timestamp + random salt
    auto now = std::chrono::system_clock::now().time_since_epoch().count();
    std::string raw = std::to_string(userId) + "_" + std::to_string(now) + "_" + Crypto::generateSalt(16);
    std::string token = Crypto::sha256Hex(raw);

    UserSession session;
    session.userId = userId;
    session.name = name;
    session.email = email;
    session.role = role;

    sessions[token] = session;
    return token;
}

bool SessionManager::getSession(const std::string& token, UserSession& session) {
    if (token.empty()) return false;
    std::lock_guard<std::mutex> lock(sessionMutex);
    auto it = sessions.find(token);
    if (it != sessions.end()) {
        session = it->second;
        return true;
    }
    return false;
}

void SessionManager::removeSession(const std::string& token) {
    if (token.empty()) return;
    std::lock_guard<std::mutex> lock(sessionMutex);
    sessions.erase(token);
}

std::string SessionManager::extractToken(const httplib::Request& req) {
    // 1. Check Authorization header
    if (req.has_header("Authorization")) {
        std::string authHeader = req.get_header_value("Authorization");
        if (authHeader.rfind("Bearer ", 0) == 0) {
            return authHeader.substr(7);
        }
    }
    // 2. Check X-Auth-Token header
    if (req.has_header("X-Auth-Token")) {
        return req.get_header_value("X-Auth-Token");
    }
    return "";
}
