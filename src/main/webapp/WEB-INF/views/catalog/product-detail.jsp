<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="activeNav" value="catalog" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="main-content" style="max-width: 950px; margin: 2rem auto;">
    <div style="margin-bottom: 1rem;">
        <a href="${pageContext.request.contextPath}/catalog" style="color: var(--accent-cyan); text-decoration: none; font-size: 0.9rem;">← Back to Product Catalog</a>
    </div>

    <c:choose>
        <c:when test="${not empty product}">
            <div class="modal-card product-detail-modal" style="position: static; margin: 0; width: 100%;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
                    <span class="card-badge-category">${product.categoryName}</span>
                    <c:if test="${product.reviewCount > 0}">
                        <span class="star-rating-pill" style="font-size: 1rem; padding: 0.3rem 0.8rem;">
                            ★ <fmt:formatNumber value="${product.averageRating}" pattern="0.0" /> (${product.reviewCount} Reviews)
                        </span>
                    </c:if>
                </div>
                
                <div class="product-detail-grid">
                    <div class="product-detail-image-wrap">
                        <img src="${pageContext.request.contextPath}/${product.imageUrl != null && !product.imageUrl.isEmpty() ? product.imageUrl : 'images/keyboard.jpg'}" alt="${product.name}">
                        <c:if test="${product.hasDiscount()}">
                            <span class="card-badge-discount" style="top: 15px; left: 15px; font-size: 0.95rem; padding: 0.4rem 0.8rem;">
                                Save ${product.discountPercentage}% OFF
                            </span>
                        </c:if>
                    </div>
                    
                    <div style="display: flex; flex-direction: column; justify-content: space-between;">
                        <div>
                            <h1 style="font-size: 1.8rem; margin-bottom: 0.5rem; color: #fff;">${product.name}</h1>
                            <div class="card-seller" style="font-size: 0.95rem; margin-bottom: 1.2rem;">Sold by: <strong>${product.sellerName}</strong></div>
                            <p style="color: var(--text-secondary); line-height: 1.6; font-size: 0.98rem; margin-bottom: 1.8rem;">${product.description}</p>
                        </div>

                        <div>
                            <div style="display: flex; align-items: baseline; gap: 1rem; margin-bottom: 1.5rem;">
                                <span style="font-size: 2.2rem; font-weight: 800; color: var(--accent-green);">
                                    ₹<fmt:formatNumber value="${product.price}" pattern="#,##0.00" />
                                </span>
                                <c:if test="${product.hasDiscount()}">
                                    <span class="mrp-price" style="font-size: 1.2rem;">
                                        M.R.P. ₹<fmt:formatNumber value="${product.originalPrice}" pattern="#,##0.00" />
                                    </span>
                                </c:if>
                            </div>

                            <div style="margin-bottom: 1.5rem;">
                                <c:choose>
                                    <c:when test="${product.stock > 5}">
                                        <span class="card-badge-stock in-stock" style="position: static;">${product.stock} Units Available</span>
                                    </c:when>
                                    <c:when test="${product.stock > 0}">
                                        <span class="card-badge-stock low-stock" style="position: static;">Low Stock (${product.stock} left)</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="card-badge-stock out-of-stock" style="position: static;">Out of Stock</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <button type="button" class="btn btn-primary" style="width: 100%; padding: 0.9rem; font-size: 1.05rem;" onclick="addToCartAjax(${product.id}, 1)" ${product.stock <= 0 ? 'disabled style="opacity: 0.5; cursor: not-allowed;"' : ''}>
                                🛒 Add to Cart
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Customer Reviews Section -->
            <section style="margin-top: 2.5rem; background: var(--glass-bg); padding: 2rem; border-radius: 16px; border: 1px solid var(--glass-border);">
                <h2 style="color: #fff; font-size: 1.4rem; margin-bottom: 1.5rem; display: flex; align-items: center; justify-content: space-between;">
                    <span>⭐ Customer Reviews & Ratings</span>
                    <span style="font-size: 0.9rem; color: var(--accent-cyan);">Verified Purchases</span>
                </h2>

                <!-- Add Review Form -->
                <c:if test="${sessionScope.user != null}">
                    <div style="background: rgba(255,255,255,0.03); padding: 1.5rem; border-radius: 12px; margin-bottom: 2rem; border: 1px solid rgba(255,255,255,0.08);">
                        <h4 style="color: #fff; margin-bottom: 1rem;">Write a Customer Review</h4>
                        <form id="review-form" onsubmit="submitReviewAjax(event, ${product.id})">
                            <div style="display: flex; gap: 1rem; align-items: center; margin-bottom: 1rem;">
                                <label style="color: var(--text-secondary);">Rating:</label>
                                <select id="review-rating" class="form-control" style="width: 120px;">
                                    <option value="5">★★★★★ (5)</option>
                                    <option value="4">★★★★☆ (4)</option>
                                    <option value="3">★★★☆☆ (3)</option>
                                    <option value="2">★★☆☆☆ (2)</option>
                                    <option value="1">★☆☆☆☆ (1)</option>
                                </select>
                            </div>
                            <div style="margin-bottom: 1rem;">
                                <textarea id="review-comment" class="form-control" rows="3" placeholder="Share your experience with this product..." required></textarea>
                            </div>
                            <div style="margin-bottom: 1rem;">
                                <input type="url" id="review-image" class="form-control" placeholder="Review Picture URL (Optional, e.g., images/keyboard.jpg)">
                            </div>
                            <button type="submit" class="btn btn-primary btn-sm">Submit Review</button>
                        </form>
                    </div>
                </c:if>

                <!-- Reviews List -->
                <div id="reviews-list">
                    <c:choose>
                        <c:when test="${not empty reviews}">
                            <c:forEach items="${reviews}" var="r">
                                <div class="review-item" style="border-bottom: 1px solid rgba(255,255,255,0.08); padding-bottom: 1.2rem; margin-bottom: 1.2rem;">
                                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                                        <div style="display: flex; align-items: center; gap: 0.6rem;">
                                            <span style="font-weight: 600; color: #fff;">${r.userName}</span>
                                            <span style="color: #f59e0b; font-size: 0.9rem;">
                                                <c:forEach begin="1" end="${r.rating}">★</c:forEach>
                                            </span>
                                        </div>
                                        <span style="font-size: 0.8rem; color: var(--text-muted);">${r.createdAt}</span>
                                    </div>
                                    <p style="color: var(--text-secondary); font-size: 0.95rem; margin-bottom: 0.8rem;">${r.comment}</p>
                                    <c:if test="${not empty r.imageUrl}">
                                        <div style="margin-top: 0.5rem;">
                                            <img src="${pageContext.request.contextPath}/${r.imageUrl}" alt="Review picture" style="height: 80px; width: 80px; border-radius: 8px; object-fit: cover; border: 1px solid var(--glass-border);">
                                        </div>
                                    </c:if>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <p style="color: var(--text-secondary); text-align: center; padding: 1.5rem 0;">No reviews yet for this product. Be the first to share your review!</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </section>
        </c:when>
        <c:otherwise>
            <div style="text-align: center; padding: 4rem 2rem; background: var(--glass-bg); border-radius: 16px;">
                <h2>Product Not Found</h2>
                <p style="color: var(--text-secondary);">The requested product listing does not exist or has been removed.</p>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
