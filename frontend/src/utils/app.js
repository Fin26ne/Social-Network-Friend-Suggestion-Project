export function getCurrentUserId() {
    if (typeof window !== 'undefined') {
        return localStorage.getItem('userId');
    }
    return null;
}

export function setCurrentUserId(id) {
    if (typeof window !== 'undefined') {
        localStorage.setItem('userId', id);
    }
}

export function clearCurrentUserId() {
    if (typeof window !== 'undefined') {
        localStorage.removeItem('userId');
    }
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
