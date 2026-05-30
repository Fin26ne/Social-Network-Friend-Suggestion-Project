// ==========================================================================
// STATE MANAGEMENT & GLOBAL VARIABLES
// ==========================================================================
const API_URL = 'http://localhost:8080/api';
let users = [];
let friendships = [];
let adjacencyMatrix = null;
let activeUserId = '';
let activeUserFriends = [];
let activeUserSuggestions = [];

// Graph Visualizer Physics State
let nodes = [];
let links = [];
let draggedNode = null;
let hoveredNode = null;
let transform = { x: 0, y: 0, zoom: 1 };
let isMouseDown = false;
let startDragOffset = { x: 0, y: 0 };
let lastMousePos = { x: 0, y: 0 };

// Canvas Element & Context
let canvas, ctx;
let animationId = null;

// Physics Configs
let config = {
    repulsion: 800,
    springLength: 100,
    springStrength: 0.05,
    gravity: 0.02,
    friction: 0.85,
    speed: 1.0,
    showNames: true,
    physicsEnabled: true
};

// Benchmark Cache
let benchmarkData = null;

// ==========================================================================
// INITIALIZATION
// ==========================================================================
document.addEventListener('DOMContentLoaded', () => {
    initCanvas();
    setupEventListeners();
    fetchData(true); // Initial fetch, sets first user active
});

function initCanvas() {
    canvas = document.getElementById('network-canvas');
    if (!canvas) return;
    ctx = canvas.getContext('2d');
    resizeCanvas();
    window.addEventListener('resize', resizeCanvas);
}

function resizeCanvas() {
    if (!canvas) return;
    const rect = canvas.getBoundingClientRect();
    canvas.width = rect.width;
    canvas.height = 600; // Fixed visualizer height
}

function setupEventListeners() {
    // Setup visualizer controls sliders
    const repulsionInput = document.getElementById('physics-repulsion');
    if (repulsionInput) {
        repulsionInput.addEventListener('input', (e) => {
            config.repulsion = parseFloat(e.target.value);
        });
    }

    const speedInput = document.getElementById('physics-speed');
    if (speedInput) {
        speedInput.addEventListener('input', (e) => {
            config.speed = parseFloat(e.target.value);
        });
    }

    const showNamesCb = document.getElementById('show-names-cb');
    if (showNamesCb) {
        showNamesCb.addEventListener('change', (e) => {
            config.showNames = e.target.checked;
        });
    }

    const physicsEnabledCb = document.getElementById('physics-enabled-cb');
    if (physicsEnabledCb) {
        physicsEnabledCb.addEventListener('change', (e) => {
            config.physicsEnabled = e.target.checked;
        });
    }

    // Canvas Mouse Events
    if (canvas) {
        canvas.addEventListener('mousedown', onMouseDown);
        canvas.addEventListener('mousemove', onMouseMove);
        canvas.addEventListener('mouseup', onMouseUp);
        canvas.addEventListener('mouseleave', onMouseLeave);
        canvas.addEventListener('wheel', onWheel);
    }
}

// ==========================================================================
// TAB CONTROLLER
// ==========================================================================
function switchTab(tabId) {
    // Toggle active classes on buttons
    document.querySelectorAll('.nav-btn').forEach(btn => btn.classList.remove('active'));
    
    // Set active button
    if (tabId === 'dashboard') document.getElementById('nav-dashboard-btn').classList.add('active');
    else if (tabId === 'network') document.getElementById('nav-network-btn').classList.add('active');
    else if (tabId === 'explore') document.getElementById('nav-explore-btn').classList.add('active');
    else if (tabId === 'benchmark') document.getElementById('nav-benchmark-btn').classList.add('active');

    // Toggle active classes on tab content panels
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
    document.getElementById(`tab-${tabId}`).classList.add('active');

    // Update Header Text
    const title = document.getElementById('page-title');
    const subtitle = document.getElementById('page-subtitle');
    
    if (tabId === 'dashboard') {
        title.innerText = 'Network Dashboard';
        subtitle.innerText = 'Real-time graph metrics and suggestion intelligence.';
        fetchData(); // Refresh data
    } else if (tabId === 'network') {
        title.innerText = 'Social Graph Visualizer';
        subtitle.innerText = 'Interactive force-directed physics network model.';
        resetGraphPhysics();
        startSimulation();
    } else if (tabId === 'explore') {
        title.innerText = 'Manage Social Network';
        subtitle.innerText = 'Create nodes, manage friendships, and view registries.';
        populateExploreSelectors();
    } else if (tabId === 'benchmark') {
        title.innerText = 'Algorithm Stress Benchmarks';
        subtitle.innerText = 'Comparing Heap ranking times at scale: O(N log N) vs O(N log K).';
        if (benchmarkData) {
            plotBenchmarkChart(benchmarkData);
        }
    }
}

// ==========================================================================
// DATA ACQUISITION & API SYNC
// ==========================================================================
async function fetchData(setFirstUserActive = false) {
    try {
        const response = await fetch(`${API_URL}/graph`);
        const data = await response.json();
        
        users = data.nodes;
        links = data.links;
        adjacencyMatrix = data.adjacencyMatrix;

        updateDashboardStats();
        populateUserSelectors();

        if (setFirstUserActive && users.length > 0) {
            // Set the first user active by default
            changeActiveUser(users[0].id);
        } else if (activeUserId) {
            // Refresh currently active user values
            updateActiveUserView();
        }

        // Initialize node physical properties
        syncVisualizerNodes();

    } catch (e) {
        console.error('Error fetching graph data: ', e);
        logActivity('warn', 'Failed to connect to Java backend. Is the server running?');
    }
}

