<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="activeNav" value="orders" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="main-content" style="max-width: 900px; margin: 2rem auto;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
        <h2>Past Order History</h2>
    </div>

    <c:choose>
        <c:when test="${not empty orders}">
            <c:forEach items="${orders}" var="order">
                <div style="background: var(--glass-bg); backdrop-filter: blur(16px); border: 1px solid var(--glass-border); border-radius: 14px; padding: 1.5rem; margin-bottom: 1.5rem;">
                    <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--glass-border); padding-bottom: 0.9rem; margin-bottom: 1rem;">
                        <div>
                            <strong style="font-size: 1.1rem; color: #fff;">Order #${order.id}</strong>
                            <div style="font-size: 0.82rem; color: var(--text-muted);">${order.createdAt}</div>
                        </div>
                        <div>
                            <span class="badge-role" style="background: rgba(2, 132, 199, 0.25); color: #38bdf8;">${order.status}</span>
                            <span style="font-size: 1.35rem; font-weight: 800; color: var(--accent-green); margin-left: 1rem;">
                                ₹<fmt:formatNumber value="${order.totalAmount}" pattern="#,##0.00" />
                            </span>
                        </div>
                    </div>

                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Item Purchased</th>
                                <th>Quantity</th>
                                <th>Unit Price</th>
                                <th>Subtotal</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${order.items}" var="item">
                                <tr>
                                    <td style="color: #fff; font-weight: 600;">${item.productName}</td>
                                    <td>${item.quantity}</td>
                                    <td>₹<fmt:formatNumber value="${item.unitPrice}" pattern="#,##0.00" /></td>
                                    <td style="font-weight: 700; color: var(--accent-green);">₹<fmt:formatNumber value="${item.subtotal}" pattern="#,##0.00" /></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <div style="text-align: center; padding: 4rem 2rem; background: var(--glass-bg); border-radius: 16px; border: 1px solid var(--glass-border);">
                <div style="font-size: 3.5rem; margin-bottom: 0.8rem;">📦</div>
                <h3 style="color: #fff; margin-bottom: 0.5rem;">No Past Orders</h3>
                <p style="color: var(--text-secondary);">You haven't placed any orders yet. Start exploring products in the marketplace!</p>
                <a href="${pageContext.request.contextPath}/catalog" class="btn btn-primary btn-sm" style="margin-top: 1rem; text-decoration: none;">Browse Catalog</a>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

