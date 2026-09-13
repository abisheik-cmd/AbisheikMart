/* AbisheikMart 2.0 — Feature JavaScript: Checkout, OTP, Coupon, Reviews, Notifications, Lucky Draw */

/* =====================================================================
   CHECKOUT — OTP + Coupon + Order Placement
   ===================================================================== */

let checkoutRawSubtotal = 0;
let checkoutAppliedDiscount = 0;
let checkoutAppliedCouponCode = '';
let otpVerified = false;

document.addEventListener('DOMContentLoaded', function () {
    // Read the raw subtotal from the page (stored as data-attr via hidden input or parsed from DOM)
    const grandTotalEl = document.getElementById('checkout-grand-total');
    if (grandTotalEl) {
        const raw = grandTotalEl.textContent.replace(/[₹,\s]/g, '');
        checkoutRawSubtotal = parseFloat(raw) || 0;
    }
    loadNotifications();
});

async function sendOtpCheckout() {
    const phone = document.getElementById('phone-number-input')?.value?.trim();
    if (!phone || phone.length < 10) {
        showToast('Please enter a valid 10-digit mobile number.', 'error');
        return;
    }
    const btn = document.getElementById('send-otp-btn');
    if (btn) { btn.disabled = true; btn.textContent = 'Sending...'; }

    try {
        const resp = await fetch(getContextPath() + '/api/otp/send', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ phone: phone })
        });
        const data = await resp.json();
        if (resp.ok && data.success) {
            document.getElementById('otp-verify-section').style.display = 'block';
            showToast('OTP sent! (Demo: ' + (data.data?.otpSimulated || '------') + ')', 'success');
            if (btn) { btn.textContent = 'Resend OTP'; btn.disabled = false; }
        } else {
            showToast(data.message || 'Failed to send OTP.', 'error');
            if (btn) { btn.disabled = false; btn.textContent = 'Send OTP'; }
        }
    } catch (e) {
        showToast('Network error sending OTP.', 'error');
        if (btn) { btn.disabled = false; btn.textContent = 'Send OTP'; }
    }
}

async function verifyOtpCheckout() {
    const phone = document.getElementById('phone-number-input')?.value?.trim();
    const otp = document.getElementById('otp-input')?.value?.trim();
    const msgEl = document.getElementById('otp-status-msg');

    if (!otp || otp.length !== 6) {
        if (msgEl) { msgEl.style.color = '#f87171'; msgEl.textContent = 'Please enter the 6-digit OTP.'; }
        return;
    }
    try {
        const resp = await fetch(getContextPath() + '/api/otp/verify', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ phone: phone, otp: otp })
        });
        const data = await resp.json();
        if (resp.ok && data.success) {
            otpVerified = true;
            if (msgEl) { msgEl.style.color = '#10b981'; msgEl.textContent = '✅ Mobile number verified successfully!'; }
            showToast('Mobile number verified!', 'success');
        } else {
            if (msgEl) { msgEl.style.color = '#f87171'; msgEl.textContent = '❌ ' + (data.message || 'Invalid OTP.'); }
        }
    } catch (e) {
        showToast('Network error verifying OTP.', 'error');
    }
}

async function applyCouponCheckout() {
    const code = document.getElementById('coupon-code-input')?.value?.trim()?.toUpperCase();
    const msgEl = document.getElementById('coupon-status-msg');
    if (!code) {
        if (msgEl) { msgEl.style.color = '#f87171'; msgEl.textContent = 'Please enter a coupon code.'; }
        return;
    }
    try {
        const resp = await fetch(getContextPath() + '/api/coupon/apply', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code: code, subtotal: checkoutRawSubtotal })
        });
        const data = await resp.json();
        if (resp.ok && data.success && data.data) {
            const d = data.data;
            checkoutAppliedDiscount = parseFloat(d.discountAmount) || 0;
            checkoutAppliedCouponCode = d.code;
            document.getElementById('checkout-coupon-code').value = d.code;

            const discountRow = document.getElementById('checkout-discount-row');
            const discountAmtEl = document.getElementById('checkout-discount-amount');
            const grandTotalEl = document.getElementById('checkout-grand-total');

            if (discountRow) discountRow.style.display = 'flex';
            if (discountAmtEl) discountAmtEl.textContent = '-₹' + checkoutAppliedDiscount.toLocaleString('en-IN', { minimumFractionDigits: 2 });
            if (grandTotalEl) grandTotalEl.textContent = '₹' + parseFloat(d.finalTotal).toLocaleString('en-IN', { minimumFractionDigits: 2 });

            if (msgEl) { msgEl.style.color = '#10b981'; msgEl.textContent = '✅ ' + data.message + ' (Saved ₹' + checkoutAppliedDiscount.toFixed(2) + ')'; }
        } else {
            if (msgEl) { msgEl.style.color = '#f87171'; msgEl.textContent = '❌ ' + (data.message || 'Invalid coupon.'); }
        }
    } catch (e) {
        showToast('Network error applying coupon.', 'error');
    }
}