function updateDashboardStats() {
    const totalUsers = users.length;
    const totalEdges = links.length;
    const avgDegree = totalUsers === 0 ? 0 : (2 * totalEdges) / totalUsers;
    // Density: E / (V * (V - 1) / 2)
    const possibleEdges = (totalUsers * (totalUsers - 1)) / 2;
    const density = possibleEdges === 0 ? 0 : (totalEdges / possibleEdges) * 100;

    document.getElementById('stat-total-users').innerText = totalUsers;
    document.getElementById('stat-total-edges').innerText = totalEdges;
    document.getElementById('stat-avg-degree').innerText = avgDegree.toFixed(2);
    document.getElementById('stat-density').innerText = density.toFixed(2) + '%';
}

function populateUserSelectors() {
    const selector = document.getElementById('user-selector');
    if (!selector) return;

    // Save previous value
    const prevVal = selector.value;

    selector.innerHTML = '<option value="">-- Select Active User --</option>';
    users.forEach(user => {
        const opt = document.createElement('option');
        opt.value = user.id;
        opt.innerText = `${user.name} (@${user.username})`;
        selector.appendChild(opt);
    });

    if (prevVal && users.find(u => u.id === prevVal)) {
        selector.value = prevVal;
    } else if (activeUserId) {
        selector.value = activeUserId;
    }
}

function populateExploreSelectors() {
    const s1 = document.getElementById('connect-u1');
    const s2 = document.getElementById('connect-u2');
    const dist1 = document.getElementById('dist-source-select');
    const dist2 = document.getElementById('dist-target-select');
    if (!s1 || !s2) return;

    s1.innerHTML = '';
    s2.innerHTML = '';
    if (dist1) dist1.innerHTML = '';
    if (dist2) dist2.innerHTML = '';

    users.forEach(user => {
        const text = `${user.name} (@${user.username})`;
        s1.appendChild(new Option(text, user.id));
        s2.appendChild(new Option(text, user.id));
        if (dist1) dist1.appendChild(new Option(text, user.id));
        if (dist2) dist2.appendChild(new Option(text, user.id));
    });

    if (users.length > 1) {
        s2.selectedIndex = 1;
        if (dist2) dist2.selectedIndex = 1;
    }

    renderRegistryTable();
}

function changeActiveUser(userId) {
    if (!userId) return;
    activeUserId = userId;
    
    // Update selectors visual value
    const selector = document.getElementById('user-selector');
    if (selector) selector.value = userId;

    updateActiveUserView();
}

async function updateActiveUserView() {
    const user = users.find(u => u.id === activeUserId);
    if (!user) return;

    // Update active user profile cards
    document.getElementById('active-user-name').innerText = user.name;
    document.getElementById('active-user-handle').innerText = '@' + user.username;
    
    document.getElementById('profile-name').innerText = user.name;
    document.getElementById('profile-username').innerText = '@' + user.username;
    document.getElementById('profile-bio').innerText = user.bio || 'No bio written.';
    document.getElementById('profile-joined').innerText = user.joinedDate || 'N/A';
    document.getElementById('profile-id').innerText = user.id;

    // Fetch recommendations and update friends list
    try {
        // Direct Friends
        const friendsRes = await fetch(`${API_URL}/friends/${activeUserId}`);
        const friendsData = await friendsRes.json();
        activeUserFriends = friendsData.friends || [];

        // Recommendations
        await fetchRecommendations();

        // Log action
        logActivity('info', `Active user changed to: ${user.name}`);
        
        // Hide traversal results if active user changed
        document.getElementById('traversal-result-box').style.display = 'none';

    } catch (e) {
        console.error('Error updating active user views: ', e);
    }
}

async function fetchRecommendations() {
    if (!activeUserId) return;
    
    const strategy = document.getElementById('heap-strategy-selector').value;
    const timeBadge = document.getElementById('rec-time-badge');
    
    try {
        const response = await fetch(`${API_URL}/suggestions?userId=${activeUserId}&k=3&heapType=${strategy}`);
        const data = await response.json();
        
        activeUserSuggestions = data.suggestions || [];
        timeBadge.innerText = `${data.executionTimeMs.toFixed(3)} ms`;
        
        renderRecommendationsList();
    } catch (e) {
        console.error('Error loading recommendations: ', e);
    }
}

