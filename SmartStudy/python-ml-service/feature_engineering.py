from datetime import datetime
import numpy as np
from config import SUBJECT_ENCODING


def encode_subject(subject_name):
    """
    Encode subject name to numeric value.
    
    Args:
        subject_name (str): Subject name string (e.g., "Mathematics")
    
    Returns:
        int: Encoded number (0, 1, 2, etc.)
    """
    if subject_name in SUBJECT_ENCODING:
        return SUBJECT_ENCODING[subject_name]

    if 'Other' in SUBJECT_ENCODING:
        return SUBJECT_ENCODING['Other']

    return 0


def extract_temporal_features(datetime_string):
    """
    Extract temporal features from datetime string.
    
    Args:
        datetime_string (str): Datetime string from Java (e.g., "2024-02-26T10:00:00")
    
    Returns:
        dict: Dictionary with hour (0-23), day (0-6), is_weekend (0/1)
    """
    # Parse the datetime string
    # Handle both formats: with and without milliseconds
    try:
        dt = datetime.fromisoformat(datetime_string.replace('Z', '+00:00'))
    except ValueError:
        # Try parsing without timezone
        dt = datetime.strptime(datetime_string[:19], "%Y-%m-%dT%H:%M:%S")
    
    hour = dt.hour
    day = dt.weekday()  # 0 = Monday, 6 = Sunday
    is_weekend = 1 if day >= 5 else 0  # Saturday (5) or Sunday (6)
    
    return {
        'hour': hour,
        'day': day,
        'is_weekend': is_weekend
    }


def prepare_features(sessions):
    """
    Prepare features from list of study sessions.
    
    Args:
        sessions (list): List of study session dictionaries from Java
            Each session should have: subject, startTime, focusRating
    
    Returns:
        tuple: (X, y) where X is feature array, y is target array
    """
    features = []
    targets = []
    
    for session in sessions:
        # Extract temporal features from start time
        temporal = extract_temporal_features(session.get('startTime', session.get('start_time', '')))
        
        # Encode subject
        subject_encoded = encode_subject(session.get('subject', 'Other'))
        
        # Build feature vector: [hour, day, is_weekend, subject_encoded]
        feature_vector = [
            temporal['hour'],
            temporal['day'],
            temporal['is_weekend'],
            subject_encoded
        ]
        features.append(feature_vector)
        
        # Get focus rating as target
        focus_rating = session.get('focusRating', session.get('focus_rating', 5))
        targets.append(focus_rating)
    
    return np.array(features), np.array(targets)


def create_prediction_features(hour, day, subject):
    """
    Create feature array for a single prediction.
    
    Args:
        hour (int): Hour of day (0-23)
        day (int): Day of week (0-6, Monday=0)
        subject (str): Subject name
    
    Returns:
        np.array: Feature array ready for model.predict()
    """
    # Encode subject
    subject_encoded = encode_subject(subject)
    
    # Calculate is_weekend
    is_weekend = 1 if day >= 5 else 0
    
    # Build feature vector: [hour, day, is_weekend, subject_encoded]
    feature_vector = np.array([[hour, day, is_weekend, subject_encoded]])
    
    return feature_vector


def get_feature_names():
    """Get names of features used in the model."""
    return ['hour', 'day', 'is_weekend', 'subject_encoded']
