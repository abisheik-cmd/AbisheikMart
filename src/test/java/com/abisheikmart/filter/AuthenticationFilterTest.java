package com.abisheikmart.filter;

import com.abisheikmart.model.User;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

class AuthenticationFilterTest {
    @Test
    void unauthenticatedApiRequestIsRejected() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter(); HttpServletRequest request = mock(HttpServletRequest.class); HttpServletResponse response = mock(HttpServletResponse.class); FilterChain chain = mock(FilterChain.class);
        when(request.getSession(false)).thenReturn(null); when(request.getContextPath()).thenReturn(""); when(request.getRequestURI()).thenReturn("/api/cart"); when(request.getHeader("Accept")).thenReturn("application/json");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        filter.doFilter(request, response, chain);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED); verifyNoInteractions(chain);
    }

    @Test
    void authenticatedRequestContinues() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter(); HttpServletRequest request = mock(HttpServletRequest.class); HttpServletResponse response = mock(HttpServletResponse.class); FilterChain chain = mock(FilterChain.class); HttpSession session = mock(HttpSession.class); User user = new User(); user.setRole("CUSTOMER");
        when(request.getSession(false)).thenReturn(session); when(session.getAttribute("user")).thenReturn(user);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    void adminLoginPageIsReachableWithoutAuthentication() throws Exception {
        AuthenticationFilter filter = new AuthenticationFilter(); HttpServletRequest request = mock(HttpServletRequest.class); HttpServletResponse response = mock(HttpServletResponse.class); FilterChain chain = mock(FilterChain.class);
        when(request.getContextPath()).thenReturn(""); when(request.getRequestURI()).thenReturn("/admin/login");
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }
}
