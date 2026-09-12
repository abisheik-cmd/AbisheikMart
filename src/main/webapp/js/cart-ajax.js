/* AbisheikMart 2.0 Shopping Cart Async AJAX Handlers */

async function addToCartAjax(productId, quantity = 1) {
    try {
        const response = await fetch(getContextPath() + '/api/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify({ productId: productId, quantity: quantity })
        });

        const data = await response.json();
        if (response.ok && data.success) {
            showToast(data.message || 'Product added to cart!', 'success');
            updateCartBadgeCount();
        } else if (response.status === 401) {
            showToast('Please log in to add items to your cart.', 'info');
            setTimeout(() => {
                window.location.href = getContextPath() + '/login?redirect=' + encodeURIComponent(window.location.pathname);
            }, 1000);
        } else {
            showToast(data.message || 'Failed to add product to cart.', 'error');
        }
    } catch (err) {
        showToast('Network error: Could not reach server.', 'error');
    }
}

async function updateCartQuantityAjax(productId, newQuantity) {
    try {
        const response = await fetch(getContextPath() + '/api/cart/update', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify({ productId: productId, quantity: newQuantity })
        });

        const data = await response.json();
        if (response.ok && data.success) {
            showToast('Cart updated.', 'success');
            window.location.reload();
        } else {
            showToast(data.message || 'Failed to update cart.', 'error');
        }
    } catch (err) {
        showToast('Network error: Could not reach server.', 'error');
    }
}

async function removeFromCartAjax(productId) {
    if (!confirm('Are you sure you want to remove this item from your cart?')) return;

    try {
        const response = await fetch(getContextPath() + '/api/cart/remove', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify({ productId: productId })
        });

        const data = await response.json();
        if (response.ok && data.success) {
            showToast('Item removed from cart.', 'info');
            window.location.reload();
        } else {
            showToast(data.message || 'Failed to remove item.', 'error');
        }
    } catch (err) {
        showToast('Network error: Could not reach server.', 'error');
    }
}

async function updateCartBadgeCount() {
    try {
        const response = await fetch(getContextPath() + '/api/cart/count', {
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        });
        if (response.ok) {
            const data = await response.json();
            const badge = document.getElementById('cart-count');
            if (badge && data.data && data.data.count !== undefined) {
                badge.textContent = data.data.count;
            }
        }
    } catch (err) {}
}

function getContextPath() {
    return window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2)) || "";
}

