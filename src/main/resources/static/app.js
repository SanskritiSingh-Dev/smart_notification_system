// Global State
let currentUser = null;
let currentCredentials = null;
let allRules = [];
let stompClient = null;
let myChart = null;
let fetchedNotificationsList = [];
let notifFilter = 'all';

// Base API URL — auto-detects local vs deployed
const API_URL = window.location.hostname === 'localhost'
    ? 'http://localhost:8081'
    : window.location.origin;

// Elements
const authContainer = document.getElementById('auth-container');
const dashboardContainer = document.getElementById('dashboard-container');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const adminSection = document.getElementById('admin-section');
const welcomeMessage = document.getElementById('welcome-message');
const roleBadge = document.getElementById('role-badge');
const notificationList = document.getElementById('notification-list');

// --- UI Helpers ---

function showToast(message, type = 'info') {
    const toastContainer = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    
    if (type === 'error') {
        toast.style.borderLeft = '4px solid var(--warning)';
    } else if (type === 'success') {
        toast.style.borderLeft = '4px solid #10b981';
    }

    toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function switchAuthTab(tab) {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    if (tab === 'login') {
        loginForm.classList.remove('hidden');
        registerForm.classList.add('hidden');
        document.querySelector('.tab:nth-child(1)').classList.add('active');
    } else {
        loginForm.classList.add('hidden');
        registerForm.classList.remove('hidden');
        document.querySelector('.tab:nth-child(2)').classList.add('active');
    }
}

function getAuthHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Basic ' + btoa(currentCredentials.email + ':' + currentCredentials.password)
    };
}

// --- Auth API ---

async function handleRegister(e) {
    e.preventDefault();
    const name = document.getElementById('register-name').value;
    const email = document.getElementById('register-email').value;
    const password = document.getElementById('register-password').value;
    const role = document.getElementById('register-role').value;

    try {
        const res = await fetch(`${API_URL}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password, role })
        });

        if (res.ok) {
            showToast('Registration successful! Please login.', 'success');
            switchAuthTab('login');
        } else {
            showToast('Registration failed.', 'error');
        }
    } catch (err) {
        showToast('Network error.', 'error');
    }
}

async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    // The backend uses Basic Auth for everything, so we'll store credentials
    // and try to fetch notifications to verify login.
    currentCredentials = { email, password };

    try {
        const res = await fetch(`${API_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        if (res.ok) {
            const user = await res.json();
            currentUser = { email: user.email, role: user.role, id: user.id };
            showDashboard();
        } else {
            showToast('Invalid credentials.', 'error');
            currentCredentials = null;
        }
    } catch (err) {
        showToast('Network error.', 'error');
    }
}

function logout() {
    // Clear Session
    currentUser = null;
    currentCredentials = null;
    fetchedNotificationsList = [];
    notifFilter = 'all';
    
    // Stop Live Feed
    if (stompClient) {
        stompClient.disconnect(() => {
            console.log("WebSocket Disconnected.");
        });
        stompClient = null;
    }

    // Reset UI
    loginForm.reset();
    registerForm.reset();
    authContainer.classList.remove('hidden');
    dashboardContainer.classList.add('hidden');
    
    showToast('Logged out successfully', 'info');
}

// --- Dashboard ---

function showDashboard() {
    authContainer.classList.add('hidden');
    dashboardContainer.classList.remove('hidden');
    
    // User info is an approximation since we didn't build a /me endpoint
    welcomeMessage.textContent = `Welcome, ${currentUser.email.split('@')[0]} (Your ID: ${currentUser.id})`;
    roleBadge.textContent = currentUser.role === 'ROLE_ADMIN' ? 'ADMIN' : 'USER';

    if (currentUser.role === 'ROLE_ADMIN') {
        adminSection.classList.remove('hidden');
        fetchRules(); // Fetch rules for the dropdown
        updateAnalyticsChart(); // Load initial stats
    } else {
        adminSection.classList.add('hidden');
    }

    fetchUserDetails();
    fetchNotifications();
    
    // --- Real-Time WebSockets ---
    if (stompClient) stompClient.disconnect();

    const socket = new SockJS(`${API_URL}/ws-notifications`);
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Hide STOMP noise from console

    stompClient.connect({}, () => {
        // Subscribe to a personalized topic for THIS specific user
        stompClient.subscribe(`/topic/notifications/${currentUser.id}`, (msg) => {
            const newNotif = JSON.parse(msg.body);
            showToast(`⚡ New Alert: ${newNotif.message}`, 'info');
            fetchNotifications(true); // Refresh silently
            
            // If admin, update the chart live!
            if (currentUser.role === 'ROLE_ADMIN') updateAnalyticsChart();
        });
        console.log("WebSocket Connected: Receiving Live Updates.");
    }, (err) => {
        console.error("WebSocket Error:", err);
    });
}

async function updateAnalyticsChart() {
    try {
        const res = await fetch(`${API_URL}/analytics/stats`, { headers: getAuthHeaders() });
        const stats = await res.json();
        
        const labels = stats.map(s => s.type || 'Unknown');
        const counts = stats.map(s => s.count);

        const ctx = document.getElementById('analyticsChart').getContext('2d');
        
        if (myChart) {
            myChart.data.labels = labels;
            myChart.data.datasets[0].data = counts;
            myChart.update();
        } else {
            myChart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Notifications by Type',
                        data: counts,
                        backgroundColor: ['#818cf8', '#f87171', '#34d399', '#fbbf24', '#a78bfa'],
                        borderWidth: 0,
                        borderRadius: 8
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false }
                    },
                    scales: {
                        y: { beginAtZero: true, grid: { color: 'rgba(255,255,255,0.1)' }, ticks: { color: '#94a3b8' } },
                        x: { grid: { display: false }, ticks: { color: '#94a3b8' } }
                    }
                }
            });
        }
    } catch (err) {
        console.error("Failed to update chart", err);
    }
}

