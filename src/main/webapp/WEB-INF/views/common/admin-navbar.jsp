<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<header class="navbar admin-navbar" id="admin-navbar">
    <div class="brand" onclick="window.location.href='${pageContext.request.contextPath}/admin/dashboard'">
        <img src="${pageContext.request.contextPath}/images/logo.jpg" alt="AbisheikMart Logo" style="height: 38px; width: 38px; border-radius: 8px; object-fit: cover; margin-right: 0.5rem; box-shadow: 0 0 10px rgba(56, 189, 248, 0.4);"> AbisheikMart
    </div>

    <nav class="nav-links" aria-label="Admin navigation">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-item ${activeNav == 'admin' ? 'active' : ''}">
            Dashboard
        </a>
        <a href="${pageContext.request.contextPath}/admin/users" class="nav-item">
            Users
        </a>
        <a href="${pageContext.request.contextPath}/admin/products" class="nav-item">
            Products
        </a>
        <a href="${pageContext.request.contextPath}/admin/orders" class="nav-item">
            Orders
        </a>
        <a href="${pageContext.request.contextPath}/admin/dashboard#admin-statistics" class="nav-item">
            Reports / Statistics
        </a>

        <div id="admin-nav-user-container" style="display: flex; align-items: center; gap: 0.8rem;">
            <span class="badge-role role-admin">ADMIN</span>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary btn-sm">Log Out</a>
        </div>
    </nav>
</header>
