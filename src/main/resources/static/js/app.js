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
            role: data.role,
            departmentId: data.departmentId || null,
            departmentName: data.departmentName || null
        }));
    },
    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/';
    },
    isLoggedIn: () => !!localStorage.getItem('token'),
    getRole: () => {
        const user = Auth.getUser();
        return user ? user.role : null;
    },
    requireAuth: () => {
        if (!Auth.isLoggedIn()) {
            window.location.href = '/';
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

            const text = await response.text();
            const data = text ? JSON.parse(text) : {};

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
        // Add transition class for smooth morph
        document.body.classList.add('theme-transitioning');
        document.documentElement.setAttribute('data-theme', next);
        localStorage.setItem('theme', next);
        // Remove transition class after animation completes
        setTimeout(() => document.body.classList.remove('theme-transitioning'), 600);
    }
};

// ===== ANIMATED COUNTER =====
function animateCounter(element, target, duration = 1200) {
    if (!element || isNaN(target)) return;
    const startTime = performance.now();
    const startVal = 0;

    function update(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        // Ease-out cubic for smooth deceleration
        const eased = 1 - Math.pow(1 - progress, 3);
        const value = Math.floor(startVal + (target - startVal) * eased);
        element.textContent = value;

        if (progress < 1) {
            requestAnimationFrame(update);
        } else {
            element.textContent = target;
            // Bounce effect on completion
            element.classList.add('counting');
            setTimeout(() => element.classList.remove('counting'), 500);
        }
    }

    requestAnimationFrame(update);
}

