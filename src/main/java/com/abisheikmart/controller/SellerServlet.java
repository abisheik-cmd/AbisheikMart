package com.abisheikmart.controller;

import com.abisheikmart.dto.ApiResponse;
import com.abisheikmart.dto.ProductRequest;
import com.abisheikmart.model.Category;
import com.abisheikmart.model.Product;
import com.abisheikmart.model.User;
import com.abisheikmart.service.CategoryService;
import com.abisheikmart.service.ProductService;
import com.abisheikmart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = {"/seller/dashboard", "/seller/product/new", "/seller/product/edit", "/seller/product/save", "/api/seller/product/delete"})
public class SellerServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || (!"SELLER".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole()))) {
            resp.sendRedirect(req.getContextPath() + "/catalog?error=seller_access_required");
            return;
        }

        String path = req.getServletPath();
        List<Category> categories = categoryService.getAllCategories();
        req.setAttribute("categories", categories);

        if ("/seller/product/new".equals(path)) {
            req.setAttribute("product", null);
            req.setAttribute("pageTitle", "Add New Product Listing - AbisheikMart 2.0");
            req.getRequestDispatcher("/WEB-INF/views/seller/product-form.jsp").forward(req, resp);
            return;

        } else if ("/seller/product/edit".equals(path)) {
            String idStr = req.getParameter("id");
            if (idStr != null) {
                try {
                    Long productId = Long.parseLong(idStr);
                    Optional<Product> pOpt = productService.getProductById(productId);
                    if (pOpt.isPresent()) {
                        Product p = pOpt.get();
                        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
                        if (isAdmin || p.getSellerId().equals(user.getId())) {
                            req.setAttribute("product", p);
                            req.setAttribute("pageTitle", "Edit Product Listing - AbisheikMart 2.0");
                            req.getRequestDispatcher("/WEB-INF/views/seller/product-form.jsp").forward(req, resp);
                            return;
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }
            resp.sendRedirect(req.getContextPath() + "/seller/dashboard?error=product_not_found");
            return;
        }

        // Dashboard view (/seller/dashboard)
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        List<Product> products = isAdmin ? productService.getAllProducts() : productService.getProductsBySellerId(user.getId());

        int totalListings = products.size();
        int totalInventory = products.stream().mapToInt(Product::getStock).sum();
        long lowStockCount = products.stream().filter(p -> p.getStock() > 0 && p.getStock() <= 5).count();

        req.setAttribute("products", products);
        req.setAttribute("totalListings", totalListings);
        req.setAttribute("totalInventory", totalInventory);
        req.setAttribute("lowStockCount", lowStockCount);
        req.setAttribute("pageTitle", "Seller Dashboard - AbisheikMart 2.0");

        req.getRequestDispatcher("/WEB-INF/views/seller/dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || (!"SELLER".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole()))) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.sendRedirect(req.getContextPath() + "/catalog");
            return;
        }

        String path = req.getServletPath();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());

        try {
            if ("/api/seller/product/delete".equals(path)) {
                ProductRequest delReq = JsonUtil.fromJson(req.getReader(), ProductRequest.class);
                if (delReq != null && delReq.getId() != null) {
                    productService.deleteProduct(user.getId(), isAdmin, delReq.getId());
                    resp.setContentType("application/json");
                    resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok("Product deleted successfully", null)));
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.setContentType("application/json");
                    resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("Product ID is required.")));
                }
                return;
            }

            if ("/seller/product/save".equals(path)) {
                String idStr = req.getParameter("id");
                ProductRequest pReq = new ProductRequest();
                if (idStr != null && !idStr.isBlank()) {
                    pReq.setId(Long.parseLong(idStr));
                }
                pReq.setName(req.getParameter("name"));
                pReq.setDescription(req.getParameter("description"));
                pReq.setPrice(Double.parseDouble(req.getParameter("price")));
                pReq.setStock(Integer.parseInt(req.getParameter("stock")));
                pReq.setCategoryId(Long.parseLong(req.getParameter("categoryId")));
                pReq.setImageUrl(req.getParameter("imageUrl"));

                if (pReq.getId() != null) {
                    productService.updateProduct(user.getId(), isAdmin, pReq);
                } else {
                    productService.createProduct(user.getId(), pReq);
                }

                resp.sendRedirect(req.getContextPath() + "/seller/dashboard?save=success");
            }
        } catch (IllegalArgumentException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("categories", categoryService.getAllCategories());
            req.getRequestDispatcher("/WEB-INF/views/seller/product-form.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Error saving product listing: " + e.getMessage());
            req.setAttribute("categories", categoryService.getAllCategories());
            req.getRequestDispatcher("/WEB-INF/views/seller/product-form.jsp").forward(req, resp);
        }
    }
}

