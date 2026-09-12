<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="main-content" style="display: flex; justify-content: center; align-items: center; min-height: 80vh;">
    <div class="auth-portal-card" style="width: 100%; max-width: 440px;">
        <div class="auth-header">
            <img src="${pageContext.request.contextPath}/images/logo.jpg" alt="AbisheikMart Logo" style="height: 72px; width: 72px; border-radius: 16px; object-fit: cover; margin-bottom: 0.8rem; box-shadow: 0 0 20px rgba(139, 92, 246, 0.5);">
            <h1>AbisheikMart 2.0</h1>
            <p>Create your marketplace account</p>
        </div>

        <div class="auth-tabs">
            <a href="${pageContext.request.contextPath}/login" class="auth-tab-btn" style="text-decoration: none; text-align: center;">Log In</a>
            <a href="${pageContext.request.contextPath}/register" class="auth-tab-btn active" style="text-decoration: none; text-align: center;">Create Account</a>
        </div>

        <c:if test="${not empty errorMessage}">
            <div style="background: rgba(244, 63, 94, 0.15); border: 1px solid #f43f5e; color: #f43f5e; padding: 0.8rem 1rem; border-radius: 10px; margin-bottom: 1rem; font-size: 0.9rem;">
                ${errorMessage}
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/register" method="POST">
            <div class="form-group">
                <label for="reg-name">Full Name</label>
                <input type="text" id="reg-name" name="name" class="form-control" placeholder="Jane Doe" required>
            </div>

            <div class="form-group">
                <label for="reg-email">Email Address</label>
                <input type="email" id="reg-email" name="email" class="form-control" placeholder="jane@example.com" required>
            </div>

            <div class="form-group">
                <label for="reg-password">Password (Min 6 chars)</label>
                <input type="password" id="reg-password" name="password" class="form-control" placeholder="••••••••" minlength="6" required>
            </div>

            <div class="form-group">
                <label for="reg-role">Account Type</label>
                <select id="reg-role" name="role" class="form-control">
                    <option value="CUSTOMER">Buyer (Shop for products)</option>
                    <option value="SELLER">Seller (List & sell items)</option>
                </select>
            </div>

            <button type="submit" class="btn btn-success" style="width: 100%; margin-top: 1.2rem;">Create Account</button>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

