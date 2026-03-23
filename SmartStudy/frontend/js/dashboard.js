// API Base URL
const API_URL = window.APP_CONFIG?.JAVA_SESSIONS_API_URL || 'http://localhost:8080/api/sessions';

// Initialize dashboard when page loads
document.addEventListener('DOMContentLoaded', function() {
    loadDashboardData();
});

/**
 * Load all dashboard data
 */
async function loadDashboardData() {
    try {
        // Load statistics
        await loadStatistics();

        // Load recent sessions
        await loadRecentSessions();

        // Load charts data
        await loadCharts();

        // Load weekly progress
        await loadWeeklyProgress();

    } catch (error) {
        console.error('Error loading dashboard:', error);
        showError('Failed to load dashboard data. Is the backend running?');
    }
}

/**
 * Load and display statistics
 */
async function loadStatistics() {
    try {
        const response = await fetch(`${API_URL}/statistics`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const stats = await response.json();

        // Update statistics cards
        document.getElementById('totalSessions').textContent = stats.totalSessions || 0;
        document.getElementById('totalHours').textContent = (stats.totalHours || 0).toFixed(1);
        document.getElementById('avgFocus').textContent = (stats.averageFocus || 0).toFixed(1);

        // Calculate this week's sessions
        const sessionsResponse = await fetch(`${API_URL}`);
        if (!sessionsResponse.ok) {
            throw new Error(`HTTP ${sessionsResponse.status}`);
        }
        const sessionsData = await sessionsResponse.json();
        const sessions = Array.isArray(sessionsData.sessions) ? sessionsData.sessions : [];
        const thisWeekCount = calculateThisWeekSessions(sessions);
        document.getElementById('thisWeek').textContent = thisWeekCount;

    } catch (error) {
        console.error('Error loading statistics:', error);
    }
}

/**
 * Calculate sessions from this week
 */
function calculateThisWeekSessions(sessions) {
    const now = new Date();
    const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);

    return sessions.filter(session => {
        const sessionDate = new Date(session.createdAt);
        return sessionDate >= weekAgo;
    }).length;
}

/**
 * Load and display recent sessions
 */
