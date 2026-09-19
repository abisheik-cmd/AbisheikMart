package com.abisheikmart.filter;

import com.abisheikmart.dto.ApiResponse;
import com.abisheikmart.model.User;
import com.abisheikmart.util.JsonUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class RoleAuthorizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        String uri = httpRequest.getRequestURI();
        boolean isSellerPath = uri.contains("/seller");
        boolean isAdminPath = uri.contains("/admin");

        if (user == null) {
            boolean isAjax = (httpRequest.getHeader("X-Requested-With") != null && "XMLHttpRequest".equals(httpRequest.getHeader("X-Requested-With")))
                    || (httpRequest.getHeader("Accept") != null && httpRequest.getHeader("Accept").contains("application/json"))
                    || uri.startsWith(httpRequest.getContextPath() + "/api/");
            if (isAjax) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(JsonUtil.toJson(ApiResponse.error("Authentication required. Please log in.")));
            } else {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login?redirect=" + uri);
            }
            return;
        }

        if (user != null) {
            String role = user.getRole();
            boolean isAllowed = false;

            if (isAdminPath && "ADMIN".equalsIgnoreCase(role)) {
                isAllowed = true;
            } else if (isSellerPath && "SELLER".equalsIgnoreCase(role)) {
                isAllowed = true;
            }

            if (!isAllowed) {
                String acceptHeader = httpRequest.getHeader("Accept");
                String requestedWith = httpRequest.getHeader("X-Requested-With");
                boolean isAjax = (requestedWith != null && "XMLHttpRequest".equals(requestedWith)) ||
                        (acceptHeader != null && acceptHeader.contains("application/json")) ||
                        uri.startsWith(httpRequest.getContextPath() + "/api/");

                if (isAjax) {
                    httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    httpResponse.setContentType("application/json");
                    httpResponse.getWriter().write(JsonUtil.toJson(ApiResponse.error("Forbidden: Insufficient privileges.")));
                } else {
                    httpResponse.sendRedirect(httpRequest.getContextPath() + "/index.jsp?error=access_denied");
                }
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
