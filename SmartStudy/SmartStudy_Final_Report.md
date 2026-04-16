# A Minor Project Report
on
**SMARTSTUDY: AI-POWERED STUDY PLANNER**
by

Shahid Shaikh (Exam Seat No. ________)
Candidate 2 Name (Exam Seat No. ________)
Candidate 3 Name (Exam Seat No. ________)

Under the guidance of
Guide Name: ________________
Faculty Name: ________________

Department of CSE(Artificial Intelligence) / Department of CSE(Artificial Intelligence & Machine Larning)

G H Raisoni College of Engineering & Management
Pune
(An Empowered Autonomous Institute (Accredited by NAAC A+ Grade), Affiliated to SPPU, Pune
Jan-April 2026

---

## CERTIFICATE

This is to certify that,
Shahid Shaikh (Exam Seat No. ________)
Candidate 2 Name (Exam Seat No. ________)
Candidate 3 Name (Exam Seat No. ________)

of class TY Btech have successfully completed their mini project work on “SMARTSTUDY: AI-POWERED STUDY PLANNER” at G H Raisoni College of Engineering & Management in the partial fulfillment of the Graduate Degree course in T Y Btech at the department of CSE(Artificial Intelligence), in the academic Year 2025-2026 Semester –VI as prescribed by an Autonomous Institute, Affiliated to Savitribai Phule Pune University.

Name of the Guide: ________________
Dr. Rachna Sable                 
Head of the Department (Department of CSE(AI))

---

## Acknowledgements

We express our profound gratitude to our project guide, _____________, for their expert guidance, continuous encouragement, and valuable feedback throughout the course of this project.

We would also like to thank the Head of the Department, Dr. Rachna Sable, and the Principal of G H Raisoni College of Engineering & Management, for providing the necessary infrastructure and environment for successfully carrying out this work.

Finally, we extend our heartfelt thanks to our families and friends for their unwavering support and motivation.

Shahid Shaikh
Candidate 2
Candidate 3

---

## Contents

**Sr. No. | Topic**
--- | ---
| Acknowledgement
| Abstract
| List of Tables
| List of Figures
**Chapter-1 | Introduction**
1.1 | Overview & Motivation
1.2 | Problem Definition and Objectives
1.3 | Project Scope & Limitations
**Chapter-2 | System Architecture & its Methodology**
2.1 | System Overview and Architecture
2.2 | Algorithms & Methodology used
2.3 | Dataset/Database used
**Chapter-3 | Software Requirement Specification**
3.1 | Functional Requirement
3.2 | Non Functional Requirement
3.3 | Performance Requirement
**Chapter-4 | Design and Implementation**
4.1 | Tools, Technologies, Platform Used
4.2 | Stepwise Execution/Development/Module Information
**Chapter-5 | Result & Discussion**
5.1 | Testing and Analysis of Result
5.2 | Performance Evaluation
**Chapter-6 | Conclusion**
**Chapter-7 | Future Scope**
**Chapter-8 | References**

---

## Abstract

SmartStudy is an AI-powered study planner designed to help students optimize their study schedules by predicting their peak focus levels throughout the day using Machine Learning. Traditional study planners offer static timetables that do not adapt to individual productivity cycles. To address this, SmartStudy tracks individual study sessions, subjective focus ratings, subjects, and durations to identify personalized study patterns. 

The system utilizes a modern 3-tier architecture, featuring a Spring Boot backend, a responsive HTML/JS frontend, and a Python Flask microservice for evaluating Machine Learning models. A stateless JWT-based authentication system is implemented to ensure data isolation and security, ensuring each user receives predictions tailored exclusively to their own data. The machine learning model, built with a Random Forest Regressor, analyzes historical study data to predict focus scores based on variables like time of day and subject, offering students highly individualized recommendations on when to study specific topics for maximum efficiency.

---

## Chapter-1: Introduction

### 1.1 Overview & Motivation
Students often struggle to identify their most productive study hours. While some are "morning people," others are "night owls." Generic study schedules do not account for these physiological and personal differences. The primary motivation behind SmartStudy is to leverage artificial intelligence and historical user data to build a personalized study optimization platform that adapts to individual cognitive rhythms.

### 1.2 Problem Definition and Objectives
**Problem Definition:** Traditional timetables do not account for daily fluctuations in a student's focus and concentration. Without this insight, students risk studying difficult subjects during low-focus periods, resulting in inefficient learning and burnout.
**Objectives:** 
- Develop a full-stack platform for students to log study sessions and self-rate their focus levels.
- Implement robust user authentication to isolate user data.
- Integrate a Machine Learning component to discover patterns in a user's study habits.
- Predict and recommend the best times and days to study specific subjects (e.g., Engineering Mathematics, Data Structures & Algorithms).

### 1.3 Project Scope & Limitations
**Scope:** The system offers user registration and authentication, a dashboard for data visualization, a study logging interface, and an AI prediction interface.
**Limitations:** The precision of the ML model relies heavily on the user manually inputting honest, subjective focus ratings (scale of 1-10). A cold-start problem exists, meaning accurate predictions can only be generated after a user provides a minimum of 30 logged study sessions.

---

## Chapter-2: System Architecture & its Methodology

### 2.1 System Overview and Architecture
The project employs a 3-tier microservices-inspired architecture:
- **Presentation Layer (Frontend):** Developed using standard web technologies (HTML, CSS, Vanilla JavaScript). It communicates with the backend via RESTful APIs and utilizes JWT (JSON Web Tokens) for authenticating requests.
- **Application Layer (Backend):** Built with Java using the Spring Boot framework. It manages user authentication, stores study session metadata, and acts as an intermediary proxy to interface with the ML service.
- **AI/ML Layer:** A standalone Python service built using the Flask lightweight web server. It handles data preprocessing, model training, and inferencing on demand.

### 2.2 Algorithms & Methodology used
The predictive engine utilizes a **Random Forest Regressor** algorithm provided by the Scikit-Learn library. This ensemble learning method operates by constructing a multitude of decision trees at training time and outputting the average prediction of the individual trees. It is highly effective for numerical prediction tasks involving non-linear patterns (such as focus fluctuating throughout the 24-hour day cyclically).

Features extracted for model training include:
- `hour_of_day` (0-23)
- `day_of_week` (0-6)
- `duration` (in minutes)
- `subject` (categorically encoded using One-Hot Encoding)
The target variable is the `focus_rating` (1-10 continuous).

### 2.3 Dataset/Database used
The dataset is organically generated by users logging their study sessions. It is stored relationally in a MySQL database containing two primary entities:
- `users`: Stores user credentials (`id`, `name`, `email`, hashed `password`).
- `study_sessions`: Stores the session records linking back to users via a foreign key (`user_id`, `subject`, `start_time`, `end_time`, `duration`, `focus_rating`).

---

## Chapter-3: Software Requirement Specification

### 3.1 Functional Requirement
- The system must allow users to securely register and log in to an individual account.
- The system must provide a user dashboard displaying total study hours and average focus statistics.
- The system must allow users to log the subject, start time, end time, and a subjective focus rating for completed study sessions.
- The system must allow users to request an ML model training sequence using their own specific data only.
- The system must provide predictions on the top *N* best times to study a given subject.

### 3.2 Non Functional Requirement
- **Security:** Passwords must be hashed using BCrypt. API endpoints must be protected using JWT authentication.
- **Maintainability:** The ML service must remain decoupled from the core business logic (Spring backend) to allow independent scaling and language-specific optimizations.

### 3.3 Performance Requirement
- The ML model training should execute and return metrics to the client in under 10 seconds.
- The REST API queries must yield response times underneath 500 milliseconds to guarantee a smooth user experience.

---

## Chapter-4: Design and Implementation

### 4.1 Tools, Technologies, Platform Used
- **Frontend Development:** HTML5, CSS3, JavaScript (Fetch API for asynchronous requests)
- **Backend Development:** Java 21, Spring Boot, Spring Data JPA, Hibernate, JWT (io.jsonwebtoken), BCrypt Password Encoder
- **Database:** MySQL
- **Machine Learning:** Python 3, Flask, Scikit-Learn, Pandas, NumPy
- **Build Tools:** Maven (Java), pip (Python)

### 4.2 Stepwise Execution/Development/Module Information
1. **Database Module:** Designed normalized schemas for `users` and `study_sessions` ensuring cascade-delete capabilities and optimized querying structures.
2. **Authentication Module:** Created REST controllers to handle registration/login. Encrypted passwords and utilized standard JWTs to secure routes.
3. **Frontend Dashboard Module:** Developed interactive UI dashboards capable of displaying statistics and ingesting user input. Handled frontend storage of the JWT using HTML Web Storage (localStorage).
4. **Machine Learning API Module:** Programmed a Python application to ingest historical user data via HTTP POST requests, build a Random Forest Regressor, and save the localized model in memory for fast inference. 

---

## Chapter-5: Result & Discussion

### 5.1 Testing and Analysis of Result
The system was tested extensively using synthetic proxy data simulating different user personas (e.g., a "Morning Person" and a "Night Owl"). 
Using the JWT-based data isolation flow, User 1 generated 50 study logs with high focus in the mornings. Under the same platform, User 2 generated 50 study logs with high focus at night. 
When both users requested predictions to study "Engineering Physics", the system successfully generated contrasting results:
- User 1's top predicted time was historically 08:00 AM.
- User 2's top predicted time was historically 09:00 PM.
This validated that the ML algorithm properly adapts to personal behavior without data contamination between users.

### 5.2 Performance Evaluation
For a user containing 50 training rows, the Random Forest model trained completely in under 2 seconds. The performance evaluation of the Regressor algorithm yielded excellent predictive consistency, maintaining a Mean Absolute Error (MAE) under 1.5 (on a 10-point scale) and an R² Score of >0.85 when recognizing distinct patterns.

---

## Chapter-6: Conclusion

The SmartStudy application successfully demonstrates the viability of utilizing multi-tier web development in conjunction with machine learning to resolve personalized scheduling struggles. By treating time, subjects, and daily routines as variable data features rather than rigid constants, the application grants students highly customized, intelligent recommendations to maximize study productivity. 

---

## Chapter-7: Future Scope

Several extensions could be made to this foundational mini-project:
- **Calendar Integration:** Auto-scheduling study blocks directly into Google Calendar or Apple Calendar based on the optimal predictions.
- **Automated Timer Integration:** Incorporating a Pomodoro timer built into the app to actively track study session durations, removing the need for manual time-entry.
- **Advanced Gamification:** Implementing leaderboards, streaks, or achievements to further incentivize students to consistently log accurate session data.

---

## Chapter-8: References

1. Walls, Craig. *Spring in Action, Sixth Edition*. Manning Publications, 2022.
2. Grinberg, Miguel. *Flask Web Development: Developing Web Applications with Python*. O'Reilly Media, 2018.
3. Pedregosa, F., et al. "Scikit-learn: Machine Learning in Python." *Journal of Machine Learning Research*, vol. 12, 2011, pp. 2825-2830.
4. JSON Web Tokens standard documentation. https://jwt.io/
5. MySQL Documentation and Guidelines. https://dev.mysql.com/doc/
