<<<<<<< HEAD
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
=======
const baseURL = "http://localhost:3001/api";

// Create offline banner if not present in the DOM
function ensureOfflineBanner() {
    let banner = document.getElementById("vietnamese-offline-banner");
    if (!banner) {
        banner = document.createElement("div");
        banner.id = "vietnamese-offline-banner";
        banner.className = "offline-banner";
        banner.innerHTML = `
            <span style="font-size: 18px;">⚠️</span>
            <span>Không thể kết nối đến máy chủ Java API. Vui lòng kiểm tra lại trạng thái server!</span>
        `;
        document.body.appendChild(banner);
    }
    return banner;
}

function showOfflineBanner() {
    const banner = ensureOfflineBanner();
    banner.classList.add("active");
}

function hideOfflineBanner() {
    const banner = document.getElementById("vietnamese-offline-banner");
    if (banner) {
        banner.classList.remove("active");
    }
}

// Global fetch wrapper with automated banner controls and response structure mapping
async function request(url, options = {}) {
    try {
        const response = await fetch(url, options);
        hideOfflineBanner(); // Online, hide banner
        
        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            return {
                success: false,
                error: errData.error || `HTTP error! status: ${response.status}`
            };
        }

        const rawData = await response.json();
        
        // standardise response formats to return { success: true, data: [...] }
        let data = rawData;
        if (rawData.users !== undefined) {
            data = rawData.users;
        } else if (rawData.friends !== undefined) {
            data = rawData.friends;
        } else if (rawData.suggestions !== undefined) {
            data = rawData.suggestions;
        } else if (rawData.user !== undefined) {
            data = rawData.user;
        } else if (rawData.benchmarkResults !== undefined) {
            data = rawData.benchmarkResults;
        }

        return {
            success: true,
            data: data,
            meta: rawData // Preserve raw data just in case we need extra fields (like executionTimeMs)
        };
    } catch (error) {
        console.error("Fetch API error:", error);
        showOfflineBanner(); // Show Vietnamese banner on failure
        return {
            success: false,
            error: "Server offline hoặc lỗi mạng"
        };
    }
}

const api = {
    async getUsers() {
        return request(`${baseURL}/users`);
    },

    async getUserById(userId) {
        return request(`${baseURL}/users/${userId}`);
    },

    async createUser(name, username, bio) {
        return request(`${baseURL}/users`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ name, username, bio })
        });
    },

    async deleteUser(userId) {
        return request(`${baseURL}/users/${userId}`, {
            method: "DELETE"
        });
    },

    async getFriends(userId) {
        return request(`${baseURL}/friends/${userId}`);
    },

    async addFriend(userId1, userId2) {
        return request(`${baseURL}/friends`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ userId1, userId2 })
        });
    },

    async removeFriend(userId1, userId2) {
        return request(`${baseURL}/friends`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ userId1, userId2 })
        });
    },

    async getMutualFriends(userId1, userId2) {
        const res1 = await this.getFriends(userId1);
        const res2 = await this.getFriends(userId2);
        
        if (!res1.success || !res2.success) {
            return { success: false, error: "Không thể lấy danh sách bạn bè" };
        }

        const friends1 = res1.data;
        const friends2 = res2.data;

        const ids2 = friends2.map(f => f.id);
        const mutual = friends1.filter(f => ids2.includes(f.id));

        return {
            success: true,
            data: mutual
        };
    },

    async getSuggestions(userId, k = 5, heapType = "min") {
        return request(`${baseURL}/suggestions?userId=${userId}&k=${k}&heapType=${heapType}`);
    },

    async getNetworkData() {
        return request(`${baseURL}/graph`);
    },

    async getBenchmarkData() {
        return request(`${baseURL}/benchmark`);
    }
};

// Make api globally available
window.api = api;
>>>>>>> e6180d4e3476907484e5820ebfcdfca1b0d9f096
