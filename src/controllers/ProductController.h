#ifndef PRODUCT_CONTROLLER_H
#define PRODUCT_CONTROLLER_H

#include "httplib/httplib.h"

class ProductController {
public:
    static void registerRoutes(httplib::Server& svr);

private:
    static void handleGetAllProducts(const httplib::Request& req, httplib::Response& res);
    static void handleGetProductById(const httplib::Request& req, httplib::Response& res);
    static void handleGetSellerProducts(const httplib::Request& req, httplib::Response& res);
    static void handleAddProduct(const httplib::Request& req, httplib::Response& res);
    static void handleUpdateProduct(const httplib::Request& req, httplib::Response& res);
    static void handleDeleteProduct(const httplib::Request& req, httplib::Response& res);
};

#endif // PRODUCT_CONTROLLER_H
