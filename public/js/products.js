let cachedProducts = [];
let selectedDetailProduct = null;

function getProductImage(p) {
    if (p.image_url && p.image_url.trim() !== '') {
        return p.image_url;
    }

    const nameLower = (p.name || '').toLowerCase();
    const catLower = (p.category || '').toLowerCase();

    if (nameLower.includes('keyboard')) return 'assets/products/keyboard.jpg';
    if (nameLower.includes('headphone') || nameLower.includes('audio')) return 'assets/products/headphones.jpg';
    if (nameLower.includes('watch') || nameLower.includes('smart')) return 'assets/products/smartwatch.jpg';
    if (nameLower.includes('coffee') || nameLower.includes('maker') || nameLower.includes('espresso')) return 'assets/products/coffeemaker.jpg';
    if (nameLower.includes('sneaker') || nameLower.includes('shoe')) return 'assets/products/sneakers.jpg';

    if (catLower.includes('fashion') || catLower.includes('apparel')) return 'assets/products/sneakers.jpg';
    if (catLower.includes('home') || catLower.includes('kitchen')) return 'assets/products/coffeemaker.jpg';
    
    // Default high quality product image
    return 'assets/products/keyboard.jpg';
}

async function loadProductCatalog() {
    const grid = document.getElementById('catalog-grid');
    if (!grid) return;

    grid.innerHTML = '<p style="color: var(--text-secondary); grid-column: 1/-1; text-align: center; padding: 3rem;">Loading product catalog...</p>';

    try {
        const data = await apiFetch('/api/products');
        cachedProducts = data.products || [];

        renderFilteredCatalog();
    } catch (err) {
        grid.innerHTML = `<p style="color: var(--accent-rose); grid-column: 1/-1;">Error loading products: ${escapeHtml(err.message)}</p>`;
    }
}

