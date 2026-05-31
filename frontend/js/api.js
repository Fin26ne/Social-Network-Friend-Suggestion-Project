const API_URL = 'http://localhost:3001/api';

function showOfflineBanner() {
    let banner = document.getElementById('offline-banner');
    if (!banner) {
        banner = document.createElement('div');
        banner.id = 'offline-banner';
        banner.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            background-color: #ff4d4f;
            color: white;
            text-align: center;
            padding: 12px;
            font-weight: bold;
            z-index: 10000;
            font-family: 'Outfit', sans-serif;
            box-shadow: 0 2px 8px rgba(0,0,0,0.2);
        `;
        banner.innerHTML = '⚠️ Không thể kết nối với máy chủ. Vui lòng kiểm tra xem backend Java đã được khởi động chưa!';
        document.body.appendChild(banner);
    } else {
        banner.style.display = 'block';
    }
}

function hideOfflineBanner() {
    let banner = document.getElementById('offline-banner');
    if (banner) {
        banner.style.display = 'none';
    }
}

async function request(endpoint, options = {}) {
    try {
        const response = await fetch(`${API_URL}${endpoint}`, options);
        const json = await response.json().catch(() => ({}));
        if (!response.ok || json.success === false) {
            throw new Error(json.error || `HTTP error! status: ${response.status}`);
        }
        hideOfflineBanner();
        return { data: json.data };
    } catch (error) {
        console.error(`API Error on ${endpoint}:`, error);
        showOfflineBanner();
        throw error;
    }
}

window.api = {
    getUsers: () => request('/users'),
    getUserById: (id) => request(`/users/${id}`),
    createUser: (user) => request('/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(user)
    }),
    deleteUser: (id) => request(`/users/${id}`, {
        method: 'DELETE'
    }),
    getFriends: (userId) => request(`/friends/${userId}`),
    addFriend: (userId1, userId2) => request('/friends', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId1, userId2 })
    }),
    removeFriend: (userId1, userId2) => request('/friends', {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId1, userId2 })
    }),
    getMutualFriends: (userId1, userId2) => request(`/friends/mutual?userId1=${userId1}&userId2=${userId2}`),
    getSuggestions: (userId, k = 5, heapType = 'min') => request(`/suggestions?userId=${userId}&k=${k}&heapType=${heapType}`),
    getNetworkData: () => request('/network'),
    getBenchmarkData: () => request('/benchmark')
};
