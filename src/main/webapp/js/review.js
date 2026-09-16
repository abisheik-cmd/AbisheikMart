/**
 * AbisheikMart 2.0 - Product Review Submission Script
 */

function submitReviewAjax(event, productId) {
    event.preventDefault();
    const rating = document.getElementById('review-rating').value;
    const comment = document.getElementById('review-comment').value.trim();
    const imageUrl = document.getElementById('review-image').value.trim();

    if (!comment) {
        alert('Please write a review comment.');
        return;
    }

    const payload = {
        productId: productId,
        rating: parseInt(rating),
        comment: comment,
        imageUrl: imageUrl
    };

    fetch(window.location.origin + (window.location.pathname.startsWith('/abisheikmart') ? '/abisheikmart' : '') + '/api/review/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(data => {
        if (data.status === 200 || data.status === 201) {
            alert('Review submitted successfully!');
            window.location.reload();
        } else {
            alert('Error submitting review: ' + data.message);
        }
    })
    .catch(err => {
        alert('Failed to submit review.');
    });
}
