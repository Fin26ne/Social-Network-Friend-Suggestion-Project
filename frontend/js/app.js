// Shared utility functions

export function initNavigation() {
    const activePage = location.pathname.split('/').pop() || 'index.html';
    const link = document.querySelector(`a[href="${activePage}"]`);
    if (link) {
        link.classList.add('active');
    }
}

export function getCurrentUserId() {
    return localStorage.getItem('userId');
}

export function setCurrentUserId(id) {
    localStorage.setItem('userId', id);
}

export function clearCurrentUserId() {
    localStorage.removeItem('userId');
}

export function createAvatar(name) {
    if (!name) return '?';
    const parts = name.split(' ');
    if (parts.length >= 2) {
        return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
}

export function formatTime(timestamp) {
    if (!timestamp) return 'Vừa xong';
    const date = new Date(timestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
});
