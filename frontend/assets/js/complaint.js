// ═══════════════════════════════════════════════════
// MINISTER OFFICE ERP - COMPLAINTS HANDLER
// ═══════════════════════════════════════════════════

const COMPLAINTS = {

    // ── Current complaints data
    allComplaints: [],
    currentComplaint: null,
    officers: [],

    // ═══════════════════════════════════════
    // LOAD COMPLAINTS
    // ═══════════════════════════════════════

    async loadAll() {
        try {
            UTILS.showLoading(true);
            const result = await API
                .complaints.getAll();

            if (result && result.success) {
                this.allComplaints =
                    result.data || [];
                this.renderTable(
                    this.allComplaints);
            }
        } catch (e) {
            UTILS.showToast(
                'Failed to load complaints',
                'error');
        } finally {
            UTILS.showLoading(false);
        }
    },

    async loadMy() {
        try {
            UTILS.showLoading(true);
            const result = await API
                .complaints.getMy();

            if (result && result.success) {
                this.allComplaints =
                    result.data || [];
                this.renderTable(
                    this.allComplaints);
                this.updateCounts(
                    this.allComplaints);
            }
        } catch (e) {
            UTILS.showToast(
                'Failed to load complaints',
                'error');
        } finally {
            UTILS.showLoading(false);
        }
    },

    // ── Update dashboard counts
    updateCounts(complaints) {
        const counts = {
            total: complaints.length,
            pending: complaints.filter(c =>
                c.status === 'PENDING'
                || c.status === 'ASSIGNED'
                || c.status === 'NEW').length,
            resolved: complaints.filter(c =>
                c.status === 'RESOLVED'
                || c.status === 'CLOSED').length,
            escalated: complaints.filter(c =>
                c.status === 'ESCALATED').length,
            actionTaken: complaints.filter(c =>
                c.status === 'ACTION_TAKEN')
                .length,
            critical: complaints.filter(c =>
                c.priority === 'CRITICAL').length
        };

        // Update DOM elements
        Object.keys(counts).forEach(key => {
            const el = document
                .getElementById(`count_${key}`);
            if (el) {
                el.textContent = counts[key];
            }
        });

        return counts;
    },

    // ═══════════════════════════════════════
    // RENDER TABLE
    // ═══════════════════════════════════════

    renderTable(complaints) {
        const tbody = document
            .querySelector(
                '#complaintsTable tbody');
        if (!tbody) return;

        if (!complaints
                || complaints.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="9"
                        class="empty-state">
                        <i class="fas
                            fa-inbox"></i>
                        <p>No complaints found
                        </p>
                    </td>
                </tr>`;
            return;
        }

        tbody.innerHTML = complaints
            .map((c, i) => `
            <tr onclick="COMPLAINTS
                .viewDetail('${c.complaintId}')"
                style="cursor:pointer"
                class="complaint-row
                priority-${c.priority
                    ? c.priority.toLowerCase()
                    : 'low'}">
                <td>${i + 1}</td>
                <td>
                    <strong style="color:
                        var(--saffron);
                        font-family:monospace">
                        ${c.complaintId}
                    </strong>
                </td>
                <td>
                    <div>
                        <strong>
                            ${c.callerName}
                        </strong>
                        <br/>
                        <small style="color:
                            var(--gray)">
                            ${c.callerMobile}
                        </small>
                    </div>
                </td>
                <td>${c.district || '-'}</td>
                <td>
                    ${UTILS.truncate(
                        c.subject, 30)}
                </td>
                <td>
                    ${UTILS.getPriorityBadge(
                        c.priority)}
                </td>
                <td>
                    ${UTILS.getStatusBadge(
                        c.status)}
                </td>
                <td>
                    ${c.assignedName || '-'}
                </td>
                <td>
                    <small style="color:
                        var(--gray)">
                        ${UTILS.getTimeAgo(
                            c.createdDate)}
                    </small>
                </td>
            </tr>
        `).join('');
    },

    // ═══════════════════════════════════════
    // VIEW COMPLAINT DETAIL
    // ═══════════════════════════════════════

    async viewDetail(complaintId) {
        try {
            UTILS.showLoading(true);
            const result = await API
                .complaints
                .getById(complaintId);

            if (result && result.success) {
                this.currentComplaint =
                    result.data;
                this.showDetailModal(
                    result.data);
            }
        } catch (e) {
            UTILS.showToast(
                'Failed to load complaint',
                'error');
        } finally {
            UTILS.showLoading(false);
        }
    },

    // ── Show detail modal
    showDetailModal(complaint) {
        const modal = document
            .getElementById('complaintModal');
        if (!modal) return;

        // Fill complaint details
        this.fillDetailModal(complaint);

        // Load audit trail
        this.loadAuditTrail(
            complaint.complaintId);

        // Show modal
        UTILS.openModal('complaintModal');
    },

    // ── Fill modal with data
    fillDetailModal(c) {
        const fields = {
            'modal_id': c.complaintId,
            'modal_date': UTILS.formatDateTime(
                c.createdDate),
            'modal_caller': c.callerName,
            'modal_mobile': c.callerMobile,
            'modal_address': c.callerAddress
                || '-',
            'modal_district': c.district,
            'modal_state': c.state || '-',
            'modal_subject': c.subject,
            'modal_description': c.description,
            'modal_category': c.category,
            'modal_assigned': c.assignedName
                || 'Unassigned',
            'modal_operator': c.operatorName,
            'modal_action': c.actionTaken
                || '-',
            'modal_minister_note':
                c.ministerNote || '-',
            'modal_direction':
                c.ministerDirection || '-'
        };

        Object.keys(fields).forEach(id => {
            const el = document.getElementById(
                id);
            if (el) {
                el.textContent = fields[id];
            }
        });

        // Set badges
        const priorityEl = document
            .getElementById('modal_priority');
        if (priorityEl) {
            priorityEl.innerHTML =
                UTILS.getPriorityBadge(
                    c.priority);
        }

        const statusEl = document
            .getElementById('modal_status');
        if (statusEl) {
            statusEl.innerHTML =
                UTILS.getStatusBadge(c.status);
        }

        // Set workflow steps
        this.updateWorkflowSteps(c.status);
    },

    // ── Update workflow steps
    updateWorkflowSteps(currentStatus) {
        const statuses = [
            'NEW', 'ASSIGNED', 'PENDING',
            'ACTION_TAKEN', 'ESCALATED',
            'MINISTER_REVIEW', 'RESOLVED',
            'CLOSED'
        ];

        const currentIndex = statuses
            .indexOf(currentStatus);

        statuses.forEach((status, index) => {
            const step = document
                .getElementById(
                    `step_${status}`);
            if (!step) return;

            step.className = 'workflow-step';
            if (index < currentIndex) {
                step.classList
                    .add('completed');
            } else if (index === currentIndex) {
                step.classList.add('active');
            }
        });
    },

    // ═══════════════════════════════════════
    // LOAD AUDIT TRAIL
    // ═══════════════════════════════════════

    async loadAuditTrail(complaintId) {
        try {
            const result = await API
                .complaints
                .getAudit(complaintId);

            if (result && result.success) {
                this.renderAuditTrail(
                    result.data);
            }
        } catch (e) {
            console.log('Audit load error:', e);
        }
    },

    renderAuditTrail(logs) {
        const container = document
            .getElementById('auditTrail');
        if (!container) return;

        if (!logs || logs.length === 0) {
            container.innerHTML =
                '<p style="color:var(--gray);'
                + 'font-size:0.85rem;">'
                + 'No activity logged yet</p>';
            return;
        }

        container.innerHTML = `
            <div class="timeline">
                ${logs.reverse().map(log => `
                    <div class="timeline-item">
                        <div class="timeline-dot">
                        </div>
                        <div class="timeline-content">
                            <div class="timeline-action">
                                ${log.action
                                    .replace(/_/g,
                                    ' ')}
                            </div>
                            <div class="timeline-user">
                                By: ${log.userName}
                                (${log.userRole})
                                ${log.previousStatus
                                ? `| ${log
                                    .previousStatus}
                                    → ${log
                                    .newStatus}`
                                : ''}
                            </div>
                            ${log.remarks
                            ? `<div style="font-size:
                                0.78rem;color:
                                var(--dark-gray);
                                margin-top:4px;
                                font-style:italic">
                                "${log.remarks}"
                                </div>`
                            : ''}
                            <div class="timeline-time">
                                ${UTILS
                                .formatDateTime(
                                log.timestamp)}
                            </div>
                        </div>
                    </div>
                `).join('')}
            </div>`;
    },

    // ═══════════════════════════════════════
    // CREATE COMPLAINT
    // ═══════════════════════════════════════

    async create(formData) {
        try {
            UTILS.showLoading(true);
            const result = await API
                .complaints.create(formData);

            if (result && result.success) {
                UTILS.showToast(
                    `Complaint registered: `
                    + result.data.complaintId,
                    'success');
                UTILS.resetForm(
                    'complaintForm');
                return result.data;
            } else {
                UTILS.showToast(
                    result.message
                    || 'Failed to register',
                    'error');
                return null;
            }
        } catch (e) {
            UTILS.showToast(
                'Registration failed',
                'error');
            return null;
        } finally {
            UTILS.showLoading(false);
        }
    },

    // ═══════════════════════════════════════
    // UPDATE STATUS
    // ═══════════════════════════════════════

    async updateStatus(complaintId,
            status, actionTaken) {
        try {
            UTILS.showLoading(true);
            const result = await API
                .complaints.updateStatus(
                    complaintId,
                    status,
                    actionTaken);

            if (result && result.success) {
                UTILS.showToast(
                    `Status updated to: `
                    + status,
                    'success');
                UTILS.closeAllModals();
                await this.loadMy();
                return true;
            } else {
                UTILS.showToast(
                    result.message
                    || 'Update failed',
                    'error');
                return false;
            }
        } catch (e) {
            UTILS.showToast(
                'Update failed', 'error');
            return false;
        } finally {
            UTILS.showLoading(false);
        }
    },

    // ═══════════════════════════════════════
    // FORWARD COMPLAINT
    // ═══════════════════════════════════════

    async forward(complaintId,
            toOfficerId, toOfficerName,
            remarks) {
        try {
            UTILS.showLoading(true);
            const result = await API
                .complaints.forward(
                    complaintId,
                    toOfficerId,
                    toOfficerName,
                    remarks);

            if (result && result.success) {
                UTILS.showToast(
                    `Forwarded to: `
                    + toOfficerName,
                    'success');
                UTILS.closeAllModals();
                await this.loadMy();
                return true;
            } else {
                UTILS.showToast(
                    result.message
                    || 'Forward failed',
                    'error');
                return false;
            }
        } catch (e) {
            UTILS.showToast(
                'Forward failed', 'error');
            return false;
        } finally {
            UTILS.showLoading(false);
        }
    },

    // ═══════════════════════════════════════
    // ESCALATE
    // ═══════════════════════════════════════

    async escalate(complaintId, remarks) {
        try {
            UTILS.showLoading(true);
            const result = await API
                .complaints.escalate(
                    complaintId, remarks);

            if (result && result.success) {
                UTILS.showToast(
                    'Complaint escalated',
                    'success');
                UTILS.closeAllModals();
                await this.loadMy();
                return true;
            } else {
                UTILS.showToast(
                    result.message
                    || 'Escalation failed',
                    'error');
                return false;
            }
        } catch (e) {
            UTILS.showToast(
                'Escalation failed', 'error');
            return false;
        } finally {
            UTILS.showLoading(false);
        }
    },

    // ═══════════════════════════════════════
    // MINISTER ACTION
    // ═══════════════════════════════════════

    async ministerAction(complaintId,
            action, direction, note,
            newOfficerId, newOfficerName) {
        try {
            UTILS.showLoading(true);
            const result = await API
                .complaints.ministerAction(
                    complaintId, action,
                    direction, note,
                    newOfficerId,
                    newOfficerName);

            if (result && result.success) {
                UTILS.showToast(
                    `Action applied: ${action}`,
                    'success');
                UTILS.closeAllModals();
                await this.loadAll();
                return true;
            } else {
                UTILS.showToast(
                    result.message
                    || 'Action failed',
                    'error');
                return false;
            }
        } catch (e) {
            UTILS.showToast(
                'Action failed', 'error');
                } finally {
            UTILS.showLoading(false);
        }
    },

    // ═══════════════════════════════════════
    // SEARCH COMPLAINTS
    // ═══════════════════════════════════════

    async search(params) {
        try {
            UTILS.showLoading(true);
            const result = await API
                .complaints.search(params);

            if (result && result.success) {
                this.allComplaints =
                    result.data || [];
                this.renderTable(
                    this.allComplaints);
                UTILS.showToast(
                    `Found ${this.allComplaints
                    .length} complaints`,
                    'info');
            }
        } catch (e) {
            UTILS.showToast(
                'Search failed', 'error');
        } finally {
            UTILS.showLoading(false);
        }
    },

    // ── Filter locally
    filterLocal(status, priority,
            category) {
        let filtered = [...this.allComplaints];

        if (status && status !== 'ALL') {
            filtered = filtered.filter(
                c => c.status === status);
        }

        if (priority && priority !== 'ALL') {
            filtered = filtered.filter(
                c => c.priority === priority);
        }

        if (category && category !== 'ALL') {
            filtered = filtered.filter(
                c => c.category === category);
        }

        this.renderTable(filtered);
        return filtered;
    },

    // ═══════════════════════════════════════
    // LOAD OFFICERS DROPDOWN
    // ═══════════════════════════════════════

    async loadOfficers(selectId) {
        try {
            const result = await API
                .users.getOfficers();

            if (result && result.success) {
                this.officers = result.data
                    || [];
                const select = document
                    .getElementById(selectId);
                if (!select) return;

                select.innerHTML =
                    '<option value="">'
                    + 'Select Officer'
                    + '</option>'
                    + this.officers.map(o =>
                        `<option
                            value="${o.userId}"
                            data-name="${o.name}"
                            data-dept=
                            "${o.department}">
                            ${o.name}
                            (${o.department})
                        </option>`
                    ).join('');
            }
        } catch (e) {
            console.log(
                'Load officers error:', e);
        }
    },

    // ── Auto suggest officer
    async suggestOfficer(category,
            district) {
        try {
            const result = await API
                .complaints.suggestOfficer(
                    category, district);

            if (result && result.success
                    && result.data) {
                const officer = result.data;

                // Show suggestion card
                const card = document
                    .getElementById(
                        'officerSuggestion');
                if (card) {
                    card.className =
                        'officer-suggestion '
                        + 'visible';
                    document.getElementById(
                        'suggested_name')
                        .textContent =
                            officer.name;
                    document.getElementById(
                        'suggested_dept')
                        .textContent =
                            officer.department;
                    document.getElementById(
                        'suggested_avatar')
                        .textContent =
                            UTILS.getInitials(
                                officer.name);

                    // Set hidden fields
                    const idField = document
                        .getElementById(
                            'assignedTo');
                    const nameField = document
                        .getElementById(
                            'assignedName');
                    if (idField) {
                        idField.value =
                            officer.userId;
                    }
                    if (nameField) {
                        nameField.value =
                            officer.name;
                    }
                }

                return officer;
            }
        } catch (e) {
            console.log(
                'Suggest officer error:', e);
        }
        return null;
    },

    // ═══════════════════════════════════════
    // COMPLAINT FORM - MULTI STEP
    // ═══════════════════════════════════════

    currentStep: 1,
    totalSteps: 3,

    initMultiStepForm() {
        this.currentStep = 1;
        this.showStep(1);
        this.setupFormEvents();
    },

    showStep(step) {
        // Hide all sections
        document.querySelectorAll(
            '.form-section').forEach(s => {
            s.classList.remove('active');
        });

        // Show current
        const current = document
            .getElementById(
                `step${step}Section`);
        if (current) {
            current.classList.add('active');
        }

        // Update step indicators
        document.querySelectorAll(
            '.form-step').forEach((s, i) => {
            s.className = 'form-step';
            if (i + 1 < step) {
                s.classList.add('completed');
            } else if (i + 1 === step) {
                s.classList.add('active');
            }
        });

        // Update buttons
        const prevBtn = document
            .getElementById('prevBtn');
        const nextBtn = document
            .getElementById('nextBtn');
        const submitBtn = document
            .getElementById('submitBtn');

        if (prevBtn) {
            prevBtn.style.display =
                step > 1 ? 'flex' : 'none';
        }

        if (nextBtn) {
            nextBtn.style.display =
                step < this.totalSteps
                    ? 'flex' : 'none';
        }

        if (submitBtn) {
            submitBtn.style.display =
                step === this.totalSteps
                    ? 'flex' : 'none';
        }
    },

    nextStep() {
        if (this.validateStep(
                this.currentStep)) {
            this.currentStep++;
            this.showStep(this.currentStep);

            // Auto suggest on step 2
            if (this.currentStep === 2) {
                const category = document
                    .getElementById(
                        'category')?.value;
                const district = document
                    .getElementById(
                        'district')?.value;
                if (category) {
                    this.suggestOfficer(
                        category, district);
                }
            }
        }
    },

    prevStep() {
        if (this.currentStep > 1) {
            this.currentStep--;
            this.showStep(this.currentStep);
        }
    },

    validateStep(step) {
        UTILS.clearFieldErrors(
            'complaintForm');
        let isValid = true;

        if (step === 1) {
            const callerName = document
                .getElementById('callerName')
                ?.value.trim();
            const callerMobile = document
                .getElementById('callerMobile')
                ?.value.trim();
            const district = document
                .getElementById('district')
                ?.value.trim();

            if (!callerName) {
                UTILS.setFieldError(
                    'callerName',
                    'Name is required');
                isValid = false;
            }

            if (!callerMobile
                    || !UTILS.validateMobile(
                        callerMobile)) {
                UTILS.setFieldError(
                    'callerMobile',
                    'Valid 10-digit mobile required');
                isValid = false;
            }

            if (!district) {
                UTILS.setFieldError(
                    'district',
                    'District is required');
                isValid = false;
            }
        }

        if (step === 2) {
            const subject = document
                .getElementById('subject')
                ?.value.trim();
            const description = document
                .getElementById('description')
                ?.value.trim();
            const category = document
                .getElementById('category')
                ?.value;
            const priority = document
                .getElementById('priority')
                ?.value;

            if (!subject) {
                UTILS.setFieldError(
                    'subject',
                    'Subject is required');
                isValid = false;
            }

            if (!description) {
                UTILS.setFieldError(
                    'description',
                    'Description is required');
                isValid = false;
            }

            if (!category) {
                UTILS.setFieldError(
                    'category',
                    'Category is required');
                isValid = false;
            }

            if (!priority) {
                UTILS.setFieldError(
                    'priority',
                    'Priority is required');
                isValid = false;
            }
        }

        return isValid;
    },

    setupFormEvents() {
        // Category change - auto suggest
        const categorySelect = document
            .getElementById('category');
        if (categorySelect) {
            categorySelect.addEventListener(
                'change', (e) => {
                const district = document
                    .getElementById('district')
                    ?.value;
                this.suggestOfficer(
                    e.target.value, district);
            });
        }

        // Priority selector
        document.querySelectorAll(
            '.priority-option')
            .forEach(option => {
            option.addEventListener(
                'click', () => {
                document.querySelectorAll(
                    '.priority-option')
                    .forEach(o => o.classList
                        .remove('selected'));
                option.classList
                    .add('selected');
                const priorityInput =
                    document.getElementById(
                        'priority');
                if (priorityInput) {
                    priorityInput.value =
                        option.dataset.priority;
                }
            });
        });

        // Form submit
        const form = document
            .getElementById('complaintForm');
        if (form) {
            form.addEventListener(
                'submit', async (e) => {
                e.preventDefault();
                if (this.validateStep(3)) {
                    const data =
                        UTILS.getFormData(
                            'complaintForm');
                    const result =
                        await this.create(data);
                    if (result) {
                        setTimeout(() => {
                            window.location
                                .href =
                                'complaints.html';
                        }, 1500);
                    }
                }
            });
        }
    }
};