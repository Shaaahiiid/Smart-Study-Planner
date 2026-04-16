"""
Predictor Module.
Handles predictions and best time recommendations.
"""

import numpy as np
from model_trainer import ModelTrainer
from feature_engineering import create_prediction_features, encode_subject
from config import DAY_NAMES, format_hour, SUBJECT_ENCODING


class FocusPredictor:
    """Handles focus predictions using trained ML model."""
    
    def __init__(self):
        """
        Initialize FocusPredictor.
        Loads saved model if available.
        """
        self.model = None
        self.algorithm = None
        self.metrics = None
        self._load_model()
    
    def _load_model(self):
        """Load the trained model from disk."""
        try:
            model_data = ModelTrainer.load_model()
            self.model = model_data['model']
            self.algorithm = model_data.get('algorithm', 'unknown')
            self.metrics = model_data.get('metrics', {})
            print("FocusPredictor ready with trained model.")
        except FileNotFoundError as e:
            print(f"Warning: {e}")
            print("Predictor initialized without model. Train a model first.")
            self.model = None
    
    def reload_model(self):
        """Reload the model from disk (after retraining)."""
        self._load_model()
    
    def is_ready(self):
        """Check if predictor has a loaded model."""
        return self.model is not None
    
    def predict_single(self, hour, day, subject, duration=None):
        """
        Predict focus rating for a single study session.
        
        Args:
            hour (int): Hour of day (0-23)
            day (int): Day of week (0-6, Monday=0)
            subject (str): Subject name
            duration (int, optional): Study duration in minutes
        
        Returns:
            dict: Prediction result with focus, confidence, etc.
        
        Raises:
            ValueError: If model is not loaded
        """
        if self.model is None:
            raise ValueError("No model loaded. Train a model first.")
        
        # Create features
        features = create_prediction_features(hour, day, subject)
        
        # Make prediction
        raw_prediction = self.model.predict(features)[0]
        
        # Clamp result to 1-10 range
        focus_prediction = max(1.0, min(10.0, raw_prediction))
        
        # Calculate confidence
        confidence = self._calculate_confidence(hour, day, subject)
        
        return {
            'predicted_focus': round(focus_prediction, 2),
            'confidence': confidence,
            'hour': hour,
            'day': day,
            'day_name': DAY_NAMES[day],
            'subject': subject,
            'time_display': format_hour(hour),
            'duration': duration
        }
    
    def predict_best_times(self, subject, top_n=5):
        """
        Find the best times to study a subject.
        
        Args:
            subject (str): Subject name
            top_n (int): Number of recommendations to return
        
        Returns:
            list: Top N best times with predictions
        
        Raises:
            ValueError: If model is not loaded
        """
        if self.model is None:
            raise ValueError("No model loaded. Train a model first.")
        
        predictions = []
        
        # Loop through reasonable study hours (6 AM to 11 PM) and all days
        for day in range(7):  # 0-6 (Monday to Sunday)
            for hour in range(6, 23):  # 6 AM to 10 PM
                prediction = self.predict_single(hour, day, subject)
                predictions.append(prediction)
        
        # Sort by predicted focus (highest first)
        predictions.sort(key=lambda x: x['predicted_focus'], reverse=True)
        
        # Return top N
        top_predictions = predictions[:top_n]
        
        # Add rank to each prediction
        for i, pred in enumerate(top_predictions):
            pred['rank'] = i + 1
            pred['recommendation'] = f"{pred['day_name']} at {pred['time_display']}"
        
        return top_predictions
    
    def _calculate_confidence(self, hour, day, subject):
        """
        Calculate prediction confidence based on input reasonableness.
        
        Args:
            hour (int): Hour of day
            day (int): Day of week
            subject (str): Subject name
        
        Returns:
            str: "high", "medium", or "low"
        """
        confidence_score = 0
        
        # Check if reasonable study hours (8 AM - 8 PM)
        if 8 <= hour <= 20:
            confidence_score += 2
        elif 6 <= hour < 8 or 20 < hour <= 22:
            confidence_score += 1
        # Early morning (before 6) or late night (after 10 PM) = 0
        
        # Known subject gives higher confidence
        if subject in SUBJECT_ENCODING and subject != 'Other':
            confidence_score += 1
        
        # Map score to confidence level
        if confidence_score >= 3:
            return "high"
        elif confidence_score >= 1:
            return "medium"
        else:
            return "low"
    
    def get_model_info(self):
        """Get information about the loaded model."""
        return {
            'model_loaded': self.model is not None,
            'algorithm': self.algorithm,
            'metrics': self.metrics,
            'available_subjects': list(SUBJECT_ENCODING.keys())
        }
