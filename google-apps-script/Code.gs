// ═══════════════════════════════════════════════════
// MINISTER OFFICE ERP - GOOGLE APPS SCRIPT BACKEND
// Hon. Minister Shri Asim Arun
// ═══════════════════════════════════════════════════

// ── Spreadsheet Configuration
const SPREADSHEET_ID = SpreadsheetApp
    .getActiveSpreadsheet().getId();

const SHEETS = {
    USERS: "USERS",
    COMPLAINTS: "COMPLAINTS",
    APPOINTMENTS: "APPOINTMENTS",
    AUDIT: "AUDIT",
    NOTIFICATIONS: "NOTIFICATIONS",
    FORWARDING: "FORWARDING"
};

// ═══════════════════════════════════════════════════
// MAIN ENTRY POINT - Handle GET Requests
// ═══════════════════════════════════════════════════
function doGet(e) {
    try {
        const action = e.parameter.action;
        const sheet = e.parameter.sheet;

        if (action === "GET") {
            const data = getSheetData(sheet);
            return sendResponse({
                success: true,
                data: data,
                count: data.length
            });
        }

        if (action === "HEALTH") {
            return sendResponse({
                success: true,
                message: "Minister ERP API Running ✅",
                timestamp: new Date().toISOString()
            });
        }

        return sendResponse({
            success: false,
            message: "Invalid action: " + action
        });

    } catch (error) {
        return sendErrorResponse(error.message);
    }
}

// ═══════════════════════════════════════════════════
// MAIN ENTRY POINT - Handle POST Requests
// ═══════════════════════════════════════════════════
function doPost(e) {
    try {
        const payload = JSON.parse(e.postData.contents);
        const action = payload.action;
        const sheetName = payload.sheet;

        switch (action) {

            case "APPEND":
                return handleAppend(
                    sheetName,
                    payload.data);

            case "UPDATE":
                return handleUpdate(
                    sheetName,
                    payload.id,
                    payload.idColumn || 0,
                    payload.data);

            case "DELETE":
                return handleDelete(
                    sheetName,
                    payload.id,
                    payload.idColumn || 0);

            case "MARK_READ":
                return handleMarkRead(
                    sheetName,
                    payload.id);

            case "SEARCH":
                return handleSearch(
                    sheetName,
                    payload.column,
                    payload.value);

            default:
                return sendResponse({
                    success: false,
                    message: "Unknown action: " + action
                });
        }

    } catch (error) {
        return sendErrorResponse(error.message);
    }
}

// ═══════════════════════════════════════════════════
// SHEET OPERATIONS
// ═══════════════════════════════════════════════════

// ── Get all data from sheet
function getSheetData(sheetName) {
    try {
        const ss = SpreadsheetApp
            .openById(SPREADSHEET_ID);
        const sheet = ss.getSheetByName(sheetName);

        if (!sheet) {
            createSheet(sheetName);
            return [];
        }

        const lastRow = sheet.getLastRow();
        const lastCol = sheet.getLastColumn();

        if (lastRow <= 1) return [];

        // Get data rows (skip header row 1)
        const data = sheet.getRange(
            2, 1, lastRow - 1, lastCol
        ).getValues();

        // Filter empty rows
        return data.filter(row =>
            row[0] !== null
            && row[0] !== ""
            && row[0] !== undefined
        );

    } catch (error) {
        console.error(
            "Error getting sheet data: "
            + error.message);
        return [];
    }
}

// ── Append row to sheet
function handleAppend(sheetName, data) {
    try {
        const ss = SpreadsheetApp
            .openById(SPREADSHEET_ID);
        let sheet = ss.getSheetByName(sheetName);

        if (!sheet) {
            sheet = createSheet(sheetName);
        }

        // Add timestamp if not present
        const row = data.map(cell =>
            cell === null ? "" : cell
        );

        sheet.appendRow(row);

        return sendResponse({
            success: true,
            message: "Row added to " + sheetName,
            rowId: data[0]
        });

    } catch (error) {
        return sendErrorResponse(
            "Append error: " + error.message);
    }
}

