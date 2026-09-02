let cartItemsCache = [];
let cartGrandTotal = 0.0;

function toggleCartDrawer(open = true) {
    if (!AppState.user) {
        showToast('Please log in to view your shopping cart.', 'info');
        showAuthLanding();
        return;
    }

    const drawer = document.getElementById('cart-drawer');
    const overlay = document.getElementById('cart-drawer-overlay');

    if (open) {
        loadCartDrawerItems();
        if (drawer) drawer.classList.add('active');
        if (overlay) overlay.classList.add('active');
    } else {
        if (drawer) drawer.classList.remove('active');
        if (overlay) overlay.classList.remove('active');
    }
}

async function loadCartDrawerItems() {
    const container = document.getElementById('cart-drawer-body');
    const totalEl = document.getElementById('cart-total-amount');
    if (!container) return;

    container.innerHTML = '<p style="color: var(--text-secondary); text-align: center; padding: 2rem;">Loading cart items...</p>';

    try {
        const data = await apiFetch('/api/cart');
        cartItemsCache = data.items || [];
        cartGrandTotal = data.total || 0.0;

        if (totalEl) totalEl.textContent = `₹${cartGrandTotal.toLocaleString('en-IN')}`;

        if (cartItemsCache.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 4rem 1rem;">
                    <div style="font-size: 3.5rem; margin-bottom: 0.8rem;">🛒</div>
                    <h4 style="color: #fff; margin-bottom: 0.5rem;">Your Cart is Empty</h4>
                    <p style="color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 1.5rem;">Browse our high-tech catalog and discover awesome items!</p>
                    <button class="btn btn-primary btn-sm" onclick="toggleCartDrawer(false); switchView('catalog')">Start Shopping</button>
                </div>
            `;
            return;
        }

        container.innerHTML = cartItemsCache.map(item => {
            const thumbSrc = getProductImage({ name: item.product_name, category: item.category });
            return `
                <div class="cart-item-card">
                    <img src="${thumbSrc}" class="cart-item-thumb" alt="${escapeHtml(item.product_name)}">
                    <div class="cart-item-info">
                        <div class="cart-item-title">${escapeHtml(item.product_name)}</div>
                        <div style="font-size: 0.78rem; color: var(--text-muted); margin-bottom: 0.2rem;">Seller: ${escapeHtml(item.seller_name)}</div>
                        <div class="cart-item-price">₹${item.unit_price.toLocaleString('en-IN')}</div>
                        <div class="cart-item-controls">
                            <button class="btn btn-secondary btn-sm" style="padding: 0.15rem 0.5rem;" onclick="handleQuantityChange(${item.product_id}, ${item.quantity - 1})">-</button>
                            <span style="font-weight: 700; font-size: 0.88rem; min-width: 18px; text-align: center;">${item.quantity}</span>
                            <button class="btn btn-secondary btn-sm" style="padding: 0.15rem 0.5rem;" onclick="handleQuantityChange(${item.product_id}, ${item.quantity + 1})" ${item.quantity >= item.available_stock ? 'disabled' : ''}>+</button>
                            <button class="btn btn-danger btn-sm" style="margin-left: auto; padding: 0.15rem 0.5rem;" onclick="handleRemoveCartItem(${item.product_id})">🗑️</button>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    } catch (err) {
        container.innerHTML = `<p style="color: var(--accent-rose);">Error loading cart: ${escapeHtml(err.message)}</p>`;
    }
}

async function handleQuantityChange(productId, newQty) {
    try {
        await apiFetch('/api/cart/update', {
            method: 'PUT',
            body: JSON.stringify({ product_id: productId, quantity: newQty })
        });
        loadCartDrawerItems();
        updateCartBadgeCount();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function handleRemoveCartItem(productId) {
    try {
        await apiFetch(`/api/cart/remove/${productId}`, { method: 'DELETE' });
        showToast('Item removed from cart.', 'info');
        loadCartDrawerItems();
        updateCartBadgeCount();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

/* Checkout Confirmation Modal */
function openCheckoutModal() {
    if (cartItemsCache.length === 0) {
        showToast('Your cart is empty! Add products before checking out.', 'info');
        return;
    }

    toggleCartDrawer(false);

    const modal = document.getElementById('checkout-modal');
    const content = document.getElementById('checkout-summary-content');

    if (content) {
        content.innerHTML = `
            <div style="background: rgba(15, 23, 42, 0.6); border: 1px solid var(--glass-border); border-radius: 12px; padding: 1.2rem; margin-bottom: 1.2rem;">
                <div style="font-size: 0.88rem; color: var(--text-secondary); margin-bottom: 0.6rem;">Customer Information:</div>
                <div style="font-weight: 700; color: #fff;">${escapeHtml(AppState.user.name)}</div>
                <div style="font-size: 0.85rem; color: var(--text-muted);">${escapeHtml(AppState.user.email)}</div>
            </div>

            <div style="max-height: 200px; overflow-y: auto; margin-bottom: 1.2rem;">
                ${cartItemsCache.map(i => `
                    <div style="display: flex; justify-content: space-between; padding: 0.6rem 0; border-bottom: 1px solid var(--glass-border); font-size: 0.9rem;">
                        <div>
                            <strong>${escapeHtml(i.product_name)}</strong>
                            <div style="font-size: 0.8rem; color: var(--text-muted);">${i.quantity} x ₹${i.unit_price.toLocaleString('en-IN')}</div>
                        </div>
                        <div style="font-weight: 700; color: var(--accent-green);">₹${i.subtotal.toLocaleString('en-IN')}</div>
                    </div>
                `).join('')}
            </div>

            <div style="display: flex; justify-content: space-between; font-size: 1.2rem; font-weight: 700; border-top: 1px solid var(--glass-border); padding-top: 1rem;">
                <span>Total Amount:</span>
                <span style="color: var(--accent-green);">₹${cartGrandTotal.toLocaleString('en-IN')}</span>
            </div>
        `;
    }

    if (modal) modal.classList.add('active');
}

function closeCheckoutModal() {
    const modal = document.getElementById('checkout-modal');
    if (modal) modal.classList.remove('active');
}

async function confirmCheckoutOrder() {
    try {
        const data = await apiFetch('/api/checkout', { method: 'POST' });
        closeCheckoutModal();
        showToast(`Order #${data.order.order_id} placed successfully! Total: ₹${data.order.total_amount.toLocaleString('en-IN')}`, 'success');
        updateCartBadgeCount();
        switchView('orders');
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function loadOrderHistory() {
    const container = document.getElementById('orders-container');
    if (!container) return;

    if (!AppState.user) {
        showAuthLanding();
        return;
    }

    container.innerHTML = '<p style="color: var(--text-secondary); text-align: center; padding: 3rem;">Loading order history...</p>';

    try {
        const data = await apiFetch('/api/orders');
        const orders = data.orders || [];

        if (orders.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 4rem 2rem; background: var(--glass-bg); border-radius: 16px; border: 1px solid var(--glass-border);">
                    <div style="font-size: 3.5rem; margin-bottom: 0.8rem;">📦</div>
                    <h3 style="color: #fff; margin-bottom: 0.5rem;">No Past Orders</h3>
                    <p style="color: var(--text-secondary);">You haven't placed any orders yet. Start exploring products in the marketplace!</p>
                </div>
            `;
            return;
        }

        container.innerHTML = orders.map(order => `
            <div style="background: var(--glass-bg); backdrop-filter: blur(16px); border: 1px solid var(--glass-border); border-radius: 14px; padding: 1.5rem; margin-bottom: 1.5rem;">
                <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--glass-border); padding-bottom: 0.9rem; margin-bottom: 1rem;">
                    <div>
                        <strong style="font-size: 1.1rem; color: #fff;">Order #${order.id}</strong>
                        <div style="font-size: 0.82rem; color: var(--text-muted);">${order.created_at}</div>
                    </div>
                    <div>
                        <span class="badge-role" style="background: rgba(2, 132, 199, 0.25); color: #38bdf8;">${order.status}</span>
                        <span style="font-size: 1.35rem; font-weight: 800; color: var(--accent-green); margin-left: 1rem;">₹${order.total_amount.toLocaleString('en-IN')}</span>
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
                        ${(order.items || []).map(item => `
                            <tr>
                                <td style="color: #fff; font-weight: 600;">${escapeHtml(item.product_name)}</td>
                                <td>${item.quantity}</td>
                                <td>₹${item.unit_price.toLocaleString('en-IN')}</td>
                                <td style="font-weight: 700; color: var(--accent-green);">₹${item.subtotal.toLocaleString('en-IN')}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `).join('');
    } catch (err) {
        container.innerHTML = `<p style="color: var(--accent-rose);">Error loading orders: ${escapeHtml(err.message)}</p>`;
    }
}
