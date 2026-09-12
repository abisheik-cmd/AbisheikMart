/* AbisheikMart 2.0 Seller Dashboard Async AJAX Handlers */

async function deleteProductSellerAjax(productId) {
    if (!confirm('Are you sure you want to delete this product listing? This action cannot be undone.')) return;

    try {
        const response = await fetch(getContextPath() + '/api/seller/product/delete', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify({ id: productId })
        });

        const data = await response.json();
        if (response.ok && data.success) {
            showToast('Product deleted successfully.', 'info');
            const row = document.getElementById('seller-prod-' + productId);
            if (row) {
                row.style.opacity = '0';
                setTimeout(() => row.remove(), 300);
            } else {
                window.location.reload();
            }
        } else {
            showToast(data.message || 'Failed to delete product listing.', 'error');
        }
    } catch (err) {
        showToast('Network error: Could not reach server.', 'error');
    }
}

