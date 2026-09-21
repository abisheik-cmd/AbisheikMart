<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="activeNav" value="catalog" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="main-content">
    <!-- Festive Event Special Banner -->
    <div class="event-banner">
        <div class="event-banner-content">
            <span class="event-tag">🔥 Grand Festive Sale — Up to 50% OFF</span>
            <h1 class="hero-title">Exclusive Indian Rupee (₹) Deals & Festive Offers</h1>
            <p class="hero-subtitle">Shop top categories with Instant Coupons, Mobile Verification, and Express Delivery.</p>
            <div class="hero-actions">
                <button class="btn btn-warning" onclick="openLuckyDrawModal()">🎁 Spin Lucky Draw Wheel</button>
                <button class="btn btn-primary" onclick="document.getElementById('catalog-grid').scrollIntoView({behavior: 'smooth'})">Browse All Products ↓</button>
            </div>
        </div>
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

    <!-- Suggested for You (Personalized Recommendations) -->
    <c:if test="${not empty recommendations}">
        <section class="recommendations-section" style="margin-bottom: 2.5rem;">
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem;">
                <h3 style="color: #fff; font-size: 1.3rem; display: flex; align-items: center; gap: 0.5rem;">
                    <span>🎯</span> Suggested for You
                </h3>
                <span style="font-size: 0.85rem; color: var(--accent-primary);">Based on your interests & top ratings</span>
            </div>
            <div class="recommendations-grid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 1.2rem;">
                <c:forEach items="${recommendations}" var="rec">
                    <div class="product-card rec-card" onclick="window.location.href='${pageContext.request.contextPath}/catalog/detail?id=${rec.id}'">
                        <div class="card-image-wrap" style="height: 150px;">
                            <img src="${pageContext.request.contextPath}/${rec.imageUrl != null && !rec.imageUrl.isEmpty() ? rec.imageUrl : 'images/keyboard.jpg'}" alt="${rec.name}">
                            <span class="card-badge-category">${rec.categoryName}</span>
                            <c:if test="${rec.hasDiscount()}">
                                <span class="card-badge-discount">-${rec.discountPercentage}% OFF</span>
                            </c:if>
                        </div>
                        <div class="card-body" style="padding: 1rem;">
                            <h4 class="card-title" style="font-size: 1rem; margin-bottom: 0.3rem;">${rec.name}</h4>
                            <div class="card-price">
                                ₹<fmt:formatNumber value="${rec.price}" pattern="#,##0.00" />
                                <c:if test="${rec.hasDiscount()}">
                                    <span class="mrp-price">₹<fmt:formatNumber value="${rec.originalPrice}" pattern="#,##0.00" /></span>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </section>
    </c:if>

    <!-- Discovery Header Bar -->
    <div class="discovery-header">
        <h2 class="discovery-title">
            <span>Explore Catalog</span>
            <span id="product-count-tag" style="font-size: 0.85rem; font-weight: 500; color: var(--text-secondary);">
                (${products != null ? products.size() : 0} items in ₹ INR)
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
                <option value="rating" ${selectedSort == 'rating' ? 'selected' : ''}>Top Customer Ratings</option>
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
                            <c:if test="${p.hasDiscount()}">
                                <span class="card-badge-discount">-${p.discountPercentage}% OFF</span>
                            </c:if>
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
                            <div class="card-seller">
                                Sold by: <strong>${p.sellerName}</strong>
                                <c:if test="${p.reviewCount > 0}">
                                    <span class="star-rating-pill">★ <fmt:formatNumber value="${p.averageRating}" pattern="0.0" /> (${p.reviewCount})</span>
                                </c:if>
                            </div>
                            <p class="card-desc">${p.description}</p>
                            <div class="card-footer">
                                <div class="card-price-group">
                                    <span class="card-price">₹<fmt:formatNumber value="${p.price}" pattern="#,##0.00" /></span>
                                    <c:if test="${p.hasDiscount()}">
                                        <span class="mrp-price">₹<fmt:formatNumber value="${p.originalPrice}" pattern="#,##0.00" /></span>
                                    </c:if>
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

<!-- Lucky Draw Wheel Modal Container -->
<div class="modal-backdrop" id="lucky-draw-modal" style="display: none;">
    <div class="modal-content lucky-modal">
        <button type="button" class="modal-close" onclick="closeLuckyDrawModal()">✕</button>
        <div style="text-align: center;">
            <h2 style="color: #f59e0b; margin-bottom: 0.3rem;">🎉 Festive Lucky Draw!</h2>
            <p style="color: var(--text-secondary); margin-bottom: 1rem;">Spin the wheel to win exclusive discount coupons!</p>
            <div class="wheel-container">
                <canvas id="wheel-canvas" width="300" height="300"></canvas>
                <div class="wheel-pointer">▼</div>
            </div>
            <button type="button" class="btn btn-warning btn-lg" id="spin-wheel-btn" onclick="spinLuckyWheel()">🌀 SPIN NOW!</button>
            <div id="lucky-result-box" style="margin-top: 1rem; display: none;"></div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
