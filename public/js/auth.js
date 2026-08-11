function switchAuthTab(tabType = 'login') {
    const loginBtn = document.getElementById('tab-login-btn');
    const regBtn = document.getElementById('tab-register-btn');
    const loginForm = document.getElementById('form-login');
    const regForm = document.getElementById('form-register');

    if (tabType === 'login') {
        if (loginBtn) loginBtn.classList.add('active');
        if (regBtn) regBtn.classList.remove('active');
        if (loginForm) loginForm.style.display = 'block';
        if (regForm) regForm.style.display = 'none';
    } else {
        if (regBtn) regBtn.classList.add('active');
        if (loginBtn) loginBtn.classList.remove('active');
        if (regForm) regForm.style.display = 'block';
        if (loginForm) loginForm.style.display = 'none';
    }
}

async function handleRegisterSubmit(event) {
    event.preventDefault();
    const name = document.getElementById('reg-name').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value.trim();
    const role = document.getElementById('reg-role').value;

    try {
        const data = await apiFetch('/api/auth/register', {
            method: 'POST',
            body: JSON.stringify({ name, email, password, role })
        });

        AppState.token = data.token;
        AppState.user = data.user;
        localStorage.setItem('token', data.token);

        updateNavUI();
        showToast(`Welcome to Abisheikmart, ${data.user.name}! Registered as ${data.user.role}.`, 'success');
        
        if (data.user.role === 'SELLER') {
            switchView('seller');
        } else {
            switchView('catalog');
        }
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function handleLoginSubmit(event) {
    event.preventDefault();
    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value.trim();

    try {
        const data = await apiFetch('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });

        AppState.token = data.token;
        AppState.user = data.user;
        localStorage.setItem('token', data.token);

        updateNavUI();
        showToast(`Logged in successfully as ${data.user.name} (${data.user.role}).`, 'success');

        if (data.user.role === 'SELLER') {
            switchView('seller');
        } else {
            switchView('catalog');
        }
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function handleLogout() {
    AppState.token = null;
    AppState.user = null;
    localStorage.removeItem('token');
    updateNavUI();
    showToast('Logged out successfully.', 'info');
}