async function submitCheckoutForm(event) {
    event.preventDefault();
    const btn = document.getElementById('place-order-btn');
    if (btn) { btn.disabled = true; btn.textContent = '⏳ Placing Order...'; }

    const deliveryAddress = document.getElementById('delivery-address')?.value?.trim();
    const phoneNumber = document.getElementById('phone-number-input')?.value?.trim();
    const otpInput = document.getElementById('otp-input')?.value?.trim() || '';
    const paymentMethod = document.getElementById('payment-method-select')?.value || 'CASH_ON_DELIVERY';
    const couponCode = document.getElementById('checkout-coupon-code')?.value || '';

    if (!deliveryAddress) {
        showToast('Please enter a delivery address.', 'error');
        if (btn) { btn.disabled = false; btn.textContent = '✅ Confirm & Place Order'; }
        return;
    }
    if (!phoneNumber || phoneNumber.length < 10) {
        showToast('Please enter a valid 10-digit mobile number.', 'error');
        if (btn) { btn.disabled = false; btn.textContent = '✅ Confirm & Place Order'; }
        return;
    }

    try {
        const resp = await fetch(getContextPath() + '/api/checkout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ deliveryAddress, phoneNumber, paymentMethod, couponCode, otp: otpInput })
        });
        const data = await resp.json();
        if ((resp.ok || resp.status === 201) && data.success) {
            showToast('🎉 Order placed successfully!', 'success');
            setTimeout(() => {
                window.location.href = getContextPath() + '/orders?checkout=success';
            }, 1200);
        } else {
            showToast(data.message || 'Failed to place order.', 'error');
            if (btn) { btn.disabled = false; btn.textContent = '✅ Confirm & Place Order'; }
        }
    } catch (e) {
        showToast('Network error placing order.', 'error');
        if (btn) { btn.disabled = false; btn.textContent = '✅ Confirm & Place Order'; }
    }
}

/* =====================================================================
   PRODUCT REVIEWS
   ===================================================================== */

let selectedReviewRating = 5;

function toggleReviewForm() {
    const container = document.getElementById('review-form-container');
    if (container) {
        container.style.display = container.style.display === 'none' ? 'block' : 'none';
    }
}

function setReviewRating(val) {
    selectedReviewRating = val;
    const stars = document.querySelectorAll('.star-inp');
    stars.forEach((s, i) => {
        s.textContent = i < val ? '★' : '☆';
        s.style.color = i < val ? '#f59e0b' : '#64748b';
    });
}

async function submitProductReview(productId) {
    const comment = document.getElementById('review-comment')?.value?.trim();
    if (!comment) { showToast('Please write a comment for your review.', 'error'); return; }

    try {
        const resp = await fetch(getContextPath() + '/api/review/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId: productId, rating: selectedReviewRating, comment: comment, imageUrl: '' })
        });
        const data = await resp.json();
        if (resp.ok && data.success) {
            showToast('Review submitted! Thank you.', 'success');
            setTimeout(() => window.location.reload(), 1200);
        } else if (resp.status === 401) {
            showToast('Please log in to submit a review.', 'info');
        } else {
            showToast(data.message || 'Failed to submit review.', 'error');
        }
    } catch (e) {
        showToast('Network error submitting review.', 'error');
    }
}

/* =====================================================================
   NOTIFICATIONS
   ===================================================================== */

let notifOpen = false;

function toggleNotificationDropdown() {
    const menu = document.getElementById('notif-menu');
    if (!menu) return;
    notifOpen = !notifOpen;
    menu.style.display = notifOpen ? 'block' : 'none';
    if (notifOpen) {
        loadNotifications();
    }
}

async function loadNotifications() {
    try {
        const resp = await fetch(getContextPath() + '/api/notifications/', {
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        });
        if (!resp.ok) return;
        const data = await resp.json();
        const list = data.data || [];
        const listEl = document.getElementById('notif-list');
        const badgeEl = document.getElementById('notif-count');

        const unreadCount = list.filter(n => !n.isRead).length;
        if (badgeEl) badgeEl.textContent = unreadCount > 0 ? unreadCount : '';

        if (listEl) {
            if (list.length === 0) {
                listEl.innerHTML = '<div class="notif-item" style="color: var(--text-muted);">No notifications</div>';
            } else {
                listEl.innerHTML = list.map(n => `
                    <div class="notif-item ${!n.isRead ? 'notif-unread' : ''}" onclick="if('${n.link}' && '${n.link}' !== '#') window.location.href='${escapeHtml(n.link)}'">
                        <div class="notif-title">${escapeHtml(n.title)}</div>
                        <div class="notif-msg">${escapeHtml(n.message)}</div>
                    </div>
                `).join('');
            }
        }
    } catch (e) {}
}

async function markNotificationsRead() {
    try {
        await fetch(getContextPath() + '/api/notifications/read', {
            method: 'POST',
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        });
        const badgeEl = document.getElementById('notif-count');
        if (badgeEl) badgeEl.textContent = '';
        await loadNotifications();
    } catch (e) {}
}

