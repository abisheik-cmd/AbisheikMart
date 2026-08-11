const AppState = {
    user: null,
    token: localStorage.getItem('token') || null,
    cartCount: 0,
    currentView: 'catalog',
    searchQuery: '',
    selectedCategory: 'ALL',
    selectedSort: 'newest'
};

async function apiFetch(endpoint, options = {}) {
    const headers = options.headers || {};
    headers['Content-Type'] = 'application/json';
    
    if (AppState.token) {
        headers['Authorization'] = `Bearer ${AppState.token}`;
    }

    options.headers = headers;

    try {
        const response = await fetch(endpoint, options);
        const data = await response.json().catch(() => ({}));
        
        if (!response.ok) {
            throw new Error(data.error || `HTTP ${response.status} Error`);
        }
        return data;
    } catch (err) {
        throw err;
    }
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    const icon = type === 'success' ? '✅' : (type === 'error' ? '⚠️' : 'ℹ️');
    toast.innerHTML = `<span>${icon}</span> <span>${escapeHtml(message)}</span>`;

    container.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 4000);
}

function switchView(viewName) {
    if (!AppState.user && viewName !== 'auth-landing') {
        // Enforce Auth-First flow
        showAuthLanding();
        return;
    }

    const views = ['auth-landing', 'catalog', 'seller', 'orders'];
    views.forEach(v => {
        const el = document.getElementById(`view-${v}`);
        const navEl = document.getElementById(`nav-${v}`);
        if (el) el.style.display = (v === viewName) ? 'block' : 'none';
        if (navEl) {
            if (v === viewName) navEl.classList.add('active');
            else navEl.classList.remove('active');
        }
    });

    const mainNavbar = document.getElementById('main-navbar');
    if (mainNavbar) {
        mainNavbar.style.display = AppState.user ? 'flex' : 'none';
    }

    AppState.currentView = viewName;

    // Trigger refresh for active view
    if (viewName === 'catalog') {
        loadProductCatalog();
        updateCartBadgeCount();
    }
    if (viewName === 'seller') loadSellerDashboard();
    if (viewName === 'orders') loadOrderHistory();
}

function showAuthLanding() {
    const views = ['catalog', 'seller', 'orders'];
    views.forEach(v => {
        const el = document.getElementById(`view-${v}`);
        if (el) el.style.display = 'none';
    });

    const landing = document.getElementById('view-auth-landing');
    if (landing) landing.style.display = 'flex';

    const mainNavbar = document.getElementById('main-navbar');
    if (mainNavbar) mainNavbar.style.display = 'none';
}

function updateNavUI() {
    const navUser = document.getElementById('nav-user-container');
    const navSeller = document.getElementById('nav-seller');
    const userNameEl = document.getElementById('nav-user-name');
    const userRoleEl = document.getElementById('nav-user-role');
    const mainNavbar = document.getElementById('main-navbar');

    if (AppState.user) {
        if (mainNavbar) mainNavbar.style.display = 'flex';
        if (navUser) navUser.style.display = 'flex';

        if (userNameEl) userNameEl.textContent = AppState.user.name;
        if (userRoleEl) {
            userRoleEl.textContent = AppState.user.role;
            userRoleEl.className = `badge-role role-${AppState.user.role.toLowerCase()}`;
        }

        // Show Seller tab only if role is SELLER or ADMIN
        if (navSeller) {
            navSeller.style.display = (AppState.user.role === 'SELLER' || AppState.user.role === 'ADMIN') ? 'flex' : 'none';
        }
    } else {
        showAuthLanding();
    }
}

async function initApp() {
    if (AppState.token) {
        try {
            const data = await apiFetch('/api/auth/me');
            AppState.user = data.user;
            updateNavUI();
            switchView('catalog');
        } catch (err) {
            console.warn('Session expired or invalid, clearing token.');
            localStorage.removeItem('token');
            AppState.token = null;
            AppState.user = null;
            updateNavUI();
        }
    } else {
        updateNavUI();
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/&/g, "&amp;")
                      .replace(/</g, "&lt;")
                      .replace(/>/g, "&gt;")
                      .replace(/"/g, "&quot;")
                      .replace(/'/g, "&#039;");
}

document.addEventListener('DOMContentLoaded', initApp);