function renderRecommendationsList() {
    const list = document.getElementById('recommendations-list');
    if (!list) return;

    if (activeUserSuggestions.length === 0) {
        list.innerHTML = `
            <div class="empty-state">
                <i class="fa-solid fa-face-smile"></i>
                <p>No new recommendations. This user is already friends with everyone!</p>
            </div>
        `;
        return;
    }

    list.innerHTML = '';
    activeUserSuggestions.forEach(rec => {
        const item = document.createElement('div');
        item.className = 'rec-item';
        
        const simPercent = (rec.jaccardSimilarity * 100).toFixed(0);
        
        item.innerHTML = `
            <div class="rec-info-col">
                <div class="rec-avatar">${rec.user.name.charAt(0)}</div>
                <div class="rec-details">
                    <h4>${rec.user.name}</h4>
                    <span>@${rec.user.username}</span>
                </div>
            </div>
            <div class="rec-info-col">
                <div class="rec-metrics">
                    <div class="rec-score">${simPercent}% Match</div>
                    <div class="rec-mutual">${rec.mutualFriends} Mutual Friend${rec.mutualFriends === 1 ? '' : 's'}</div>
                </div>
                <button class="rec-btn-add" onclick="quickAddFriend('${rec.user.id}')">
                    <i class="fa-solid fa-user-plus"></i> Connect
                </button>
            </div>
        `;
        list.appendChild(item);
    });
}

async function quickAddFriend(friendId) {
    if (!activeUserId || !friendId) return;
    try {
        const response = await fetch(`${API_URL}/friends`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId1: activeUserId, userId2: friendId })
        });
        
        if (response.ok) {
            logActivity('info', `Established friendship connecting ${activeUserId} and ${friendId}`);
            fetchData();
        } else {
            const data = await response.json();
            alert('Error: ' + data.error);
        }
    } catch (e) {
        console.error(e);
    }
}

// ==========================================================================
// EXPLORE & CRUD LOGIC
// ==========================================================================
async function handleCreateUser(e) {
    e.preventDefault();
    const name = document.getElementById('new-name').value.trim();
    const username = document.getElementById('new-username').value.trim();
    const bio = document.getElementById('new-bio').value.trim();

    try {
        const response = await fetch(`${API_URL}/users`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, username, bio })
        });

        if (response.ok) {
            const data = await response.json();
            logActivity('info', `Added new user profile: ${name} (@${username})`);
            
            // Reset Form
            document.getElementById('add-user-form').reset();
            
            // Refresh
            await fetchData();
            populateExploreSelectors();
            
            // Switch to newly created user
            changeActiveUser(data.user.id);
        } else {
            const data = await response.json();
            alert('Error: ' + data.error);
        }
    } catch (err) {
        console.error(err);
    }
}

