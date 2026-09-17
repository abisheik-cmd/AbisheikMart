<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="activeNav" value="seller" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="main-content">
    <div style="display:flex; justify-content:space-between; align-items:center; gap:1rem; flex-wrap:wrap; margin-bottom:2rem;">
        <div><h2 style="font-size:1.8rem; font-weight:800; color:#fff;">Seller Orders</h2><p style="color:var(--text-secondary);">Orders containing products owned by your seller account.</p></div>
        <a href="${pageContext.request.contextPath}/seller/dashboard" class="btn btn-secondary" style="text-decoration:none;">← Dashboard</a>
    </div>
    <c:if test="${not empty errorMessage}"><div style="background:rgba(244,63,94,.15); border:1px solid #f43f5e; color:#f43f5e; padding:.8rem 1rem; border-radius:10px; margin-bottom:1rem;">${errorMessage}</div></c:if>
    <c:choose>
        <c:when test="${not empty orders}">
            <div style="display:grid; gap:1rem;">
                <c:forEach items="${orders}" var="line">
                    <div style="background:var(--glass-bg); border:1px solid var(--glass-border); border-radius:var(--radius-md); padding:1.2rem;">
                        <div style="display:flex; justify-content:space-between; gap:1rem; flex-wrap:wrap; margin-bottom:.65rem;">
                            <strong style="color:#fff;">Order #${line.orderId} · ${line.productName}</strong>
                            <span class="card-badge-stock low-stock" style="position:static;">${line.status}</span>
                        </div>
                        <p style="color:var(--text-secondary); margin:.35rem 0;">${line.quantity} × ₹<fmt:formatNumber value="${line.unitPrice}" pattern="#,##0.00" /> = ₹<fmt:formatNumber value="${line.subtotal}" pattern="#,##0.00" /></p>
                        <p style="color:var(--text-secondary); margin:.35rem 0;">Buyer: ${line.buyerName} · ${line.buyerEmail} · ${line.phoneNumber}<br>Delivery: ${line.deliveryAddress}</p>
                        <form method="post" action="${pageContext.request.contextPath}/seller/order/status" style="display:flex; align-items:center; gap:.6rem; max-width:440px; margin-top:.9rem;">
                            <input type="hidden" name="orderId" value="${line.orderId}">
                            <label for="status-${line.orderId}-${line.productId}" style="color:var(--text-muted);">Fulfillment</label>
                            <select id="status-${line.orderId}-${line.productId}" name="status" class="form-control" style="flex:1;">
                                <option value="${line.status}" selected>${line.status}</option>
                                <c:choose><c:when test="${line.status eq 'PLACED'}"><option value="SHIPPED">SHIPPED</option></c:when><c:when test="${line.status eq 'SHIPPED'}"><option value="DELIVERED">DELIVERED</option></c:when></c:choose>
                            </select>
                            <button type="submit" class="btn btn-primary">Update</button>
                        </form>
                    </div>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise><div style="text-align:center; padding:4rem 2rem; background:var(--glass-bg); border-radius:16px; border:1px solid var(--glass-border);"><h3 style="color:#fff;">No Seller Orders Yet</h3><p style="color:var(--text-secondary);">Orders containing your products will appear here.</p></div></c:otherwise>
    </c:choose>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