async function fetchUserDetails() {
    try {
        const res = await fetch(`${API_URL}/users/${currentUser.id}`, { headers: getAuthHeaders() });
        const user = await res.json();
        if (user.discordWebhookUrl) {
            document.getElementById('user-webhook').value = user.discordWebhookUrl;
        }
    } catch (err) {
        console.error("Failed to fetch user details", err);
    }
}

async function handleUpdateWebhook() {
    const url = document.getElementById('user-webhook').value;
    
    try {
        const res = await fetch(`${API_URL}/users/${currentUser.id}/webhook`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(url)
        });

        if (res.ok) {
            showToast('Channel settings saved!', 'success');
        } else {
            showToast('Failed to save channel', 'error');
        }
    } catch (err) {
        showToast('Error saving channel', 'error');
    }
}

async function fetchNotifications(silent = false) {
    if (!silent) {
        notificationList.innerHTML = '<div class="empty-state">Loading...</div>';
    }
    
    try {
        let endpoint = currentUser.role === 'ROLE_ADMIN' 
            ? `${API_URL}/notifications` 
            : `${API_URL}/notifications/user/${currentUser.id}`;

        const res = await fetch(endpoint, { headers: getAuthHeaders() });
        const data = await res.json();

        // Update local cache
        fetchedNotificationsList = data || [];

        renderNotifications(fetchedNotificationsList);
    } catch (err) {
        if (!silent) {
            notificationList.innerHTML = '<div class="empty-state">Failed to load notifications.</div>';
        }
    }
}

function formatMessage(msg) {
    // Basic Markdown: Turn **text** into <strong>text</strong>
    return msg.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
}

function formatTime(dateTimeStr) {
    if (!dateTimeStr) return '';
    const date = new Date(dateTimeStr);
    
    let hours = date.getHours();
    let minutes = date.getMinutes();
    const ampm = hours >= 12 ? 'PM' : 'AM';
    hours = hours % 12;
    hours = hours ? hours : 12; // the hour '0' should be '12'
    minutes = minutes < 10 ? '0' + minutes : minutes;
    
    const timeStr = `${hours}:${minutes} ${ampm}`;
    
    // Check if it is today
    const today = new Date();
    if (date.toDateString() === today.toDateString()) {
        return timeStr;
    } else {
        const options = { month: 'short', day: 'numeric' };
        return `${timeStr}, ${date.toLocaleDateString(undefined, options)}`;
    }
}

function setNotifFilter(filter) {
    notifFilter = filter;
    document.querySelectorAll('.filter-tab').forEach(btn => btn.classList.remove('active'));
    
    const activeBtn = document.getElementById(`filter-${filter}`);
    if (activeBtn) {
        activeBtn.classList.add('active');
    }
    
    renderNotifications(fetchedNotificationsList);
}