// ===== NAVIGATION =====
function initNavigation() {
    const user = Auth.getUser();
    if (!user) return;

    // Set user info in Top Nav
    const avatarEl = document.querySelector('.nav-avatar');
    if (avatarEl) {
        avatarEl.textContent = user.fullName.charAt(0).toUpperCase();
        
        // Display user ID for easier assignment
        const navUser = document.querySelector('.nav-user');
        if (navUser && !document.querySelector('.nav-user-id')) {
            const idBadge = document.createElement('div');
            idBadge.className = 'nav-user-id';
            idBadge.style.cssText = 'font-size: 0.75rem; font-weight: 700; color: var(--primary); margin-right: 8px; background: rgba(139, 92, 246, 0.1); padding: 4px 10px; border-radius: 12px; border: 1px solid rgba(139, 92, 246, 0.2); display: flex; align-items: center; gap: 4px;';
            idBadge.innerHTML = `<span style="opacity:0.7">ID:</span> <span>${user.userId}</span>`;
            navUser.insertBefore(idBadge, avatarEl);
        }
    }

    // Show/hide nav items based on role
    const role = user.role;
    document.querySelectorAll('[data-role]').forEach(el => {
        const roles = el.getAttribute('data-role').split(',');
        el.style.display = roles.includes(role) ? '' : 'none';
        if (el.style.display === 'none') {
            el.setAttribute('hidden', ''); // Accessibility
        }
    });

    // Set active nav link — works with both clean URLs (/dashboard) and .html paths
    const currentPath = window.location.pathname; // e.g. '/dashboard' or '/tickets'
    document.querySelectorAll('.nav-link').forEach(link => {
        const href = link.getAttribute('href');
        if (!href || href === '#') return;
        const hrefPath = href.split('?')[0]; // strip query params
        if (currentPath === hrefPath || (currentPath === '/' && hrefPath === '/dashboard')) {
            document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
            link.classList.add('active');
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

function timeAgo(dateStr) {
    if (!dateStr) return '';
    const now = new Date();
    const date = new Date(dateStr);
    const seconds = Math.floor((now - date) / 1000);

    if (seconds < 60) return 'Just now';
    if (seconds < 3600) return `${Math.floor(seconds / 60)}m ago`;
    if (seconds < 86400) return `${Math.floor(seconds / 3600)}h ago`;
    if (seconds < 604800) return `${Math.floor(seconds / 86400)}d ago`;
    return formatDate(dateStr);
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

// ===== SKELETON LOADERS =====
const Skeleton = {
    cards(count = 6) {
        return Array(count).fill('').map(() => `
            <div class="skeleton-card">
                <div style="display:flex;justify-content:space-between;margin-bottom:16px">
                    <div class="skeleton-text w-33" style="margin:0"></div>
                    <div class="skeleton-badge"></div>
                </div>
                <div class="skeleton-text w-75"></div>
                <div class="skeleton-text w-50"></div>
                <div style="margin-top:18px;display:flex;justify-content:space-between">
                    <div class="skeleton-text w-33" style="margin:0"></div>
                    <div class="skeleton-text w-33" style="margin:0"></div>
                </div>
            </div>
        `).join('');
    },

    table(rows = 5, cols = 5) {
        return Array(rows).fill('').map(() => `
            <tr>
                ${Array(cols).fill('').map(() => `
                    <td><div class="skeleton-text" style="margin:0;width:${60 + Math.random() * 40}%"></div></td>
                `).join('')}
            </tr>
        `).join('');
    }
};

// ===== CONFETTI CELEBRATION =====
const Confetti = {
    fire(duration = 2500) {
        const canvas = document.createElement('canvas');
        canvas.className = 'confetti-canvas';
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
        document.body.appendChild(canvas);

        const ctx = canvas.getContext('2d');
        const particles = [];
        const colors = ['#8B5CF6', '#EC4899', '#10B981', '#F59E0B', '#3B82F6', '#EF4444'];

        for (let i = 0; i < 120; i++) {
            particles.push({
                x: Math.random() * canvas.width,
                y: canvas.height + 10,
                size: Math.random() * 8 + 3,
                color: colors[Math.floor(Math.random() * colors.length)],
                speedX: (Math.random() - 0.5) * 8,
                speedY: -(Math.random() * 14 + 6),
                rotation: Math.random() * 360,
                rotSpeed: (Math.random() - 0.5) * 12,
                gravity: 0.12 + Math.random() * 0.08,
                opacity: 1
            });
        }

        const start = Date.now();
        function animate() {
            const elapsed = Date.now() - start;
            if (elapsed > duration) {
                canvas.remove();
                return;
            }

            ctx.clearRect(0, 0, canvas.width, canvas.height);

            particles.forEach(p => {
                p.x += p.speedX;
                p.y += p.speedY;
                p.speedY += p.gravity;
                p.rotation += p.rotSpeed;
                p.opacity = Math.max(0, 1 - elapsed / duration);

                ctx.save();
                ctx.translate(p.x, p.y);
                ctx.rotate(p.rotation * Math.PI / 180);
                ctx.globalAlpha = p.opacity;
                ctx.fillStyle = p.color;
                ctx.fillRect(-p.size / 2, -p.size / 2, p.size, p.size * 0.6);
                ctx.restore();
            });

            requestAnimationFrame(animate);
        }

        animate();
    }
};

// ===== CONFIRMATION MODAL =====
const Modal = {
    confirm(title, message, onConfirm, confirmText = 'Confirm') {
        const overlay = document.createElement('div');
        overlay.className = 'modal-overlay';
        overlay.innerHTML = `
            <div class="modal-content">
                <h3 style="font-size:1.2rem;font-weight:700;margin-bottom:12px">${title}</h3>
                <p style="color:var(--text-secondary);font-size:0.95rem;margin-bottom:24px;line-height:1.6">${message}</p>
                <div style="display:flex;gap:12px;justify-content:flex-end">
                    <button class="btn btn-outline btn-sm modal-cancel">Cancel</button>
                    <button class="btn btn-primary btn-sm modal-confirm">${confirmText}</button>
                </div>
            </div>
        `;

        document.body.appendChild(overlay);

        const close = () => {
            const content = overlay.querySelector('.modal-content');
            content.classList.add('closing');
            setTimeout(() => overlay.remove(), 200);
        };

        overlay.querySelector('.modal-cancel').onclick = close;
        overlay.querySelector('.modal-confirm').onclick = () => {
            close();
            if (onConfirm) onConfirm();
        };
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) close();
        });
    }
};

// ===== PAGE INIT =====
document.addEventListener('DOMContentLoaded', () => {
    ThemeManager.init();
    initNavigation();

    // Page loader → fade out
    const loader = document.querySelector('.page-loader');
    if (loader) {
        setTimeout(() => loader.classList.add('loaded'), 300);
    }

    // Page enter animation
    const mainContent = document.querySelector('.main-content');
    if (mainContent) {
        mainContent.classList.add('page-enter');
    }

    // Mobile hamburger menu
    const navContainer = document.querySelector('.nav-container');
    const navLinks = document.querySelector('.nav-links');
    if (navContainer && navLinks && !document.querySelector('.hamburger')) {
        const hamburger = document.createElement('button');
        hamburger.className = 'hamburger';
        hamburger.setAttribute('aria-label', 'Toggle navigation');
        hamburger.innerHTML = '<span></span><span></span><span></span>';
        // Insert after logo
        const logo = navContainer.querySelector('.nav-logo');
        if (logo) {
            logo.after(hamburger);
        }
        hamburger.addEventListener('click', () => {
            hamburger.classList.toggle('active');
            navLinks.classList.toggle('mobile-open');
        });
        // Close on nav link click
        navLinks.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', () => {
                hamburger.classList.remove('active');
                navLinks.classList.remove('mobile-open');
            });
        });
    }
});

