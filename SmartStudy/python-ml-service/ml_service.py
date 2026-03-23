"""
ML Service - Flask Application.
REST API for focus prediction and model training.
"""

from flask import Flask, request, jsonify
from flask_cors import CORS

from predictor import FocusPredictor
from model_trainer import ModelTrainer
from config import (
    FLASK_HOST, 
    FLASK_PORT, 
    FLASK_DEBUG,
    MIN_SESSIONS_FOR_TRAINING,
    SUBJECT_ENCODING,
    DAY_NAMES,
    format_hour
)


# Create Flask app
app = Flask(__name__)

# Enable CORS (allows Java backend to call Python service)
CORS(app)

# Initialize predictor (will load model if available)
predictor = FocusPredictor()


# =============================================================================
# Health Check Endpoint
# =============================================================================

@app.route('/', methods=['GET'])
def health_check():
    """
    Health check endpoint.
    Returns service status and basic info.
    """
    return jsonify({
        'status': 'healthy',
        'service': 'SmartStudy ML Service',
        'version': '1.0.0',
        'model_loaded': predictor.is_ready()
    })


@app.route('/health', methods=['GET'])
def health():
    """Alternative health check endpoint."""
    return health_check()


# =============================================================================
# Prediction Endpoints
# =============================================================================

@app.route('/predict', methods=['POST'])
def predict():
    """
    Predict focus rating for a study session.
    
    Request JSON:
        {
            "hour": 10,
            "day": 1,
            "subject": "Mathematics",
            "duration": 60  (optional)
        }
    
    Response JSON:
        {
            "success": true,
            "prediction": {
                "predicted_focus": 7.5,
                "confidence": "high",
                "hour": 10,
                "day": 1,
                "day_name": "Tuesday",
                "subject": "Mathematics",
                "time_display": "10:00 AM"
            }
        }
    """
    try:
        # Check if model is loaded
        if not predictor.is_ready():
            return jsonify({
                'success': False,
                'error': 'Model not trained yet. Please train the model first using /train endpoint.'
            }), 400
        
        # Get JSON data
        data = request.get_json()
        
        if not data:
            return jsonify({
                'success': False,
                'error': 'No JSON data provided'
            }), 400
        
        # Extract parameters
        hour = data.get('hour')
        day = data.get('day')
        subject = data.get('subject')
        duration = data.get('duration')
        
        # Validate required fields
        if hour is None or day is None or subject is None:
            return jsonify({
                'success': False,
                'error': 'Missing required fields: hour, day, subject'
            }), 400
        
        # Validate hour (0-23)
        if not isinstance(hour, int) or hour < 0 or hour > 23:
            return jsonify({
                'success': False,
                'error': 'Hour must be an integer between 0 and 23'
            }), 400
        
        # Validate day (0-6)
        if not isinstance(day, int) or day < 0 or day > 6:
            return jsonify({
                'success': False,
                'error': 'Day must be an integer between 0 (Monday) and 6 (Sunday)'
            }), 400
        
        # Make prediction
        prediction = predictor.predict_single(hour, day, subject, duration)
        
        return jsonify({
            'success': True,
            'prediction': prediction
        })
    
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


@app.route('/predict/best-times', methods=['POST'])
def predict_best_times():
    """
    Get best times to study a subject.
    
    Request JSON:
        {
            "subject": "Mathematics",
            "top_n": 5  (optional, default 5)
        }
    
    Response JSON:
        {
            "success": true,
            "subject": "Mathematics",
            "best_times": [
                {
                    "rank": 1,
                    "predicted_focus": 8.5,
                    "day_name": "Tuesday",
                    "time_display": "10:00 AM",
                    "recommendation": "Tuesday at 10:00 AM"
                },
                ...
            ]
        }
    """
    try:
        # Check if model is loaded
        if not predictor.is_ready():
            return jsonify({
                'success': False,
                'error': 'Model not trained yet. Please train the model first using /train endpoint.'
            }), 400
        
        # Get JSON data
        data = request.get_json()
        
        if not data:
            return jsonify({
                'success': False,
                'error': 'No JSON data provided'
            }), 400
        
        # Extract parameters
        subject = data.get('subject')
        top_n = data.get('top_n', 5)
        
        # Validate subject
        if not subject:
            return jsonify({
                'success': False,
                'error': 'Missing required field: subject'
            }), 400
        
        # Validate top_n
        if not isinstance(top_n, int) or top_n < 1 or top_n > 20:
            top_n = 5
        
        # Get best times
        best_times = predictor.predict_best_times(subject, top_n)
        
        return jsonify({
            'success': True,
            'subject': subject,
            'best_times': best_times
        })
    
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


