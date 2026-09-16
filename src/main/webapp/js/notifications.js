/**
 * AbisheikMart 2.0 - Notifications Bell Script
 */

function toggleNotificationDropdown() {
    const menu = document.getElementById('notif-menu');
    if (menu) {
        if (menu.style.display === 'block') {
            menu.style.display = 'none';
        } else {
            menu.style.display = 'block';
            fetchNotifications();
        }
    }
}

function fetchNotifications() {
    const basePath = window.location.origin + window.location.pathname.split('/')[1];
    fetch(window.location.origin + (window.location.pathname.startsWith('/abisheikmart') ? '/abisheikmart' : '') + '/api/notifications')
    .then(res => res.json())
    .then(data => {
        if (data.status === 200 && data.data) {
            const listContainer = document.getElementById('notif-list');
            const badge = document.getElementById('notif-count');
            const items = data.data;

            let unreadCount = items.filter(n => !n.isRead).length;
            if (badge) badge.innerText = unreadCount;

            if (listContainer) {
                if (items.length === 0) {
                    listContainer.innerHTML = '<div class="notif-item">No new notifications</div>';
                } else {
                    listContainer.innerHTML = items.map(n => `
                        <div class="notif-item ${!n.isRead ? 'unread' : ''}">
                            <div style="font-weight: 600; color: #fff; font-size: 0.85rem;">${n.title}</div>
                            <div style="font-size: 0.8rem; color: var(--text-secondary); margin: 0.2rem 0;">${n.message}</div>
                            <div style="font-size: 0.72rem; color: var(--text-muted);">${n.createdAt}</div>
                        </div>
                    `).join('');
                }
            }
        }
    })
    .catch(err => {});
}

function markNotificationsRead() {
    fetch(window.location.origin + (window.location.pathname.startsWith('/abisheikmart') ? '/abisheikmart' : '') + '/api/notifications/read', {
        method: 'POST'
    })
    .then(res => res.json())
    .then(data => {
        const badge = document.getElementById('notif-count');
        if (badge) badge.innerText = '0';
        fetchNotifications();
    });
}

document.addEventListener('DOMContentLoaded', () => {
    fetchNotifications();
});
