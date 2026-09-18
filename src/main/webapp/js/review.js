/** Secure Product Review actions for AbisheikMart. */
function reviewBasePath() {
    return window.location.origin + (window.location.pathname.startsWith('/abisheikmart') ? '/abisheikmart' : '');
}

function reviewPayload(productId) {
    return {
        productId: Number(productId),
        rating: Number(document.getElementById('review-rating').value),
        comment: document.getElementById('review-comment').value.trim(),
        imageUrl: (document.getElementById('review-image')?.value || '').trim()
    };
}

function submitReviewAjax(event, productId) {
    event.preventDefault();
    const comment = document.getElementById('review-comment').value.trim();
    if (!comment) { alert('Please write a review comment.'); return; }
    const reviewId = document.getElementById('review-id')?.value;
    const payload = reviewPayload(productId);
    if (reviewId) payload.reviewId = Number(reviewId);
    const endpoint = reviewId ? '/api/review/update' : '/api/review/add';
    fetch(reviewBasePath() + endpoint, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
        .then(res => res.json().then(data => ({ ok: res.ok, data })))
        .then(result => { if (result.ok && result.data.status >= 200 && result.data.status < 300) { alert(result.data.message || 'Review saved.'); window.location.reload(); } else alert(result.data.message || 'Review request failed.'); })
        .catch(() => alert('Failed to save review.'));
}

function deleteReviewAjax(reviewId) {
    if (!confirm('Delete your review?')) return;
    fetch(reviewBasePath() + '/api/review/delete', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ reviewId: Number(reviewId) }) })
        .then(res => res.json().then(data => ({ ok: res.ok, data })))
        .then(result => { if (result.ok) { alert(result.data.message || 'Review deleted.'); window.location.reload(); } else alert(result.data.message || 'Unable to delete review.'); })
        .catch(() => alert('Failed to delete review.'));
}