function renderNotifications(notifications) {
    if (!notifications || notifications.length === 0) {
        notificationList.innerHTML = '<div class="empty-state">No notifications right now.</div>';
        return;
    }

    // Filter based on active filter state
    let filtered = notifications;
    if (notifFilter === 'unread') {
        filtered = notifications.filter(n => n.status === 'UNREAD');
    } else if (notifFilter === 'read') {
        filtered = notifications.filter(n => n.status === 'READ');
    }

    if (filtered.length === 0) {
        notificationList.innerHTML = `<div class="empty-state">No ${notifFilter} notifications right now.</div>`;
        return;
    }

    // Sort newest first
    filtered.sort((a, b) => new Date(b.createdTime) - new Date(a.createdTime));
    
    // Only show the 15 most recent notifications
    const recentNotifications = filtered.slice(0, 15);

    // Build the new HTML string first to avoid DOM flashing
    let newHTML = '';
    
    recentNotifications.forEach(notif => {
        const isUnread = notif.status === 'UNREAD';
        const targetUser = currentUser.role === 'ROLE_ADMIN' && notif.user ? ` (User #${notif.user.id})` : '';

        // Calculate formatted times
        const createdTimeStr = formatTime(notif.createdTime);
        const readTimeStr = notif.readTime ? formatTime(notif.readTime) : '';

        let timeString = '';
        if (isUnread) {
            timeString = `Delivered at ${createdTimeStr}`;
        } else {
            timeString = `Read at ${readTimeStr} (Delivered at ${createdTimeStr})`;
        }

        let buttonHtml = '';
        if (currentUser.role === 'ROLE_USER') {
            buttonHtml = isUnread 
                ? `<button class="btn secondary-btn small-btn" onclick="markAsRead(${notif.id})">Mark Read</button>` 
                : '';
        }

        const ticksClass = isUnread ? 'unread-ticks' : 'read-ticks';

        newHTML += `
            <div class="notification-item ${isUnread ? 'unread' : ''}">
                <div class="notification-content">
                    <p>${formatMessage(notif.message)}${targetUser}</p>
                    <span class="notification-meta">
                        <span class="ticks-container ${ticksClass}">✓✓</span>
                        <span class="time-text">${timeString}</span>
                    </span>
                </div>
                ${buttonHtml}
            </div>
        `;
    });

    // Only update the DOM if the content actually changed to prevent focus stealing/flashing
    if (notificationList.innerHTML !== newHTML) {
        notificationList.innerHTML = newHTML;
    }
}

// --- Modal Helper ---
function showConfirmModal(title, message, onConfirm) {
    const overlay = document.getElementById('modal-overlay');
    const titleEl = document.getElementById('modal-title');
    const msgEl = document.getElementById('modal-message');
    const confirmBtn = document.getElementById('modal-confirm');
    const cancelBtn = document.getElementById('modal-cancel');

    titleEl.textContent = title;
    msgEl.textContent = message;
    overlay.classList.remove('hidden');

    const handleConfirm = () => {
        onConfirm();
        overlay.classList.add('hidden');
        cleanup();
    };

    const handleCancel = () => {
        overlay.classList.add('hidden');
        cleanup();
    };

    const cleanup = () => {
        confirmBtn.removeEventListener('click', handleConfirm);
        cancelBtn.removeEventListener('click', handleCancel);
    };

    confirmBtn.addEventListener('click', handleConfirm);
    cancelBtn.addEventListener('click', handleCancel);
}

async function markAsRead(id) {
    try {
        const res = await fetch(`${API_URL}/notifications/${id}/read`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });

        if (res.ok) {
            fetchNotifications();
        }
    } catch (err) {
        showToast('Error marking as read', 'error');
    }
}

async function handleMarkAllRead() {
    showConfirmModal('Clear Everything?', 'This will mark all notifications as read and hide them.', async () => {
        try {
            const res = await fetch(`${API_URL}/notifications/read-all`, {
                method: 'PUT',
                headers: getAuthHeaders()
            });

            if (res.ok) {
                showToast('All notifications cleared!', 'success');
                fetchNotifications();
            }
        } catch (err) {
            showToast('Error clearing notifications', 'error');
        }
    });
}

// --- Admin APIs ---

