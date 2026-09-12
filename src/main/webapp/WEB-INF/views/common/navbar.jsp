<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<header class="navbar" id="main-navbar">
    <div class="brand" onclick="window.location.href='${pageContext.request.contextPath}/catalog'">
        <img src="${pageContext.request.contextPath}/images/logo.jpg" alt="AbisheikMart Logo" style="height: 38px; width: 38px; border-radius: 8px; object-fit: cover; margin-right: 0.5rem; box-shadow: 0 0 10px rgba(56, 189, 248, 0.4);"> AbisheikMart
    </div>

    <!-- Global Search Bar -->
    <form action="${pageContext.request.contextPath}/catalog" method="GET" class="nav-search" id="nav-search-container">
        <span class="nav-search-icon">🔍</span>
        <input type="text" name="q" id="global-search-input" value="${param.q}" placeholder="Search products, categories, sellers..." oninput="handleSearchDebounce(event)">
    </form>

    <nav class="nav-links">
        <a href="${pageContext.request.contextPath}/catalog" class="nav-item ${activeNav == 'catalog' ? 'active' : ''}">
            <span>🛍️</span> Browse
        </a>
        <a href="${pageContext.request.contextPath}/cart" class="nav-item cart-btn-trigger ${activeNav == 'cart' ? 'active' : ''}">
            <span>🛒</span> Cart <span class="cart-count-badge" id="cart-count">${cartItemCount != null ? cartItemCount : 0}</span>
        </a>
        <a href="${pageContext.request.contextPath}/orders" class="nav-item ${activeNav == 'orders' ? 'active' : ''}">
            <span>📦</span> Orders
        </a>

        <c:if test="${sessionScope.user != null && (sessionScope.user.role == 'SELLER' || sessionScope.user.role == 'ADMIN')}">
            <a href="${pageContext.request.contextPath}/seller/dashboard" class="nav-item ${activeNav == 'seller' ? 'active' : ''}">
                <span>📊</span> Seller Dashboard
            </a>
        </c:if>

        <div id="nav-user-container" style="display: flex; align-items: center; gap: 0.8rem;">
            <c:choose>
                <c:when test="${sessionScope.user != null}">
                    <span class="badge-role role-${sessionScope.user.role.toLowerCase()}">${sessionScope.user.role}</span>
                    <span style="font-weight: 600; color: #fff;">${sessionScope.user.name}</span>
                    <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary btn-sm">Log Out</a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/login" class="btn btn-primary btn-sm">Log In</a>
                    <a href="${pageContext.request.contextPath}/register" class="btn btn-secondary btn-sm">Register</a>
                </c:otherwise>
            </c:choose>
        </div>
    </nav>
</header>

