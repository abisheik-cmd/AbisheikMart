#ifndef AUTH_CONTROLLER_H
#define AUTH_CONTROLLER_H

#include "httplib/httplib.h"

class AuthController {
public:
    static void registerRoutes(httplib::Server& svr);

private:
    static void handleRegister(const httplib::Request& req, httplib::Response& res);
    static void handleLogin(const httplib::Request& req, httplib::Response& res);
    static void handleMe(const httplib::Request& req, httplib::Response& res);

    static bool isValidEmail(const std::string& email);
};

#endif // AUTH_CONTROLLER_H