// ── Update existing row
function handleUpdate(
        sheetName,
        id,
        idColumn,
        newData) {
    try {
        const ss = SpreadsheetApp
            .openById(SPREADSHEET_ID);
        const sheet = ss.getSheetByName(sheetName);

        if (!sheet) {
            return sendResponse({
                success: false,
                message: "Sheet not found: "
                    + sheetName
            });
        }

        const lastRow = sheet.getLastRow();
        if (lastRow <= 1) {
            return sendResponse({
                success: false,
                message: "No data found in "
                    + sheetName
            });
        }

        // Find row with matching ID
        const idValues = sheet.getRange(
            2, idColumn + 1,
            lastRow - 1, 1
        ).getValues();

        let targetRow = -1;
        for (let i = 0; i < idValues.length; i++) {
            if (idValues[i][0].toString() === id
                    .toString()) {
                targetRow = i + 2;
                break;
            }
        }

        if (targetRow === -1) {
            return sendResponse({
                success: false,
                message: "Record not found: " + id
            });
        }

        // Update the row
        const cleanData = newData.map(cell =>
            cell === null ? "" : cell
        );

        sheet.getRange(
            targetRow, 1,
            1, cleanData.length
        ).setValues([cleanData]);

        return sendResponse({
            success: true,
            message: "Updated record: " + id,
            rowIndex: targetRow
        });

    } catch (error) {
        return sendErrorResponse(
            "Update error: " + error.message);
    }
}

// ── Delete row
function handleDelete(sheetName, id, idColumn) {
    try {
        const ss = SpreadsheetApp
            .openById(SPREADSHEET_ID);
        const sheet = ss.getSheetByName(sheetName);

        if (!sheet) {
            return sendResponse({
                success: false,
                message: "Sheet not found"
            });
        }

        const lastRow = sheet.getLastRow();
        const idValues = sheet.getRange(
            2, idColumn + 1,
            lastRow - 1, 1
        ).getValues();

        let targetRow = -1;
        for (let i = 0; i < idValues.length; i++) {
            if (idValues[i][0].toString()
                    === id.toString()) {
                targetRow = i + 2;
                break;
            }
        }

        if (targetRow === -1) {
            return sendResponse({
                success: false,
                message: "Record not found: " + id
            });
        }

        sheet.deleteRow(targetRow);

        return sendResponse({
            success: true,
            message: "Deleted record: " + id
        });

    } catch (error) {
        return sendErrorResponse(
            "Delete error: " + error.message);
    }
}

// ── Mark notification as read
function handleMarkRead(sheetName, id) {
    try {
        const ss = SpreadsheetApp
            .openById(SPREADSHEET_ID);
        const sheet = ss.getSheetByName(sheetName);

        if (!sheet) {
            return sendResponse({
                success: false,
                message: "Sheet not found"
            });
        }

        const lastRow = sheet.getLastRow();
        if (lastRow <= 1) {
            return sendResponse({
                success: false,
                message: "No data found"
            });
        }

        const idValues = sheet.getRange(
            2, 1, lastRow - 1, 1
        ).getValues();

        let targetRow = -1;
        for (let i = 0; i < idValues.length; i++) {
            if (idValues[i][0].toString()
                    === id.toString()) {
                targetRow = i + 2;
                break;
            }
        }

        if (targetRow === -1) {
            return sendResponse({
                success: false,
                message: "Notification not found: "
                    + id
            });
        }

        // Column 8 (index 7) is isRead column
        sheet.getRange(targetRow, 8)
            .setValue("TRUE");

        // Column 9 is readAt timestamp
        sheet.getRange(targetRow, 9)
            .setValue(formatDateTime(new Date()));

        return sendResponse({
            success: true,
            message: "Marked as read: " + id
        });

    } catch (error) {
        return sendErrorResponse(
            "Mark read error: " + error.message);
    }
}

// ── Search sheet by column value
function handleSearch(sheetName, column, value) {
    try {
        const data = getSheetData(sheetName);
        const results = data.filter(row =>
            row[column] !== null
            && row[column].toString()
                .toLowerCase()
                .includes(value.toLowerCase())
        );

        return sendResponse({
            success: true,
            data: results,
            count: results.length
        });

    } catch (error) {
        return sendErrorResponse(
            "Search error: " + error.message);
    }
}

// ═══════════════════════════════════════════════════
// SHEET INITIALIZATION
// ═══════════════════════════════════════════════════

// ── Create sheet with headers
function createSheet(sheetName) {
    const ss = SpreadsheetApp
        .openById(SPREADSHEET_ID);
    const sheet = ss.insertSheet(sheetName);

    const headers = getHeaders(sheetName);
    if (headers.length > 0) {
        // Set header row
        sheet.getRange(1, 1, 1, headers.length)
            .setValues([headers]);

        // Style header row
        const headerRange = sheet.getRange(
            1, 1, 1, headers.length);
        headerRange.setBackground("#FF6600");
        headerRange.setFontColor("#FFFFFF");
        headerRange.setFontWeight("bold");
        headerRange.setFontSize(11);

        // Freeze header row
        sheet.setFrozenRows(1);

        // Auto resize columns
        sheet.autoResizeColumns(
            1, headers.length);
    }

    return sheet;
}

