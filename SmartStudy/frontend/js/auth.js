/**
 * auth.js — Authentication utilities
 * Handles JWT token storage, auth checks, and redirects
 */

const AUTH_TOKEN_KEY = 'smartstudy_token';
const AUTH_USER_KEY = 'smartstudy_user';

/**
 * Save auth data after login
 */
function saveAuth(token, name, email) {
    localStorage.setItem(AUTH_TOKEN_KEY, token);
    localStorage.setItem(AUTH_USER_KEY, JSON.stringify({ name, email }));
}

/**
 * Get stored JWT token
 */
function getToken() {
    return localStorage.getItem(AUTH_TOKEN_KEY);
}

/**
 * Get stored user info
 */
function getUser() {
    const data = localStorage.getItem(AUTH_USER_KEY);
    return data ? JSON.parse(data) : null;
}

/**
 * Check if user is logged in
 */
function isLoggedIn() {
    return getToken() !== null;
}

/**
 * Logout — clear stored data and redirect to login
 */
function logout() {
    localStorage.removeItem(AUTH_TOKEN_KEY);
    localStorage.removeItem(AUTH_USER_KEY);
    window.location.href = 'login.html';
}

/**
 * Auth guard — redirect to login if not logged in
 * Call this on every protected page
 */
function requireAuth() {
    if (!isLoggedIn()) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

/**
 * Get auth headers for fetch requests
 */
function getAuthHeaders() {
    const token = getToken();
    return token ? { 'Authorization': 'Bearer ' + token } : {};
}

/**
 * Update navbar to show user name and logout button
 */
function updateNavbar() {
    const user = getUser();
    if (!user) return;

    const navLinks = document.querySelector('.nav-links');
    if (navLinks) {
        // Add user greeting and logout
        const greeting = document.createElement('li');
        greeting.innerHTML = `<span style="color: #667eea; font-weight: 600;">👤 ${user.name}</span>`;
        navLinks.appendChild(greeting);

        const logoutItem = document.createElement('li');
        logoutItem.innerHTML = `<a href="#" onclick="logout(); return false;" style="color: #e74c3c;">Logout</a>`;
        navLinks.appendChild(logoutItem);
    }
}

// Auto-setup on page load (for protected pages)
document.addEventListener('DOMContentLoaded', function() {
    // Only run auth guard on protected pages (not login/register)
    const page = window.location.pathname;
    if (!page.includes('login.html') && !page.includes('register.html') && !page.includes('index.html')) {
        if (!requireAuth()) return;
        updateNavbar();
    }
});

console.log('✅ auth.js loaded successfully');
