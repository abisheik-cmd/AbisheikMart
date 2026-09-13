<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="activeNav" value="catalog" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="main-content" style="max-width: 920px; margin: 2rem auto;">
    <div style="margin-bottom: 1rem;">
        <a href="${pageContext.request.contextPath}/catalog" style="color: var(--accent-cyan); text-decoration: none; font-size: 0.9rem;">← Back to Product Catalog</a>
    </div>

    <c:choose>
        <c:when test="${not empty product}">
            <!-- Product Detail Card -->
            <div class="modal-card product-detail-modal" style="position: static; margin: 0; width: 100%;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
                    <span class="card-badge-category">${product.categoryName}</span>
                    <span style="font-size: 0.85rem; color: var(--text-muted);">Listed: ${product.createdAt}</span>
                </div>

                <div class="product-detail-grid">
                    <div class="product-detail-image-wrap">
                        <img src="${pageContext.request.contextPath}/${product.imageUrl != null && !product.imageUrl.isEmpty() ? product.imageUrl : 'images/keyboard.jpg'}" alt="${product.name}">
                    </div>

                    <div style="display: flex; flex-direction: column; justify-content: space-between;">
                        <div>
                            <h1 style="font-size: 1.8rem; margin-bottom: 0.5rem; color: #fff;">${product.name}</h1>
                            <div class="card-seller" style="font-size: 0.95rem; margin-bottom: 0.6rem;">
                                Sold by: <strong>${product.sellerName}</strong>
                                <c:if test="${product.reviewCount > 0}">
                                    <span class="star-rating-pill" style="margin-left: 0.7rem;">★ <fmt:formatNumber value="${product.averageRating}" pattern="0.0" /> (${product.reviewCount} reviews)</span>
                                </c:if>
                            </div>
                            <p style="color: var(--text-secondary); line-height: 1.6; font-size: 0.98rem; margin-bottom: 1.8rem;">${product.description}</p>
                        </div>

                        <div>
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                                <div>
                                    <span style="font-size: 2rem; font-weight: 800; color: var(--accent-green);">
                                        ₹<fmt:formatNumber value="${product.price}" pattern="#,##0.00" />
                                    </span>
                                    <c:if test="${product.hasDiscount()}">
                                        <span class="mrp-price" style="margin-left: 0.8rem; font-size: 1rem;">₹<fmt:formatNumber value="${product.originalPrice}" pattern="#,##0.00" /></span>
                                        <span class="card-badge-discount" style="position: static; margin-left: 0.5rem;">-${product.discountPercentage}% OFF</span>
                                    </c:if>
                                </div>
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

                            <div style="display: flex; gap: 0.8rem;">
                                <button type="button" class="btn btn-primary" style="flex: 1; padding: 0.9rem; font-size: 1.05rem;"
                                        onclick="addToCartAjax(${product.id}, 1)"
                                        ${product.stock <= 0 ? 'disabled style="opacity: 0.5; cursor: not-allowed;"' : ''}>
                                    🛒 Add to Cart
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Reviews Section -->
            <div style="margin-top: 2.5rem;">
                <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.2rem;">
                    <h2 style="font-size: 1.35rem; font-weight: 800; color: #fff;">⭐ Customer Reviews <span style="font-size: 0.9rem; color: var(--text-muted); font-weight: 400;">(${reviews != null ? reviews.size() : 0})</span></h2>
                    <c:if test="${sessionScope.user != null}">
                        <button type="button" class="btn btn-secondary btn-sm" onclick="toggleReviewForm()">Write a Review</button>
                    </c:if>
                </div>

                <!-- Write Review Form (hidden by default, shown for logged-in users) -->
                <c:if test="${sessionScope.user != null}">
                    <div id="review-form-container" style="display: none; background: var(--glass-bg); border: 1px solid var(--glass-border); border-radius: 14px; padding: 1.5rem; margin-bottom: 1.5rem;">
                        <h4 style="margin-bottom: 1rem; color: var(--accent-cyan);">Your Review</h4>
                        <div style="margin-bottom: 1rem;">
                            <label style="display: block; font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 0.4rem;">Rating *</label>
                            <div id="star-input-row" style="display: flex; gap: 0.3rem; font-size: 1.8rem; cursor: pointer;">
                                <span data-val="1" onclick="setReviewRating(1)" class="star-inp">☆</span>
                                <span data-val="2" onclick="setReviewRating(2)" class="star-inp">☆</span>
                                <span data-val="3" onclick="setReviewRating(3)" class="star-inp">☆</span>
                                <span data-val="4" onclick="setReviewRating(4)" class="star-inp">☆</span>
                                <span data-val="5" onclick="setReviewRating(5)" class="star-inp">☆</span>
                            </div>
                        </div>
                        <div style="margin-bottom: 1rem;">
                            <label style="display: block; font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 0.4rem;">Comment *</label>
                            <textarea id="review-comment" rows="3" placeholder="Share your experience with this product..."
                                      style="width: 100%; background: rgba(15,23,42,0.7); border: 1px solid var(--glass-border); border-radius: 10px; padding: 0.7rem 1rem; color: #fff; font-size: 0.9rem; resize: vertical;"></textarea>
                        </div>
                        <button type="button" class="btn btn-primary" onclick="submitProductReview(${product.id})">Submit Review</button>
                    </div>
                </c:if>

                <!-- Reviews List -->
                <c:choose>
                    <c:when test="${not empty reviews}">
                        <c:forEach items="${reviews}" var="review">
                            <div style="background: var(--glass-bg); border: 1px solid var(--glass-border); border-radius: 12px; padding: 1.2rem; margin-bottom: 1rem;">
                                <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 0.6rem;">
                                    <div style="display: flex; align-items: center; gap: 0.8rem;">
                                        <div style="width: 36px; height: 36px; border-radius: 50%; background: linear-gradient(135deg, var(--accent-cyan), var(--accent-purple)); display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 0.9rem;">
                                            ${review.userName.substring(0,1).toUpperCase()}
                                        </div>
                                        <div>
                                            <div style="font-weight: 600; color: #fff;">${review.userName}</div>
                                            <div style="font-size: 0.78rem; color: var(--text-muted);">${review.createdAt}</div>
                                        </div>
                                    </div>
                                    <div style="color: var(--accent-amber); font-size: 1.1rem;">
                                        <c:forEach begin="1" end="${review.rating}" var="s">★</c:forEach><c:forEach begin="${review.rating + 1}" end="5" var="s">☆</c:forEach>
                                    </div>
                                </div>
                                <p style="color: var(--text-secondary); font-size: 0.92rem; line-height: 1.5;">${review.comment}</p>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div style="text-align: center; padding: 2.5rem 1.5rem; background: var(--glass-bg); border: 1px solid var(--glass-border); border-radius: 14px;">
                            <div style="font-size: 2.5rem; margin-bottom: 0.6rem;">💬</div>
                            <p style="color: var(--text-secondary);">No reviews yet. Be the first to share your thoughts!</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
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