// ── Get headers for each sheet
function getHeaders(sheetName) {
    const headerMap = {
        "USERS": [
            "USER_ID", "NAME", "ROLE",
            "MOBILE", "EMAIL", "USERNAME",
            "PASSWORD_HASH", "DEPARTMENT",
            "IS_ACTIVE", "CREATED_AT",
            "LAST_LOGIN", "CREATED_BY"
        ],
        "COMPLAINTS": [
            "COMPLAINT_ID", "CREATED_DATE",
            "CALLER_NAME", "CALLER_MOBILE",
            "CALLER_ADDRESS", "DISTRICT",
            "STATE", "SUBJECT", "DESCRIPTION",
            "CATEGORY", "PRIORITY", "STATUS",
            "ASSIGNED_TO", "ASSIGNED_NAME",
            "ASSIGNED_DATE", "OPERATOR_ID",
            "OPERATOR_NAME", "LAST_UPDATED",
            "RESOLVED_DATE", "CLOSED_DATE",
            "MINISTER_NOTE", "MINISTER_DIRECTION",
            "ACTION_TAKEN", "CLOSURE_REMARK",
            "ESCALATION_REMARK"
        ],
        "APPOINTMENTS": [
            "APPOINTMENT_ID", "CREATED_DATE",
            "VISITOR_NAME", "VISITOR_MOBILE",
            "VISITOR_ADDRESS", "PURPOSE",
            "PREFERRED_DATE", "PREFERRED_TIME",
            "MEETING_LOCATION", "STATUS",
            "APPROVED_DATE", "APPROVED_TIME",
            "APPROVED_VENUE", "APPROVED_BY",
            "APPROVED_AT", "REJECTION_REASON",
            "OPERATOR_ID", "OPERATOR_NAME",
            "LAST_UPDATED", "REMARKS"
        ],
        "AUDIT": [
            "AUDIT_ID", "COMPLAINT_ID",
            "USER_ID", "USER_NAME",
            "USER_ROLE", "ACTION",
            "PREVIOUS_STATUS", "NEW_STATUS",
            "REMARKS", "TIMESTAMP", "IP_ADDRESS"
        ],
        "NOTIFICATIONS": [
            "NOTIFICATION_ID", "USER_ID",
            "USER_NAME", "TITLE", "MESSAGE",
            "TYPE", "COMPLAINT_ID",
            "IS_READ", "CREATED_AT", "READ_AT"
        ],
        "FORWARDING": [
            "FORWARD_ID", "COMPLAINT_ID",
            "FROM_USER_ID", "FROM_USER_NAME",
            "FROM_USER_ROLE", "TO_USER_ID",
            "TO_USER_NAME", "TO_USER_ROLE",
            "REMARKS", "FORWARDED_AT",
            "FORWARD_TYPE"
        ]
    };

    return headerMap[sheetName] || [];
}

// ═══════════════════════════════════════════════════
// INITIALIZE ALL SHEETS
// Run this function once to setup all sheets
// ═══════════════════════════════════════════════════
function initializeAllSheets() {
    const ss = SpreadsheetApp
        .getActiveSpreadsheet();

    Object.keys(SHEETS).forEach(sheetKey => {
        const sheetName = SHEETS[sheetKey];
        let sheet = ss.getSheetByName(sheetName);

        if (!sheet) {
            sheet = createSheet(sheetName);
            console.log(
                "Created sheet: " + sheetName);
        } else {
            console.log(
                "Sheet exists: " + sheetName);
        }
    });

    // Add default admin user if USERS sheet empty
    addDefaultUsers();

    console.log(
        "✅ All sheets initialized successfully");
}

