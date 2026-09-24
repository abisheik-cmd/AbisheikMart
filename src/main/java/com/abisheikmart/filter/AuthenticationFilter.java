package com.abisheikmart.filter;

import com.abisheikmart.dto.ApiResponse;
import com.abisheikmart.model.User;
import com.abisheikmart.util.JsonUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestUri = httpRequest.getRequestURI();
        if (requestUri == null) requestUri = "";
        boolean isAdminLogin = requestUri.equals(httpRequest.getContextPath() + "/admin/login");
        if (isAdminLogin) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            String acceptHeader = httpRequest.getHeader("Accept");
            String requestedWith = httpRequest.getHeader("X-Requested-With");
            boolean isAjax = (requestedWith != null && "XMLHttpRequest".equals(requestedWith)) ||
                    (acceptHeader != null && acceptHeader.contains("application/json")) ||
                    requestUri.startsWith(httpRequest.getContextPath() + "/api/");

            if (isAjax) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(JsonUtil.toJson(ApiResponse.error("Authentication required. Please log in.")));
            } else {
                String loginPath = requestUri.startsWith(httpRequest.getContextPath() + "/admin") ? "/admin/login" : "/login";
                httpResponse.sendRedirect(httpRequest.getContextPath() + loginPath + "?redirect=" + requestUri);
            }
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
