package com.abisheikmart.controller;

import com.abisheikmart.dto.ApiResponse;
import com.abisheikmart.dto.LoginRequest;
import com.abisheikmart.dto.RegisterRequest;
import com.abisheikmart.model.User;
import com.abisheikmart.service.AuthService;
import com.abisheikmart.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = {"/login", "/register", "/api/auth/login", "/api/auth/register"})
public class AuthServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/login".equals(path)) {
            req.setAttribute("pageTitle", "Log In - AbisheikMart 2.0");
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
        } else if ("/register".equals(path)) {
            req.setAttribute("pageTitle", "Create Account - AbisheikMart 2.0");
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        boolean isApi = path.startsWith("/api/");

        try {
            if ("/login".equals(path) || "/api/auth/login".equals(path)) {
                LoginRequest loginReq;
                if (isApi) {
                    loginReq = JsonUtil.fromJson(req.getReader(), LoginRequest.class);
                } else {
                    loginReq = new LoginRequest();
                    loginReq.setEmail(req.getParameter("email"));
                    loginReq.setPassword(req.getParameter("password"));
                }

                User user = authService.login(loginReq);
                HttpSession session = req.getSession(true);
                session.setAttribute("user", user);

                if (isApi) {
                    resp.setContentType("application/json");
                    resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok("Login successful", Map.of("user", user))));
                } else {
                    String redirect = req.getParameter("redirect");
                    if (redirect != null && !redirect.isBlank() && !redirect.contains("login")) {
                        resp.sendRedirect(redirect);
                    } else if ("SELLER".equalsIgnoreCase(user.getRole())) {
                        resp.sendRedirect(req.getContextPath() + "/seller/dashboard");
                    } else {
                        resp.sendRedirect(req.getContextPath() + "/catalog");
                    }
                }

            } else if ("/register".equals(path) || "/api/auth/register".equals(path)) {
                RegisterRequest regReq;
                if (isApi) {
                    regReq = JsonUtil.fromJson(req.getReader(), RegisterRequest.class);
                } else {
                    regReq = new RegisterRequest();
                    regReq.setName(req.getParameter("name"));
                    regReq.setEmail(req.getParameter("email"));
                    regReq.setPassword(req.getParameter("password"));
                    regReq.setRole(req.getParameter("role"));
                }

                User user = authService.register(regReq);
                HttpSession session = req.getSession(true);
                session.setAttribute("user", user);

                if (isApi) {
                    resp.setContentType("application/json");
                    resp.setStatus(HttpServletResponse.SC_CREATED);
                    resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok("Registration successful", Map.of("user", user))));
                } else {
                    if ("SELLER".equalsIgnoreCase(user.getRole())) {
                        resp.sendRedirect(req.getContextPath() + "/seller/dashboard");
                    } else {
                        resp.sendRedirect(req.getContextPath() + "/catalog");
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            if (isApi) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error(e.getMessage())));
            } else {
                req.setAttribute("errorMessage", e.getMessage());
                if ("/login".equals(path)) {
                    req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
                } else {
                    req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
                }
            }
        } catch (Exception e) {
            if (isApi) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.setContentType("application/json");
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("Server error: " + e.getMessage())));
            } else {
                req.setAttribute("errorMessage", "An unexpected error occurred. Please try again.");
                req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
            }
        }
    }
}

