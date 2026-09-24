<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="main-content" style="display:flex;justify-content:center;align-items:center;min-height:80vh;">
    <div class="auth-portal-card" style="width:100%;max-width:440px;">
        <div class="auth-header">
            <img src="${pageContext.request.contextPath}/images/logo.jpg" alt="AbisheikMart Logo" style="height:72px;width:72px;border-radius:16px;object-fit:cover;margin-bottom:.8rem;">
            <h1>Admin Portal</h1>
            <p>Sign in with your provisioned administrator account.</p>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="admin-login-error" role="alert">${errorMessage}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/admin/login" method="post">
            <input type="hidden" name="redirect" value="${param.redirect}">
            <div class="form-group">
                <label for="admin-email">Administrator email</label>
                <input type="email" id="admin-email" name="email" class="form-control" autocomplete="username" required>
            </div>
            <div class="form-group">
                <label for="admin-password">Password</label>
                <input type="password" id="admin-password" name="password" class="form-control" autocomplete="current-password" required>
            </div>
            <button type="submit" class="btn btn-primary" style="width:100%;margin-top:1.2rem;">Sign in to Admin</button>
        </form>

        <p style="margin-top:1.5rem;text-align:center;font-size:.85rem;color:var(--text-muted);">
            Admin accounts are provisioned through controlled application initialization and cannot be created through public registration.
        </p>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
