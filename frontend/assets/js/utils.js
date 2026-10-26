// ═══════════════════════════════════════════════════
// MINISTER OFFICE ERP - UTILITY FUNCTIONS
// ═══════════════════════════════════════════════════

const UTILS = {

    // ═══════════════════════════════════════
    // TOAST NOTIFICATIONS
    // ═══════════════════════════════════════

    showToast(message, type = 'info',
            duration = 3000) {

        let container = document
            .getElementById('toastContainer');

        if (!container) {
            container = document
                .createElement('div');
            container.id = 'toastContainer';
            container.className =
                'toast-container';
            document.body
                .appendChild(container);
        }

        const icons = {
            success: 'fa-check-circle',
            error:   'fa-times-circle',
            warning: 'fa-exclamation-triangle',
            info:    'fa-info-circle'
        };

        const toast = document
            .createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <div class="toast-icon">
                <i class="fas ${icons[type]
                    || icons.info}"></i>
            </div>
            <div class="toast-content">
                <p>${message}</p>
                <span>${this.getCurrentTime()}
                </span>
            </div>
            <button onclick="this
                .parentElement.remove()"
                style="background:none;
                border:none;cursor:pointer;
                color:#999;margin-left:8px;
                font-size:1rem;">
                <i class="fas fa-times"></i>
            </button>
        `;

        container.appendChild(toast);

        setTimeout(() => {
            if (toast.parentElement) {
                toast.style.animation =
                    'slideOutRight 0.3s ease';
                setTimeout(() => {
                    toast.remove();
                }, 300);
            }
        }, duration);
    },

    // ═══════════════════════════════════════
    // LOADING OVERLAY
    // ═══════════════════════════════════════

    showLoading(show, message = 'Loading...') {
        let overlay = document
            .getElementById('loadingOverlay');

        if (show) {
            if (!overlay) {
                overlay = document
                    .createElement('div');
                overlay.id = 'loadingOverlay';
                overlay.className =
                    'loading-overlay';
                overlay.innerHTML = `
                    <div class="spinner-large">
                    </div>
                    <p>${message}</p>
                `;
                document.body
                    .appendChild(overlay);
            }
        } else {
            if (overlay) {
                overlay.remove();
            }
        }
    },

    // ═══════════════════════════════════════
    // DATE & TIME UTILITIES
    // ═══════════════════════════════════════

    getCurrentTime() {
        return new Date().toLocaleTimeString(
            'en-IN',
            {
                hour: '2-digit',
                minute: '2-digit'
            });
    },

    getCurrentDate() {
        return new Date().toLocaleDateString(
            'en-IN',
            {
                day: '2-digit',
                month: 'short',
                year: 'numeric'
            });
    },

    formatDate(dateStr) {
        if (!dateStr) return '-';
        try {
            const date = new Date(dateStr);
            return date.toLocaleDateString(
                'en-IN',
                {
                    day: '2-digit',
                    month: 'short',
                    year: 'numeric'
                });
        } catch (e) {
            return dateStr;
        }
    },

    formatDateTime(dateStr) {
        if (!dateStr) return '-';
        try {
            const date = new Date(dateStr);
            return date.toLocaleString(
                'en-IN',
                {
                    day: '2-digit',
                    month: 'short',
                    year: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                });
        } catch (e) {
            return dateStr;
        }
    },

    getTimeAgo(dateStr) {
        if (!dateStr) return '-';
        try {
            const date = new Date(dateStr);
            const now = new Date();
            const diff = now - date;
            const minutes =
                Math.floor(diff / 60000);
            const hours =
                Math.floor(diff / 3600000);
            const days =
                Math.floor(diff / 86400000);

            if (minutes < 1)
                return 'Just now';
            if (minutes < 60)
                return `${minutes}m ago`;
            if (hours < 24)
                return `${hours}h ago`;
            return `${days}d ago`;
        } catch (e) {
            return dateStr;
        }
    },

    startClock() {
        const updateClock = () => {
            const timeEl = document
                .getElementById('currentTime');
            const dateEl = document
                .getElementById('currentDate');

            if (timeEl) {
                timeEl.textContent =
                    new Date()
                    .toLocaleTimeString(
                        'en-IN',
                        {
                            hour: '2-digit',
                            minute: '2-digit',
                            second: '2-digit'
                        });
            }
            if (dateEl) {
                dateEl.textContent =
                    this.getCurrentDate();
            }
        };

        updateClock();
        setInterval(updateClock, 1000);
    },

    // ═══════════════════════════════════════
    // STATUS & PRIORITY HELPERS
    // ═══════════════════════════════════════

    getStatusBadge(status) {
        const badges = {
            'NEW': '<span class="badge '
                + 'badge-new">🆕 New</span>',
            'ASSIGNED': '<span class="badge '
                + 'badge-assigned">'
                + '📋 Assigned</span>',
            'PENDING': '<span class="badge '
                + 'badge-pending">'
                + '⏳ Pending</span>',
            'ACTION_TAKEN': '<span class="badge '
                + 'badge-action">'
                + '✅ Action Taken</span>',
            'ESCALATED': '<span class="badge '
                + 'badge-escalated">'
                + '⚠️ Escalated</span>',
            'MINISTER_REVIEW':
                '<span class="badge '
                + 'badge-minister">'
                + '👁️ Minister Review</span>',
            'RESOLVED': '<span class="badge '
                + 'badge-resolved">'
                + '✔️ Resolved</span>',
            'CLOSED': '<span class="badge '
                + 'badge-closed">'
                + '🔒 Closed</span>'
        };
        return badges[status]
            || `<span class="badge">`
            + `${status}</span>`;
    },

    getPriorityBadge(priority) {
        const badges = {
            'LOW': '<span class="badge '
                + 'badge-low">🟢 Low</span>',
            'MEDIUM': '<span class="badge '
                + 'badge-medium">'
                + '🟡 Medium</span>',
            'HIGH': '<span class="badge '
                + 'badge-high">'
                + '🟠 High</span>',
            'CRITICAL': '<span class="badge '
                + 'badge-critical">'
                + '🔴 Critical</span>'
        };
        return badges[priority]
            || `<span class="badge">`
            + `${priority}</span>`;
    },

    getAppointmentBadge(status) {
        const badges = {
            'PENDING': '<span class="badge '
                + 'badge-pending">'
                + '⏳ Pending</span>',
            'APPROVED': '<span class="badge '
                + 'badge-action">'
                + '✅ Approved</span>',
            'REJECTED': '<span class="badge '
                + 'badge-escalated">'
                + '❌ Rejected</span>'
        };
        return badges[status]
            || `<span class="badge">`
            + `${status}</span>`;
    },

    // ═══════════════════════════════════════
    // STRING UTILITIES
    // ═══════════════════════════════════════

    getInitials(name) {
        if (!name) return '?';
        return name
            .split(' ')
            .slice(0, 2)
            .map(word => word[0])
            .join('')
            .toUpperCase();
    },

    truncate(text, length = 50) {
        if (!text) return '-';
        return text.length > length
            ? text.substring(0, length) + '...'
            : text;
    },

    capitalize(str) {
        if (!str) return '';
        return str.charAt(0).toUpperCase()
            + str.slice(1).toLowerCase();
    },

    // ═══════════════════════════════════════
    // FORM UTILITIES
    // ═══════════════════════════════════════

    getFormData(formId) {
        const form = document
            .getElementById(formId);
        if (!form) return {};

        const formData = new FormData(form);
        const data = {};
        formData.forEach((value, key) => {
            data[key] = value;
        });
        return data;
    },

    resetForm(formId) {
        const form = document
            .getElementById(formId);
        if (form) form.reset();
    },

    setFieldError(fieldId, message) {
        const field = document
            .getElementById(fieldId);
        if (field) {
            field.classList.add('is-invalid');
            const error = document
                .createElement('div');
            error.className = 'field-error';
            error.style.cssText =
                'color:#DC3545;font-size:'
                + '0.78rem;margin-top:4px;';
            error.textContent = message;
            field.parentElement
                .appendChild(error);
        }
    },

    clearFieldErrors(formId) {
        const form = document
            .getElementById(formId);
        if (!form) return;

        form.querySelectorAll('.is-invalid')
            .forEach(el => {
                el.classList
                    .remove('is-invalid');
            });

        form.querySelectorAll('.field-error')
            .forEach(el => el.remove());
    },

    // ═══════════════════════════════════════
    // TABLE UTILITIES
    // ═══════════════════════════════════════

    populateTable(tableId, data, renderRow) {
        const tbody = document
            .querySelector(
                `#${tableId} tbody`);
        if (!tbody) return;

        if (!data || data.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="100%"
                        style="text-align:center;
                        padding:40px;color:#999;">
                        <i class="fas fa-inbox"
                            style="font-size:2rem;
                            display:block;
                            margin-bottom:8px;">
                        </i>
                        No records found
                    </td>
                </tr>`;
            return;
        }

        tbody.innerHTML = data
            .map((item, index) =>
                renderRow(item, index))
            .join('');
    },

    // ═══════════════════════════════════════
    // MODAL UTILITIES
    // ═══════════════════════════════════════

    openModal(modalId) {
        const modal = document
            .getElementById(modalId);
        if (modal) {
            modal.classList.add('active');
            document.body.style
                .overflow = 'hidden';
        }
    },

    closeModal(modalId) {
        const modal = document
            .getElementById(modalId);
        if (modal) {
            modal.classList.remove('active');
            document.body.style
                .overflow = '';
        }
    },

    closeAllModals() {
        document.querySelectorAll(
            '.modal-overlay.active')
            .forEach(modal => {
                modal.classList
                    .remove('active');
            });
        document.body.style.overflow = '';
    },

    // ── Close modal on overlay click
    initModalClose() {
        document.querySelectorAll(
            '.modal-overlay')
            .forEach(overlay => {
                overlay.addEventListener(
                    'click', (e) => {
                    if (e.target === overlay) {
                        this.closeAllModals();
                    }
                });
            });

        // Close on Escape key
        document.addEventListener(
            'keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeAllModals();
            }
        });
    },

    // ═══════════════════════════════════════
    // SIDEBAR UTILITIES
    // ═══════════════════════════════════════

    initSidebar() {
        const toggle = document
            .getElementById('sidebarToggle');
        const sidebar = document
            .getElementById('sidebar');
        const mainContent = document
            .getElementById('mainContent');

        if (!toggle || !sidebar) return;

        toggle.addEventListener('click', () => {
            sidebar.classList
                .toggle('collapsed');
            mainContent?.classList
                .toggle('expanded');

            // Save state
            localStorage.setItem(
                'sidebar_collapsed',
                sidebar.classList
                    .contains('collapsed'));
        });

        // Restore state
        const isCollapsed = localStorage
            .getItem('sidebar_collapsed')
            === 'true';
        if (isCollapsed) {
            sidebar.classList.add('collapsed');
            mainContent?.classList
                .add('expanded');
        }

        // Mobile toggle
        const mobileToggle = document
            .getElementById(
                'mobileSidebarToggle');
        if (mobileToggle) {
            mobileToggle.addEventListener(
                'click', () => {
                sidebar.classList
                    .toggle('mobile-open');
            });
        }

        // Close sidebar on mobile
        // when clicking outside
        document.addEventListener(
            'click', (e) => {
            if (window.innerWidth <= 992) {
                if (!sidebar.contains(e.target)
                    && !toggle.contains(
                        e.target)) {
                    sidebar.classList
                        .remove('mobile-open');
                }
            }
        });
    },

    // ── Set active nav item
    setActiveNav(pageName) {
        document.querySelectorAll('.nav-item')
            .forEach(item => {
                item.classList
                    .remove('active');
                if (item.dataset.page
                        === pageName) {
                    item.classList
                        .add('active');
                }
            });
    },

    // ═══════════════════════════════════════
    // NOTIFICATION UTILITIES
    // ═══════════════════════════════════════

    async loadNotificationCount() {
        try {
            const result = await API
                .notifications
                .getUnreadCount();

            if (result && result.success) {
                const count = result.data;
                const badge = document
                    .getElementById(
                        'notifCount');
                if (badge) {
                    badge.textContent =
                        count > 0 ? count : '';
                    badge.style.display =
                        count > 0
                            ? 'flex'
                            : 'none';
                }
            }
        } catch (e) {
            console.log(
                'Notification count error:',
                e);
        }
    },

    // ── Load notifications panel
    async loadNotifications() {
        try {
            const result = await API
                .notifications.getMy();

            if (result && result.success) {
                const list = document
                    .getElementById(
                        'notificationList');
                if (!list) return;

                const notifications =
                    result.data;

                if (!notifications
                        || notifications.length
                        === 0) {
                    list.innerHTML = `
                        <div class="empty-state"
                            style="padding:30px">
                            <i class="fas
                                fa-bell-slash">
                            </i>
                            <p>No notifications
                            </p>
                        </div>`;
                    return;
                }

                list.innerHTML =
                    notifications
                    .slice(0, 20)
                    .map(n => `
                    <div class="notification-item
                        ${!n.read
                            ? 'unread' : ''}"
                        onclick="UTILS
                        .markNotifRead(
                        '${n.notificationId}',
                        this)">
                        <div class="notification-icon">
                            <i class="fas
                                ${this
                                .getNotifIcon(
                                n.type)}">
                            </i>
                        </div>
                        <div class="notification-text">
                            <h4>${n.title}</h4>
                            <p>${this.truncate(
                                n.message, 60)}
                            </p>
                            <span>${this
                                .getTimeAgo(
                                n.createdAt)}
                            </span>
                        </div>
                    </div>
                `).join('');
            }
        } catch (e) {
            console.log(
                'Load notifications error:', e);
        }
    },

    async markNotifRead(id, element) {
        await API.notifications.markRead(id);
        element.classList.remove('unread');
        this.loadNotificationCount();
    },

    getNotifIcon(type) {
        const icons = {
            'ASSIGNMENT':  'fa-clipboard-list',
            'ESCALATION':  'fa-exclamation-triangle',
            'FORWARDING':  'fa-share',
            'REMINDER':    'fa-bell',
            'DIRECTION':   'fa-directions',
            'APPOINTMENT': 'fa-calendar-check',
            'RESOLVED':    'fa-check-circle'
        };
        return icons[type] || 'fa-bell';
    },

    toggleNotificationPanel() {
        const panel = document
            .getElementById(
                'notificationPanel');
        if (panel) {
            panel.classList.toggle('open');
            if (panel.classList
                    .contains('open')) {
                this.loadNotifications();
            }
        }
    },

    // ═══════════════════════════════════════
    // SEARCH & FILTER UTILITIES
    // ═══════════════════════════════════════

    debounce(func, wait) {
        let timeout;
        return function executedFunction(
                ...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },

    // ── Search table rows
    searchTable(inputId, tableId) {
        const input = document
            .getElementById(inputId);
        const table = document
            .getElementById(tableId);

        if (!input || !table) return;

        input.addEventListener('input',
            this.debounce((e) => {
                const query = e.target.value
                    .toLowerCase();
                const rows = table
                    .querySelectorAll('tbody tr');

                rows.forEach(row => {
                    const text = row
                        .textContent
                        .toLowerCase();
                    row.style.display =
                        text.includes(query)
                            ? ''
                            : 'none';
                });
            }, 300));
    },

    // ── Filter by select
    filterTable(selectId, tableId,
            columnIndex) {
        const select = document
            .getElementById(selectId);
        const table = document
            .getElementById(tableId);

        if (!select || !table) return;

        select.addEventListener('change',
            (e) => {
            const value = e.target.value
                .toLowerCase();
            const rows = table
                .querySelectorAll('tbody tr');

            rows.forEach(row => {
                const cell =
                    row.cells[columnIndex];
                if (!cell) return;

                const cellText = cell
                    .textContent.toLowerCase();
                row.style.display =
                    !value
                    || cellText.includes(value)
                        ? '' : 'none';
            });
        });
    },

    // ═══════════════════════════════════════
    // VALIDATION UTILITIES
    // ═══════════════════════════════════════

    validateMobile(mobile) {
        return /^[6-9]\d{9}$/.test(mobile);
    },

    validateEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/
            .test(email);
    },

    validateRequired(value) {
        return value !== null
            && value !== undefined
            && value.toString().trim() !== '';
    },

    // ═══════════════════════════════════════
    // PRINT UTILITY
    // ═══════════════════════════════════════

    printSection(sectionId) {
        const section = document
            .getElementById(sectionId);
        if (!section) return;

        const printWindow =
            window.open('', '_blank');
        printWindow.document.write(`
            <html>
            <head>
                <title>Minister Office ERP
                    - Print</title>
                <style>
                    body {
                        font-family: Arial,
                            sans-serif;
                        font-size: 12px;
                        padding: 20px;
                    }
                    table {
                        width: 100%;
                        border-collapse:
                            collapse;
                    }
                    th, td {
                        border: 1px solid #ddd;
                        padding: 8px;
                        text-align: left;
                    }
                    th {
                        background: #FF6600;
                        color: white;
                    }
                    .badge {
                        padding: 2px 8px;
                        border-radius: 10px;
                        font-size: 11px;
                    }
                    @media print {
                        button { display: none; }
                    }
                </style>
            </head>
            <body>
                <h2 style="color:#003399">
                    Minister Office ERP
                </h2>
                <p>Printed: ${this
                    .getCurrentDate()}
                    ${this.getCurrentTime()}
                </p>
                <hr/>
                ${section.innerHTML}
            </body>
            </html>
        `);
        printWindow.document.close();
        printWindow.print();
    },

    // ═══════════════════════════════════════
    // CONFIRM DIALOG
    // ═══════════════════════════════════════

    confirm(message, onConfirm,
            title = 'Confirm Action') {

        let dialog = document
            .getElementById('confirmDialog');

        if (!dialog) {
            dialog = document
                .createElement('div');
            dialog.id = 'confirmDialog';
            dialog.className = 'modal-overlay';
            dialog.innerHTML = `
                <div class="modal"
                    style="max-width:400px">
                    <div class="modal-header">
                        <h3>
                            <i class="fas
                                fa-question-circle">
                            </i>
                            <span id="confirmTitle">
                            </span>
                        </h3>
                    </div>
                    <div class="modal-body">
                        <p id="confirmMessage"
                            style="font-size:
                            0.95rem;color:#333">
                        </p>
                    </div>
                    <div class="modal-footer">
                        <button class="btn
                            btn-outline"
                            id="confirmCancel">
                            Cancel
                        </button>
                        <button class="btn
                            btn-primary"
                            id="confirmOk">
                            Confirm
                        </button>
                    </div>
                </div>`;
            document.body
                .appendChild(dialog);
        }

        document.getElementById(
            'confirmTitle')
            .textContent = title;
        document.getElementById(
            'confirmMessage')
            .textContent = message;

        dialog.classList.add('active');

        document.getElementById('confirmOk')
            .onclick = () => {
            dialog.classList.remove('active');
            onConfirm();
        };

        document.getElementById(
            'confirmCancel').onclick = () => {
            dialog.classList.remove('active');
        };
    },

    // ═══════════════════════════════════════
    // INITIALIZE ALL UTILITIES
    // ═══════════════════════════════════════

    init() {
        this.startClock();
        this.initSidebar();
        this.initModalClose();
        AUTH.loadUserInfo();
        this.loadNotificationCount();

        // Auto refresh notifications
        setInterval(() => {
            this.loadNotificationCount();
        }, 30000);
    }
};

// ── Auto init when DOM ready
document.addEventListener(
    'DOMContentLoaded', () => {
    // Only init if user is logged in
    if (AUTH.isLoggedIn()) {
        UTILS.init();
    }
});