#ifndef SESSION_MANAGER_H
#define SESSION_MANAGER_H

#include <string>
#include <unordered_map>
#include <mutex>
#include "httplib/httplib.h"

struct UserSession {
    int userId;
    std::string name;
    std::string email;
    std::string role; // BUYER, SELLER, ADMIN
};

class SessionManager {
public:
    static SessionManager& getInstance();

    std::string createSession(int userId, const std::string& name, const std::string& email, const std::string& role);
    bool getSession(const std::string& token, UserSession& session);
    void removeSession(const std::string& token);

    static std::string extractToken(const httplib::Request& req);

private:
    SessionManager() = default;
    SessionManager(const SessionManager&) = delete;
    SessionManager& operator=(const SessionManager&) = delete;

    std::unordered_map<std::string, UserSession> sessions;
    std::mutex sessionMutex;
};

#endif // SESSION_MANAGER_H