async function loadRecentSessions() {
    try {
        const response = await fetch(`${API_URL}?recent=true`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const data = await response.json();

        const container = document.getElementById('recentSessions');
        const sessions = Array.isArray(data.sessions) ? data.sessions : [];

        if (sessions.length === 0) {
            container.innerHTML = '<p class="no-data">No sessions yet. <a href="log-session.html">Log your first session!</a></p>';
            return;
        }

        // Create table
        let html = `
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Subject</th>
                        <th>Date</th>
                        <th>Time</th>
                        <th>Duration</th>
                        <th>Focus</th>
                    </tr>
                </thead>
                <tbody>
        `;

        sessions.slice(0, 10).forEach(session => {
            const date = new Date(session.startTime);
            const dateStr = date.toLocaleDateString();
            const timeStr = date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
            const focusClass = session.focusRating >= 7 ? 'high-focus' : session.focusRating >= 4 ? 'medium-focus' : 'low-focus';

            html += `
                <tr>
                    <td><strong>${session.subject}</strong></td>
                    <td>${dateStr}</td>
                    <td>${timeStr}</td>
                    <td>${session.duration} min</td>
                    <td><span class="focus-badge ${focusClass}">${session.focusRating}/10</span></td>
                </tr>
            `;
        });

        html += `
                </tbody>
            </table>
        `;

        container.innerHTML = html;

    } catch (error) {
        console.error('Error loading recent sessions:', error);
        document.getElementById('recentSessions').innerHTML = '<p class="error">Failed to load sessions</p>';
    }
}

/**
 * Load and display charts
 */
async function loadCharts() {
    try {
        const response = await fetch(`${API_URL}/statistics`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const stats = await response.json();

        // Display subject chart
        displaySubjectChart(stats.bySubject);

        // Display time distribution
        await displayTimeChart();

    } catch (error) {
        console.error('Error loading charts:', error);
    }
}

/**
 * Display focus by subject chart
 */
function displaySubjectChart(bySubject) {
    const container = document.getElementById('subjectChart');

    if (!bySubject || bySubject.length === 0) {
        container.innerHTML = '<p class="no-data">No data yet</p>';
        return;
    }

    let html = '<div class="bar-chart">';

    bySubject.forEach(item => {
        const subject = item[0];
        const count = item[1];
        const avgFocus = item[2];
        const percentage = (avgFocus / 10) * 100;

        html += `
            <div class="bar-item">
                <div class="bar-label">${subject}</div>
                <div class="bar-wrapper">
                    <div class="bar" style="width: ${percentage}%">
                        <span class="bar-value">${avgFocus.toFixed(1)}/10</span>
                    </div>
                </div>
                <div class="bar-count">${count} sessions</div>
            </div>
        `;
    });

    html += '</div>';
    container.innerHTML = html;
}

/**
 * Display time distribution chart
 */
async function displayTimeChart() {
    try {
        const response = await fetch(`${API_URL}`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const data = await response.json();

        const container = document.getElementById('timeChart');
        const sessions = Array.isArray(data.sessions) ? data.sessions : [];

        if (sessions.length === 0) {
            container.innerHTML = '<p class="no-data">No data yet</p>';
            return;
        }

        // Group by hour
        const hourCounts = {};
        sessions.forEach(session => {
            const hour = session.hourOfDay;
            hourCounts[hour] = (hourCounts[hour] || 0) + 1;
        });

        // Create time blocks
        const timeBlocks = {
            'Morning (6-9 AM)': [6, 7, 8, 9],
            'Late Morning (9-12 PM)': [9, 10, 11, 12],
            'Afternoon (12-3 PM)': [12, 13, 14, 15],
            'Evening (3-6 PM)': [15, 16, 17, 18],
            'Night (6-9 PM)': [18, 19, 20, 21]
        };

        let html = '<div class="bar-chart">';

        Object.entries(timeBlocks).forEach(([label, hours]) => {
            const count = hours.reduce((sum, h) => sum + (hourCounts[h] || 0), 0);
            const maxCount = Math.max(...Object.values(hourCounts));
            const percentage = maxCount > 0 ? (count / maxCount) * 100 : 0;

            html += `
                <div class="bar-item">
                    <div class="bar-label">${label}</div>
                    <div class="bar-wrapper">
                        <div class="bar bar-time" style="width: ${percentage}%">
                            <span class="bar-value">${count}</span>
                        </div>
                    </div>
                </div>
            `;
        });

        html += '</div>';
        container.innerHTML = html;

    } catch (error) {
        console.error('Error loading time chart:', error);
    }
}

/**
 * Load weekly progress
 */
async function loadWeeklyProgress() {
    try {
        const response = await fetch(`${API_URL}`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const data = await response.json();

        const container = document.getElementById('weeklyProgress');
        const sessions = Array.isArray(data.sessions) ? data.sessions : [];

        if (sessions.length === 0) {
            container.innerHTML = '<p class="no-data">No data yet</p>';
            return;
        }

        // Group by day of week
        const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
        const dayCounts = [0, 0, 0, 0, 0, 0, 0];
        const dayFocus = [0, 0, 0, 0, 0, 0, 0];

        sessions.forEach(session => {
            const day = session.dayOfWeek;
            dayCounts[day]++;
            dayFocus[day] += session.focusRating;
        });

        let html = '<div class="weekly-grid">';

        dayNames.forEach((name, index) => {
            const count = dayCounts[index];
            const avgFocus = count > 0 ? (dayFocus[index] / count).toFixed(1) : 0;
            const isToday = new Date().getDay() === index;

            html += `
                <div class="day-card ${isToday ? 'today' : ''}">
                    <div class="day-name">${name.substring(0, 3)}</div>
                    <div class="day-count">${count}</div>
                    <div class="day-focus">${avgFocus}/10</div>
                </div>
            `;
        });

        html += '</div>';
        container.innerHTML = html;

    } catch (error) {
        console.error('Error loading weekly progress:', error);
    }
}

/**
 * Refresh dashboard
 */
function refreshDashboard() {
    // Show loading state
    document.querySelectorAll('.loading').forEach(el => {
        el.style.display = 'block';
    });

    // Reload all data
    loadDashboardData();
}

/**
 * Show error message
 */
function showError(message) {
    alert(message);
}