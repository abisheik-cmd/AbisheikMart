package com.abisheikmart.filter;

import com.abisheikmart.model.User;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

class RoleAuthorizationFilterTest {
    private final RoleAuthorizationFilter filter = new RoleAuthorizationFilter();

    @Test
    void unauthenticatedAdminApiRequestIsRejectedEvenIfRoleFilterRunsFirst() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class); HttpServletResponse response = mock(HttpServletResponse.class); FilterChain chain = mock(FilterChain.class);
        when(request.getSession(false)).thenReturn(null); when(request.getContextPath()).thenReturn(""); when(request.getRequestURI()).thenReturn("/api/admin/users"); when(request.getHeader("Accept")).thenReturn("application/json");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        filter.doFilter(request, response, chain);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED); verifyNoInteractions(chain);
    }

    @Test
    void customerCannotAccessSellerRoute() throws Exception {
        HttpServletRequest request = requestFor("/seller/products", "CUSTOMER"); HttpServletResponse response = mock(HttpServletResponse.class); FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);
        verify(response).sendRedirect(contains("access_denied")); verifyNoInteractions(chain);
    }

    @Test
    void sellerAndAdminCanAccessTheirRoutes() throws Exception {
        HttpServletResponse sellerResponse = mock(HttpServletResponse.class); FilterChain sellerChain = mock(FilterChain.class); filter.doFilter(requestFor("/seller/dashboard", "SELLER"), sellerResponse, sellerChain); verify(sellerChain).doFilter(any(), eq(sellerResponse));
        HttpServletResponse adminResponse = mock(HttpServletResponse.class); FilterChain adminChain = mock(FilterChain.class); filter.doFilter(requestFor("/admin", "ADMIN"), adminResponse, adminChain); verify(adminChain).doFilter(any(), eq(adminResponse));
    }

    private HttpServletRequest requestFor(String uri, String role) {
        HttpServletRequest request = mock(HttpServletRequest.class); HttpSession session = mock(HttpSession.class); User user = new User(); user.setRole(role);
        when(request.getSession(false)).thenReturn(session); when(session.getAttribute("user")).thenReturn(user); when(request.getRequestURI()).thenReturn(uri); when(request.getContextPath()).thenReturn(""); when(request.getHeader("Accept")).thenReturn("text/html"); return request;
    }
}
