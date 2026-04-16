"""
Configuration settings for the ML Service.
Central place for all settings - no hardcoded values in code.
"""

import os
import json

# Base directory
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(BASE_DIR)

# Model settings
MODEL_DIR = os.path.join(BASE_DIR, 'models')
MODEL_FILE_PATH = os.path.join(MODEL_DIR, 'focus_model.pkl')

# Training settings
MIN_SESSIONS_FOR_TRAINING = 30
TRAIN_TEST_SPLIT_RATIO = 0.8  # 80% train, 20% test
RANDOM_STATE = 42

SUBJECTS_FILE_PATH = os.path.join(PROJECT_ROOT, 'shared', 'subjects.json')


def load_subjects():
    """Load canonical subjects from shared/subjects.json."""
    with open(SUBJECTS_FILE_PATH, 'r', encoding='utf-8') as f:
        subjects = json.load(f)

    if not isinstance(subjects, list) or not subjects:
        raise ValueError('shared/subjects.json must contain a non-empty JSON array')

    normalized = [s.strip() for s in subjects if isinstance(s, str) and s.strip()]
    if len(normalized) != len(subjects):
        raise ValueError('shared/subjects.json must contain only non-empty strings')

    return normalized


SUBJECTS = load_subjects()

# Subject encoding dictionary
# Maps subject names to numeric values for ML model
SUBJECT_ENCODING = {name: index for index, name in enumerate(SUBJECTS)}

# Reverse mapping for decoding
SUBJECT_DECODING = {v: k for k, v in SUBJECT_ENCODING.items()}

# Flask settings
FLASK_HOST = '0.0.0.0'
FLASK_PORT = 5001  # Changed from 5000 (used by AirPlay on macOS)
FLASK_DEBUG = True

# Day names for human-readable output
DAY_NAMES = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']

# Hour display format
def format_hour(hour):
    """Convert 24-hour format to 12-hour format with AM/PM."""
    if hour == 0:
        return "12:00 AM"
    elif hour < 12:
        return f"{hour}:00 AM"
    elif hour == 12:
        return "12:00 PM"
    else:
        return f"{hour - 12}:00 PM"
