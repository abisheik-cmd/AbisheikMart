/**
 * AbisheikMart 2.0 - Lucky Draw Spinner Widget
 */

const prizes = [
    { label: "15% OFF", code: "LUCKY15", color: "#f59e0b" },
    { label: "₹200 OFF", code: "SUPER500", color: "#38bdf8" },
    { label: "10% OFF", code: "WELCOME10", color: "#10b981" },
    { label: "20% OFF", code: "FESTIVE20", color: "#ec4899" },
    { label: "Free Shipping", code: "WELCOME10", color: "#8b5cf6" },
    { label: "Try Again", code: "", color: "#64748b" }
];

let currentAngle = 0;
let isSpinning = false;

function openLuckyDrawModal() {
    const modal = document.getElementById('lucky-draw-modal');
    if (modal) {
        modal.style.display = 'flex';
        drawWheel();
    }
}

function closeLuckyDrawModal() {
    const modal = document.getElementById('lucky-draw-modal');
    if (modal) {
        modal.style.display = 'none';
    }
}

function drawWheel() {
    const canvas = document.getElementById('wheel-canvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const numPrizes = prizes.length;
    const arcSize = (2 * Math.PI) / numPrizes;

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    const radius = 130;

    for (let i = 0; i < numPrizes; i++) {
        const angle = currentAngle + i * arcSize;
        ctx.beginPath();
        ctx.fillStyle = prizes[i].color;
        ctx.moveTo(centerX, centerY);
        ctx.arc(centerX, centerY, radius, angle, angle + arcSize);
        ctx.lineTo(centerX, centerY);
        ctx.fill();
        ctx.stroke();

        ctx.save();
        ctx.translate(centerX, centerY);
        ctx.rotate(angle + arcSize / 2);
        ctx.textAlign = "right";
        ctx.fillStyle = "#ffffff";
        ctx.font = "bold 14px sans-serif";
        ctx.fillText(prizes[i].label, radius - 15, 5);
        ctx.restore();
    }
}

function spinLuckyWheel() {
    if (isSpinning) return;
    isSpinning = true;
    const spinBtn = document.getElementById('spin-wheel-btn');
    if (spinBtn) spinBtn.disabled = true;

    const spins = 5 + Math.floor(Math.random() * 5);
    const prizeIndex = Math.floor(Math.random() * (prizes.length - 1)); // win a coupon
    const arcSize = (2 * Math.PI) / prizes.length;
    const targetAngle = (2 * Math.PI * spins) + (prizes.length - 1 - prizeIndex) * arcSize;

    let startTime = null;
    const duration = 4000;

    function animateWheel(timestamp) {
        if (!startTime) startTime = timestamp;
        const progress = Math.min((timestamp - startTime) / duration, 1);
        const easeOut = 1 - Math.pow(1 - progress, 3);
        currentAngle = easeOut * targetAngle;

        drawWheel();

        if (progress < 1) {
            requestAnimationFrame(animateWheel);
        } else {
            isSpinning = false;
            if (spinBtn) spinBtn.disabled = false;
            const prize = prizes[prizeIndex];
            const resultBox = document.getElementById('lucky-result-box');
            if (resultBox) {
                resultBox.style.display = 'block';
                if (prize.code) {
                    resultBox.innerHTML = '<div style="background: rgba(16,185,129,0.15); border: 1px solid #10b981; padding: 1rem; border-radius: 12px; color: #10b981;"><h4 style="margin-bottom:0.3rem;">🎉 Congratulations!</h4><p>You won: <strong>' + prize.label + '</strong></p><p style="margin-top:0.5rem;">Use Coupon Code: <strong style="font-size:1.2rem; letter-spacing:1px;">' + prize.code + '</strong></p></div>';
                } else {
                    resultBox.innerHTML = '<div style="color: var(--text-secondary);">Better luck next time! Try spinning again tomorrow.</div>';
                }
            }
        }
    }

    requestAnimationFrame(animateWheel);
}
