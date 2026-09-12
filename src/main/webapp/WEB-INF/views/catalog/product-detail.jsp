<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="activeNav" value="catalog" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="main-content" style="max-width: 900px; margin: 2rem auto;">
    <div style="margin-bottom: 1rem;">
        <a href="${pageContext.request.contextPath}/catalog" style="color: var(--accent-cyan); text-decoration: none; font-size: 0.9rem;">← Back to Product Catalog</a>
    </div>

    <c:choose>
        <c:when test="${not empty product}">
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
                            <div class="card-seller" style="font-size: 0.95rem; margin-bottom: 1.2rem;">Sold by: <strong>${product.sellerName}</strong></div>
                            <p style="color: var(--text-secondary); line-height: 1.6; font-size: 0.98rem; margin-bottom: 1.8rem;">${product.description}</p>
                        </div>

                        <div>
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
                                <span style="font-size: 2rem; font-weight: 800; color: var(--accent-green);">
                                    ₹<fmt:formatNumber value="${product.price}" pattern="#,##0.00" />
                                </span>
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

