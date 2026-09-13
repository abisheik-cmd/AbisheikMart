<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="activeNav" value="cart" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/navbar.jsp" />

<main class="main-content" style="max-width: 720px; margin: 2rem auto;">
    <div style="margin-bottom: 1.5rem;">
        <h2 style="font-size: 1.8rem; font-weight: 800;">Order Checkout</h2>
        <p style="color: var(--text-secondary); font-size: 0.9rem;">Review your order, apply coupons, and confirm delivery details.</p>
    </div>

    <c:if test="${not empty errorMessage}">
        <div style="background: rgba(244, 63, 94, 0.15); border: 1px solid rgba(244, 63, 94, 0.4); border-radius: 10px; padding: 1rem; margin-bottom: 1.2rem; color: #f87171;">
            ⚠️ ${errorMessage}
        </div>
    </c:if>

    <div style="display: grid; gap: 1.5rem;">

        <!-- Order Summary Card -->
        <div style="background: var(--glass-bg); backdrop-filter: blur(16px); border: 1px solid var(--glass-border); border-radius: 16px; padding: 1.5rem;">
            <h3 style="font-size: 1.05rem; font-weight: 700; margin-bottom: 1rem; color: var(--accent-cyan);">📋 Order Summary</h3>

            <div style="background: rgba(15, 23, 42, 0.6); border: 1px solid var(--glass-border); border-radius: 12px; padding: 1rem; margin-bottom: 1rem;">
                <div style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 0.4rem;">Customer</div>
                <div style="font-weight: 700; color: #fff;">${sessionScope.user.name}</div>
                <div style="font-size: 0.82rem; color: var(--text-muted);">${sessionScope.user.email}</div>
            </div>

            <div style="max-height: 260px; overflow-y: auto; margin-bottom: 1rem;">
                <c:forEach items="${cart.items}" var="i">
                    <div style="display: flex; justify-content: space-between; padding: 0.65rem 0; border-bottom: 1px solid var(--glass-border); font-size: 0.9rem;">
                        <div>
                            <strong style="color: #fff;">${i.productName}</strong>
                            <div style="font-size: 0.8rem; color: var(--text-muted);">${i.quantity} × ₹<fmt:formatNumber value="${i.unitPrice}" pattern="#,##0.00" /></div>
                        </div>
                        <div style="font-weight: 700; color: var(--accent-green);">₹<fmt:formatNumber value="${i.subtotal}" pattern="#,##0.00" /></div>
                    </div>
                </c:forEach>
            </div>

            <!-- Cart Subtotal -->
            <div id="checkout-subtotal-row" style="display: flex; justify-content: space-between; padding: 0.5rem 0; font-size: 1rem;">
                <span style="color: var(--text-secondary);">Subtotal</span>
                <span style="font-weight: 600; color: #fff;">₹<fmt:formatNumber value="${cart.grandTotal}" pattern="#,##0.00" /></span>
            </div>
            <!-- Discount row (hidden until coupon applied) -->
            <div id="checkout-discount-row" style="display: none; justify-content: space-between; padding: 0.5rem 0; font-size: 1rem;">
                <span style="color: var(--accent-amber);">🏷️ Coupon Discount</span>
                <span id="checkout-discount-amount" style="font-weight: 600; color: var(--accent-amber);">-₹0.00</span>
            </div>
            <!-- Grand Total -->
            <div style="display: flex; justify-content: space-between; font-size: 1.25rem; font-weight: 800; border-top: 2px solid var(--glass-border); padding-top: 1rem; margin-top: 0.5rem;">
                <span>Grand Total (INR)</span>
                <span id="checkout-grand-total" style="color: var(--accent-green);">₹<fmt:formatNumber value="${cart.grandTotal}" pattern="#,##0.00" /></span>
            </div>
        </div>

        <!-- Coupon Section -->
        <div style="background: var(--glass-bg); backdrop-filter: blur(16px); border: 1px solid var(--glass-border); border-radius: 16px; padding: 1.5rem;">
            <h3 style="font-size: 1.05rem; font-weight: 700; margin-bottom: 1rem; color: var(--accent-amber);">🏷️ Apply Coupon Code</h3>
            <div style="display: flex; gap: 0.8rem;">
                <input type="text" id="coupon-code-input" placeholder="e.g. WELCOME10, FESTIVE20, SUPER500"
                       style="flex: 1; background: rgba(15,23,42,0.7); border: 1px solid var(--glass-border); border-radius: 10px; padding: 0.7rem 1rem; color: #fff; font-size: 0.95rem; text-transform: uppercase;">
                <button type="button" class="btn btn-warning" onclick="applyCouponCheckout()" style="white-space: nowrap;">Apply Coupon</button>
            </div>
            <div id="coupon-status-msg" style="margin-top: 0.6rem; font-size: 0.85rem;"></div>
        </div>

        <!-- Delivery Details & Payment Form -->
        <div style="background: var(--glass-bg); backdrop-filter: blur(16px); border: 1px solid var(--glass-border); border-radius: 16px; padding: 1.5rem;">
            <h3 style="font-size: 1.05rem; font-weight: 700; margin-bottom: 1.2rem; color: var(--accent-cyan);">🚚 Delivery Details</h3>

            <form id="checkout-form" onsubmit="submitCheckoutForm(event)">
                <input type="hidden" id="checkout-coupon-code" name="couponCode" value="">

                <div style="margin-bottom: 1.1rem;">
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; color: var(--text-secondary); margin-bottom: 0.5rem;">Delivery Address *</label>
                    <textarea id="delivery-address" name="deliveryAddress" rows="3" required
                              placeholder="Full delivery address including Flat/House No, Street, City, State, PIN..."
                              style="width: 100%; background: rgba(15,23,42,0.7); border: 1px solid var(--glass-border); border-radius: 10px; padding: 0.7rem 1rem; color: #fff; font-size: 0.9rem; resize: vertical;"></textarea>
                </div>

                <div style="margin-bottom: 1.1rem;">
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; color: var(--text-secondary); margin-bottom: 0.5rem;">Mobile Number *</label>
                    <div style="display: flex; gap: 0.8rem;">
                        <input type="tel" id="phone-number-input" name="phoneNumber" required
                               placeholder="10-digit mobile number" maxlength="10" pattern="[0-9]{10}"
                               style="flex: 1; background: rgba(15,23,42,0.7); border: 1px solid var(--glass-border); border-radius: 10px; padding: 0.7rem 1rem; color: #fff; font-size: 0.9rem;">
                        <button type="button" class="btn btn-secondary" id="send-otp-btn" onclick="sendOtpCheckout()" style="white-space: nowrap;">Send OTP</button>
                    </div>
                </div>

                <div id="otp-verify-section" style="margin-bottom: 1.1rem; display: none;">
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; color: var(--text-secondary); margin-bottom: 0.5rem;">Enter OTP *</label>
                    <div style="display: flex; gap: 0.8rem;">
                        <input type="text" id="otp-input" name="otp" maxlength="6" placeholder="6-digit OTP"
                               style="flex: 1; background: rgba(15,23,42,0.7); border: 1px solid var(--glass-border); border-radius: 10px; padding: 0.7rem 1rem; color: #fff; font-size: 0.9rem; letter-spacing: 0.3rem; text-align: center;">
                        <button type="button" class="btn btn-primary" onclick="verifyOtpCheckout()">Verify OTP</button>
                    </div>
                    <div id="otp-status-msg" style="margin-top: 0.5rem; font-size: 0.82rem;"></div>
                </div>

                <div style="margin-bottom: 1.5rem;">
                    <label style="display: block; font-size: 0.85rem; font-weight: 600; color: var(--text-secondary); margin-bottom: 0.5rem;">Payment Method</label>
                    <select id="payment-method-select" name="paymentMethod"
                            style="width: 100%; background: rgba(15,23,42,0.7); border: 1px solid var(--glass-border); border-radius: 10px; padding: 0.7rem 1rem; color: #fff; font-size: 0.9rem;">
                        <option value="CASH_ON_DELIVERY">💵 Cash on Delivery (COD)</option>
                        <option value="UPI">📱 UPI / QR Payment</option>
                        <option value="CARD">💳 Debit / Credit Card</option>
                        <option value="NETBANKING">🏦 Net Banking</option>
                    </select>
                </div>

                <button type="submit" id="place-order-btn" class="btn btn-success" style="width: 100%; padding: 1rem; font-size: 1.05rem; font-weight: 700;">
                    ✅ Confirm &amp; Place Order
                </button>
            </form>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
