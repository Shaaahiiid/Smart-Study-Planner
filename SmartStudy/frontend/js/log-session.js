// Wait for page to fully load before running code
document.addEventListener('DOMContentLoaded', function() {
    const SUBJECTS_URL = window.APP_CONFIG?.SUBJECTS_URL || '../shared/subjects.json';
    const SESSIONS_API_URL = window.APP_CONFIG?.JAVA_SESSIONS_API_URL || 'http://localhost:8080/api/sessions';
    
    // ========== PART 1: Get references to HTML elements ==========
    const form = document.getElementById('sessionForm');
    const subjectSelect = document.getElementById('subject');
    const focusSlider = document.getElementById('focusRating');
    const focusValue = document.getElementById('focusValue');
    const messageDiv = document.getElementById('message');
    const recentSessionsDiv = document.getElementById('recentSessions');

    async function loadSubjects() {
        try {
            const response = await fetch(SUBJECTS_URL);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const subjects = await response.json();
            if (!Array.isArray(subjects) || subjects.length === 0) {
                throw new Error('Invalid subjects format');
            }

            subjectSelect.innerHTML = '<option value="">-- Select Subject --</option>';
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

    
    // ========== PART 2: Update slider value display in real-time ==========
    focusSlider.addEventListener('input', function() {
        // 'this.value' is the current slider position (1-10)
        focusValue.textContent = this.value;
        
        // Change color based on value (visual feedback)
        if (this.value <= 3) {
            focusValue.style.color = '#e74c3c'; // Red for low focus
        } else if (this.value <= 7) {
            focusValue.style.color = '#f39c12'; // Orange for medium
        } else {
            focusValue.style.color = '#27ae60'; // Green for high focus
        }
    });

    
    // ========== PART 3: Handle form submission ==========
    form.addEventListener('submit', async function(e) {
        // Prevent default form submission (page refresh)
        e.preventDefault();
        
        // Collect all form data
        const formData = {
            subject: document.getElementById('subject').value,
            startTime: document.getElementById('startTime').value,
            endTime: document.getElementById('endTime').value,
            focusRating: parseInt(document.getElementById('focusRating').value),
            notes: document.getElementById('notes').value
        };

        // Validate: end time must be after start time
        if (new Date(formData.endTime) <= new Date(formData.startTime)) {
            showMessage('End time must be after start time!', 'error');
            return; // Stop here, don't submit
        }

        // Calculate duration in minutes
        const start = new Date(formData.startTime);
        const end = new Date(formData.endTime);
        const durationMinutes = (end - start) / (1000 * 60); // Convert ms to minutes
        formData.duration = durationMinutes;

        // Extract hour and day for ML features
        formData.hourOfDay = start.getHours(); // 0-23
        formData.dayOfWeek = start.getDay(); // 0=Sunday, 6=Saturday

        
        // ========== SEND DATA TO BACKEND ==========
        try {
            const response = await fetch(SESSIONS_API_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...getAuthHeaders()
                },
                body: JSON.stringify(formData)
            });

            // Check if request was successful
            if (response.ok) {
                const result = await response.json();
                showMessage('Session saved successfully! 🎉', 'success');
                form.reset(); // Clear the form
                focusValue.textContent = '5'; // Reset slider display
                loadRecentSessions(); // Refresh the list
            } else {
                showMessage('Error saving session. Please try again.', 'error');
            }
        } catch (error) {
            console.error('Error:', error);
            showMessage('Connection error. Is the server running?', 'error');
        }
    });

    
    // ========== PART 4: Show success/error messages ==========
    function showMessage(text, type) {
        messageDiv.textContent = text;
        messageDiv.className = `message ${type}`; // Add 'success' or 'error' class
        messageDiv.classList.remove('hidden');
        
        // Auto-hide after 5 seconds
        setTimeout(() => {
            messageDiv.classList.add('hidden');
        }, 5000);
    }

    
    // ========== PART 5: Load and display recent sessions ==========
    async function loadRecentSessions() {
        try {
            const response = await fetch(`${SESSIONS_API_URL}?recent=true`, {
                headers: getAuthHeaders()
            });
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const data = await response.json();
            const sessions = Array.isArray(data.sessions) ? data.sessions : [];

            if (sessions.length === 0) {
                recentSessionsDiv.innerHTML = '<p>No sessions yet. Log your first one!</p>';
                return;
            }

            // Build HTML for each session
            let html = '<div class="sessions-list">';
            sessions.forEach(session => {
                const date = new Date(session.startTime);
                const formattedDate = date.toLocaleDateString();
                const formattedTime = date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
                
                html += `
                    <div class="session-item">
                        <div class="session-header">
                            <strong>${session.subject}</strong>
                            <span class="session-date">${formattedDate} ${formattedTime}</span>
                        </div>
                        <div class="session-details">
                            <span>Duration: ${session.duration} min</span>
                            <span>Focus: ${session.focusRating}/10</span>
                        </div>
                    </div>
                `;
            });
            html += '</div>';

            recentSessionsDiv.innerHTML = html;
        } catch (error) {
            console.error('Error loading sessions:', error);
            recentSessionsDiv.innerHTML = '<p>Error loading sessions</p>';
        }
    }

    
    // Load subjects and recent sessions when page loads
    loadSubjects();
    loadRecentSessions();
});