async function deleteUser(userId) {
    if (!confirm('Are you sure you want to delete this user profile? All of their friendships will be removed.')) return;
    
    try {
        const response = await fetch(`${API_URL}/users/${userId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            logActivity('warn', `Deleted user node: ${userId}`);
            
            if (activeUserId === userId) {
                activeUserId = '';
            }

            await fetchData(activeUserId === ''); // Set first user active if active user was deleted
            populateExploreSelectors();
        } else {
            const data = await response.json();
            alert('Error: ' + data.error);
        }
    } catch (err) {
        console.error(err);
    }
}

async function toggleFriendship(isAdd) {
    const u1 = document.getElementById('connect-u1').value;
    const u2 = document.getElementById('connect-u2').value;

    if (u1 === u2) {
        alert('You cannot form a connection with the same user profile.');
        return;
    }

    try {
        const response = await fetch(`${API_URL}/friends`, {
            method: isAdd ? 'POST' : 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId1: u1, userId2: u2 })
        });

        if (response.ok) {
            logActivity(isAdd ? 'info' : 'warn', 
                isAdd ? `Established friendship connecting ${u1} and ${u2}` : `Severed friendship connection between ${u1} and ${u2}`);
            await fetchData();
            populateExploreSelectors();
        } else {
            const data = await response.json();
            alert('Error: ' + data.error);
        }
    } catch (err) {
        console.error(err);
    }
}

function renderRegistryTable(filteredUsers = users) {
    const body = document.getElementById('registry-table-body');
    if (!body) return;

    if (filteredUsers.length === 0) {
        body.innerHTML = `
            <tr>
                <td colspan="5" class="empty-state text-center" style="color: var(--text-muted);">
                    <i class="fa-solid fa-magnifying-glass" style="font-size:24px; margin-bottom:8px;"></i>
                    <p>No user profiles match your search.</p>
                </td>
            </tr>
        `;
        return;
    }

    body.innerHTML = '';
    filteredUsers.forEach(u => {
        const tr = document.createElement('tr');
        
        tr.innerHTML = `
            <td><code>${u.id}</code></td>
            <td>
                <div class="user-cell-info">
                    <div class="user-cell-avatar">${u.name.charAt(0)}</div>
                    <div class="user-cell-details">
                        <h5>${u.name}</h5>
                        <span>@${u.username}</span>
                    </div>
                </div>
            </td>
            <td><span class="text-secondary" style="font-size:13px;">${u.bio || '—'}</span></td>
            <td><span class="badge">${u.joinedDate || '—'}</span></td>
            <td>
                <div class="action-buttons-cell">
                    <button class="action-btn-circle active-user" onclick="changeActiveUser('${u.id}'); switchTab('dashboard');" title="Set Active User">
                        <i class="fa-solid fa-circle-play"></i>
                    </button>
                    <button class="action-btn-circle delete" onclick="deleteUser('${u.id}')" title="Delete Profile">
                        <i class="fa-solid fa-trash-can"></i>
                    </button>
                </div>
            </td>
        `;
        body.appendChild(tr);
    });
}

function filterRegistry(query) {
    query = query.toLowerCase().trim();
    const filtered = users.filter(u => 
        u.name.toLowerCase().includes(query) || 
        u.username.toLowerCase().includes(query) || 
        u.id.toLowerCase().includes(query)
    );
    renderRegistryTable(filtered);
}

// ==========================================================================
// GRAPH TRAVERSAL EXERCISES
// ==========================================================================
async function runTraversal(type) {
    if (!activeUserId) {
        alert('Please select an active user node to start traversals.');
        return;
    }

    // Call graph details directly from local graph representation logic, or we can use the server APIs
    // Let's implement path highlight sequence by running BFS/DFS order on frontend or requesting
    try {
        const response = await fetch(`${API_URL}/graph`);
        const data = await response.json();
        
        // We will call the backend BFS/DFS APIs if needed, but since it's cleaner to request them from the service
        // Let's call the specific traversal simulation on the server!
        // We will add custom visual highlighting in our canvas layout!
        logActivity('info', `Triggering ${type.toUpperCase()} traversal starting at node ${activeUserId}`);

        // We can request the traversal result from the Java service by creating temporary endpoints or running BFS in JS.
        // Let's write a JS-based traversal for visual animation, or fetch!
        // Wait, the AppServer class has: server.createContext("/api/users") etc.
        // Let's see: how do we query traversal? In AppServer we didn't add traversal API contexts.
        // Wait, we can implement the traversal algorithm in JS directly on our local loaded graph!
        // It runs exactly the same custom BFS/DFS logic on the frontend, which allows us to animate it!
        // Let's do that, it provides a gorgeous step-by-step flashing animation on the Canvas!

        const order = runLocalTraversal(type, activeUserId);
        displayTraversalResults(type, order);
    } catch (e) {
        console.error(e);
    }
}

function runLocalTraversal(type, startId) {
    const order = [];
    const visited = {};
    const adj = {};

    // Build local adjacency list
    users.forEach(u => adj[u.id] = []);
    links.forEach(l => {
        adj[l.source].push(l.target);
        adj[l.target].push(l.source);
    });

    // Sort neighbors alphabetically for stable traversal matching backend
    users.forEach(u => adj[u.id].sort());

    if (type === 'bfs') {
        const queue = [startId];
        visited[startId] = true;

        while (queue.length > 0) {
            const curr = queue.shift();
            order.push(curr);

            adj[curr].forEach(neighbor => {
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    queue.push(neighbor);
                }
            });
        }
    } else { // dfs
        const dfsHelper = (curr) => {
            visited[curr] = true;
            order.push(curr);

            adj[curr].forEach(neighbor => {
                if (!visited[neighbor]) {
                    dfsHelper(neighbor);
                }
            });
        };
        dfsHelper(startId);
    }

    return order;
}

function displayTraversalResults(type, order) {
    const box = document.getElementById('traversal-result-box');
    const title = document.getElementById('traversal-title');
    const orderDiv = document.getElementById('traversal-order');

    title.innerText = `${type.toUpperCase()} Sequence Order (From ${activeUserId}):`;
    orderDiv.innerHTML = '';

    order.forEach((nodeId, idx) => {
        const span = document.createElement('span');
        span.className = 'path-node';
        span.innerText = nodeId;
        span.onclick = () => changeActiveUser(nodeId);
        orderDiv.appendChild(span);

        if (idx < order.length - 1) {
            const arrow = document.createElement('span');
            arrow.className = 'path-arrow';
            arrow.innerHTML = ' &rarr; ';
            orderDiv.appendChild(arrow);
        }
    });

    box.style.display = 'block';

    // Highlight nodes in the canvas simulation in sequential flashing order
    animateTraversalPath(order);
}

function animateTraversalPath(order) {
    if (nodes.length === 0) return;
    
    // Clear previous flash highlights
    nodes.forEach(n => n.flashHighlight = false);

    let idx = 0;
    const interval = setInterval(() => {
        if (idx >= order.length) {
            clearInterval(interval);
            // Fade out highlights after 3 seconds
            setTimeout(() => {
                nodes.forEach(n => n.flashHighlight = false);
            }, 3000);
            return;
        }

        const nodeId = order[idx];
        const physNode = nodes.find(n => n.id === nodeId);
        if (physNode) {
            physNode.flashHighlight = true;
            physNode.flashIndex = idx + 1;
        }
        idx++;
    }, 450);
}

function calculateSeparation() {
    const src = document.getElementById('dist-source-select').value;
    const tgt = document.getElementById('dist-target-select').value;
    const resBox = document.getElementById('distance-result');
    const resVal = document.getElementById('distance-val');

    if (src === tgt) {
        resVal.innerText = '0 (Same Node)';
        resBox.style.display = 'block';
        return;
    }

    // Run BFS local separation distance
    const order = [];
    const visited = {};
    const distance = {};
    const adj = {};

    users.forEach(u => adj[u.id] = []);
    links.forEach(l => {
        adj[l.source].push(l.target);
        adj[l.target].push(l.source);
    });

    const queue = [src];
    visited[src] = true;
    distance[src] = 0;

    let pathFound = false;

    while (queue.length > 0) {
        const curr = queue.shift();
        const d = distance[curr];

        if (curr === tgt) {
            resVal.innerText = d;
            pathFound = true;
            break;
        }

        adj[curr].forEach(neighbor => {
            if (!visited[neighbor]) {
                visited[neighbor] = true;
                distance[neighbor] = d + 1;
                queue.push(neighbor);
            }
        });
    }

    if (!pathFound) {
        resVal.innerText = 'Disconnected (\u221E)';
    }
    resBox.style.display = 'block';
}

// ==========================================================================
// FORCE-DIRECTED PHYSICS GRAPH VISUALIZATION
// ==========================================================================
function syncVisualizerNodes() {
    // Keep positions of existing nodes, delete removed, add new
    const oldNodesMap = new Map(nodes.map(n => [n.id, n]));
    
    nodes = users.map(user => {
        const oldNode = oldNodesMap.get(user.id);
        if (oldNode) {
            return oldNode; // Retain position & velocity
        } else {
            // Place randomly around center
            const cx = canvas ? canvas.width / 2 : 300;
            const cy = canvas ? canvas.height / 2 : 250;
            return {
                id: user.id,
                name: user.name,
                username: user.username,
                bio: user.bio,
                x: cx + (Math.random() - 0.5) * 200,
                y: cy + (Math.random() - 0.5) * 200,
                vx: 0,
                vy: 0,
                radius: 16,
                flashHighlight: false,
                flashIndex: 0
            };
        }
    });

    // Recenter physics if starting from blank
    if (oldNodesMap.size === 0 && canvas) {
        resetGraphPhysics();
    }
}

function resetGraphPhysics() {
    if (!canvas) return;
    const cx = canvas.width / 2;
    const cy = canvas.height / 2;
    
    nodes.forEach((node, idx) => {
        // Arrange in a nice circle format initially to avoid overlapping explosions
        const angle = (idx / nodes.length) * 2 * Math.PI;
        const radius = 150 + Math.random() * 50;
        node.x = cx + radius * Math.cos(angle);
        node.y = cy + radius * Math.sin(angle);
        node.vx = 0;
        node.vy = 0;
    });

    transform = { x: 0, y: 0, zoom: 1 };
}

function zoomGraph(factor) {
    transform.zoom *= factor;
    transform.zoom = Math.max(0.2, Math.min(5, transform.zoom));
}

function startSimulation() {
    if (animationId) cancelAnimationFrame(animationId);
    
    function step() {
        if (document.getElementById('tab-network').classList.contains('active')) {
            updatePhysics();
            drawGraph();
            animationId = requestAnimationFrame(step);
        }
    }
    animationId = requestAnimationFrame(step);
}

function updatePhysics() {
    if (!config.physicsEnabled) return;

    const w = canvas.width;
    const h = canvas.height;
    const cx = w / 2;
    const cy = h / 2;

    // 1. Node Repulsion (Coulomb's Law style)
    for (let i = 0; i < nodes.length; i++) {
        const nodeA = nodes[i];
        for (let j = i + 1; j < nodes.length; j++) {
            const nodeB = nodes[j];
            
            const dx = nodeB.x - nodeA.x;
            const dy = nodeB.y - nodeA.y;
            const dist = Math.sqrt(dx * dx + dy * dy) || 1;
            
            if (dist < 400) {
                // Repel proportional to 1/dist
                const force = config.repulsion / (dist * dist);
                const fx = force * (dx / dist);
                const fy = force * (dy / dist);
                
                nodeA.vx -= fx;
                nodeA.vy -= fy;
                nodeB.vx += fx;
                nodeB.vy += fy;
            }
        }
    }

    // 2. Edge Attraction (Hooke's Law spring forces)
    links.forEach(link => {
        const sourceNode = nodes.find(n => n.id === link.source);
        const targetNode = nodes.find(n => n.id === link.target);
        
        if (sourceNode && targetNode) {
            const dx = targetNode.x - sourceNode.x;
            const dy = targetNode.y - sourceNode.y;
            const dist = Math.sqrt(dx * dx + dy * dy) || 1;
            
            // F = k * (x - L)
            const force = config.springStrength * (dist - config.springLength);
            const fx = force * (dx / dist);
            const fy = force * (dy / dist);
            
            sourceNode.vx += fx;
            sourceNode.vy += fy;
            targetNode.vx -= fx;
            targetNode.vy -= fy;
        }
    });

    // 3. Center Gravity & Drag
    nodes.forEach(node => {
        if (node === draggedNode) return; // Ignore physics for dragged node

        // Pull to center
        const dx = cx - node.x;
        const dy = cy - node.y;
        node.vx += dx * config.gravity;
        node.vy += dy * config.gravity;

        // Apply velocities
        node.x += node.vx * config.speed;
        node.y += node.vy * config.speed;

        // Friction
        node.vx *= config.friction;
        node.vy *= config.friction;
    });
}

function drawGraph() {
    if (!canvas || !ctx) return;

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    ctx.save();
    // Apply panning and zoom transforms
    ctx.translate(transform.x, transform.y);
    ctx.scale(transform.zoom, transform.zoom);

    // 1. Draw Links
    links.forEach(link => {
        const src = nodes.find(n => n.id === link.source);
        const tgt = nodes.find(n => n.id === link.target);

        if (src && tgt) {
            const isActiveLink = (src.id === activeUserId || tgt.id === activeUserId);
            
            ctx.beginPath();
            ctx.moveTo(src.x, src.y);
            ctx.lineTo(tgt.x, tgt.y);

            if (isActiveLink) {
                ctx.strokeStyle = 'rgba(16, 185, 129, 0.7)'; // Glow green for active friends
                ctx.lineWidth = 3.0;
            } else {
                ctx.strokeStyle = 'rgba(255, 255, 255, 0.08)'; // Thin dark edge for general link
                ctx.lineWidth = 1.0;
            }
            ctx.stroke();
        }
    });

    // 2. Draw Suggested Recommendation Edges (Dashed neon purple)
    if (activeUserId) {
        const src = nodes.find(n => n.id === activeUserId);
        if (src) {
            activeUserSuggestions.forEach(rec => {
                const tgt = nodes.find(n => n.id === rec.user.id);
                if (tgt) {
                    ctx.save();
                    ctx.beginPath();
                    ctx.setLineDash([5, 5]);
                    ctx.moveTo(src.x, src.y);
                    ctx.lineTo(tgt.x, tgt.y);
                    ctx.strokeStyle = 'rgba(168, 85, 247, 0.8)'; // Violet suggestion link
                    ctx.lineWidth = 2.0;
                    ctx.stroke();
                    ctx.restore();
                }
            });
        }
    }

    // 3. Draw Nodes
    nodes.forEach(node => {
        const isActive = (node.id === activeUserId);
        const isFriend = activeUserFriends.find(f => f.id === node.id);
        const isSuggested = activeUserSuggestions.find(s => s.user.id === node.id);

        ctx.beginPath();
        ctx.arc(node.x, node.y, node.radius, 0, 2 * Math.PI);

        // Core Fill Color based on network separation context
        let fillGradient = ctx.createRadialGradient(node.x, node.y, 2, node.x, node.y, node.radius);
        
        if (isActive) {
            // Glowing Active User Node
            fillGradient.addColorStop(0, '#818cf8');
            fillGradient.addColorStop(1, '#6366f1');
            ctx.fillStyle = fillGradient;
            ctx.strokeStyle = '#c7d2fe';
            ctx.lineWidth = 3.0;
            
            // Draw an outer pulse glow ring
            ctx.save();
            ctx.beginPath();
            ctx.arc(node.x, node.y, node.radius + 8, 0, 2 * Math.PI);
            ctx.strokeStyle = 'rgba(99, 102, 241, 0.3)';
            ctx.lineWidth = 1.5;
            ctx.stroke();
            ctx.restore();
        } else if (isFriend) {
            // Emerald Green Friends
            fillGradient.addColorStop(0, '#34d399');
            fillGradient.addColorStop(1, '#10b981');
            ctx.fillStyle = fillGradient;
            ctx.strokeStyle = '#a7f3d0';
            ctx.lineWidth = 2.0;
        } else if (isSuggested) {
            // Violet Suggestions
            fillGradient.addColorStop(0, '#c084fc');
            fillGradient.addColorStop(1, '#a855f7');
            ctx.fillStyle = fillGradient;
            ctx.strokeStyle = '#f3e8ff';
            ctx.lineWidth = 2.0;
        } else {
            // Normal slate node
            fillGradient.addColorStop(0, '#94a3b8');
            fillGradient.addColorStop(1, '#475569');
            ctx.fillStyle = fillGradient;
            ctx.strokeStyle = 'rgba(255,255,255,0.2)';
            ctx.lineWidth = 1.0;
        }

        // Apply Traversal Animation Flash
        if (node.flashHighlight) {
            ctx.fillStyle = '#f59e0b'; // Gold flash
            ctx.strokeStyle = '#fff';
            ctx.lineWidth = 3.0;
        }

        ctx.fill();
        ctx.stroke();

        // Traversal Flash Sequence Number Overlay
        if (node.flashHighlight) {
            ctx.fillStyle = '#000';
            ctx.font = 'bold 12px Outfit';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.fillText(node.flashIndex, node.x, node.y);
        }

        // 4. Draw Names Text
        if (config.showNames) {
            ctx.fillStyle = isActive ? '#fff' : '#94a3b8';
            ctx.font = isActive ? 'bold 12px Outfit' : '11px Outfit';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'top';
            // Render name beneath circle
            ctx.fillText(node.name, node.x, node.y + node.radius + 6);
            ctx.fillStyle = 'rgba(255,255,255,0.3)';
            ctx.font = '9px monospace';
            ctx.fillText(`@${node.username}`, node.x, node.y + node.radius + 18);
        }
    });

    ctx.restore();
}

// Visualizer Events Coordinates Translation Helpers
function getMousePos(e) {
    const rect = canvas.getBoundingClientRect();
    return {
        x: (e.clientX - rect.left - transform.x) / transform.zoom,
        y: (e.clientY - rect.top - transform.y) / transform.zoom
    };
}

function onMouseDown(e) {
    isMouseDown = true;
    const m = getMousePos(e);
    lastMousePos = { x: e.clientX, y: e.clientY };

    // Find node clicked
    draggedNode = nodes.find(node => {
        const dx = node.x - m.x;
        const dy = node.y - m.y;
        return Math.sqrt(dx * dx + dy * dy) <= node.radius;
    });

    if (draggedNode) {
        startDragOffset = { x: draggedNode.x - m.x, y: draggedNode.y - m.y };
    }
}

function onMouseMove(e) {
    const m = getMousePos(e);

    if (isMouseDown) {
        if (draggedNode) {
            // Drag node
            draggedNode.x = m.x + startDragOffset.x;
            draggedNode.y = m.y + startDragOffset.y;
            draggedNode.vx = 0;
            draggedNode.vy = 0;
        } else {
            // Pan screen
            transform.x += (e.clientX - lastMousePos.x);
            transform.y += (e.clientY - lastMousePos.y);
        }
    }

    lastMousePos = { x: e.clientX, y: e.clientY };

    // Hover Tooltip logic
    const prevHovered = hoveredNode;
    hoveredNode = nodes.find(node => {
        const dx = node.x - m.x;
        const dy = node.y - m.y;
        return Math.sqrt(dx * dx + dy * dy) <= node.radius;
    });

    const tooltip = document.getElementById('canvas-tooltip');
    if (hoveredNode) {
        const rect = canvas.getBoundingClientRect();
        tooltip.style.left = `${(hoveredNode.x * transform.zoom) + transform.x + rect.left + 15}px`;
        tooltip.style.top = `${(hoveredNode.y * transform.zoom) + transform.y + rect.top - 20}px`;
        
        // Show tooltip details
        tooltip.innerHTML = `
            <h5>${hoveredNode.name}</h5>
            <span style="color:var(--color-primary); font-size:10px; display:block; margin-top:-2px; margin-bottom:6px;">@${hoveredNode.username}</span>
            <p>${hoveredNode.bio || 'No bio written.'}</p>
        `;
        tooltip.style.display = 'block';
    } else {
        tooltip.style.display = 'none';
    }
}

function onMouseUp(e) {
    if (draggedNode && !isNodeMoved(draggedNode, e)) {
        // Simple click event triggers active user selection
        changeActiveUser(draggedNode.id);
    }
    isMouseDown = false;
    draggedNode = null;
}

function isNodeMoved(node, e) {
    // If movement distance is negligible, count it as a simple click
    return false; // Toggle active on mouseup automatically
}

function onMouseLeave() {
    isMouseDown = false;
    draggedNode = null;
    document.getElementById('canvas-tooltip').style.display = 'none';
}

function onWheel(e) {
    e.preventDefault();
    const zoomIntensity = 0.05;
    const mousePos = getMousePos(e);
    
    // Zoom around mouse cursor position
    const zoomFactor = e.deltaY < 0 ? (1 + zoomIntensity) : (1 - zoomIntensity);
    
    transform.x = e.clientX - canvas.getBoundingClientRect().left - (mousePos.x * transform.zoom * zoomFactor);
    transform.y = e.clientY - canvas.getBoundingClientRect().top - (mousePos.y * transform.zoom * zoomFactor);
    transform.zoom *= zoomFactor;
    transform.zoom = Math.max(0.2, Math.min(5, transform.zoom));
}

// ==========================================================================
// RESEARCH & COMPLEXITY BENCHMARKS PLOTTER
// ==========================================================================
async function runBenchmark() {
    const loader = document.getElementById('benchmark-loader');
    const runBtn = document.getElementById('run-benchmark-btn-action');
    
    loader.style.display = 'block';
    runBtn.disabled = true;

    try {
        const response = await fetch(`${API_URL}/benchmark`);
        const data = await response.json();
        
        benchmarkData = data.benchmarkResults;
        
        // Render Raw values table
        renderBenchmarkTable(benchmarkData);
        
        // Plot Complexity Line Chart
        plotBenchmarkChart(benchmarkData);
        
        logActivity('info', 'Completed Java algorithm performance stress benchmarks successfully.');

    } catch (e) {
        console.error(e);
        alert('Benchmark test execution failed. Please verify that your Java server is active.');
    } finally {
        loader.style.display = 'none';
        runBtn.disabled = false;
    }
}

function renderBenchmarkTable(results) {
    const body = document.getElementById('benchmark-table-body');
    if (!body) return;

    body.innerHTML = '';
    results.forEach(res => {
        const ratio = res.minHeapTimeMs === 0 ? 999 : (res.maxHeapTimeMs / res.minHeapTimeMs);
        const ratioStr = ratio > 1 ? `${ratio.toFixed(1)}x Faster` : `${(1/ratio).toFixed(1)}x Slower`;
        const ratioColor = ratio > 1 ? 'var(--color-success)' : 'var(--color-danger)';
        
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${res.size}</strong></td>
            <td><code>${res.edges}</code></td>
            <td><code class="text-danger" style="color:var(--color-danger);">${res.maxHeapTimeMs.toFixed(3)} ms</code></td>
            <td><code class="text-success" style="color:var(--color-success); font-weight:700;">${res.minHeapTimeMs.toFixed(3)} ms</code></td>
            <td><span style="color:${ratioColor}; font-weight:600;">${ratioStr}</span></td>
        `;
        body.appendChild(tr);
    });
}