function renderFilteredCatalog() {
    const grid = document.getElementById('catalog-grid');
    if (!grid) return;

    let filtered = [...cachedProducts];

    // Category Filter
    if (AppState.selectedCategory && AppState.selectedCategory !== 'ALL') {
        filtered = filtered.filter(p => (p.category || '').toLowerCase() === AppState.selectedCategory.toLowerCase());
    }

    // Search Query Filter
    if (AppState.searchQuery && AppState.searchQuery.trim() !== '') {
        const q = AppState.searchQuery.toLowerCase();
        filtered = filtered.filter(p => 
            (p.name || '').toLowerCase().includes(q) ||
            (p.description || '').toLowerCase().includes(q) ||
            (p.category || '').toLowerCase().includes(q) ||
            (p.seller_name || '').toLowerCase().includes(q)
        );
    }

    // Sorting
    if (AppState.selectedSort === 'price-low') {
        filtered.sort((a, b) => a.price - b.price);
    } else if (AppState.selectedSort === 'price-high') {
        filtered.sort((a, b) => b.price - a.price);
    } else if (AppState.selectedSort === 'name') {
        filtered.sort((a, b) => a.name.localeCompare(b.name));
    } else {
        // newest (default ID desc)
        filtered.sort((a, b) => b.id - a.id);
    }

    const countTag = document.getElementById('product-count-tag');
    if (countTag) {
        countTag.textContent = `(${filtered.length} ${filtered.length === 1 ? 'item' : 'items'} available)`;
    }

    if (filtered.length === 0) {
        grid.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; padding: 4rem 2rem; background: var(--glass-bg); border-radius: 16px; border: 1px solid var(--glass-border);">
                <div style="font-size: 3rem; margin-bottom: 0.8rem;">🔍</div>
                <h3 style="color: #fff; margin-bottom: 0.5rem;">No Products Found</h3>
                <p style="color: var(--text-secondary);">Try adjusting your search query or selecting a different category filter.</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = filtered.map(p => {
        const imgSrc = getProductImage(p);
        const stockClass = p.stock > 5 ? 'in-stock' : (p.stock > 0 ? 'low-stock' : 'out-of-stock');
        const stockText = p.stock > 5 ? `${p.stock} in stock` : (p.stock > 0 ? `Only ${p.stock} left!` : 'Out of Stock');

        return `
            <div class="product-card" onclick="openProductDetailModal(${p.id})">
                <div class="card-image-wrap">
                    <img src="${imgSrc}" alt="${escapeHtml(p.name)}" loading="lazy">
                    <span class="card-badge-category">${escapeHtml(p.category || 'General')}</span>
                    <span class="card-badge-stock ${stockClass}">${stockText}</span>
                </div>
                <div class="card-body">
                    <h3 class="card-title">${escapeHtml(p.name)}</h3>
                    <div class="card-seller">Sold by: <strong>${escapeHtml(p.seller_name || 'Seller #' + p.seller_id)}</strong></div>
                    <p class="card-desc">${escapeHtml(p.description || 'No detailed description available.')}</p>
                    <div class="card-footer">
                        <div class="card-price">$${p.price.toFixed(2)}</div>
                        <button class="btn btn-primary btn-sm" onclick="event.stopPropagation(); handleAddToCartClick(${p.id})" ${p.stock <= 0 ? 'disabled style="opacity: 0.5; cursor: not-allowed;"' : ''}>
                            🛒 Add to Cart
                        </button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function filterByCategory(category) {
    AppState.selectedCategory = category;
    
    // Update Active Chips UI
    const chips = document.querySelectorAll('.category-chip');
    chips.forEach(chip => {
        if (chip.textContent.toLowerCase().includes(category.toLowerCase()) || (category === 'ALL' && chip.textContent.includes('All'))) {
            chip.classList.add('active');
        } else {
            chip.classList.remove('active');
        }
    });

    renderFilteredCatalog();
}

function handleSearchInput(event) {
    AppState.searchQuery = event.target.value;
    renderFilteredCatalog();
}

function handleSortChange(event) {
    AppState.selectedSort = event.target.value;
    renderFilteredCatalog();
}

/* Product Details Glass Modal */
function openProductDetailModal(productId) {
    const product = cachedProducts.find(p => p.id === productId);
    if (!product) return;

    selectedDetailProduct = product;
    const modal = document.getElementById('product-detail-modal');
    
    document.getElementById('detail-img').src = getProductImage(product);
    document.getElementById('detail-title').textContent = product.name;
    document.getElementById('detail-category-badge').textContent = product.category || 'General';
    document.getElementById('detail-seller').innerHTML = `Sold by: <strong>${escapeHtml(product.seller_name)}</strong>`;
    document.getElementById('detail-desc').textContent = product.description || 'No detailed product description available.';
    document.getElementById('detail-price').textContent = `$${product.price.toFixed(2)}`;
    
    const stockBadge = document.getElementById('detail-stock-badge');
    if (product.stock > 5) {
        stockBadge.className = 'card-badge-stock in-stock';
        stockBadge.textContent = `${product.stock} Units Available`;
    } else if (product.stock > 0) {
        stockBadge.className = 'card-badge-stock low-stock';
        stockBadge.textContent = `Low Stock (${product.stock} left)`;
    } else {
        stockBadge.className = 'card-badge-stock out-of-stock';
        stockBadge.textContent = 'Out of Stock';
    }

    const addBtn = document.getElementById('detail-add-btn');
    if (addBtn) {
        addBtn.disabled = product.stock <= 0;
        addBtn.style.opacity = product.stock <= 0 ? '0.5' : '1';
    }

    modal.classList.add('active');
}

function closeProductDetailModal() {
    const modal = document.getElementById('product-detail-modal');
    if (modal) modal.classList.remove('active');
    selectedDetailProduct = null;
}

async function handleAddToCartFromDetail() {
    if (!selectedDetailProduct) return;
    await handleAddToCartClick(selectedDetailProduct.id);
    closeProductDetailModal();
}

async function handleAddToCartClick(productId) {
    if (!AppState.user) {
        showToast('Please log in or register as a Buyer to add products to your cart.', 'info');
        showAuthLanding();
        return;
    }

    try {
        await apiFetch('/api/cart/add', {
            method: 'POST',
            body: JSON.stringify({ product_id: productId, quantity: 1 })
        });

        showToast('Product added to your cart!', 'success');
        updateCartBadgeCount();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function updateCartBadgeCount() {
    if (!AppState.user) return;
    try {
        const data = await apiFetch('/api/cart');
        const items = data.items || [];
        const count = items.reduce((sum, item) => sum + item.quantity, 0);
        const badge = document.getElementById('cart-count');
        if (badge) badge.textContent = count;
    } catch (err) {
        console.warn('Failed to update cart badge count');
    }
}
