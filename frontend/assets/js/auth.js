// ═══════════════════════════════════════════════════
// MINISTER OFFICE ERP - AUTH HANDLER
// ═══════════════════════════════════════════════════

const AUTH = {

    // ── Storage Keys
    TOKEN_KEY: 'erp_token',
    USER_KEY: 'erp_user',
    ROLE_KEY: 'erp_role',

    // ── Save login data
    saveSession(data) {
        localStorage.setItem(
            this.TOKEN_KEY,
            data.token);
        localStorage.setItem(
            this.USER_KEY,
            JSON.stringify(data));
        localStorage.setItem(
            this.ROLE_KEY,
            data.role);
    },

    // ── Get current user
    getUser() {
        const user = localStorage
            .getItem(this.USER_KEY);
        return user ? JSON.parse(user) : null;
    },

    // ── Get current role
    getRole() {
        return localStorage
            .getItem(this.ROLE_KEY);
    },

    // ── Get token
    getToken() {
        return localStorage
            .getItem(this.TOKEN_KEY);
    },

    // ── Check if logged in
    isLoggedIn() {
        return !!this.getToken();
    },

    // ── Check role
    isMinister() {
        return this.getRole() === 'MINISTER';
    },

    isOperator() {
        return this.getRole() === 'OPERATOR';
    },

    isOfficer() {
        return this.getRole() === 'OFFICER';
    },

    // ── Login
    async login(username, password,
            portalRole) {
        try {
            UTILS.showLoading(true);

            const result = await API.auth
                .login(username, password);

            if (result && result.success) {
                const userData = result.data;

                // Check role matches portal
                if (portalRole &&
                        userData.role !==
                        portalRole) {
                    UTILS.showToast(
                        `Access denied. This is `
                        + `${portalRole} portal.`,
                        'error');
                    UTILS.showLoading(false);
                    return false;
                }

                // Save session
                this.saveSession(userData);

                UTILS.showToast(
                    `Welcome ${userData.name}! `
                    + `Login successful.`,
                    'success');

                // Redirect to dashboard
                setTimeout(() => {
                    this.redirectToDashboard(
                        userData.role);
                }, 1000);

                return true;
            } else {
                UTILS.showToast(
                    result.message
                    || 'Invalid credentials',
                    'error');
                UTILS.showLoading(false);
                return false;
            }
        } catch (error) {
            UTILS.showToast(
                'Login failed. Try again.',
                'error');
            UTILS.showLoading(false);
            return false;
        }
    },

    // ── Redirect based on role
    redirectToDashboard(role) {
        switch(role) {
            case 'MINISTER':
                window.location.href =
                    '../minister/dashboard.html';
                break;
            case 'OPERATOR':
                window.location.href =
                    '../operator/dashboard.html';
                break;
            case 'OFFICER':
                window.location.href =
                    '../officer/dashboard.html';
                break;
            default:
                window.location.href =
                    'index.html';
        }
    },

    // ── Logout
    logout() {
        localStorage.removeItem(
            this.TOKEN_KEY);
        localStorage.removeItem(
            this.USER_KEY);
        localStorage.removeItem(
            this.ROLE_KEY);

        UTILS.showToast(
            'Logged out successfully',
            'success');

        setTimeout(() => {
            window.location.href =
                'index.html';
        }, 800);
    },

    // ── Guard Page (call on every page)
    guardPage(requiredRole) {
        if (!this.isLoggedIn()) {
            window.location.href =
                'index.html';
            return false;
        }

        const currentRole = this.getRole();

        if (requiredRole &&
                currentRole !== requiredRole
                && currentRole !== 'MINISTER') {
            window.location.href =
                'index.html';
            return false;
        }

        return true;
    },

    // ── Initialize Login Page
    initLoginPage(portalRole) {
        // If already logged in redirect
        if (this.isLoggedIn()) {
            this.redirectToDashboard(
                this.getRole());
            return;
        }

        // Setup login form
        const form = document
            .getElementById('loginForm');
        if (!form) return;

        form.addEventListener(
                'submit', async (e) => {
            e.preventDefault();

            const username = document
                .getElementById('username')
                .value.trim();
            const password = document
                .getElementById('password')
                .value.trim();

            if (!username || !password) {
                UTILS.showToast(
                    'Please enter username '
                    + 'and password',
                    'warning');
                return;
            }

            const btn = form.querySelector(
                'button[type="submit"]');
            btn.disabled = true;
            btn.innerHTML =
                '<span class="spinner"></span>'
                + ' Logging in...';

            const success = await this.login(
                username,
                password,
                portalRole);

            if (!success) {
                btn.disabled = false;
                btn.innerHTML =
                    '<i class="fas fa-sign-in-alt">'
                    + '</i> Login';
            }
        });

        // Toggle password visibility
        const toggleBtn = document
            .getElementById('togglePassword');
        if (toggleBtn) {
            toggleBtn.addEventListener(
                    'click', () => {
                const pwdInput = document
                    .getElementById('password');
                const icon = toggleBtn
                    .querySelector('i');

                if (pwdInput.type === 'password') {
                    pwdInput.type = 'text';
                    icon.className =
                        'fas fa-eye-slash';
                } else {
                    pwdInput.type = 'password';
                    icon.className =
                        'fas fa-eye';
                }
            });
        }
    },

    // ── Load User Info in Header
    loadUserInfo() {
        const user = this.getUser();
        if (!user) return;

        // Set user name
        const nameEls = document
            .querySelectorAll('.user-name');
        nameEls.forEach(el => {
            el.textContent = user.name;
        });

        // Set user role
        const roleEls = document
            .querySelectorAll('.user-role');
        roleEls.forEach(el => {
            el.textContent = user.role;
        });

        // Set avatar initials
        const avatarEls = document
            .querySelectorAll('.user-avatar');
        avatarEls.forEach(el => {
            el.textContent = UTILS
                .getInitials(user.name);
        });

        // Set department
        const deptEls = document
            .querySelectorAll('.user-dept');
        deptEls.forEach(el => {
            el.textContent =
                user.department || '';
        });
    }
};