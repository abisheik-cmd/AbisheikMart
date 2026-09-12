<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="activeNav" value="catalog" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="main-content">
    <!-- Hero Banner -->
    <div class="hero-banner">
        <span class="hero-badge">⚡ Featured Showcase</span>
        <h1 class="hero-title">Next-Gen Tech & Premium Lifestyle Gear</h1>
        <p class="hero-subtitle">Discover top-rated products from verified multi-seller stores on AbisheikMart 2.0.</p>
        <button class="btn btn-primary" onclick="document.getElementById('catalog-grid').scrollIntoView({behavior: 'smooth'})">Explore Products ↓</button>
    </div>

    <!-- Category Chips Bar -->
    <div class="category-chips-bar" id="category-chips-container">
        <a href="${pageContext.request.contextPath}/catalog" class="category-chip ${empty selectedCategory || selectedCategory == 'ALL' ? 'active' : ''}">✨ All Items</a>
        <c:forEach items="${categories}" var="cat">
            <a href="${pageContext.request.contextPath}/catalog?category=${cat.name}" class="category-chip ${selectedCategory == cat.name ? 'active' : ''}">
                ${cat.name}
            </a>
        </c:forEach>
    </div>

    <!-- Discovery Header Bar -->
    <div class="discovery-header">
        <h2 class="discovery-title">
            <span>Explore Products</span>
            <span id="product-count-tag" style="font-size: 0.85rem; font-weight: 500; color: var(--text-secondary);">
                (${products != null ? products.size() : 0} items available)
            </span>
        </h2>

        <form action="${pageContext.request.contextPath}/catalog" method="GET" style="display: flex; align-items: center; gap: 0.8rem;">
            <c:if test="${not empty param.category}">
                <input type="hidden" name="category" value="${param.category}">
            </c:if>
            <c:if test="${not empty param.q}">
                <input type="hidden" name="q" value="${param.q}">
            </c:if>

            <label for="sort-select" style="font-size: 0.85rem; color: var(--text-secondary);">Sort By:</label>
            <select id="sort-select" name="sort" class="sort-select" onchange="this.form.submit()">
                <option value="newest" ${empty selectedSort || selectedSort == 'newest' ? 'selected' : ''}>Newest Arrivals</option>
                <option value="price-low" ${selectedSort == 'price-low' ? 'selected' : ''}>Price: Low to High</option>
                <option value="price-high" ${selectedSort == 'price-high' ? 'selected' : ''}>Price: High to Low</option>
                <option value="name" ${selectedSort == 'name' ? 'selected' : ''}>Name: A to Z</option>
            </select>
        </form>
    </div>

    <!-- Product Grid -->
    <div id="catalog-grid" class="product-grid">
        <c:choose>
            <c:when test="${not empty products}">
                <c:forEach items="${products}" var="p">
                    <div class="product-card" onclick="window.location.href='${pageContext.request.contextPath}/catalog/detail?id=${p.id}'">
                        <div class="card-image-wrap">
                            <img src="${pageContext.request.contextPath}/${p.imageUrl != null && !p.imageUrl.isEmpty() ? p.imageUrl : 'images/keyboard.jpg'}" alt="${p.name}" loading="lazy">
                            <span class="card-badge-category">${p.categoryName}</span>
                            <c:choose>
                                <c:when test="${p.stock > 5}">
                                    <span class="card-badge-stock in-stock">${p.stock} in stock</span>
                                </c:when>
                                <c:when test="${p.stock > 0}">
                                    <span class="card-badge-stock low-stock">Only ${p.stock} left!</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="card-badge-stock out-of-stock">Out of Stock</span>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="card-body">
                            <h3 class="card-title">${p.name}</h3>
                            <div class="card-seller">Sold by: <strong>${p.sellerName}</strong></div>
                            <p class="card-desc">${p.description}</p>
                            <div class="card-footer">
                                <div class="card-price">
                                    ₹<fmt:formatNumber value="${p.price}" pattern="#,##0.00" />
                                </div>
                                <button type="button" class="btn btn-primary btn-sm" onclick="event.stopPropagation(); addToCartAjax(${p.id}, 1)" ${p.stock <= 0 ? 'disabled style="opacity: 0.5; cursor: not-allowed;"' : ''}>
                                    🛒 Add to Cart
                                </button>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div style="grid-column: 1/-1; text-align: center; padding: 4rem 2rem; background: var(--glass-bg); border-radius: 16px; border: 1px solid var(--glass-border);">
                    <div style="font-size: 3rem; margin-bottom: 0.8rem;">🔍</div>
                    <h3 style="color: #fff; margin-bottom: 0.5rem;">No Products Found</h3>
                    <p style="color: var(--text-secondary);">Try adjusting your search query or selecting a different category filter.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

