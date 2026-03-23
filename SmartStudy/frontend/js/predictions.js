// API URLs
const JAVA_API_URL = window.APP_CONFIG?.JAVA_API_BASE_URL || 'http://localhost:8080/api';
const PYTHON_API_URL = window.APP_CONFIG?.PYTHON_API_BASE_URL || 'http://localhost:5001';
const SUBJECTS_URL = window.APP_CONFIG?.SUBJECTS_URL || '../shared/subjects.json';

// Initialize page
document.addEventListener('DOMContentLoaded', async function() {
    await loadSubjects();
    checkMLServiceStatus();
    setupPredictionForm();
});

async function loadSubjects() {
    const subjectSelect = document.getElementById('subject');

    try {
        const response = await fetch(SUBJECTS_URL);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const subjects = await response.json();
        if (!Array.isArray(subjects) || subjects.length === 0) {
            throw new Error('Invalid subjects format');
        }

        subjectSelect.innerHTML = '<option value="">-- Choose Subject --</option>';
        subjects.forEach(subject => {
            const option = document.createElement('option');
            option.value = subject;
            option.textContent = subject;
            subjectSelect.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading subjects:', error);
        showMessage('Could not load subject list. Please reload after starting a local server.', 'error');
    }
}

/**
 * Check if ML service is available
 */
async function checkMLServiceStatus() {
    const statusDot = document.getElementById('statusDot');
    const statusText = document.getElementById('statusText');

    try {
        // Try to reach Python ML service directly
        const response = await fetch(`${PYTHON_API_URL}/`, {
            method: 'GET',
            mode: 'cors'
        });

        if (response.ok) {
            statusDot.className = 'status-dot online';
            statusText.textContent = 'ML Service Online ✓';
        } else {
            throw new Error('Service not responding');
        }
    } catch (error) {
        statusDot.className = 'status-dot offline';
        statusText.textContent = 'ML Service Offline ✗';
        console.error('ML Service is not available:', error);
    }
}

/**
 * Setup prediction form
 */
function setupPredictionForm() {
    const form = document.getElementById('predictionForm');

    form.addEventListener('submit', async function(e) {
        e.preventDefault();

        const subject = document.getElementById('subject').value;
        const topN = parseInt(document.getElementById('topN').value);

        if (!subject) {
            showMessage('Please select a subject', 'error');
            return;
        }

        await getPredictions(subject, topN);
    });
}

/**
 * Get predictions from ML service
 */
async function getPredictions(subject, topN) {
    const submitBtn = document.querySelector('button[type="submit"]');
    const messageDiv = document.getElementById('message');
    const resultsCard = document.getElementById('resultsCard');

    // Show loading
    submitBtn.disabled = true;
    submitBtn.textContent = '🔄 Loading predictions...';
    messageDiv.classList.add('hidden');
    resultsCard.style.display = 'none';

    try {
        // Call Java backend which will call Python ML service
        const response = await fetch(`${JAVA_API_URL}/predictions/best-times?subject=${encodeURIComponent(subject)}&top_n=${topN}`);

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to get predictions');
        }

        const data = await response.json();

        // Display predictions
        displayPredictions(data, subject);

    } catch (error) {
        console.error('Error getting predictions:', error);
        showMessage(error.message || 'Failed to get predictions. Make sure both Java backend and Python ML service are running.', 'error');
    } finally {
        // Reset button
        submitBtn.disabled = false;
        submitBtn.textContent = '🔮 Get Predictions';
    }
}

/**
 * Display predictions
 */
function displayPredictions(data, subject) {
    const resultsCard = document.getElementById('resultsCard');
    const predictionsContainer = document.getElementById('predictions');
    const resultSubject = document.getElementById('resultSubject');

    resultSubject.textContent = subject;

    if (!data.bestTimes || data.bestTimes.length === 0) {
        predictionsContainer.innerHTML = '<p class="no-data">No predictions available. You need at least 30 study sessions for predictions to work.</p>';
        resultsCard.style.display = 'block';
        return;
    }

    let html = '';

    data.bestTimes.forEach((pred, index) => {
        const rank = index + 1;
        const emoji = rank === 1 ? '🥇' : rank === 2 ? '🥈' : rank === 3 ? '🥉' : '⭐';
        const focusClass = pred.predictedFocus >= 8 ? 'excellent' : pred.predictedFocus >= 6 ? 'good' : 'moderate';
        const confidenceClass = pred.confidence === 'high' ? 'high-confidence' : pred.confidence === 'medium' ? 'medium-confidence' : 'low-confidence';

        html += `
            <div class="prediction-card ${focusClass}">
                <div class="prediction-rank">${emoji} #${rank}</div>
                <div class="prediction-content">
                    <div class="prediction-time">
                        <h3>${pred.dayName} ${formatHour(pred.hour)}</h3>
                        <p class="prediction-subtitle">${getTimeOfDay(pred.hour)}</p>
                    </div>
                    <div class="prediction-score">
                        <div class="score-circle">
                            <span class="score-value">${pred.predictedFocus.toFixed(1)}</span>
                            <span class="score-max">/10</span>
                        </div>
                        <p class="score-label">Predicted Focus</p>
                    </div>
                    <div class="prediction-confidence">
                        <span class="confidence-badge ${confidenceClass}">
                            ${pred.confidence} confidence
                        </span>
                    </div>
                </div>
            </div>
        `;
    });

    predictionsContainer.innerHTML = html;
    resultsCard.style.display = 'block';

    // Scroll to results
    resultsCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

/**
 * Format hour to 12-hour format
 */
function formatHour(hour) {
    const period = hour < 12 ? 'AM' : 'PM';
    const hour12 = hour % 12 || 12;
    return `${hour12}:00 ${period}`;
}

/**
 * Get time of day label
 */
function getTimeOfDay(hour) {
    if (hour < 6) return 'Late Night';
    if (hour < 12) return 'Morning';
    if (hour < 17) return 'Afternoon';
    if (hour < 21) return 'Evening';
    return 'Night';
}

/**
 * Train/retrain model
 */
async function trainModel() {
    if (!confirm('This will retrain the ML model with all your study sessions. This may take a few moments. Continue?')) {
        return;
    }

    const btn = event.target;
    btn.disabled = true;
    btn.textContent = '🔄 Training model...';

    try {
        const response = await fetch(`${JAVA_API_URL}/predictions/train`, {
            method: 'POST'
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to train model');
        }

        const data = await response.json();

        showMessage(`Model trained successfully! MAE: ${data.details.metrics.mae}, R²: ${data.details.metrics.r2_score}`, 'success');

        // Refresh predictions if form was submitted
        const subject = document.getElementById('subject').value;
        if (subject) {
            const topN = parseInt(document.getElementById('topN').value);
            await getPredictions(subject, topN);
        }

    } catch (error) {
        console.error('Error training model:', error);
        showMessage(error.message || 'Failed to train model. Make sure you have at least 30 study sessions.', 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = '🔄 Retrain Model with Latest Data';
    }
}

/**
 * Show message
 */
function showMessage(text, type) {
    const messageDiv = document.getElementById('message');
    messageDiv.textContent = text;
    messageDiv.className = `message ${type}`;
    messageDiv.classList.remove('hidden');

    // Auto-hide after 5 seconds for success messages
    if (type === 'success') {
        setTimeout(() => {
            messageDiv.classList.add('hidden');
        }, 5000);
    }
}