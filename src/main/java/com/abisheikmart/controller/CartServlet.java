package com.abisheikmart.controller;

import com.abisheikmart.dto.ApiResponse;
import com.abisheikmart.dto.CartRequest;
import com.abisheikmart.model.Cart;
import com.abisheikmart.model.User;
import com.abisheikmart.service.CartService;
import com.abisheikmart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = {"/cart", "/api/cart", "/api/cart/add", "/api/cart/update", "/api/cart/remove", "/api/cart/count"})
public class CartServlet extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if ("/api/cart/count".equals(path)) {
            resp.setContentType("application/json");
            if (user == null) {
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok(Map.of("count", 0))));
                return;
            }
            try {
                Cart cart = cartService.getOrCreateCart(user.getId());
                int count = cart.getItems().stream().mapToInt(i -> i.getQuantity()).sum();
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok(Map.of("count", count))));
            } catch (Exception e) {
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok(Map.of("count", 0))));
            }
            return;
        }

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/cart");
            return;
        }

        try {
            Cart cart = cartService.getOrCreateCart(user.getId());
            if ("/api/cart".equals(path)) {
                resp.setContentType("application/json");
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok(cart)));
            } else {
                req.setAttribute("cart", cart);
                req.setAttribute("cartItemCount", cart.getItems().stream().mapToInt(i -> i.getQuantity()).sum());
                req.setAttribute("pageTitle", "Shopping Cart - AbisheikMart 2.0");
                req.getRequestDispatcher("/WEB-INF/views/cart/cart.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("Authentication required.")));
            return;
        }

        try {
            CartRequest cartReq = JsonUtil.fromJson(req.getReader(), CartRequest.class);

            if ("/api/cart/add".equals(path)) {
                Cart cart = cartService.addToCart(user.getId(), cartReq.getProductId(), cartReq.getQuantity() != null ? cartReq.getQuantity() : 1);
                resp.setContentType("application/json");
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok("Added to cart", cart)));

            } else if ("/api/cart/update".equals(path)) {
                Cart cart = cartService.updateCartItemQuantity(user.getId(), cartReq.getProductId(), cartReq.getQuantity() != null ? cartReq.getQuantity() : 0);
                resp.setContentType("application/json");
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok("Cart updated", cart)));

            } else if ("/api/cart/remove".equals(path)) {
                Cart cart = cartService.removeFromCart(user.getId(), cartReq.getProductId());
                resp.setContentType("application/json");
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok("Item removed", cart)));
            }
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error(e.getMessage())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("Error updating cart: " + e.getMessage())));
        }
    }
}

