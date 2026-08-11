#ifndef CART_CONTROLLER_H
#define CART_CONTROLLER_H

#include "httplib/httplib.h"

class CartController {
public:
    static void registerRoutes(httplib::Server& svr);

private:
    static void handleAddToCart(const httplib::Request& req, httplib::Response& res);
    static void handleGetCart(const httplib::Request& req, httplib::Response& res);
    static void handleUpdateCartQuantity(const httplib::Request& req, httplib::Response& res);
    static void handleRemoveFromCart(const httplib::Request& req, httplib::Response& res);
};

#endif // CART_CONTROLLER_H
