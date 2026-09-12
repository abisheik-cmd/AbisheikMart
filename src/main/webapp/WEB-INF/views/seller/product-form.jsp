<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="activeNav" value="seller" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="main-content" style="max-width: 650px; margin: 2rem auto;">
    <div style="margin-bottom: 1rem;">
        <a href="${pageContext.request.contextPath}/seller/dashboard" style="color: var(--accent-cyan); text-decoration: none; font-size: 0.9rem;">← Back to Seller Dashboard</a>
    </div>

    <div class="modal-card" style="position: static; margin: 0; width: 100%;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
            <h3>${product != null ? 'Edit Product Listing' : 'Add New Product Listing'}</h3>
        </div>

        <c:if test="${not empty errorMessage}">
            <div style="background: rgba(244, 63, 94, 0.15); border: 1px solid #f43f5e; color: #f43f5e; padding: 0.8rem 1rem; border-radius: 10px; margin-bottom: 1rem; font-size: 0.9rem;">
                ${errorMessage}
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/seller/product/save" method="POST">
            <c:if test="${product != null}">
                <input type="hidden" name="id" value="${product.id}">
            </c:if>

            <div class="form-group">
                <label for="prod-name">Product Name</label>
                <input type="text" id="prod-name" name="name" class="form-control" value="${product != null ? product.name : ''}" placeholder="e.g. Wireless Mechanical Keyboard" required>
            </div>

            <div class="form-group">
                <label for="prod-desc">Description</label>
                <textarea id="prod-desc" name="description" class="form-control" rows="3" placeholder="Detailed product description...">${product != null ? product.description : ''}</textarea>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                <div class="form-group">
                    <label for="prod-price">Price (Rupees INR)</label>
                    <input type="number" id="prod-price" name="price" class="form-control" step="0.01" min="0" value="${product != null ? product.price : ''}" placeholder="4499" required>
                </div>
                <div class="form-group">
                    <label for="prod-stock">Stock Quantity</label>
                    <input type="number" id="prod-stock" name="stock" class="form-control" min="0" value="${product != null ? product.stock : ''}" placeholder="10" required>
                </div>
            </div>

            <div class="form-group">
                <label for="prod-category">Category</label>
                <select id="prod-category" name="categoryId" class="form-control">
                    <c:forEach items="${categories}" var="cat">
                        <option value="${cat.id}" ${product != null && product.categoryId == cat.id ? 'selected' : ''}>${cat.name}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label for="prod-image-preset">Product Image Asset</label>
                <select id="prod-image-preset" name="imageUrl" class="form-control">
                    <option value="images/keyboard.jpg" ${product != null && product.imageUrl == 'images/keyboard.jpg' ? 'selected' : ''}>Wireless Keyboard Asset</option>
                    <option value="images/headphones.jpg" ${product != null && product.imageUrl == 'images/headphones.jpg' ? 'selected' : ''}>Studio Headphones Asset</option>
                    <option value="images/smartwatch.jpg" ${product != null && product.imageUrl == 'images/smartwatch.jpg' ? 'selected' : ''}>Fitness Smartwatch Asset</option>
                    <option value="images/camera.jpg" ${product != null && product.imageUrl == 'images/camera.jpg' ? 'selected' : ''}>4K Vlog Camera Asset</option>
                    <option value="images/coffeemaker.jpg" ${product != null && product.imageUrl == 'images/coffeemaker.jpg' ? 'selected' : ''}>Espresso Maker Asset</option>
                    <option value="images/airfryer.jpg" ${product != null && product.imageUrl == 'images/airfryer.jpg' ? 'selected' : ''}>Air Fryer Asset</option>
                    <option value="images/sneakers.jpg" ${product != null && product.imageUrl == 'images/sneakers.jpg' ? 'selected' : ''}>Athletic Sneakers Asset</option>
                    <option value="images/jacket.jpg" ${product != null && product.imageUrl == 'images/jacket.jpg' ? 'selected' : ''}>Hiking Jacket Asset</option>
                    <option value="images/books.jpg" ${product != null && product.imageUrl == 'images/books.jpg' ? 'selected' : ''}>Software Books Asset</option>
                    <option value="images/dumbbell.jpg" ${product != null && product.imageUrl == 'images/dumbbell.jpg' ? 'selected' : ''}>Adjustable Dumbbell Asset</option>
                </select>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 1rem;">
                ${product != null ? 'Update Product Listing' : 'Save New Product'}
            </button>
        </form>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

