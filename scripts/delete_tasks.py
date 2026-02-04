"""
Delete Tasks from Firestore
===========================

This script deletes all tasks from the Firestore 'tasks' collection.
Use with caution - this action is irreversible.

Requirements:
    pip install firebase-admin

Setup:
    1. Place serviceAccountKey.json in the parent directory (or update SERVICE_ACCOUNT_PATH)

Usage:
    python delete_tasks.py
"""

import firebase_admin
from firebase_admin import credentials, firestore

SERVICE_ACCOUNT_PATH = "../serviceAccountKey.json"
COLLECTION_NAME = "tasks"


def delete_all_tasks():
    """Delete all documents from the tasks collection in Firestore."""
    cred = credentials.Certificate(SERVICE_ACCOUNT_PATH)
    firebase_admin.initialize_app(cred)
    db = firestore.client()
    
    collection_ref = db.collection(COLLECTION_NAME)
    docs = collection_ref.stream()
    
    deleted_count = 0
    for doc in docs:
        doc.reference.delete()
        print(f"✗ Deleted: {doc.id}")
        deleted_count += 1
    
    if deleted_count > 0:
        print(f"\n✅ Successfully deleted {deleted_count} tasks from Firestore!")
    else:
        print("\n⚠️  No tasks found to delete.")


if __name__ == "__main__":
    delete_all_tasks()