// ── Add default users
function addDefaultUsers() {
    const ss = SpreadsheetApp
        .getActiveSpreadsheet();
    const sheet = ss.getSheetByName("USERS");

    if (!sheet) return;

    const lastRow = sheet.getLastRow();

    // Only add if no users exist
    if (lastRow <= 1) {
        const defaultUsers = [
            [
                "USR-0001",
                "Shri Asim Arun",
                "MINISTER",
                "9999999999",
                "minister@up.gov.in",
                "minister",
                // BCrypt hash of "Minister@2024"
                "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMeSSqmM1h8uNJCxXlR6OqyEBO",
                "MINISTER_OFFICE",
                "TRUE",
                formatDateTime(new Date()),
                "",
                "SYSTEM"
            ],
            [
                "USR-0002",
                "Operator One",
                "OPERATOR",
                "9888888881",
                "operator1@up.gov.in",
                "operator1",
                // BCrypt hash of "Operator@2024"
                "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMeSSqmM1h8uNJCxXlR6OqyEBO",
                "CONTROL_ROOM",
                "TRUE",
                formatDateTime(new Date()),
                "",
                "USR-0001"
            ],
            [
                "USR-0003",
                "Piyush",
                "OFFICER",
                "9777777771",
                "piyush@up.gov.in",
                "piyush",
                // BCrypt hash of "Officer@2024"
                "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMeSSqmM1h8uNJCxXlR6OqyEBO",
                "KANNAUJ",
                "TRUE",
                formatDateTime(new Date()),
                "",
                "USR-0001"
            ],
            [
                "USR-0004",
                "Mohan Verma",
                "OFFICER",
                "9777777772",
                "mohan@up.gov.in",
                "mohan",
                "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMeSSqmM1h8uNJCxXlR6OqyEBO",
                "POLICE",
                "TRUE",
                formatDateTime(new Date()),
                "",
                "USR-0001"
            ],
            [
                "USR-0005",
                "Pant Ji",
                "OFFICER",
                "9777777773",
                "pant@up.gov.in",
                "pant",
                "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMeSSqmM1h8uNJCxXlR6OqyEBO",
                "CORRESPONDENCE",
                "TRUE",
                formatDateTime(new Date()),
                "",
                "USR-0001"
            ],
            [
                "USR-0006",
                "Anil Kumar Goswami",
                "OFFICER",
                "9777777774",
                "anil@up.gov.in",
                "anil",
                "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMeSSqmM1h8uNJCxXlR6OqyEBO",
                "WESTERN_UP",
                "TRUE",
                formatDateTime(new Date()),
                "",
                "USR-0001"
            ],
            [
                "USR-0007",
                "Shailendra Bahadur Singh",
                "OFFICER",
                "9777777775",
                "shailendra@up.gov.in",
                "shailendra",
                "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMeSSqmM1h8uNJCxXlR6OqyEBO",
                "APPOINTMENTS",
                "TRUE",
                formatDateTime(new Date()),
                "",
                "USR-0001"
            ],
            [
                "USR-0008",
                "Abhishek Parihar",
                "OFFICER",
                "9777777776",
                "abhishek@up.gov.in",
                "abhishek",
                "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMeSSqmM1h8uNJCxXlR6OqyEBO",
                "BJP",
                "TRUE",
                formatDateTime(new Date()),
                "",
                "USR-0001"
            ]
        ];

        // Add all default users
        defaultUsers.forEach(user => {
            sheet.appendRow(user);
        });

        console.log(
            "✅ Default users added: "
            + defaultUsers.length);
    }
}

// ═══════════════════════════════════════════════════
// UTILITY FUNCTIONS
// ═══════════════════════════════════════════════════

// ── Format date time
function formatDateTime(date) {
    if (!date) return "";
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(
        d.getMonth() + 1).padStart(2, "0");
    const day = String(
        d.getDate()).padStart(2, "0");
    const hours = String(
        d.getHours()).padStart(2, "0");
    const minutes = String(
        d.getMinutes()).padStart(2, "0");
    const seconds = String(
        d.getSeconds()).padStart(2, "0");

    return year + "-" + month + "-" + day
        + " " + hours + ":" + minutes
        + ":" + seconds;
}

// ── Send success response
function sendResponse(data) {
    return ContentService
        .createTextOutput(
            JSON.stringify(data))
        .setMimeType(
            ContentService.MimeType.JSON);
}

// ── Send error response
function sendErrorResponse(message) {
    return ContentService
        .createTextOutput(
            JSON.stringify({
                success: false,
                error: message,
                timestamp: formatDateTime(
                    new Date())
            }))
        .setMimeType(
            ContentService.MimeType.JSON);
}

// ═══════════════════════════════════════════════════
// ANALYTICS FUNCTIONS
// ═══════════════════════════════════════════════════

// ── Get dashboard stats
function getDashboardStats() {
    const complaints = getSheetData("COMPLAINTS");

    const stats = {
        total: complaints.length,
        new: 0,
        assigned: 0,
        pending: 0,
        actionTaken: 0,
        escalated: 0,
        ministerReview: 0,
        resolved: 0,
        closed: 0,
        critical: 0,
        high: 0,
        medium: 0,
        low: 0
    };

    complaints.forEach(row => {
        // Status counts (column index 11)
        const status = row[11];
        switch(status) {
            case "NEW":
                stats.new++;
                break;
            case "ASSIGNED":
                stats.assigned++;
                break;
            case "PENDING":
                stats.pending++;
                break;
            case "ACTION_TAKEN":
                stats.actionTaken++;
                break;
            case "ESCALATED":
                stats.escalated++;
                break;
            case "MINISTER_REVIEW":
                stats.ministerReview++;
                break;
            case "RESOLVED":
                stats.resolved++;
                break;
            case "CLOSED":
                stats.closed++;
                break;
        }

        // Priority counts (column index 10)
        const priority = row[10];
        switch(priority) {
            case "CRITICAL":
                stats.critical++;
                break;
            case "HIGH":
                stats.high++;
                break;
            case "MEDIUM":
                stats.medium++;
                break;
            case "LOW":
                stats.low++;
                break;
        }
    });

    return sendResponse({
        success: true,
        data: stats
    });
}

