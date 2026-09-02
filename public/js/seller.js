let sellerProductsCache = [];
let editingProductId = null;

async function loadSellerDashboard() {
    const container = document.getElementById('seller-container');
    if (!container) return;

    if (!AppState.user || (AppState.user.role !== 'SELLER' && AppState.user.role !== 'ADMIN')) {
        container.innerHTML = `
            <div style="text-align: center; padding: 4rem 2rem; background: var(--glass-bg); border-radius: 16px; border: 1px solid var(--glass-border);">
                <div style="font-size: 3.5rem; margin-bottom: 0.8rem;">🔒</div>
                <h3 style="color: #fff; margin-bottom: 0.5rem;">Access Restricted</h3>
                <p style="color: var(--text-secondary); margin-bottom: 1.5rem;">Only registered Seller or Admin accounts can access the Seller Dashboard.</p>
            </div>
        `;
        return;
    }

    try {
        const data = await apiFetch('/api/seller/products');
        sellerProductsCache = data.products || [];

        const totalItems = sellerProductsCache.length;
        const totalStock = sellerProductsCache.reduce((sum, p) => sum + p.stock, 0);
        const lowStockCount = sellerProductsCache.filter(p => p.stock > 0 && p.stock <= 5).length;

        container.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
                <div>
                    <h2 style="font-size: 1.8rem; font-weight: 800; color: #fff;">Seller Product Dashboard</h2>
                    <p style="color: var(--text-secondary); font-size: 0.95rem;">Manage inventory and catalog listings for <strong style="color: var(--accent-cyan);">${escapeHtml(AppState.user.name)}</strong></p>
                </div>
                <button class="btn btn-success" onclick="openProductModal()">+ Add New Product</button>
            </div>

            <!-- Stats Bar -->
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.2rem; margin-bottom: 2rem;">
                <div style="background: var(--glass-bg); border: 1px solid var(--glass-border); padding: 1.4rem; border-radius: var(--radius-md);">
                    <div style="color: var(--text-muted); font-size: 0.82rem; font-weight: 700; text-transform: uppercase;">Active Listings</div>
                    <div style="font-size: 2rem; font-weight: 800; color: #fff; margin-top: 0.4rem;">${totalItems}</div>
                </div>
                <div style="background: var(--glass-bg); border: 1px solid var(--glass-border); padding: 1.4rem; border-radius: var(--radius-md);">
                    <div style="color: var(--text-muted); font-size: 0.82rem; font-weight: 700; text-transform: uppercase;">Total Inventory Units</div>
                    <div style="font-size: 2rem; font-weight: 800; color: var(--accent-cyan); margin-top: 0.4rem;">${totalStock}</div>
                </div>
                <div style="background: var(--glass-bg); border: 1px solid var(--glass-border); padding: 1.4rem; border-radius: var(--radius-md);">
                    <div style="color: var(--text-muted); font-size: 0.82rem; font-weight: 700; text-transform: uppercase;">Low Stock Items</div>
                    <div style="font-size: 2rem; font-weight: 800; color: var(--accent-amber); margin-top: 0.4rem;">${lowStockCount}</div>
                </div>
            </div>

            ${sellerProductsCache.length === 0 ? `
                <div style="text-align: center; padding: 4rem 2rem; background: var(--glass-bg); border-radius: 16px; border: 1px solid var(--glass-border);">
                    <div style="font-size: 3.5rem; margin-bottom: 0.8rem;">📦</div>
                    <h3 style="color: #fff; margin-bottom: 0.5rem;">No Products Listed Yet</h3>
                    <p style="color: var(--text-secondary); margin-bottom: 1.5rem;">You haven't listed any items in your store yet.</p>
                    <button class="btn btn-primary" onclick="openProductModal()">Add Your First Product</button>
                </div>
            ` : `
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Product</th>
                            <th>Category</th>
                            <th>Price</th>
                            <th>Stock Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${sellerProductsCache.map(p => {
                            const imgSrc = getProductImage(p);
                            const stockClass = p.stock > 5 ? 'in-stock' : (p.stock > 0 ? 'low-stock' : 'out-of-stock');
                            return `
                                <tr>
                                    <td>
                                        <div style="display: flex; align-items: center; gap: 1rem;">
                                            <img src="${imgSrc}" style="width: 50px; height: 50px; border-radius: 8px; object-fit: cover; background: #0f172a;" alt="${escapeHtml(p.name)}">
                                            <div>
                                                <strong style="color: #fff; font-size: 1rem;">${escapeHtml(p.name)}</strong>
                                                <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(p.description || '')}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td><span class="card-badge-category" style="position: static;">${escapeHtml(p.category || 'General')}</span></td>
                                    <td style="font-weight: 700; color: var(--accent-green); font-size: 1.05rem;">₹${p.price.toLocaleString('en-IN')}</td>
                                    <td>
                                        <span class="card-badge-stock ${stockClass}" style="position: static;">
                                            ${p.stock} units
                                        </span>
                                    </td>
                                    <td>
                                        <div style="display: flex; gap: 0.5rem;">
                                            <button class="btn btn-secondary btn-sm" onclick="openProductModal(${p.id})">Edit</button>
                                            <button class="btn btn-danger btn-sm" onclick="handleDeleteProductClick(${p.id})">Delete</button>
                                        </div>
                                    </td>
                                </tr>
                            `;
                        }).join('')}
                    </tbody>
                </table>
            `}
        `;
    } catch (err) {
        container.innerHTML = `<p style="color: var(--accent-rose);">Error loading seller dashboard: ${escapeHtml(err.message)}</p>`;
    }
}

function openProductModal(productId = null) {
    editingProductId = productId;
    const modal = document.getElementById('product-modal');
    const title = document.getElementById('product-modal-title');
    
    const nameInput = document.getElementById('prod-name');
    const descInput = document.getElementById('prod-desc');
    const priceInput = document.getElementById('prod-price');
    const stockInput = document.getElementById('prod-stock');
    const catInput = document.getElementById('prod-category');
    const imgInput = document.getElementById('prod-image-preset');

    if (productId) {
        title.textContent = 'Edit Product Listing';
        const product = sellerProductsCache.find(p => p.id === productId);
        if (product) {
            nameInput.value = product.name;
            descInput.value = product.description || '';
            priceInput.value = product.price;
            stockInput.value = product.stock;
            catInput.value = product.category || 'General';
            if (imgInput) imgInput.value = product.image_url || getProductImage(product);
        }
    } else {
        title.textContent = 'Add New Product Listing';
        nameInput.value = '';
        descInput.value = '';
        priceInput.value = '';
        stockInput.value = '';
        catInput.value = 'Electronics';
    }

    if (modal) modal.classList.add('active');
}

function closeProductModal() {
    const modal = document.getElementById('product-modal');
    if (modal) modal.classList.remove('active');
    editingProductId = null;
}

async function handleSaveProductSubmit(event) {
    event.preventDefault();
    const name = document.getElementById('prod-name').value.trim();
    const description = document.getElementById('prod-desc').value.trim();
    const price = parseFloat(document.getElementById('prod-price').value);
    const stock = parseInt(document.getElementById('prod-stock').value, 10);
    const category = document.getElementById('prod-category').value;
    const imageUrl = document.getElementById('prod-image-preset').value;

    if (!name || isNaN(price) || isNaN(stock)) {
        showToast('Please provide a valid product name, price, and stock quantity.', 'error');
        return;
    }

    try {
        if (editingProductId) {
            // Update Product
            await apiFetch(`/api/seller/products/${editingProductId}`, {
                method: 'PUT',
                body: JSON.stringify({ name, description, price, stock, category, image_url: imageUrl })
            });
            showToast('Product updated successfully!', 'success');
        } else {
            // Add Product
            await apiFetch('/api/seller/products', {
                method: 'POST',
                body: JSON.stringify({ name, description, price, stock, category, image_url: imageUrl })
            });
            showToast('New product added to your catalog!', 'success');
        }

        closeProductModal();
        loadSellerDashboard();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function handleDeleteProductClick(productId) {
    if (!confirm('Are you sure you want to delete this product listing? This action cannot be undone.')) return;

    try {
        await apiFetch(`/api/seller/products/${productId}`, {
            method: 'DELETE'
        });
        showToast('Product deleted successfully.', 'info');
        loadSellerDashboard();
    } catch (err) {
        showToast(err.message, 'error');
    }
}