// Close notification dropdown when clicking outside
document.addEventListener('click', function (e) {
    const container = document.querySelector('.notification-dropdown-container');
    if (container && !container.contains(e.target)) {
        notifOpen = false;
        const menu = document.getElementById('notif-menu');
        if (menu) menu.style.display = 'none';
    }
});

/* =====================================================================
   LUCKY DRAW WHEEL
   ===================================================================== */

const LUCKY_SEGMENTS = [
    { label: 'WELCOME10', color: '#0284c7', prize: '10% OFF — Code: WELCOME10' },
    { label: 'TRY AGAIN', color: '#475569', prize: null },
    { label: 'FESTIVE20', color: '#7c3aed', prize: '20% OFF — Code: FESTIVE20' },
    { label: 'TRY AGAIN', color: '#334155', prize: null },
    { label: 'LUCKY15', color: '#0891b2', prize: '15% OFF — Code: LUCKY15' },
    { label: 'TRY AGAIN', color: '#475569', prize: null },
    { label: 'SUPER500', color: '#059669', prize: '₹500 OFF — Code: SUPER500' },
    { label: 'TRY AGAIN', color: '#334155', prize: null },
];

let wheelSpinning = false;
let wheelAngle = 0;

function openLuckyDrawModal() {
    const modal = document.getElementById('lucky-draw-modal');
    if (modal) {
        modal.style.display = 'flex';
        drawWheel(wheelAngle);
    }
}

function closeLuckyDrawModal() {
    const modal = document.getElementById('lucky-draw-modal');
    if (modal) modal.style.display = 'none';
}

function drawWheel(rotation) {
    const canvas = document.getElementById('wheel-canvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const cx = canvas.width / 2;
    const cy = canvas.height / 2;
    const r = cx - 10;
    const segCount = LUCKY_SEGMENTS.length;
    const segAngle = (2 * Math.PI) / segCount;

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    LUCKY_SEGMENTS.forEach((seg, i) => {
        const startAngle = rotation + i * segAngle;
        const endAngle = startAngle + segAngle;

        ctx.beginPath();
        ctx.moveTo(cx, cy);
        ctx.arc(cx, cy, r, startAngle, endAngle);
        ctx.closePath();
        ctx.fillStyle = seg.color;
        ctx.fill();
        ctx.strokeStyle = 'rgba(255,255,255,0.15)';
        ctx.lineWidth = 2;
        ctx.stroke();

        ctx.save();
        ctx.translate(cx, cy);
        ctx.rotate(startAngle + segAngle / 2);
        ctx.textAlign = 'right';
        ctx.fillStyle = '#fff';
        ctx.font = 'bold 11px Inter, sans-serif';
        ctx.fillText(seg.label, r - 12, 4);
        ctx.restore();
    });

    // Center circle
    ctx.beginPath();
    ctx.arc(cx, cy, 22, 0, 2 * Math.PI);
    ctx.fillStyle = '#090d16';
    ctx.fill();
    ctx.strokeStyle = 'rgba(255,255,255,0.2)';
    ctx.lineWidth = 2;
    ctx.stroke();
}

function spinLuckyWheel() {
    if (wheelSpinning) return;
    wheelSpinning = true;
    const btn = document.getElementById('spin-wheel-btn');
    if (btn) btn.disabled = true;
    const resultEl = document.getElementById('lucky-result-box');
    if (resultEl) resultEl.style.display = 'none';

    const segCount = LUCKY_SEGMENTS.length;
    const extraSpins = (Math.floor(Math.random() * 4) + 5) * 2 * Math.PI;
    const winIndex = Math.floor(Math.random() * segCount);
    const segAngle = (2 * Math.PI) / segCount;
    const targetAngle = extraSpins + (2 * Math.PI - winIndex * segAngle - segAngle / 2);

    const duration = 4500;
    const startAngle = wheelAngle;
    const startTime = performance.now();

    function easeOut(t) { return 1 - Math.pow(1 - t, 3); }

    function animate(ts) {
        const elapsed = ts - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const currentAngle = startAngle + targetAngle * easeOut(progress);
        wheelAngle = currentAngle;
        drawWheel(currentAngle);

        if (progress < 1) {
            requestAnimationFrame(animate);
        } else {
            wheelSpinning = false;
            if (btn) { btn.disabled = false; }
            const won = LUCKY_SEGMENTS[winIndex];
            if (resultEl) {
                resultEl.style.display = 'block';
                if (won.prize) {
                    resultEl.innerHTML = `<div style="background: rgba(16,185,129,0.2); border: 1px solid rgba(16,185,129,0.5); border-radius: 12px; padding: 1rem; color: #34d399; font-weight: 700;">🎉 Congratulations! You won: ${escapeHtml(won.prize)}</div>`;
                } else {
                    resultEl.innerHTML = `<div style="background: rgba(71,85,105,0.4); border: 1px solid rgba(71,85,105,0.5); border-radius: 12px; padding: 1rem; color: var(--text-secondary);">😊 Better luck next time! Try spinning again.</div>`;
                }
            }
        }
    }
    requestAnimationFrame(animate);
}