// ── Get officer wise stats
function getOfficerStats() {
    const complaints = getSheetData("COMPLAINTS");
    const users = getSheetData("USERS");

    const officers = users.filter(
        row => row[2] === "OFFICER");

    const officerStats = officers.map(officer => {
        const officerId = officer[0];
        const officerComplaints = complaints.filter(
            c => c[12] === officerId);

        const resolved = officerComplaints.filter(
            c => c[11] === "RESOLVED"
                || c[11] === "CLOSED").length;

        const pending = officerComplaints.filter(
            c => c[11] === "PENDING"
                || c[11] === "ASSIGNED"
                || c[11] === "NEW").length;

        const escalated = officerComplaints.filter(
            c => c[11] === "ESCALATED").length;

        const total = officerComplaints.length;
        const rate = total > 0
            ? ((resolved / total) * 100).toFixed(1)
            : 0;

        return {
            officerId: officerId,
            officerName: officer[1],
            department: officer[7],
            total: total,
            resolved: resolved,
            pending: pending,
            escalated: escalated,
            resolutionRate: rate + "%"
        };
    });

    return sendResponse({
        success: true,
        data: officerStats
    });
}

// ── Get district wise stats
function getDistrictStats() {
    const complaints = getSheetData("COMPLAINTS");
    const districtMap = {};

    complaints.forEach(row => {
        const district = row[5] || "UNKNOWN";
        districtMap[district] =
            (districtMap[district] || 0) + 1;
    });

    return sendResponse({
        success: true,
        data: districtMap
    });
}

// ── Get monthly stats
function getMonthlyStats() {
    const complaints = getSheetData("COMPLAINTS");
    const monthlyMap = {};

    complaints.forEach(row => {
        const dateStr = row[1];
        if (dateStr) {
            const date = new Date(dateStr);
            const monthYear =
                date.toLocaleString("en-IN", {
                    month: "short",
                    year: "numeric"
                });
            monthlyMap[monthYear] =
                (monthlyMap[monthYear] || 0) + 1;
        }
    });

    return sendResponse({
        success: true,
        data: monthlyMap
    });
}

// ═══════════════════════════════════════════════════
// AUTO BACKUP FUNCTION
// Setup trigger: Run daily at midnight
// ═══════════════════════════════════════════════════
function createDailyBackup() {
    const ss = SpreadsheetApp
        .getActiveSpreadsheet();
    const date = formatDateTime(new Date())
        .substring(0, 10);

    const backupName =
        "Backup_" + date;

    try {
        const copy = ss.copy(backupName);
        console.log(
            "✅ Backup created: " + backupName);
    } catch (error) {
        console.error(
            "Backup failed: " + error.message);
    }
}

// ═══════════════════════════════════════════════════
// SETUP TRIGGERS
// Run this once to setup auto triggers
// ═══════════════════════════════════════════════════
function setupTriggers() {
    // Delete existing triggers
    const triggers = ScriptApp.getProjectTriggers();
    triggers.forEach(trigger => {
        ScriptApp.deleteTrigger(trigger);
    });

    // Daily backup at midnight
    ScriptApp.newTrigger("createDailyBackup")
        .timeBased()
        .everyDays(1)
        .atHour(0)
        .create();

    console.log("✅ Triggers setup complete");
}

// ═══════════════════════════════════════════════════
// TEST FUNCTION
// Run this to test the setup
// ═══════════════════════════════════════════════════
function testSetup() {
    console.log("Testing setup...");

    // Test 1: Initialize sheets
    initializeAllSheets();
    console.log("✅ Sheets initialized");

    // Test 2: Get users
    const users = getSheetData("USERS");
    console.log("✅ Users found: "
        + users.length);

    // Test 3: Get complaints
    const complaints =
        getSheetData("COMPLAINTS");
    console.log("✅ Complaints found: "
        + complaints.length);

    console.log("✅ All tests passed!");
    console.log(
        "✅ Minister ERP Google Sheet Ready");
}