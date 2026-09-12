<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="activeNav" value="cart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="main-content" style="max-width: 900px; margin: 2rem auto;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
        <h2>🛒 Shopping Cart Summary</h2>
        <a href="${pageContext.request.contextPath}/catalog" class="btn btn-secondary btn-sm">Continue Shopping</a>
    </div>

    <c:choose>
        <c:when test="${not empty cart && not empty cart.items}">
            <div style="background: var(--glass-bg); backdrop-filter: blur(16px); border: 1px solid var(--glass-border); border-radius: 16px; padding: 1.5rem;">
                <div style="display: flex; flex-direction: column; gap: 1rem; margin-bottom: 1.5rem;">
                    <c:forEach items="${cart.items}" var="item">
                        <div class="cart-item-card" id="cart-item-${item.productId}">
                            <img src="${pageContext.request.contextPath}/${item.imageUrl != null && !item.imageUrl.isEmpty() ? item.imageUrl : 'images/keyboard.jpg'}" class="cart-item-thumb" alt="${item.productName}">
                            <div class="cart-item-info">
                                <div class="cart-item-title">${item.productName}</div>
                                <div style="font-size: 0.78rem; color: var(--text-muted); margin-bottom: 0.2rem;">Seller: ${item.sellerName}</div>
                                <div class="cart-item-price">₹<fmt:formatNumber value="${item.unitPrice}" pattern="#,##0.00" /></div>
                                <div class="cart-item-controls">
                                    <button type="button" class="btn btn-secondary btn-sm" style="padding: 0.15rem 0.5rem;" onclick="updateCartQuantityAjax(${item.productId}, ${item.quantity - 1})">-</button>
                                    <span style="font-weight: 700; font-size: 0.88rem; min-width: 18px; text-align: center;">${item.quantity}</span>
                                    <button type="button" class="btn btn-secondary btn-sm" style="padding: 0.15rem 0.5rem;" onclick="updateCartQuantityAjax(${item.productId}, ${item.quantity + 1})" ${item.quantity >= item.availableStock ? 'disabled' : ''}>+</button>
                                    <button type="button" class="btn btn-danger btn-sm" style="margin-left: auto; padding: 0.15rem 0.5rem;" onclick="removeFromCartAjax(${item.productId})">🗑️</button>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <div class="cart-total-row" style="border-top: 1px solid var(--glass-border); padding-top: 1.2rem; margin-bottom: 1.5rem;">
                    <span style="font-size: 1.2rem; font-weight: 700;">Grand Total (Rupees INR):</span>
                    <span class="cart-total-amount" style="font-size: 1.5rem; font-weight: 800; color: var(--accent-green);">
                        ₹<fmt:formatNumber value="${cart.grandTotal}" pattern="#,##0.00" />
                    </span>
                </div>

                <a href="${pageContext.request.contextPath}/checkout" class="btn btn-success" style="display: block; text-align: center; width: 100%; padding: 0.9rem; font-size: 1.05rem; text-decoration: none;">
                    Proceed to Checkout
                </a>
            </div>
        </c:when>
        <c:otherwise>
            <div style="text-align: center; padding: 4rem 1rem; background: var(--glass-bg); border-radius: 16px; border: 1px solid var(--glass-border);">
                <div style="font-size: 3.5rem; margin-bottom: 0.8rem;">🛒</div>
                <h3 style="color: #fff; margin-bottom: 0.5rem;">Your Cart is Empty</h3>
                <p style="color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 1.5rem;">Browse our high-tech catalog and discover awesome items!</p>
                <a href="${pageContext.request.contextPath}/catalog" class="btn btn-primary btn-sm" style="text-decoration: none;">Start Shopping</a>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

