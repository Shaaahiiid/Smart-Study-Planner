"""
Import sample sessions to Java backend.
Run once to populate database with test data.
"""

import json
import requests

# Configuration
JAVA_API_URL = "http://localhost:8080/api/sessions"
SAMPLE_DATA_FILE = "data/sample_sessions.json"


def import_sessions():
    """Import all sessions from sample_sessions.json to Java backend."""

    # Load sample data
    print(f"Loading sessions from {SAMPLE_DATA_FILE}...")
    with open(SAMPLE_DATA_FILE, 'r') as f:
        sessions = json.load(f)

    print(f"Found {len(sessions)} sessions to import.")

    # Import each session
    success_count = 0
    fail_count = 0

    for i, session in enumerate(sessions, 1):
        try:
            # Send POST request to Java backend
            response = requests.post(JAVA_API_URL, json=session)

            if response.status_code in [200, 201]:
                success_count += 1
                print(f"✓ [{i}/{len(sessions)}] Imported: {session['subject']} - {session['startTime']}")
            else:
                fail_count += 1
                print(f"✗ [{i}/{len(sessions)}] Failed: {response.status_code} - {response.text}")

        except Exception as e:
            fail_count += 1
            print(f"✗ [{i}/{len(sessions)}] Error: {e}")

    print("\n" + "=" * 50)
    print(f"Import Complete!")
    print(f"  Success: {success_count}")
    print(f"  Failed: {fail_count}")
    print(f"  Total: {len(sessions)}")
    print("=" * 50)


if __name__ == "__main__":
    print("=" * 50)
    print("Sample Sessions Importer")
    print("=" * 50)

    # Check if Java backend is running
    try:
        response = requests.get("http://localhost:8080/api/sessions")
        print("✓ Java backend is running")
    except:
        print("✗ Java backend is not running!")
        print("  Start Java backend in IntelliJ first.")
        exit(1)

    # Import sessions
    import_sessions()