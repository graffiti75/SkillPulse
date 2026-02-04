#!/usr/bin/env python3
"""
Firebase Task Migration Script
Updates all existing tasks in Firestore by adding userId field

Usage:
    python update_tasks_userid.py
"""

import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
import sys
from datetime import datetime

# ============================================================================
# CONFIGURATION - MODIFY THESE BEFORE RUNNING
# ============================================================================

# Path to your Firebase service account key JSON file
# Download from: Firebase Console > Project Settings > Service Accounts > Generate New Private Key
SERVICE_ACCOUNT_PATH = "../serviceAccountKey.json"

# Firestore collection name containing tasks
COLLECTION_NAME = "tasks"

# Metadata collection (optional - for tracking migration status)
METADATA_COLLECTION = "migrations"

# Metadata document name (optional - for tracking this migration)
METADATA_DOC = "userid_migration"

# The userId to assign to all tasks
TARGET_USER_ID = "a@a.com"

# ============================================================================
# MAIN SCRIPT
# ============================================================================

def initialize_firebase():
    """Initialize Firebase Admin SDK"""
    try:
        # Check if Firebase is already initialized
        firebase_admin.get_app()
    except ValueError:
        # Firebase not initialized yet
        try:
            cred = credentials.Certificate(SERVICE_ACCOUNT_PATH)
            firebase_admin.initialize_app(cred)
            print(f"✅ Firebase initialized with: {SERVICE_ACCOUNT_PATH}")
        except FileNotFoundError:
            print(f"❌ Error: Firebase credentials file not found at: {SERVICE_ACCOUNT_PATH}")
            print("\nHow to fix:")
            print("1. Go to Firebase Console > Project Settings > Service Accounts")
            print("2. Click 'Generate New Private Key'")
            print("3. Save the JSON file and update SERVICE_ACCOUNT_PATH in this script")
            sys.exit(1)
        except Exception as e:
            print(f"❌ Error initializing Firebase: {e}")
            sys.exit(1)


def count_tasks():
    """Count total number of tasks in Firestore"""
    try:
        db = firestore.client()
        docs = db.collection(COLLECTION_NAME).stream()
        count = 0
        for _ in docs:
            count += 1
        return count
    except Exception as e:
        print(f"❌ Error counting tasks: {e}")
        return 0


def get_tasks_without_userid():
    """Get all tasks that don't have userId field"""
    try:
        db = firestore.client()
        docs = db.collection(COLLECTION_NAME).stream()
        
        tasks_without_userid = []
        for doc in docs:
            data = doc.to_dict()
            if "userId" not in data:
                tasks_without_userid.append({
                    "doc_id": doc.id,
                    "data": data
                })
        
        return tasks_without_userid
    except Exception as e:
        print(f"❌ Error fetching tasks: {e}")
        return []


def update_task_with_userid(doc_id, user_id):
    """Update a single task with userId field"""
    try:
        db = firestore.client()
        db.collection(COLLECTION_NAME).document(doc_id).update({
            "userId": user_id
        })
        return True
    except Exception as e:
        print(f"❌ Error updating task {doc_id}: {e}")
        return False


def update_all_tasks(user_id):
    """Update all tasks without userId"""
    print(f"\n🔍 Searching for tasks without userId field...")
    
    tasks_to_update = get_tasks_without_userid()
    
    if not tasks_to_update:
        print("✅ No tasks found without userId field. All tasks are already updated!")
        return True
    
    total = len(tasks_to_update)
    print(f"📊 Found {total} task(s) to update")
    
    # Show preview
    print(f"\n📋 Preview of tasks to be updated:")
    for i, task in enumerate(tasks_to_update[:3], 1):
        print(f"\n  Task {i}:")
        print(f"    Document ID: {task['doc_id']}")
        print(f"    Description: {task['data'].get('description', 'N/A')}")
        print(f"    Task ID: {task['data'].get('id', 'N/A')}")
    
    if total > 3:
        print(f"\n  ... and {total - 3} more task(s)")
    
    # Confirmation
    print(f"\n⚠️  About to update {total} task(s) with userId = '{user_id}'")
    response = input("\nContinue? (yes/no): ").strip().lower()
    
    if response not in ["yes", "y"]:
        print("❌ Operation cancelled by user")
        return False
    
    # Update tasks
    print(f"\n🔄 Updating tasks...")
    successful = 0
    failed = 0
    
    for i, task in enumerate(tasks_to_update, 1):
        doc_id = task["doc_id"]
        if update_task_with_userid(doc_id, user_id):
            successful += 1
            status = "✅"
        else:
            failed += 1
            status = "❌"
        
        # Progress indicator
        if i % 10 == 0 or i == total:
            print(f"  {status} Progress: {i}/{total}")
    
    # Summary
    print(f"\n{'='*50}")
    print(f"✅ Successfully updated: {successful}")
    print(f"❌ Failed: {failed}")
    print(f"📊 Total: {total}")
    print(f"{'='*50}")
    
    return failed == 0


def verify_updates(user_id):
    """Verify that all tasks were updated with userId"""
    print(f"\n✔️  Verifying updates...")
    
    db = firestore.client()
    docs = db.collection(COLLECTION_NAME).stream()
    
    total = 0
    with_userid = 0
    without_userid = 0
    
    for doc in docs:
        data = doc.to_dict()
        total += 1
        
        if "userId" in data:
            with_userid += 1
            if data["userId"] != user_id:
                print(f"⚠️  Task {doc.id} has userId: {data['userId']} (expected: {user_id})")
        else:
            without_userid += 1
            print(f"❌ Task {doc.id} still missing userId field")
    
    print(f"\n📊 Verification Results:")
    print(f"  Total tasks: {total}")
    print(f"  With userId: {with_userid}")
    print(f"  Without userId: {without_userid}")
    
    if without_userid == 0:
        print(f"\n✅ All tasks successfully updated!")
        return True
    else:
        print(f"\n⚠️  Some tasks still need updating")
        return False


def main():
    """Main execution"""
    print("="*60)
    print("Firebase Task Migration - Add userId Field")
    print("="*60)
    print(f"\n📝 Configuration:")
    print(f"  Service Account: {SERVICE_ACCOUNT_PATH}")
    print(f"  Collection: {COLLECTION_NAME}")
    print(f"  Metadata Collection: {METADATA_COLLECTION}")
    print(f"  Metadata Doc: {METADATA_DOC}")
    print(f"  Target userId: {TARGET_USER_ID}")
    
    # Initialize Firebase
    print(f"\n🔐 Initializing Firebase...")
    initialize_firebase()
    
    # Count tasks
    print(f"\n📊 Counting total tasks...")
    total_tasks = count_tasks()
    print(f"  Total tasks in database: {total_tasks}")
    
    if total_tasks == 0:
        print("⚠️  No tasks found in database. Nothing to update.")
        return
    
    # Update tasks
    success = update_all_tasks(TARGET_USER_ID)
    
    if not success:
        print("\n⚠️  Update process completed with issues")
        return
    
    # Verify
    verify_updates(TARGET_USER_ID)
    
    print(f"\n{'='*60}")
    print(f"✅ Migration completed successfully!")
    print(f"{'='*60}\n")


if __name__ == "__main__":
    main()