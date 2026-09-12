<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="activeNav" value="cart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="main-content" style="max-width: 700px; margin: 2rem auto;">
    <div style="margin-bottom: 1.5rem;">
        <h2>Order Checkout Summary</h2>
    </div>

    <div style="background: var(--glass-bg); backdrop-filter: blur(16px); border: 1px solid var(--glass-border); border-radius: 16px; padding: 1.5rem;">
        <div style="background: rgba(15, 23, 42, 0.6); border: 1px solid var(--glass-border); border-radius: 12px; padding: 1.2rem; margin-bottom: 1.2rem;">
            <div style="font-size: 0.88rem; color: var(--text-secondary); margin-bottom: 0.6rem;">Customer Information:</div>
            <div style="font-weight: 700; color: #fff;">${sessionScope.user.name}</div>
            <div style="font-size: 0.85rem; color: var(--text-muted);">${sessionScope.user.email}</div>
        </div>

        <div style="max-height: 250px; overflow-y: auto; margin-bottom: 1.2rem;">
            <c:forEach items="${cart.items}" var="i">
                <div style="display: flex; justify-content: space-between; padding: 0.6rem 0; border-bottom: 1px solid var(--glass-border); font-size: 0.9rem;">
                    <div>
                        <strong>${i.productName}</strong>
                        <div style="font-size: 0.8rem; color: var(--text-muted);">${i.quantity} x ₹<fmt:formatNumber value="${i.unitPrice}" pattern="#,##0.00" /></div>
                    </div>
                    <div style="font-weight: 700; color: var(--accent-green);">₹<fmt:formatNumber value="${i.subtotal}" pattern="#,##0.00" /></div>
                </div>
            </c:forEach>
        </div>

        <div style="display: flex; justify-content: space-between; font-size: 1.2rem; font-weight: 700; border-top: 1px solid var(--glass-border); padding-top: 1rem; margin-bottom: 1.5rem;">
            <span>Total Amount (Rupees INR):</span>
            <span style="color: var(--accent-green);">₹<fmt:formatNumber value="${cart.grandTotal}" pattern="#,##0.00" /></span>
        </div>

        <form action="${pageContext.request.contextPath}/checkout" method="POST">
            <button type="submit" class="btn btn-success" style="width: 100%; padding: 0.85rem; font-size: 1.05rem;">
                Confirm & Place Order
            </button>
        </form>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

