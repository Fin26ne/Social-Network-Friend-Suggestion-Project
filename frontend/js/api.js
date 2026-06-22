const API = 'http://localhost:3003/api';

async function fetchAPI(endpoint, options = {}) {
    try {
        const response = await fetch(`${API}${endpoint}`, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });
        
        if (!response.ok) {
            throw new Error(`API error: ${response.status}`);
        }
        
        const result = await response.json();
        if (!result.success) throw new Error(result.error || 'API error');
        return result.data;
    } catch (error) {
        console.error('API Fetch Error:', error);
        showApiErrorBanner();
        throw error;
    }
}

function showApiErrorBanner() {
    let banner = document.getElementById('api-error-banner');
    if (!banner) {
        banner = document.createElement('div');
        banner.id = 'api-error-banner';
        banner.className = 'banner';
        banner.innerHTML = '⚠ Không thể kết nối Java server. Chạy: java Main';
        document.body.prepend(banner);
    }
}

export async function getUsers() {
    return fetchAPI('/users');
}

export async function getUser(id) {
    return fetchAPI(`/users?id=${id}`);
}

export async function addUser(user) {
    return fetchAPI('/users', {
        method: 'POST',
        body: JSON.stringify(user)
    });
}

export async function getSuggestions(userId) {
    return fetchAPI(`/suggestions?userId=${userId}`);
}

export async function getFriends(userId) {
    return fetchAPI(`/friends?userId=${userId}`);
}

export async function addFriend(id1, id2) {
    const response = await fetch('http://localhost:3003/api/friends', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ userId1: id1, userId2: id2 })
    });
    const result = await response.json();
    if (!result.success) throw new Error(result.error || 'API error');
    return result.data;
}

export async function removeFriend(id1, id2) {
    return fetchAPI('/friends', {
        method: 'DELETE',
        body: JSON.stringify({ userId1: id1, userId2: id2 })
    });
}

export async function getNetworkData(userId) {
    const url = userId ? `/suggestions/graph?userId=${userId}` : `/suggestions/graph`;
    const data = await fetchAPI(url);
    
    const users = await getUsers();
    const userMap = {};
    users.forEach(u => userMap[u.id] = u);

    const nodes = (data.nodes || []).map(n => {
        const u = userMap[n.id];
        return {
            id: n.id,
            name: u ? u.displayName : n.label,
            avatar: u ? u.avatarColor : null
        };
    });

    const links = (data.edges || []).map(e => ({
        source: e.source,
        target: e.target
    }));
    
    return { nodes, links };
}
