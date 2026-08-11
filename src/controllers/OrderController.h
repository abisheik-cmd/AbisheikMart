#ifndef ORDER_CONTROLLER_H
#define ORDER_CONTROLLER_H

#include "httplib/httplib.h"

class OrderController {
public:
    static void registerRoutes(httplib::Server& svr);

private:
    static void handleCheckout(const httplib::Request& req, httplib::Response& res);
    static void handleGetOrders(const httplib::Request& req, httplib::Response& res);
};

#endif // ORDER_CONTROLLER_H
