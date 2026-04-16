/**
 * main.js
 * Common utilities and shared functions across all pages
 */

// API Base URL - Update this if backend port changes
const API_BASE_URL = window.APP_CONFIG?.JAVA_API_BASE_URL || 'http://localhost:8080/api';

/**
 * Make API request with error handling
 */
async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;

    try {
        // Get auth headers (from auth.js)
        const authHeaders = typeof getAuthHeaders === 'function' ? getAuthHeaders() : {};

        const response = await fetch(url, {
            headers: {
                'Content-Type': 'application/json',
                ...authHeaders,
                ...options.headers
            },
            ...options
        });

        // If unauthorized, redirect to login
        if (response.status === 401) {
            if (typeof logout === 'function') logout();
            return;
        }

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error(`API request failed for ${endpoint}:`, error);
        throw error;
    }
}

/**
 * Format date to readable string
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

/**
 * Format time to readable string
 */
function formatTime(dateString) {
    const date = new Date(dateString);
    return date.toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Format datetime to readable string
 */
function formatDateTime(dateString) {
    return `${formatDate(dateString)} ${formatTime(dateString)}`;
}

/**
 * Show loading spinner
 */
function showLoading(containerId) {
    const container = document.getElementById(containerId);
    if (container) {
        container.innerHTML = '<div class="loading">Loading...</div>';
    }
}

/**
 * Show error message
 */
function showError(containerId, message) {
    const container = document.getElementById(containerId);
    if (container) {
        container.innerHTML = `<div class="error">${message}</div>`;
    }
}

/**
 * Show success message
 */
function showSuccess(containerId, message) {
    const container = document.getElementById(containerId);
    if (container) {
        container.innerHTML = `<div class="success">${message}</div>`;
    }
}

/**
 * Get focus rating color class
 */
function getFocusClass(rating) {
    if (rating >= 7) return 'high-focus';
    if (rating >= 4) return 'medium-focus';
    return 'low-focus';
}

/**
 * Get day name from day number (0=Sunday, 6=Saturday)
 */
function getDayName(dayNumber) {
    const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    return days[dayNumber] || 'Unknown';
}

/**
 * Get subject icon/emoji
 */
function getSubjectIcon(subject) {
    const icons = {
        'Deep Learning (DL)': '🧬',
        'Intro to AI': '🧠',
        'DSA': '🧩',
        'Intro to Machine Learning (ML)': '🤖',
        'Java Programming': '☕',
        'Programming for Problem Solving (PPS)': '💻'
    };
    return icons[subject] || '📖';
}

/**
 * Debounce function to limit API calls
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

/**
 * Check if backend is reachable
 */
async function checkBackendHealth() {
    try {
        const response = await fetch(`${API_BASE_URL}/sessions`);
        return response.ok;
    } catch (error) {
        console.error('Backend health check failed:', error);
        return false;
    }
}

/**
 * Display backend status indicator
 */
async function updateBackendStatus() {
    const statusElements = document.querySelectorAll('.backend-status');
    const isHealthy = await checkBackendHealth();

    statusElements.forEach(element => {
        element.classList.remove('online', 'offline');
        element.classList.add(isHealthy ? 'online' : 'offline');
        element.textContent = isHealthy ? '● Backend Online' : '● Backend Offline';
    });
}

// Initialize backend status check on page load
document.addEventListener('DOMContentLoaded', function() {
    // Check backend status
    updateBackendStatus();

    // Set up periodic health checks (every 30 seconds)
    setInterval(updateBackendStatus, 30000);
});

console.log('✅ main.js loaded successfully');