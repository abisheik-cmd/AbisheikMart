package com.abisheikmart.controller;

import com.abisheikmart.dto.ApiResponse;
import com.abisheikmart.model.Order;
import com.abisheikmart.model.User;
import com.abisheikmart.service.OrderService;
import com.abisheikmart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/orders", "/api/orders"})
public class OrderServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login?redirect=/orders");
            return;
        }

        List<Order> orders = orderService.getOrderHistory(user.getId());

        if (req.getServletPath().startsWith("/api/")) {
            resp.setContentType("application/json");
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok(orders)));
        } else {
            req.setAttribute("orders", orders);
            req.setAttribute("pageTitle", "Order History - AbisheikMart 2.0");
            req.getRequestDispatcher("/WEB-INF/views/order/order-history.jsp").forward(req, resp);
        }
    }
}

