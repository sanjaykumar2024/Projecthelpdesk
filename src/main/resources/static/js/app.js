// ===== Multi-Department Helpdesk - Core App JS =====

const API_BASE = '/api';

// ===== AUTH HELPERS =====
const Auth = {
    getToken: () => localStorage.getItem('token'),
    getUser: () => JSON.parse(localStorage.getItem('user') || 'null'),
    setAuth: (data) => {
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify({
            userId: data.userId,
            email: data.email,
            fullName: data.fullName,
            role: data.role
        }));
    },
    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/index.html';
    },
    isLoggedIn: () => !!localStorage.getItem('token'),
    getRole: () => {
        const user = Auth.getUser();
        return user ? user.role : null;
    },
    requireAuth: () => {
        if (!Auth.isLoggedIn()) {
            window.location.href = '/index.html';
            return false;
        }
        return true;
    }
};

// ===== API SERVICE =====
const API = {
    headers: () => ({
        'Content-Type': 'application/json',
        ...(Auth.getToken() ? { 'Authorization': `Bearer ${Auth.getToken()}` } : {})
    }),

    async request(method, url, body = null) {
        try {
            const options = {
                method,
                headers: this.headers()
            };
            if (body) options.body = JSON.stringify(body);

            const response = await fetch(`${API_BASE}${url}`, options);

            if (response.status === 401 || response.status === 403) {
                if (url !== '/auth/login' && url !== '/auth/register') {
                    Auth.logout();
                    return;
                }
            }

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || data.error || 'Request failed');
            }

            return data;
        } catch (error) {
            throw error;
        }
    },

    get: (url) => API.request('GET', url),
    post: (url, body) => API.request('POST', url, body),
    put: (url, body) => API.request('PUT', url, body),
    patch: (url, body) => API.request('PATCH', url, body),
    delete: (url) => API.request('DELETE', url)
};

// ===== TOAST NOTIFICATIONS =====
const Toast = {
    container: null,

    init() {
        if (!this.container) {
            this.container = document.createElement('div');
            this.container.className = 'toast-container';
            document.body.appendChild(this.container);
        }
    },

    show(message, type = 'info', duration = 4000) {
        this.init();
        const icons = {
            success: '✅',
            error: '❌',
            info: 'ℹ️',
            warning: '⚠️'
        };

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerHTML = `
            <span class="toast-icon">${icons[type]}</span>
            <span class="toast-message">${message}</span>
            <button class="toast-close" onclick="this.parentElement.remove()">✕</button>
        `;

        this.container.appendChild(toast);

        setTimeout(() => {
            toast.classList.add('toast-exit');
            setTimeout(() => toast.remove(), 300);
        }, duration);
    },

    success: (msg) => Toast.show(msg, 'success'),
    error: (msg) => Toast.show(msg, 'error'),
    info: (msg) => Toast.show(msg, 'info'),
    warning: (msg) => Toast.show(msg, 'warning')
};

// ===== DARK MODE =====
const ThemeManager = {
    init() {
        const theme = localStorage.getItem('theme') || 'light';
        document.documentElement.setAttribute('data-theme', theme);
    },

    toggle() {
        const current = document.documentElement.getAttribute('data-theme');
        const next = current === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', next);
        localStorage.setItem('theme', next);
    }
};

// ===== ANIMATED COUNTER =====
function animateCounter(element, target, duration = 1500) {
    let start = 0;
    const increment = target / (duration / 16);
    const timer = setInterval(() => {
        start += increment;
        if (start >= target) {
            element.textContent = target;
            clearInterval(timer);
        } else {
            element.textContent = Math.floor(start);
        }
    }, 16);
}

// ===== SIDEBAR =====
function initSidebar() {
    const user = Auth.getUser();
    if (!user) return;

    const sidebar = document.getElementById('sidebar');
    if (!sidebar) return;

    // Set user info
    const avatarEl = sidebar.querySelector('.user-avatar');
    const nameEl = sidebar.querySelector('.user-name');
    const roleEl = sidebar.querySelector('.user-role');
    if (avatarEl) avatarEl.textContent = user.fullName.charAt(0).toUpperCase();
    if (nameEl) nameEl.textContent = user.fullName;
    if (roleEl) roleEl.textContent = user.role;

    // Show/hide nav items based on role
    const role = user.role;
    document.querySelectorAll('[data-role]').forEach(el => {
        const roles = el.getAttribute('data-role').split(',');
        el.style.display = roles.includes(role) ? '' : 'none';
    });

    // Set active nav link
    const currentPage = window.location.pathname.split('/').pop();
    sidebar.querySelectorAll('.sidebar-nav a').forEach(link => {
        const href = link.getAttribute('href');
        if (href === currentPage || (href && currentPage.startsWith(href.split('?')[0]))) {
            link.classList.add('active');
        }
    });

    // Restore collapsed state from localStorage (desktop only)
    const isMobile = () => window.innerWidth <= 768;
    if (!isMobile() && localStorage.getItem('sidebar-collapsed') === 'true') {
        document.body.classList.add('sidebar-collapsed');
    }

    // Sidebar toggle button
    const toggleBtn = document.getElementById('sidebarToggle');
    const overlay = document.getElementById('sidebarOverlay');

    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            if (isMobile()) {
                // Mobile: overlay mode
                sidebar.classList.toggle('mobile-open');
                if (overlay) overlay.classList.toggle('active');
            } else {
                // Desktop: collapse mode
                document.body.classList.toggle('sidebar-collapsed');
                const collapsed = document.body.classList.contains('sidebar-collapsed');
                localStorage.setItem('sidebar-collapsed', collapsed);
            }
        });
    }

    // Close sidebar when clicking overlay (mobile)
    if (overlay) {
        overlay.addEventListener('click', () => {
            sidebar.classList.remove('mobile-open');
            overlay.classList.remove('active');
        });
    }

    // Handle window resize: clean up states
    window.addEventListener('resize', () => {
        if (!isMobile()) {
            sidebar.classList.remove('mobile-open');
            if (overlay) overlay.classList.remove('active');
        }
    });
}

// ===== UTILITY FUNCTIONS =====
function formatDate(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function getStatusBadge(status) {
    const labels = {
        'OPEN': 'Open',
        'IN_PROGRESS': 'In Progress',
        'ON_HOLD': 'On Hold',
        'RESOLVED': 'Resolved',
        'CLOSED': 'Closed'
    };
    return `<span class="badge badge-${status.toLowerCase()}">${labels[status] || status}</span>`;
}

function getPriorityBadge(priority) {
    return `<span class="badge badge-${priority.toLowerCase()}">${priority}</span>`;
}

// ===== PAGE INIT =====
document.addEventListener('DOMContentLoaded', () => {
    ThemeManager.init();
    initSidebar();

    // Page loader hide
    const loader = document.querySelector('.page-loader');
    if (loader) {
        setTimeout(() => loader.classList.add('hidden'), 500);
    }
});
