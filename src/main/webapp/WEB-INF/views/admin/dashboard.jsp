<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="activeNav" value="admin" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" /><jsp:include page="/WEB-INF/views/common/navbar.jsp" />
<main class="main-content">
<h2 style="color:#fff; margin-bottom:.4rem;">Admin Dashboard</h2><p style="color:var(--text-secondary); margin-bottom:1.5rem;">Marketplace administration backed by H2.</p>
<div style="display:grid; grid-template-columns:repeat(auto-fit,minmax(190px,1fr)); gap:1rem;">
<div class="modal-card" style="position:static;width:auto;margin:0;"><small>Total Users</small><h2>${stats.totalUsers}</h2><a href="${pageContext.request.contextPath}/admin/users">Manage users →</a></div>
<div class="modal-card" style="position:static;width:auto;margin:0;"><small>Active Users</small><h2>${stats.activeUsers}</h2><span class="card-badge-stock in-stock" style="position:static;">Buyers ${stats.buyers} · Sellers ${stats.sellers}</span></div>
<div class="modal-card" style="position:static;width:auto;margin:0;"><small>Products</small><h2>${stats.totalProducts}</h2><span class="card-badge-stock in-stock" style="position:static;">Active ${stats.activeProducts}</span> <span class="card-badge-stock out-of-stock" style="position:static;">Inactive ${stats.inactiveProducts}</span></div>
<div class="modal-card" style="position:static;width:auto;margin:0;"><small>Orders</small><h2>${stats.totalOrders}</h2><span class="card-badge-stock low-stock" style="position:static;">Pending ${stats.pendingOrders} · Delivered ${stats.deliveredOrders}</span></div>
<div class="modal-card" style="position:static;width:auto;margin:0;"><small>Marketplace Order Value</small><h2>₹<fmt:formatNumber value="${stats.marketplaceOrderValue}" pattern="#,##0.00" /></h2><a href="${pageContext.request.contextPath}/admin/orders">Review orders →</a></div>
</div>
<div style="display:flex; gap:.8rem; flex-wrap:wrap; margin-top:2rem;"><a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/users">User Management</a><a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/products">Product Management</a><a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/orders">Order Management</a></div>
</main><jsp:include page="/WEB-INF/views/common/footer.jsp" />
