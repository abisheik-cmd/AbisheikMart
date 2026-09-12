<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="main-content" style="display: flex; justify-content: center; align-items: center; min-height: 80vh;">
    <div class="auth-portal-card" style="width: 100%; max-width: 440px;">
        <div class="auth-header">
            <img src="${pageContext.request.contextPath}/images/logo.jpg" alt="AbisheikMart Logo" style="height: 72px; width: 72px; border-radius: 16px; object-fit: cover; margin-bottom: 0.8rem; box-shadow: 0 0 20px rgba(139, 92, 246, 0.5);">
            <h1>AbisheikMart 2.0</h1>
            <p>Sign in to your e-commerce account</p>
        </div>

        <div class="auth-tabs">
            <a href="${pageContext.request.contextPath}/login" class="auth-tab-btn active" style="text-decoration: none; text-align: center;">Log In</a>
            <a href="${pageContext.request.contextPath}/register" class="auth-tab-btn" style="text-decoration: none; text-align: center;">Create Account</a>
        </div>

        <c:if test="${not empty errorMessage}">
            <div style="background: rgba(244, 63, 94, 0.15); border: 1px solid #f43f5e; color: #f43f5e; padding: 0.8rem 1rem; border-radius: 10px; margin-bottom: 1rem; font-size: 0.9rem;">
                ${errorMessage}
            </div>
        </c:if>

        <c:if test="${param.logout == 'success'}">
            <div style="background: rgba(34, 197, 94, 0.15); border: 1px solid #22c55e; color: #22c55e; padding: 0.8rem 1rem; border-radius: 10px; margin-bottom: 1rem; font-size: 0.9rem;">
                You have logged out successfully.
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="POST">
            <input type="hidden" name="redirect" value="${param.redirect}">

            <div class="form-group">
                <label for="login-email">Email Address</label>
                <input type="email" id="login-email" name="email" class="form-control" placeholder="admin@abishmart.com or buyer" required>
            </div>

            <div class="form-group">
                <label for="login-password">Password</label>
                <input type="password" id="login-password" name="password" class="form-control" placeholder="••••••••" required>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 1.2rem;">Log In to Marketplace</button>
        </form>

        <div style="margin-top: 1.5rem; text-align: center; font-size: 0.85rem; color: var(--text-muted);">
            Demo Accounts:<br>
            <strong>Admin</strong>: admin@abishmart.com / Admin@123<br>
            <strong>Seller</strong>: seller@abishmart.com / Seller@123
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

