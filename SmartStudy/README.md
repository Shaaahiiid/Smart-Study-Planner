# 📚 SmartStudy: AI-Powered Study Planner

SmartStudy is an intelligent, personalized study scheduling web application that tracks your study sessions and uses Machine Learning to learn your productivity patterns. By analyzing your subjective focus ratings across different subjects and times of the day, SmartStudy's AI predicts your peak cognitive hours, allowing you to optimize your study schedule.

## ✨ Features

- **Personalized Accounts (JWT Auth):** Secure registration and login. Your data and predictions are isolated to your account.
- **Study Session Logging:** Log the subject, start time, end time, and rate your focus level (1-10) for every study session.
- **Interactive Dashboard:** View your study statistics, average focus times, and visualizations of your recent activity.
- **AI-Powered Predictions:** Uses a Random Forest Regressor to predict your optimal focus hours for any specific subject based on your unique historical patterns.
- **Smart Retraining:** The ML model automatically adapts and learns as you log more sessions.

## 🛠️ Technology Stack

SmartStudy utilizes a modern 3-tier microservices architecture:

- **Frontend:** HTML5, CSS3, Vanilla JavaScript (Fetch API)
- **Backend:** Java 21, Spring Boot, Spring Security (BCrypt), Spring Data JPA, JWT (io.jsonwebtoken)
- **Database:** MySQL
- **Machine Learning Service:** Python 3, Flask, Scikit-Learn, Pandas

## 📂 Project Structure

```text
SmartStudy/
├── backend/                  # Java Spring Boot backend (Core APIs & Authentication)
│   ├── src/main/java         # Controllers, Services, Models, Repositories, Security
│   └── pom.xml               # Maven dependencies
├── frontend/                 # Client-side web interface (HTML/CSS/JS)
│   ├── index.html            # Landing page
│   ├── dashboard.html        # User statistics dashboard
│   ├── log-session.html      # Study session entry form
│   ├── predictions.html      # AI prediction interface
│   ├── login.html            # User login
│   ├── register.html         # User registration
│   ├── css/                  # Stylesheets
│   └── js/                   # Client-side logic & API integration
├── python-ml-service/        # Python Flask microservice (Machine Learning)
│   ├── ml_service.py         # Flask API & RandomForestRegressor logic
│   └── requirements.txt      # Python dependencies
└── shared/                   # Shared configurations and data
    ├── subjects.json         # Master list of subjects
    └── seed_sessions.sql     # SQL script to seed sample data
```

## 🚀 Getting Started

To run SmartStudy locally, you will need to start three separate components: the MySQL Database, the Spring Boot Backend, and the Python ML Service.

### Prerequisites

- **Java 21** or higher
- **Maven**
- **Python 3.8** or higher
- **MySQL** installed and running

### 1. Database Setup

1. Open MySQL Command Line or Workbench.
2. Create the database:
   ```sql
   CREATE DATABASE study_planner;
   ```
3. Update the database credentials in `backend/src/main/resources/application.properties` to match your local MySQL username and password.

### 2. Spring Boot Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Compile and run the application using Maven:
   ```bash
   ./mvnw spring-boot:run
   ```
   *The backend will start on `http://localhost:8080`. (Hovering the database schema creation automatically via Hibernate).*

### 3. Python ML Service Setup

1. Open a new terminal and navigate to the ML service directory:
   ```bash
   cd python-ml-service
   ```
2. (Optional but recommended) Create and activate a virtual environment:
   ```bash
   python3 -m venv venv
   source venv/bin/activate
   ```
3. Install the required dependencies:
   ```bash
   pip install -r requirements.txt
   ```
4. Run the Flask service:
   ```bash
   python ml_service.py
   ```
   *The ML service will start on `http://localhost:5001`.*

### 4. Frontend Setup

The frontend uses vanilla web technologies and does not require a build step.
1. Install a local web server tool like [Live Server](https://marketplace.visualstudio.com/items?itemName=ritwickdey.LiveServer) in VS Code, or use Python's built-in server:
   ```bash
   cd frontend
   python3 -m http.server 5501
   ```
2. Open your browser and navigate to `http://localhost:5501` to access the application.

---

## 🧪 Demo Data (Optional)

To test the Machine Learning functionality immediately, you need at least 30 logged sessions. We have provided a SQL script to seed 50 realistic sessions.

1. Register an account using the frontend UI.
2. Open MySQL and check your generated `user_id`: `SELECT id, name FROM users;`
3. Open `shared/seed_sessions.sql`. Replace all instances of `(1,` with your specific `user_id` if it differs.
4. Execute the SQL script in your database.

---
*Developed for a minor project showcasing Full-Stack Web Development, API Integration, and Applied Machine Learning.*