async function handleCreateRule(e) {
    e.preventDefault();
    const name = document.getElementById('rule-name').value;
    const conditionType = document.getElementById('rule-type').value;
    const conditionValue = document.getElementById('rule-value').value;
    const messageTemplate = document.getElementById('rule-template').value;
    const targetChannel = document.getElementById('rule-target').value;
    const webhookUrl = document.getElementById('rule-webhook').value;

    try {
        const res = await fetch(`${API_URL}/rules`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ name, conditionType, conditionValue, messageTemplate, targetChannel, webhookUrl, active: true })
        });

        if (res.ok) {
            showToast('Rule created successfully!', 'success');
            e.target.reset();
            fetchRules(); // Refresh the dropdown
        } else {
            showToast('Failed to create rule', 'error');
        }
    } catch (err) {
        showToast('Network error.', 'error');
    }
}

async function fetchRules() {
    try {
        const res = await fetch(`${API_URL}/rules`, { headers: getAuthHeaders() });
        allRules = await res.json();
        
        // Populate Simulation Dropdown
        const select = document.getElementById('event-rule-select');
        select.innerHTML = '<option value="" disabled selected>Select a Rule to simulate...</option>';
        allRules.forEach(rule => {
            const option = document.createElement('option');
            option.value = rule.id;
            const target = rule.targetChannel ? ` → ${rule.targetChannel}` : '';
            option.textContent = `${rule.name}${target}`;
            select.appendChild(option);
        });

        // Populate Manage Rules List
        const ruleList = document.getElementById('rule-list');
        if (ruleList) {
            ruleList.innerHTML = '';
            allRules.forEach(rule => {
                const item = document.createElement('div');
                item.className = 'mini-item';
                const target = rule.targetChannel ? `<br><small style="color:var(--text-muted)">Target: ${rule.targetChannel}</small>` : '';
                item.innerHTML = `
                    <div>
                        <strong>${rule.name}</strong>
                        ${target}
                    </div>
                    <button class="delete-btn" onclick="deleteRule(${rule.id})">🗑️</button>
                `;
                ruleList.appendChild(item);
            });
        }
    } catch(err) {
        console.error("Failed to fetch rules", err);
    }
}

async function deleteRule(id) {
    showConfirmModal('Delete Rule?', 'Are you sure you want to delete this rule permanently?', async () => {
        try {
            const res = await fetch(`${API_URL}/rules/${id}`, {
                method: 'DELETE',
                headers: getAuthHeaders()
            });

            if (res.ok) {
                showToast('Rule deleted!', 'success');
                fetchRules();
            } else {
                showToast('Failed to delete rule', 'error');
            }
        } catch (err) {
            showToast('Error deleting rule', 'error');
        }
    });
}

function onRuleSelect() {
    const select = document.getElementById('event-rule-select');
    const ruleId = select.value;
    const rule = allRules.find(r => r.id == ruleId);
    
    if (rule) {
        document.getElementById('event-type').value = rule.conditionType;
        const targetDisplay = document.getElementById('target-display');
        targetDisplay.textContent = rule.targetChannel ? `📍 Sending to: ${rule.targetChannel}` : '📍 Sending to: Global Channel';
        
        // Suggest JSON format based on SpEL variables
        const matches = [...rule.conditionValue.matchAll(/#([a-zA-Z0-9_]+)/g)];
        let placeholderData = {};
        if (matches.length > 0) {
            matches.forEach(m => placeholderData[m[1]] = "value");
        } else {
            placeholderData = { "key": "value" };
        }
        
        document.getElementById('event-data-json').placeholder = `Event Data (JSON) - e.g. ${JSON.stringify(placeholderData)}`;
    }
}

async function handleTriggerEvent(e) {
    e.preventDefault();
    const eventType = document.getElementById('event-type').value;
    const referenceId = document.getElementById('event-ref').value;
    const jsonStr = document.getElementById('event-data-json').value || '{}';

    let data = {};
    try {
        data = JSON.parse(jsonStr);
    } catch (e) {
        showToast('Invalid JSON in data field!', 'error');
        return;
    }

    try {
        const res = await fetch(`${API_URL}/events`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }, // Event endpoint is permitAll()
            body: JSON.stringify({ eventType, referenceId, data })
        });

        if (res.ok) {
            showToast('Event triggered!', 'success');
            e.target.reset();
            // Refresh notifications if admin to see if it generated one
            fetchNotifications();
        } else {
            showToast('Failed to trigger event', 'error');
        }
    } catch (err) {
        showToast('Network error.', 'error');
    }
}

// Periodic UI Refresh for relative times (every 10 seconds)
setInterval(() => {
    if (currentUser && fetchedNotificationsList.length > 0) {
        renderNotifications(fetchedNotificationsList);
    }
}, 10000);
