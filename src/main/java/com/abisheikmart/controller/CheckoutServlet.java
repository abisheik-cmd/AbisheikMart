package com.abisheikmart.controller;

import com.abisheikmart.dto.ApiResponse;
import com.abisheikmart.model.Cart;
import com.abisheikmart.model.Order;
import com.abisheikmart.model.User;
import com.abisheikmart.service.CartService;
import com.abisheikmart.service.OrderService;
import com.abisheikmart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = {"/checkout", "/api/checkout"})
public class CheckoutServlet extends HttpServlet {

    private final CartService cartService = new CartService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
            return;
        }

        try {
            Cart cart = cartService.getOrCreateCart(user.getId());
            if (cart.getItems().isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/cart?error=empty_cart");
                return;
            }

            req.setAttribute("cart", cart);
            req.setAttribute("pageTitle", "Order Checkout - AbisheikMart 2.0");
            req.getRequestDispatcher("/WEB-INF/views/cart/checkout.jsp").forward(req, resp);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        boolean isApi = req.getServletPath().startsWith("/api/");

        try {
            Order order = orderService.checkout(user.getId());

            if (isApi) {
                resp.setContentType("application/json");
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok("Order placed successfully", order)));
            } else {
                resp.sendRedirect(req.getContextPath() + "/orders?checkout=success&orderId=" + order.getId());
            }
        } catch (IllegalArgumentException e) {
            if (isApi) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error(e.getMessage())));
            } else {
                req.setAttribute("errorMessage", e.getMessage());
                doGet(req, resp);
            }
        } catch (Exception e) {
            if (isApi) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.setContentType("application/json");
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("Checkout failed: " + e.getMessage())));
            } else {
                req.setAttribute("errorMessage", "Failed to place order: " + e.getMessage());
                doGet(req, resp);
            }
        }
    }
}