# =============================================================================
# Training Endpoint
# =============================================================================

@app.route('/train', methods=['POST'])
def train():
    """
    Train the ML model with study sessions.
    
    Request JSON:
        {
            "sessions": [
                {
                    "subject": "Mathematics",
                    "startTime": "2024-02-26T10:00:00",
                    "endTime": "2024-02-26T11:00:00",
                    "focusRating": 8,
                    "duration": 60
                },
                ...
            ],
            "algorithm": "random_forest"  (optional, default "random_forest")
        }
    
    Response JSON:
        {
            "success": true,
            "message": "Model trained successfully",
            "metrics": {
                "algorithm": "random_forest",
                "total_sessions": 50,
                "mae": 0.85,
                "r2_score": 0.72,
                "within_1_accuracy": 78.5
            }
        }
    """
    try:
        # Get JSON data
        data = request.get_json()
        
        if not data:
            return jsonify({
                'success': False,
                'error': 'No JSON data provided'
            }), 400
        
        # Extract sessions
        sessions = data.get('sessions', [])
        algorithm = data.get('algorithm', 'random_forest')
        
        # Check minimum data requirement
        if len(sessions) < MIN_SESSIONS_FOR_TRAINING:
            return jsonify({
                'success': False,
                'error': f'Insufficient data: {len(sessions)} sessions provided, '
                        f'minimum {MIN_SESSIONS_FOR_TRAINING} required.'
            }), 400
        
        # Create trainer and train model
        trainer = ModelTrainer(algorithm=algorithm)
        metrics = trainer.train(sessions)
        
        # Save model
        trainer.save_model()
        
        # Reload predictor with new model
        predictor.reload_model()
        
        return jsonify({
            'success': True,
            'message': 'Model trained successfully',
            'metrics': metrics
        })
    
    except ValueError as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 400
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


# =============================================================================
# Model Info Endpoint
# =============================================================================

@app.route('/model/info', methods=['GET'])
def model_info():
    """
    Get model metadata and status.
    
    Response JSON:
        {
            "model_exists": true,
            "model_loaded": true,
            "algorithm": "random_forest",
            "metrics": {...},
            "min_sessions_required": 30,
            "available_subjects": ["Mathematics", "Physics", ...]
        }
    """
    info = predictor.get_model_info()
    info['model_exists'] = ModelTrainer.model_exists()
    info['min_sessions_required'] = MIN_SESSIONS_FOR_TRAINING
    
    return jsonify(info)


# =============================================================================
# Utility Endpoints
# =============================================================================

@app.route('/subjects', methods=['GET'])
def get_subjects():
    """Get list of available subjects."""
    return jsonify({
        'subjects': list(SUBJECT_ENCODING.keys())
    })


@app.route('/days', methods=['GET'])
def get_days():
    """Get day mappings."""
    return jsonify({
        'days': {i: name for i, name in enumerate(DAY_NAMES)}
    })


# =============================================================================
# Main Entry Point
# =============================================================================

if __name__ == '__main__':
    print("=" * 50)
    print("Starting SmartStudy ML Service")
    print("=" * 50)
    print(f"Host: {FLASK_HOST}")
    print(f"Port: {FLASK_PORT}")
    print(f"Debug: {FLASK_DEBUG}")
    print(f"Model loaded: {predictor.is_ready()}")
    print("=" * 50)
    
    app.run(
        host=FLASK_HOST,
        port=FLASK_PORT,
        debug=FLASK_DEBUG
    )
