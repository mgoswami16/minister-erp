// ═══════════════════════════════════════════════════
// MINISTER OFFICE ERP - CHARTS & ANALYTICS
// Uses Chart.js library
// ═══════════════════════════════════════════════════

const CHARTS = {

    // ── Chart instances store
    instances: {},

    // ── Default colors
    colors: {
        saffron:  '#FF6600',
        blue:     '#003399',
        green:    '#28A745',
        red:      '#DC3545',
        yellow:   '#FFC107',
        purple:   '#6F42C1',
        teal:     '#20C997',
        cyan:     '#17A2B8',
        gray:     '#6C757D',
        orange:   '#FF8533'
    },

    // ── Destroy existing chart
    destroy(canvasId) {
        if (this.instances[canvasId]) {
            this.instances[canvasId].destroy();
            delete this.instances[canvasId];
        }
    },

    // ═══════════════════════════════════════
    // STATUS DOUGHNUT CHART
    // ═══════════════════════════════════════

    renderStatusChart(canvasId, statusData) {
        this.destroy(canvasId);
        const canvas = document
            .getElementById(canvasId);
        if (!canvas) return;

        const labels = Object.keys(statusData);
        const data = Object.values(statusData);

        const backgroundColors = {
            'NEW':             '#17A2B8',
            'ASSIGNED':        '#003399',
            'PENDING':         '#FFC107',
            'ACTION_TAKEN':    '#28A745',
            'ESCALATED':       '#DC3545',
            'MINISTER_REVIEW': '#6F42C1',
            'RESOLVED':        '#20C997',
            'CLOSED':          '#6C757D'
        };

        this.instances[canvasId] =
            new Chart(canvas, {
            type: 'doughnut',
            data: {
                labels: labels.map(l =>
                    l.replace(/_/g, ' ')),
                datasets: [{
                    data: data,
                    backgroundColor: labels
                        .map(l =>
                            backgroundColors[l]
                            || '#999'),
                    borderWidth: 2,
                    borderColor: '#fff',
                    hoverOffset: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '65%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 12,
                            font: {
                                family:
                                    'Poppins',
                                size: 11
                            },
                            usePointStyle: true,
                            pointStyleWidth: 8
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: (context) => {
                                const total =
                                    context.dataset
                                    .data.reduce(
                                        (a, b) =>
                                        a + b, 0);
                                const pct = Math
                                    .round(
                                    (context.raw
                                    / total)
                                    * 100);
                                return ` ${context
                                    .label}: `
                                    + `${context
                                    .raw} `
                                    + `(${pct}%)`;
                            }
                        }
                    }
                }
            }
        });
    },

    // ═══════════════════════════════════════
    // PRIORITY BAR CHART
    // ═══════════════════════════════════════

    renderPriorityChart(canvasId,
            priorityData) {
        this.destroy(canvasId);
        const canvas = document
            .getElementById(canvasId);
        if (!canvas) return;

        this.instances[canvasId] =
            new Chart(canvas, {
            type: 'bar',
            data: {
                labels: ['Low', 'Medium',
                    'High', 'Critical'],
                datasets: [{
                    label: 'Complaints',
                    data: [
                        priorityData.LOW || 0,
                        priorityData.MEDIUM || 0,
                        priorityData.HIGH || 0,
                        priorityData.CRITICAL || 0
                    ],
                    backgroundColor: [
                        'rgba(40,167,69,0.8)',
                        'rgba(255,193,7,0.8)',
                        'rgba(255,102,0,0.8)',
                        'rgba(220,53,69,0.8)'
                    ],
                    borderColor: [
                        '#28A745',
                        '#FFC107',
                        '#FF6600',
                        '#DC3545'
                    ],
                    borderWidth: 2,
                    borderRadius: 6,
                    borderSkipped: false
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: (ctx) =>
                                ` ${ctx.raw} `
                                + `complaints`
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1,
                            font: {
                                family: 'Poppins',
                                size: 11
                            }
                        },
                        grid: {
                            color: 'rgba(0,0,0,0.05)'
                        }
                    },
                    x: {
                        ticks: {
                            font: {
                                family: 'Poppins',
                                size: 11
                            }
                        },
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    },

    // ═══════════════════════════════════════
    // MONTHLY TREND LINE CHART
    // ═══════════════════════════════════════

    renderMonthlyChart(canvasId,
            monthlyData) {
        this.destroy(canvasId);
        const canvas = document
            .getElementById(canvasId);
        if (!canvas) return;

        const labels = Object.keys(
            monthlyData);
        const data = Object.values(
            monthlyData);

        this.instances[canvasId] =
            new Chart(canvas, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Complaints',
                    data: data,
                    borderColor: '#FF6600',
                    backgroundColor:
                        'rgba(255,102,0,0.1)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.4,
                    pointBackgroundColor:
                        '#FF6600',
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2,
                    pointRadius: 5,
                    pointHoverRadius: 7
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        backgroundColor:
                            'rgba(0,51,153,0.9)',
                        titleFont: {
                            family: 'Poppins',
                            size: 12
                        },
                        bodyFont: {
                            family: 'Poppins',
                            size: 11
                        },
                        callbacks: {
                            label: (ctx) =>
                                ` ${ctx.raw} `
                                + `complaints`
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1,
                            font: {
                                family:
                                    'Poppins',
                                size: 11
                            }
                        },
                        grid: {
                            color:
                                'rgba(0,0,0,0.05)'
                        }
                    },
                    x: {
                        ticks: {
                            font: {
                                family:
                                    'Poppins',
                                size: 11
                            }
                        },
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    },

    // ═══════════════════════════════════════
    // CATEGORY PIE CHART
    // ═══════════════════════════════════════

    renderCategoryChart(canvasId,
            categoryData) {
        this.destroy(canvasId);
        const canvas = document
            .getElementById(canvasId);
        if (!canvas) return;

        const labels = Object.keys(
            categoryData);
        const data = Object.values(
            categoryData);

        const colors = [
            '#FF6600', '#003399',
            '#28A745', '#DC3545',
            '#FFC107', '#6F42C1',
            '#20C997', '#17A2B8',
            '#FF8533', '#6C757D'
        ];

        this.instances[canvasId] =
            new Chart(canvas, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: colors
                        .slice(0, labels.length),
                    borderWidth: 2,
                    borderColor: '#fff',
                    hoverOffset: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'right',
                        labels: {
                            padding: 12,
                            font: {
                                family:
                                    'Poppins',
                                size: 11
                            },
                            usePointStyle: true
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: (ctx) => {
                                const total =
                                    ctx.dataset
                                    .data
                                    .reduce(
                                    (a, b) =>
                                    a + b, 0);
                                const pct =
                                    Math.round(
                                    (ctx.raw
                                    / total)
                                    * 100);
                                return ` ${ctx
                                    .label}: `
                                    + `${ctx.raw}`
                                    + ` (${pct}%)`;
                            }
                        }
                    }
                }
            }
        });
    },

    // ═══════════════════════════════════════
    // OFFICER PERFORMANCE CHART
    // ═══════════════════════════════════════

    renderOfficerChart(canvasId,
            officerData) {
        this.destroy(canvasId);
        const canvas = document
            .getElementById(canvasId);
        if (!canvas) return;

        const labels = officerData
            .map(o => o.officerName
                .split(' ')[0]);

        this.instances[canvasId] =
            new Chart(canvas, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Assigned',
                        data: officerData.map(
                            o => o.totalAssigned),
                        backgroundColor:
                            'rgba(0,51,153,0.7)',
                        borderColor: '#003399',
                        borderWidth: 1,
                        borderRadius: 4
                    },
                    {
                        label: 'Resolved',
                        data: officerData.map(
                            o => o.resolved),
                        backgroundColor:
                            'rgba(40,167,69,0.7)',
                        borderColor: '#28A745',
                        borderWidth: 1,
                        borderRadius: 4
                    },
                    {
                        label: 'Pending',
                        data: officerData.map(
                            o => o.pending),
                        backgroundColor:
                            'rgba(255,193,7,0.7)',
                        borderColor: '#FFC107',
                        borderWidth: 1,
                        borderRadius: 4
                    },
                    {
                        label: 'Escalated',
                        data: officerData.map(
                            o => o.escalated),
                        backgroundColor:
                            'rgba(220,53,69,0.7)',
                        borderColor: '#DC3545',
                        borderWidth: 1,
                        borderRadius: 4
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                        labels: {
                            font: {
                                family:
                                    'Poppins',
                                size: 11
                            },
                            usePointStyle: true
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1,
                            font: {
                                family:
                                    'Poppins',
                                size: 11
                            }
                        },
                        grid: {
                            color:
                                'rgba(0,0,0,0.05)'
                        }
                    },
                    x: {
                        ticks: {
                            font: {
                                family:
                                    'Poppins',
                                size: 11
                            }
                        },
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    },

    // ═══════════════════════════════════════
    // DISTRICT HORIZONTAL BAR CHART
    // ═══════════════════════════════════════

    renderDistrictChart(canvasId,
            districtData) {
        this.destroy(canvasId);
        const canvas = document
            .getElementById(canvasId);
        if (!canvas) return;

        // Top 10 districts
        const sorted = Object.entries(
            districtData)
            .sort((a, b) => b[1] - a[1])
            .slice(0, 10);

        const labels = sorted.map(d => d[0]);
        const data = sorted.map(d => d[1]);

        this.instances[canvasId] =
            new Chart(canvas, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Complaints',
                    data: data,
                    backgroundColor:
                        'rgba(255,102,0,0.8)',
                    borderColor: '#FF6600',
                    borderWidth: 1,
                    borderRadius: 4
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1,
                            font: {
                                family:
                                    'Poppins',
                                size: 11
                            }
                        },
                        grid: {
                            color:
                                'rgba(0,0,0,0.05)'
                        }
                    },
                    y: {
                        ticks: {
                            font: {
                                family:
                                    'Poppins',
                                size: 11
                            }
                        },
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    },

    // ═══════════════════════════════════════
    // LOAD ALL MINISTER DASHBOARD CHARTS
    // ═══════════════════════════════════════

    async loadMinisterCharts() {
        try {
            const result = await API
                .dashboard.minister();

            if (!result || !result.success) {
                return;
            }

            const data = result.data;

            // Status chart
            if (data.statusWise) {
                this.renderStatusChart(
                    'statusChart',
                    data.statusWise);
            }

            // Priority chart
            if (data.priorityWise) {
                this.renderPriorityChart(
                    'priorityChart',
                    data.priorityWise);
            }

            // Monthly chart
            if (data.monthlyTrend) {
                this.renderMonthlyChart(
                    'monthlyChart',
                    data.monthlyTrend);
            }

            // Category chart
            if (data.categoryWise) {
                this.renderCategoryChart(
                    'categoryChart',
                    data.categoryWise);
            }

            // Officer chart
            if (data.officerPerformance) {
                this.renderOfficerChart(
                    'officerChart',
                    data.officerPerformance);
            }

            // District chart
            if (data.districtWise) {
                this.renderDistrictChart(
                    'districtChart',
                    data.districtWise);
            }

        } catch (e) {
            console.log(
                'Chart load error:', e);
        }
    },

    // ── Refresh all charts
    refreshAll() {
        Object.keys(this.instances)
            .forEach(id => {
            if (this.instances[id]) {
                this.instances[id].update();
            }
        });
    },

    // ── Destroy all charts
    destroyAll() {
        Object.keys(this.instances)
            .forEach(id => {
            this.destroy(id);
        });
    }
};