package com.abisheikmart.controller;

import com.abisheikmart.dao.OrderDao;
import com.abisheikmart.dao.ProductDao;
import com.abisheikmart.dto.ApiResponse;
import com.abisheikmart.dto.ProductRequest;
import com.abisheikmart.model.Category;
import com.abisheikmart.model.Product;
import com.abisheikmart.model.SellerDashboardStats;
import com.abisheikmart.model.SellerOrderSummary;
import com.abisheikmart.model.User;
import com.abisheikmart.service.CategoryService;
import com.abisheikmart.service.ProductService;
import com.abisheikmart.service.SellerService;
import com.abisheikmart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = {"/seller/dashboard", "/seller/orders", "/seller/product/new", "/seller/product/edit", "/seller/product/save", "/seller/order/status", "/api/seller/product/delete", "/api/seller/order/status"})
public class SellerServlet extends HttpServlet {
    private final ProductDao productDao = new ProductDao();
    private final ProductService productService = new ProductService(productDao);
    private final SellerService sellerService = new SellerService(productDao, productService, new OrderDao());
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireSeller(req, resp, false);
        if (user == null) return;

        String path = req.getServletPath();
        if ("/seller/product/new".equals(path)) {
            req.setAttribute("product", null);
            req.setAttribute("categories", categoryService.getAllCategories());
            req.setAttribute("pageTitle", "Add New Product Listing - AbisheikMart");
            req.getRequestDispatcher("/WEB-INF/views/seller/product-form.jsp").forward(req, resp);
            return;
        }
        if ("/seller/product/edit".equals(path)) {
            try {
                long productId = Long.parseLong(req.getParameter("id"));
                Product product = sellerService.getProducts(user.getId(), user.getRole()).stream()
                        .filter(candidate -> productId == candidate.getId()).findFirst()
                        .orElseThrow(() -> new SecurityException("Product not found or not owned by you."));
                req.setAttribute("product", product);
                req.setAttribute("categories", categoryService.getAllCategories());
                req.setAttribute("pageTitle", "Edit Product Listing - AbisheikMart");
                req.getRequestDispatcher("/WEB-INF/views/seller/product-form.jsp").forward(req, resp);
            } catch (RuntimeException exception) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, exception.getMessage());
            }
            return;
        }
        if ("/seller/orders".equals(path)) {
            req.setAttribute("orders", sellerService.getOrders(user.getId(), user.getRole()));
            req.setAttribute("pageTitle", "Seller Orders - AbisheikMart");
            req.getRequestDispatcher("/WEB-INF/views/seller/orders.jsp").forward(req, resp);
            return;
        }

        List<Product> products = sellerService.getProducts(user.getId(), user.getRole());
        SellerDashboardStats stats = sellerService.getDashboard(user.getId(), user.getRole());
        req.setAttribute("products", products);
        req.setAttribute("stats", stats);
        req.setAttribute("totalListings", stats.getActiveProducts());
        req.setAttribute("totalInventory", products.stream().mapToInt(product -> product.getStock() == null ? 0 : product.getStock()).sum());
        req.setAttribute("lowStockCount", products.stream().filter(product -> product.getStock() != null && product.getStock() > 0 && product.getStock() <= 5).count());
        req.setAttribute("pageTitle", "Seller Dashboard - AbisheikMart");
        req.getRequestDispatcher("/WEB-INF/views/seller/dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireSeller(req, resp, true);
        if (user == null) return;
        String path = req.getServletPath();
        try {
            if ("/api/seller/product/delete".equals(path)) {
                ProductRequest deleteRequest = JsonUtil.fromJson(req.getReader(), ProductRequest.class);
                if (deleteRequest == null || deleteRequest.getId() == null) {
                    writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Product ID is required.");
                    return;
                }
                sellerService.deleteProduct(user.getId(), user.getRole(), deleteRequest.getId());
                resp.setContentType("application/json");
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok("Product deleted successfully", null)));
                return;
            }

            if ("/seller/product/save".equals(path)) {
                ProductRequest productRequest = productRequest(req);
                if (productRequest.getId() == null) sellerService.createProduct(user.getId(), user.getRole(), productRequest);
                else sellerService.updateProduct(user.getId(), user.getRole(), productRequest);
                resp.sendRedirect(req.getContextPath() + "/seller/dashboard?save=success");
                return;
            }

            if ("/seller/order/status".equals(path) || "/api/seller/order/status".equals(path)) {
                Long orderId = parseLong(req.getParameter("orderId"), "Order ID is required.");
                String status = req.getParameter("status");
                sellerService.updateOrderStatus(user.getId(), user.getRole(), orderId, status);
                if ("/api/seller/order/status".equals(path)) {
                    resp.setContentType("application/json");
                    resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok("Order status updated.", null)));
                } else {
                    resp.sendRedirect(req.getContextPath() + "/seller/orders?updated=success");
                }
            }
        } catch (SecurityException exception) {
            writeJsonOrPageError(req, resp, path, HttpServletResponse.SC_FORBIDDEN, exception.getMessage());
        } catch (IllegalArgumentException | SQLException exception) {
            writeJsonOrPageError(req, resp, path, HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        }
    }

    private ProductRequest productRequest(HttpServletRequest req) {
        ProductRequest product = new ProductRequest();
        String id = req.getParameter("id");
        if (id != null && !id.isBlank()) product.setId(Long.parseLong(id));
        product.setName(req.getParameter("name"));
        product.setDescription(req.getParameter("description"));
        product.setOriginalPrice(parseDouble(req.getParameter("originalPrice"), null));
        product.setPrice(parseDouble(req.getParameter("price"), null));
        product.setStock(parseInteger(req.getParameter("stock"), null));
        product.setCategoryId(parseLong(req.getParameter("categoryId"), null));
        product.setImageUrl(req.getParameter("imageUrl"));
        return product;
    }

    private User requireSeller(HttpServletRequest req, HttpServletResponse resp, boolean api) throws IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null || !"SELLER".equalsIgnoreCase(user.getRole())) {
            if (api) writeJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "SELLER access is required.");
            else resp.sendError(HttpServletResponse.SC_FORBIDDEN, "SELLER access is required.");
            return null;
        }
        return user;
    }

    private void writeJsonOrPageError(HttpServletRequest req, HttpServletResponse resp, String path, int status, String message) throws ServletException, IOException {
        if (path != null && path.startsWith("/api/")) writeJsonError(resp, status, message);
        else if ("/seller/orders".equals(path)) {
            resp.setStatus(status);
            req.setAttribute("errorMessage", message);
            req.setAttribute("orders", sellerService.getOrders(((User) req.getSession(false).getAttribute("user")).getId(), "SELLER"));
            req.getRequestDispatcher("/WEB-INF/views/seller/orders.jsp").forward(req, resp);
        } else {
            req.setAttribute("errorMessage", message);
            req.setAttribute("categories", categoryService.getAllCategories());
            req.getRequestDispatcher("/WEB-INF/views/seller/product-form.jsp").forward(req, resp);
        }
    }

    private void writeJsonError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.getWriter().write(JsonUtil.toJson(ApiResponse.error(message == null ? "Seller operation failed." : message)));
    }

    private Long parseLong(String value, String missingMessage) {
        if (value == null || value.isBlank()) {
            if (missingMessage == null) return null;
            throw new IllegalArgumentException(missingMessage);
        }
        return Long.parseLong(value);
    }

    private Integer parseInteger(String value, Integer fallback) { return value == null || value.isBlank() ? fallback : Integer.valueOf(value); }
    private Double parseDouble(String value, Double fallback) { return value == null || value.isBlank() ? fallback : Double.valueOf(value); }
}
