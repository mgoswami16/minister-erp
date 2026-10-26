// ═══════════════════════════════════════════════════
// MINISTER OFFICE ERP - API CLIENT
// All API calls to Spring Boot Backend
// ═══════════════════════════════════════════════════

const API = {

    // ── Base URL
    BASE_URL: 'http://localhost:8080/api/v1',

    // ── Get JWT Token from storage
    getToken() {
        return localStorage.getItem('erp_token');
    },

    // ── Get Auth Headers
    getHeaders() {
        const token = this.getToken();
        return {
            'Content-Type': 'application/json',
            'Authorization': token
                ? `Bearer ${token}`
                : ''
        };
    },

    // ── Generic GET Request
    async get(endpoint) {
        try {
            const response = await fetch(
                `${this.BASE_URL}${endpoint}`,
                {
                    method: 'GET',
                    headers: this.getHeaders()
                }
            );
            return await this.handleResponse(
                response);
        } catch (error) {
            return this.handleError(error);
        }
    },

    // ── Generic POST Request
    async post(endpoint, data) {
        try {
            const response = await fetch(
                `${this.BASE_URL}${endpoint}`,
                {
                    method: 'POST',
                    headers: this.getHeaders(),
                    body: JSON.stringify(data)
                }
            );
            return await this.handleResponse(
                response);
        } catch (error) {
            return this.handleError(error);
        }
    },

    // ── Generic PUT Request
    async put(endpoint, data) {
        try {
            const response = await fetch(
                `${this.BASE_URL}${endpoint}`,
                {
                    method: 'PUT',
                    headers: this.getHeaders(),
                    body: JSON.stringify(data)
                }
            );
            return await this.handleResponse(
                response);
        } catch (error) {
            return this.handleError(error);
        }
    },

    // ── Generic DELETE Request
    async delete(endpoint) {
        try {
            const response = await fetch(
                `${this.BASE_URL}${endpoint}`,
                {
                    method: 'DELETE',
                    headers: this.getHeaders()
                }
            );
            return await this.handleResponse(
                response);
        } catch (error) {
            return this.handleError(error);
        }
    },

    // ── Download File (Excel/PDF)
    async download(endpoint, filename) {
        try {
            const response = await fetch(
                `${this.BASE_URL}${endpoint}`,
                {
                    method: 'GET',
                    headers: this.getHeaders()
                }
            );

            if (!response.ok) {
                throw new Error(
                    'Download failed');
            }

            const blob = await response.blob();
            const url = window.URL
                .createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);

            return { success: true };
        } catch (error) {
            return this.handleError(error);
        }
    },

    // ── Handle Response
    async handleResponse(response) {
        const data = await response.json();

        if (response.status === 401) {
            // Token expired - redirect to login
            AUTH.logout();
            return;
        }

        if (response.status === 403) {
            UTILS.showToast(
                'Access denied',
                'error');
            return data;
        }

        return data;
    },

    // ── Handle Error
    handleError(error) {
        console.error('API Error:', error);
        UTILS.showToast(
            'Network error. Please check '
            + 'your connection.',
            'error');
        return {
            success: false,
            message: error.message
        };
    },

    // ═══════════════════════════════════════
    // AUTH APIs
    // ═══════════════════════════════════════

    auth: {
        login(username, password) {
            return API.post('/auth/login', {
                username,
                password
            });
        },
        me() {
            return API.get('/auth/me');
        },
        validate() {
            return API.get('/auth/validate');
        },
        logout() {
            return API.post('/auth/logout', {});
        }
    },

    // ═══════════════════════════════════════
    // COMPLAINT APIs
    // ═══════════════════════════════════════

    complaints: {
        create(data) {
            return API.post('/complaints', data);
        },
        getAll() {
            return API.get('/complaints');
        },
        getById(id) {
            return API.get(
                `/complaints/${id}`);
        },
        getMy() {
            return API.get('/complaints/my');
        },
        search(params) {
            const query = new URLSearchParams(
                params).toString();
            return API.get(
                `/complaints/search?${query}`);
        },
        updateStatus(id, status, actionTaken) {
            return API.put(
                `/complaints/${id}/status`,
                { status, actionTaken });
        },
        assign(id, officerId, officerName) {
            return API.put(
                `/complaints/${id}/assign`,
                { officerId, officerName });
        },
        forward(id, toOfficerId,
                toOfficerName, remarks) {
            return API.put(
                `/complaints/${id}/forward`,
                {
                    toOfficerId,
                    toOfficerName,
                    remarks
                });
        },
        escalate(id, remarks) {
            return API.put(
                `/complaints/${id}/escalate`,
                { remarks });
        },
        sendToMinister(id, remarks) {
            return API.put(
                `/complaints/${id}/minister-review`,
                { remarks });
        },
        ministerAction(id, action,
                direction, note,
                newOfficerId, newOfficerName) {
            return API.put(
                `/complaints/${id}/minister-action`,
                {
                    action,
                    direction,
                    note,
                    newOfficerId,
                    newOfficerName
                });
        },
        close(id, closureRemark) {
            return API.put(
                `/complaints/${id}/close`,
                { closureRemark });
        },
        getAudit(id) {
            return API.get(
                `/complaints/${id}/audit`);
        },
        suggestOfficer(category, district) {
            return API.get(
                `/complaints/suggest-officer`
                + `?category=${category}`
                + `&district=${district || ''}`);
        }
    },

    // ═══════════════════════════════════════
    // APPOINTMENT APIs
    // ═══════════════════════════════════════

    appointments: {
        create(data) {
            return API.post(
                '/appointments', data);
        },
        getAll() {
            return API.get('/appointments');
        },
        getPending() {
            return API.get(
                '/appointments/pending');
        },
        getById(id) {
            return API.get(
                `/appointments/${id}`);
        },
        approve(id, approvedDate,
                approvedTime, approvedVenue) {
            return API.put(
                `/appointments/${id}/approve`,
                {
                    approvedDate,
                    approvedTime,
                    approvedVenue
                });
        },
        reject(id, rejectionReason) {
            return API.put(
                `/appointments/${id}/reject`,
                { rejectionReason });
        },
        keepPending(id, remarks) {
            return API.put(
                `/appointments/${id}/pending`,
                { remarks });
        },
        search(keyword) {
            return API.get(
                `/appointments/search`
                + `?keyword=${keyword}`);
        },
        getByStatus(status) {
            return API.get(
                `/appointments/status/${status}`);
        }
    },

    // ═══════════════════════════════════════
    // USER APIs
    // ═══════════════════════════════════════

    users: {
        getAll() {
            return API.get('/users');
        },
        getOfficers() {
            return API.get('/users/officers');
        },
        getById(id) {
            return API.get(`/users/${id}`);
        },
        create(data) {
            return API.post('/users', data);
        },
        update(id, data) {
            return API.put(`/users/${id}`, data);
        },
        toggleStatus(id) {
            return API.put(
                `/users/${id}/toggle-status`,
                {});
        },
        changePassword(oldPassword,
                newPassword) {
            return API.put(
                '/users/change-password',
                { oldPassword, newPassword });
        },
        suggest(category, district) {
            return API.get(
                `/users/suggest`
                + `?category=${category}`
                + `&district=${district || ''}`);
        }
    },

    // ═══════════════════════════════════════
    // DASHBOARD APIs
    // ═══════════════════════════════════════

    dashboard: {
        operator() {
            return API.get(
                '/dashboard/operator');
        },
        officer() {
            return API.get(
                '/dashboard/officer');
        },
        minister() {
            return API.get(
                '/dashboard/minister');
        }
    },

    // ═══════════════════════════════════════
    // REPORT APIs
    // ═══════════════════════════════════════

    reports: {
        daily() {
            return API.get('/reports/daily');
        },
        weekly() {
            return API.get('/reports/weekly');
        },
        monthly() {
            return API.get('/reports/monthly');
        },
        range(fromDate, toDate) {
            return API.get(
                `/reports/range`
                + `?fromDate=${fromDate}`
                + `&toDate=${toDate}`);
        },
        exportDaily() {
            return API.download(
                '/reports/export/daily',
                'daily-report.xlsx');
        },
        exportWeekly() {
            return API.download(
                '/reports/export/weekly',
                'weekly-report.xlsx');
        },
        exportMonthly() {
            return API.download(
                '/reports/export/monthly',
                'monthly-report.xlsx');
        },
        exportRange(fromDate, toDate) {
            return API.download(
                `/reports/export/range`
                + `?fromDate=${fromDate}`
                + `&toDate=${toDate}`,
                `report-${fromDate}-to-`
                + `${toDate}.xlsx`);
        },
        exportOfficerPerformance() {
            return API.download(
                '/reports/export/'
                + 'officer-performance',
                'officer-performance.xlsx');
        }
    },

    // ═══════════════════════════════════════
    // NOTIFICATION APIs
    // ═══════════════════════════════════════

    notifications: {
        getMy() {
            return API.get('/notifications');
        },
        getUnreadCount() {
            return API.get(
                '/notifications/unread-count');
        },
        markRead(id) {
            return API.put(
                `/notifications/${id}/read`,
                {});
        },
        sendReminder(toUserId,
                toUserName, message,
                complaintId) {
            return API.post(
                '/notifications/reminder',
                {
                    toUserId,
                    toUserName,
                    message,
                    complaintId
                });
        }
    }
};