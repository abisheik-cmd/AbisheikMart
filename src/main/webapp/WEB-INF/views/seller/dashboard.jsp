<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="activeNav" value="seller" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="main-content">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
        <div>
            <h2 style="font-size: 1.8rem; font-weight: 800; color: #fff;">Seller Product Dashboard</h2>
            <p style="color: var(--text-secondary); font-size: 0.95rem;">Manage inventory and catalog listings for <strong style="color: var(--accent-cyan);">${sessionScope.user.name}</strong></p>
        </div>
        <a href="${pageContext.request.contextPath}/seller/product/new" class="btn btn-success" style="text-decoration: none;">+ Add New Product</a>
    </div>

    <!-- Stats Bar -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.2rem; margin-bottom: 2rem;">
        <div style="background: var(--glass-bg); border: 1px solid var(--glass-border); padding: 1.4rem; border-radius: var(--radius-md);">
            <div style="color: var(--text-muted); font-size: 0.82rem; font-weight: 700; text-transform: uppercase;">Active Listings</div>
            <div style="font-size: 2rem; font-weight: 800; color: #fff; margin-top: 0.4rem;">${totalListings != null ? totalListings : 0}</div>
        </div>
        <div style="background: var(--glass-bg); border: 1px solid var(--glass-border); padding: 1.4rem; border-radius: var(--radius-md);">
            <div style="color: var(--text-muted); font-size: 0.82rem; font-weight: 700; text-transform: uppercase;">Total Inventory Units</div>
            <div style="font-size: 2rem; font-weight: 800; color: var(--accent-cyan); margin-top: 0.4rem;">${totalInventory != null ? totalInventory : 0}</div>
        </div>
        <div style="background: var(--glass-bg); border: 1px solid var(--glass-border); padding: 1.4rem; border-radius: var(--radius-md);">
            <div style="color: var(--text-muted); font-size: 0.82rem; font-weight: 700; text-transform: uppercase;">Low Stock Items</div>
            <div style="font-size: 2rem; font-weight: 800; color: var(--accent-amber); margin-top: 0.4rem;">${lowStockCount != null ? lowStockCount : 0}</div>
        </div>
    </div>

    <c:choose>
        <c:when test="${not empty products}">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Product</th>
                        <th>Category</th>
                        <th>Price (Rupees INR)</th>
                        <th>Stock Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${products}" var="p">
                        <tr id="seller-prod-${p.id}">
                            <td>
                                <div style="display: flex; align-items: center; gap: 1rem;">
                                    <img src="${pageContext.request.contextPath}/${p.imageUrl != null && !p.imageUrl.isEmpty() ? p.imageUrl : 'images/keyboard.jpg'}" style="width: 50px; height: 50px; border-radius: 8px; object-fit: cover; background: #0f172a;" alt="${p.name}">
                                    <div>
                                        <strong style="color: #fff; font-size: 1rem;">${p.name}</strong>
                                        <div style="font-size: 0.8rem; color: var(--text-muted);">${p.description}</div>
                                    </div>
                                </div>
                            </td>
                            <td><span class="card-badge-category" style="position: static;">${p.categoryName}</span></td>
                            <td style="font-weight: 700; color: var(--accent-green); font-size: 1.05rem;">
                                ₹<fmt:formatNumber value="${p.price}" pattern="#,##0.00" />
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${p.stock > 5}">
                                        <span class="card-badge-stock in-stock" style="position: static;">${p.stock} units</span>
                                    </c:when>
                                    <c:when test="${p.stock > 0}">
                                        <span class="card-badge-stock low-stock" style="position: static;">${p.stock} units</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="card-badge-stock out-of-stock" style="position: static;">0 units</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <div style="display: flex; gap: 0.5rem;">
                                    <a href="${pageContext.request.contextPath}/seller/product/edit?id=${p.id}" class="btn btn-secondary btn-sm" style="text-decoration: none;">Edit</a>
                                    <button type="button" class="btn btn-danger btn-sm" onclick="deleteProductSellerAjax(${p.id})">Delete</button>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <div style="text-align: center; padding: 4rem 2rem; background: var(--glass-bg); border-radius: 16px; border: 1px solid var(--glass-border);">
                <div style="font-size: 3.5rem; margin-bottom: 0.8rem;">📦</div>
                <h3 style="color: #fff; margin-bottom: 0.5rem;">No Products Listed Yet</h3>
                <p style="color: var(--text-secondary); margin-bottom: 1.5rem;">You haven't listed any items in your store yet.</p>
                <a href="${pageContext.request.contextPath}/seller/product/new" class="btn btn-primary" style="text-decoration: none;">Add Your First Product</a>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

