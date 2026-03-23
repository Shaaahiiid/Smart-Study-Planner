"""
Model Trainer Module.
Handles model training, evaluation, and persistence.
"""

import os
import joblib
import numpy as np
from sklearn.linear_model import LinearRegression
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error, r2_score

from config import (
    MODEL_FILE_PATH, 
    MODEL_DIR,
    MIN_SESSIONS_FOR_TRAINING, 
    TRAIN_TEST_SPLIT_RATIO,
    RANDOM_STATE
)
from feature_engineering import prepare_features


class ModelTrainer:
    """Handles training and saving ML models for focus prediction."""
    
    def __init__(self, algorithm='random_forest'):
        """
        Initialize ModelTrainer.
        
        Args:
            algorithm (str): Algorithm choice - 'linear_regression' or 'random_forest'
        """
        self.algorithm = algorithm
        self.model = None
        self.metrics = {}
    
    def _create_model(self):
        """
        Create model based on algorithm choice.
        
        Returns:
            sklearn model object
        """
        if self.algorithm == 'linear_regression':
            return LinearRegression()
        elif self.algorithm == 'random_forest':
            return RandomForestRegressor(
                n_estimators=100,
                max_depth=10,
                random_state=RANDOM_STATE
            )
        else:
            # Default to Random Forest
            return RandomForestRegressor(
                n_estimators=100,
                max_depth=10,
                random_state=RANDOM_STATE
            )
    
    def train(self, sessions):
        """
        Train the model on study sessions.
        
        Args:
            sessions (list): List of study session dictionaries
        
        Returns:
            dict: Training metrics (MAE, R², within-1 accuracy)
        
        Raises:
            ValueError: If not enough training data
        """
        # Check minimum data requirement
        if len(sessions) < MIN_SESSIONS_FOR_TRAINING:
            raise ValueError(
                f"Insufficient data: {len(sessions)} sessions provided, "
                f"minimum {MIN_SESSIONS_FOR_TRAINING} required."
            )
        
        # Prepare features
        X, y = prepare_features(sessions)
        
        # Split data 80-20
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, 
            test_size=(1 - TRAIN_TEST_SPLIT_RATIO),
            random_state=RANDOM_STATE
        )
        
        # Create and train model
        self.model = self._create_model()
        self.model.fit(X_train, y_train)
        
        # Evaluate on test set
        y_pred = self.model.predict(X_test)
        
        # Calculate metrics
        mae = mean_absolute_error(y_test, y_pred)
        r2 = r2_score(y_test, y_pred)
        
        # Calculate within-1 accuracy (predictions within 1 point of actual)
        within_1 = np.mean(np.abs(y_pred - y_test) <= 1.0) * 100
        
        self.metrics = {
            'algorithm': self.algorithm,
            'total_sessions': len(sessions),
            'training_samples': len(X_train),
            'test_samples': len(X_test),
            'mae': round(mae, 3),
            'r2_score': round(r2, 3),
            'within_1_accuracy': round(within_1, 1)
        }
        
        print(f"Training completed!")
        print(f"  Algorithm: {self.algorithm}")
        print(f"  MAE: {mae:.3f}")
        print(f"  R² Score: {r2:.3f}")
        print(f"  Within-1 Accuracy: {within_1:.1f}%")
        
        return self.metrics
    
    def save_model(self):
        """
        Save trained model to disk.
        
        Raises:
            ValueError: If no model has been trained
        """
        if self.model is None:
            raise ValueError("No model to save. Train a model first.")
        
        # Ensure models directory exists
        os.makedirs(MODEL_DIR, exist_ok=True)
        
        # Save model and metrics together
        model_data = {
            'model': self.model,
            'algorithm': self.algorithm,
            'metrics': self.metrics
        }
        
        joblib.dump(model_data, MODEL_FILE_PATH)
        print(f"Model saved to {MODEL_FILE_PATH}")
    
    @staticmethod
    def load_model():
        """
        Load model from disk.
        
        Returns:
            dict: Model data containing 'model', 'algorithm', 'metrics'
        
        Raises:
            FileNotFoundError: If model file doesn't exist
        """
        if not os.path.exists(MODEL_FILE_PATH):
            raise FileNotFoundError(
                f"Model file not found at {MODEL_FILE_PATH}. "
                "Train a model first using the /train endpoint."
            )
        
        model_data = joblib.load(MODEL_FILE_PATH)
        print(f"Model loaded from {MODEL_FILE_PATH}")
        return model_data
    
    @staticmethod
    def model_exists():
        """Check if a trained model exists."""
        return os.path.exists(MODEL_FILE_PATH)
