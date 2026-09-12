package com.abisheikmart.controller;

import com.abisheikmart.dto.ApiResponse;
import com.abisheikmart.model.Category;
import com.abisheikmart.model.Product;
import com.abisheikmart.service.CategoryService;
import com.abisheikmart.service.ProductService;
import com.abisheikmart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = {"/catalog", "/catalog/detail", "/api/products"})
public class CatalogServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/api/products".equals(path)) {
            List<Product> products = productService.getAllProducts();
            resp.setContentType("application/json");
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok(products)));
            return;
        }

        if ("/catalog/detail".equals(path)) {
            String idStr = req.getParameter("id");
            if (idStr != null) {
                try {
                    Long id = Long.parseLong(idStr);
                    Optional<Product> pOpt = productService.getProductById(id);
                    if (pOpt.isPresent()) {
                        req.setAttribute("product", pOpt.get());
                        req.setAttribute("pageTitle", pOpt.get().getName() + " - AbisheikMart 2.0");
                        req.getRequestDispatcher("/WEB-INF/views/catalog/product-detail.jsp").forward(req, resp);
                        return;
                    }
                } catch (NumberFormatException ignored) {}
            }
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Standard /catalog listing view
        String query = req.getParameter("q");
        String category = req.getParameter("category");
        String sort = req.getParameter("sort");

        List<Product> products = productService.searchProducts(query, category, sort);
        List<Category> categories = categoryService.getAllCategories();

        req.setAttribute("products", products);
        req.setAttribute("categories", categories);
        req.setAttribute("selectedCategory", category != null ? category : "ALL");
        req.setAttribute("selectedSort", sort != null ? sort : "newest");
        req.setAttribute("pageTitle", "Product Catalog - AbisheikMart 2.0");

        req.getRequestDispatcher("/WEB-INF/views/catalog/browse.jsp").forward(req, resp);
    }
}