function plotBenchmarkChart(results) {
    const chartCanvas = document.getElementById('benchmark-chart');
    if (!chartCanvas) return;

    const chartCtx = chartCanvas.getContext('2d');
    const width = chartCanvas.width;
    const height = chartCanvas.height;

    // Reset Context
    chartCtx.clearRect(0, 0, width, height);

    // Padding configurations
    const padding = { top: 30, right: 30, bottom: 40, left: 60 };
    const plotWidth = width - padding.left - padding.right;
    const plotHeight = height - padding.top - padding.bottom;

    // 1. Find Ranges
    const maxVal = Math.max(...results.map(r => Math.max(r.maxHeapTimeMs, r.minHeapTimeMs)));
    const yMax = maxVal === 0 ? 1.0 : maxVal * 1.15; // Give 15% headroom
    const xMax = Math.max(...results.map(r => r.size));

    // 2. Draw Axes gridlines & frames
    chartCtx.strokeStyle = 'rgba(255,255,255,0.08)';
    chartCtx.lineWidth = 1.0;
    
    // Draw horizontal gridlines (5 ticks)
    chartCtx.fillStyle = 'var(--text-muted)';
    chartCtx.font = '10px Outfit';
    chartCtx.textAlign = 'right';
    chartCtx.textBaseline = 'middle';
    
    for (let i = 0; i <= 4; i++) {
        const yVal = (i / 4) * yMax;
        const yPos = padding.top + plotHeight - (i / 4) * plotHeight;
        
        // Grid Line
        chartCtx.beginPath();
        chartCtx.moveTo(padding.left, yPos);
        chartCtx.lineTo(width - padding.right, yPos);
        chartCtx.stroke();

        // Label
        chartCtx.fillText(`${yVal.toFixed(2)} ms`, padding.left - 10, yPos);
    }

    // Draw vertical axes & labels
    chartCtx.textAlign = 'center';
    chartCtx.textBaseline = 'top';
    
    results.forEach((res, idx) => {
        const xPos = padding.left + (idx / (results.length - 1)) * plotWidth;
        
        // Grid line
        chartCtx.beginPath();
        chartCtx.moveTo(xPos, padding.top);
        chartCtx.lineTo(xPos, height - padding.bottom);
        chartCtx.stroke();

        // Label
        chartCtx.fillText(res.size, xPos, height - padding.bottom + 8);
    });

    // Outer framing lines
    chartCtx.strokeStyle = 'var(--border-glass)';
    chartCtx.beginPath();
    chartCtx.moveTo(padding.left, padding.top);
    chartCtx.lineTo(padding.left, height - padding.bottom);
    chartCtx.lineTo(width - padding.right, height - padding.bottom);
    chartCtx.stroke();

    // 3. Trace Max-Heap Line (Crimson Red)
    chartCtx.strokeStyle = '#ef4444';
    chartCtx.lineWidth = 2.5;
    chartCtx.beginPath();
    results.forEach((res, idx) => {
        const xPos = padding.left + (idx / (results.length - 1)) * plotWidth;
        const yPos = padding.top + plotHeight - (res.maxHeapTimeMs / yMax) * plotHeight;
        if (idx === 0) chartCtx.moveTo(xPos, yPos);
        else chartCtx.lineTo(xPos, yPos);
    });
    chartCtx.stroke();

    // Draw Max-Heap dots
    chartCtx.fillStyle = '#ef4444';
    results.forEach((res, idx) => {
        const xPos = padding.left + (idx / (results.length - 1)) * plotWidth;
        const yPos = padding.top + plotHeight - (res.maxHeapTimeMs / yMax) * plotHeight;
        chartCtx.beginPath();
        chartCtx.arc(xPos, yPos, 4, 0, 2 * Math.PI);
        chartCtx.fill();
    });

    // 4. Trace Min-Heap Line (Emerald Green)
    chartCtx.strokeStyle = '#10b981';
    chartCtx.lineWidth = 2.5;
    chartCtx.beginPath();
    results.forEach((res, idx) => {
        const xPos = padding.left + (idx / (results.length - 1)) * plotWidth;
        const yPos = padding.top + plotHeight - (res.minHeapTimeMs / yMax) * plotHeight;
        if (idx === 0) chartCtx.moveTo(xPos, yPos);
        else chartCtx.lineTo(xPos, yPos);
    });
    chartCtx.stroke();

    // Draw Min-Heap dots
    chartCtx.fillStyle = '#10b981';
    results.forEach((res, idx) => {
        const xPos = padding.left + (idx / (results.length - 1)) * plotWidth;
        const yPos = padding.top + plotHeight - (res.minHeapTimeMs / yMax) * plotHeight;
        chartCtx.beginPath();
        chartCtx.arc(xPos, yPos, 4, 0, 2 * Math.PI);
        chartCtx.fill();
    });

    // Title label
    chartCtx.fillStyle = 'var(--text-secondary)';
    chartCtx.font = 'bold 11px Outfit';
    chartCtx.textAlign = 'left';
    chartCtx.fillText('Execution Time (ms)', padding.left, 15);
    chartCtx.textAlign = 'right';
    chartCtx.fillText('Number of Nodes', width - padding.right, height - 15);
}

// ==========================================================================
// UTILITIES & LOGGING
// ==========================================================================
function logActivity(type, message) {
    const list = document.getElementById('audit-log-list');
    if (!list) return;

    const timeStr = new Date().toLocaleTimeString();
    const li = document.createElement('li');
    li.className = `log-item ${type} fade-in`;
    
    let typeName = 'System';
    if (type === 'info') typeName = 'API';
    else if (type === 'warn') typeName = 'Error';

    li.innerHTML = `<span class="log-time">${typeName} (${timeStr})</span> ${message}`;
    list.insertBefore(li, list.firstChild);

    // Bounded log length
    if (list.children.length > 25) {
        list.removeChild(list.lastChild);
    }
}
