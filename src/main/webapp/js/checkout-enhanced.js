/**
 * AbisheikMart 2.0 - Enhanced Checkout JS
 */

let isOtpVerified = false;
let appliedDiscountAmount = 0.0;

function togglePaymentFields() {
    const method = document.getElementById('payment-method-select').value;
    const upiBox = document.getElementById('upi-details-box');
    const netbankingBox = document.getElementById('netbanking-details-box');

    if (upiBox) upiBox.style.display = (method === 'UPI') ? 'block' : 'none';
    if (netbankingBox) netbankingBox.style.display = (method === 'NETBANKING') ? 'block' : 'none';
}

function applyCouponCheckout(cartSubtotal) {
    const code = document.getElementById('coupon-code-input').value.trim();
    const statusMsg = document.getElementById('coupon-status-msg');
    if (!code) {
        statusMsg.innerHTML = '<span style="color: #f87171;">Please enter a coupon code.</span>';
        return;
    }

    fetch(window.location.origin + window.location.pathname.replace('/checkout', '') + '/api/coupon/apply', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ code: code, subtotal: cartSubtotal })
    })
    .then(res => res.json())
    .then(data => {
        if (data.status === 200 && data.data) {
            appliedDiscountAmount = data.data.discountAmount;
            document.getElementById('checkout-coupon-code').value = data.data.code;
            
            document.getElementById('checkout-discount-row').style.display = 'flex';
            document.getElementById('checkout-discount-amount').innerText = '-₹' + appliedDiscountAmount.toFixed(2);
            document.getElementById('checkout-grand-total').innerText = '₹' + data.data.finalTotal.toFixed(2);
            
            statusMsg.innerHTML = '<span style="color: #10b981;">✅ Coupon "' + data.data.code + '" applied! You saved ₹' + appliedDiscountAmount.toFixed(2) + '</span>';
        } else {
            statusMsg.innerHTML = '<span style="color: #f87171;">⚠️ ' + (data.message || 'Invalid coupon code.') + '</span>';
        }
    })
    .catch(err => {
        statusMsg.innerHTML = '<span style="color: #f87171;">Error validating coupon.</span>';
    });
}

function sendOtpCheckout() {
    const phoneInput = document.getElementById('phone-number-input').value.trim();
    const statusMsg = document.getElementById('otp-status-msg');
    if (phoneInput.length < 10) {
        alert('Please enter a valid 10-digit mobile number first.');
        return;
    }

    fetch(window.location.origin + window.location.pathname.replace('/checkout', '') + '/api/otp/send', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phone: phoneInput })
    })
    .then(res => res.json())
    .then(data => {
        if (data.status === 200) {
            document.getElementById('otp-verify-section').style.display = 'block';
            const simulatedCode = data.data ? data.data.otpSimulated : '';
            statusMsg.innerHTML = '<span style="color: #38bdf8;">OTP sent! [Test Verification Code: <strong>' + simulatedCode + '</strong>]</span>';
        } else {
            alert('Failed to send OTP: ' + data.message);
        }
    })
    .catch(err => alert('Error sending OTP.'));
}

function verifyOtpCheckout() {
    const phoneInput = document.getElementById('phone-number-input').value.trim();
    const otpInput = document.getElementById('otp-input').value.trim();
    const statusMsg = document.getElementById('otp-status-msg');

    if (otpInput.length < 6) {
        statusMsg.innerHTML = '<span style="color: #f87171;">Please enter the full 6-digit OTP.</span>';
        return;
    }

    fetch(window.location.origin + window.location.pathname.replace('/checkout', '') + '/api/otp/verify', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phone: phoneInput, otp: otpInput })
    })
    .then(res => res.json())
    .then(data => {
        if (data.status === 200) {
            isOtpVerified = true;
            statusMsg.innerHTML = '<span style="color: #10b981; font-weight: 700;">✅ Mobile Number Verified Successfully!</span>';
        } else {
            statusMsg.innerHTML = '<span style="color: #f87171;">⚠️ ' + data.message + '</span>';
        }
    })
    .catch(err => {
        statusMsg.innerHTML = '<span style="color: #f87171;">Verification error.</span>';
    });
}

function submitCheckoutForm(e) {
    e.preventDefault();
    const address = document.getElementById('delivery-address').value.trim();
    const phone = document.getElementById('phone-number-input').value.trim();
    const method = document.getElementById('payment-method-select').value;
    const coupon = document.getElementById('checkout-coupon-code').value;
    const otp = document.getElementById('otp-input') ? document.getElementById('otp-input').value : '';

    if (!address) {
        alert('Please enter your complete delivery address.');
        return;
    }
    if (!phone || phone.length < 10) {
        alert('Please enter a valid 10-digit mobile number.');
        return;
    }

    const payload = {
        deliveryAddress: address,
        phoneNumber: phone,
        paymentMethod: method,
        couponCode: coupon,
        otp: otp
    };

    fetch(window.location.origin + window.location.pathname.replace('/checkout', '') + '/api/checkout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(data => {
        if (data.status === 201 || data.status === 200) {
            window.location.href = window.location.origin + window.location.pathname.replace('/checkout', '') + '/orders?checkout=success&orderId=' + (data.data ? data.data.id : '');
        } else {
            alert('Checkout error: ' + data.message);
        }
    })
    .catch(err => {
        alert('Checkout order placement failed.');
    });
}
