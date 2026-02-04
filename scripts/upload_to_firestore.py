"""
Upload Tasks to Firestore
=========================

This script uploads tasks from a CSV file to Firebase Firestore.
It includes a hash check to prevent duplicate uploads and save on Firebase costs.

Requirements:
    pip install firebase-admin

Setup:
    1. Go to Firebase Console > Project Settings > Service Accounts
    2. Click "Generate new private key" and save as serviceAccountKey.json
    3. Place serviceAccountKey.json in the parent directory (or update SERVICE_ACCOUNT_PATH)
    4. Place your tasks.txt file in the parent directory (or update TXT_FILE_PATH)

Usage:
    python upload_to_firestore.py

File format (tasks.txt):
    Each line should be: ID;DESCRIPTION;TIMESTAMP
    Example: 20251101_1;Acordar;2025-11-01T06:45:00-03:00

Firestore structure:
    Collection: tasks
        Document ID: task ID (e.g., 20251101_1)
        Fields: id, description, startTime, endTime, timestamp
    
    Collection: _metadata
        Document: tasks_upload
        Fields: last_file_hash, last_upload, task_count
"""

import firebase_admin
from firebase_admin import credentials, firestore
from datetime import datetime, timezone
import hashlib

# === CONFIGURATION ===
TXT_FILE_PATH = "../tasks.txt"
SERVICE_ACCOUNT_PATH = "../serviceAccountKey.json"
COLLECTION_NAME = "tasks"
METADATA_COLLECTION = "_metadata"
METADATA_DOC = "tasks_upload"

def calculate_file_hash(file_path: str) -> str:
    """Calculate SHA256 hash of a file to detect changes."""
    sha256_hash = hashlib.sha256()
    with open(file_path, 'rb') as f:
        for byte_block in iter(lambda: f.read(4096), b""):
            sha256_hash.update(byte_block)
    return sha256_hash.hexdigest()


def timestamp_to_readable(timestamp_str: str) -> str:
    """Convert ISO timestamp to human-readable format for display."""
    if 'T' in timestamp_str:
        dt_part = timestamp_str.split('T')[0]
        time_part = timestamp_str.split('T')[1].split('-')[0].split('+')[0][:5]
        return f"{dt_part} {time_part}"
    return timestamp_str

def pad_task_id(task_id: str) -> str:
    """
    Convert task ID from YYYYMMDD_NN format to YYYYMMDDNNN format (no underscore, 3-digit number).
    
    This allows proper string sorting in Firestore. Numbers are padded to 3 digits 
    to support up to 999 tasks per day.
    
    Examples:
        "20251226_1" → "202512260001"
        "20251226_9" → "202512260009"
        "20251226_79" → "202512260079"
        "20251226_150" → "202512260150"
        "20251226_999" → "202512260999"
    
    Args:
        task_id (str): Task ID in format "YYYYMMDD_NN"
    
    Returns:
        str: Task ID in format "YYYYMMDDNNN"
    """
    if "_" in task_id:
        date_part, num_part = task_id.split("_")
        padded_num = num_part.zfill(3)  # ← 3 digits (CHANGED)
        return f"{date_part}{padded_num}"  # ← no underscore (CHANGED)
    return task_id

def parse_txt_file(file_path: str) -> list[dict]:
    """
    Parse the tasks.txt CSV file into a list of task dictionaries.
    Each task has: id, description, startTime, endTime, timestamp.
    The endTime is calculated from the next task's startTime.
    """
    tasks = []
    
    with open(file_path, "r", encoding="utf-8") as f:
        lines = [line.strip() for line in f if line.strip()]
    
    for i, line in enumerate(lines):
        parts = line.split(";")
        task_id = pad_task_id(parts[0])
        description = parts[1]
        start_time = parts[2]
        
        if i + 1 < len(lines):
            next_parts = lines[i + 1].split(";")
            end_time = next_parts[2]
        else:
            end_time = start_time
        
        tasks.append({
            "id": task_id,
            "description": description,
            "startTime": start_time,
            "endTime": end_time,
            "timestamp": None
        })
    
    return tasks


def is_collection_empty(db) -> bool:
    """Check if the tasks collection is empty or doesn't exist."""
    collection_ref = db.collection(COLLECTION_NAME)
    docs = collection_ref.limit(1).stream()
    return len(list(docs)) == 0


def check_file_changed(db, file_hash: str) -> bool:
    """
    Check if file has changed since last upload by comparing SHA256 hashes.
    Also returns True if the collection is empty (data was deleted).
    Returns True if file changed, collection is empty, or first upload.
    Returns False if file is unchanged and data exists.
    """
    # First check if collection is empty
    if is_collection_empty(db):
        print("   Collection is empty. Will upload.")
        return True
    
    meta_ref = db.collection(METADATA_COLLECTION).document(METADATA_DOC)
    meta_doc = meta_ref.get()
    
    if meta_doc.exists:
        stored_hash = meta_doc.to_dict().get('last_file_hash')
        if stored_hash == file_hash:
            return False
    
    return True


def update_metadata(db, file_hash: str, task_count: int):
    """Store the current file hash in Firestore to detect future changes."""
    meta_ref = db.collection(METADATA_COLLECTION).document(METADATA_DOC)
    meta_ref.set({
        'last_file_hash': file_hash,
        'last_upload': datetime.now().isoformat(),
        'task_count': task_count
    })


def upload_to_firestore(tasks: list[dict], db):
    """Upload all tasks to Firestore using task ID as document ID."""
    collection_ref = db.collection(COLLECTION_NAME)
    
    for task in tasks:
        doc_ref = collection_ref.document(task["id"])
        
        task_data = {
            "id": task["id"],
            "description": task["description"],
            "startTime": task["startTime"],
            "endTime": task["endTime"],
            "timestamp": datetime.now().astimezone().replace(microsecond=0).isoformat()
        }
        
        doc_ref.set(task_data)
        print(f"✓ Uploaded: {task['id']} - {task['description']}")
    
    print(f"\n✅ Successfully uploaded {len(tasks)} tasks to Firestore!")


def main():
    """Main entry point. Checks for file changes before uploading to save Firebase costs."""
    print("🔐 Calculating file hash...")
    file_hash = calculate_file_hash(TXT_FILE_PATH)
    print(f"   Hash: {file_hash[:16]}...")
    
    cred = credentials.Certificate(SERVICE_ACCOUNT_PATH)
    firebase_admin.initialize_app(cred)
    db = firestore.client()
    
    print("\n🔍 Checking if file has changed since last upload...")
    if not check_file_changed(db, file_hash):
        print("⏭️  File unchanged since last upload. Skipping upload.")
        print("   (0 writes, 1 read)")
        return
    
    print("   File changed or first upload. Proceeding...")
    
    print("\n📖 Reading TXT file...")
    tasks = parse_txt_file(TXT_FILE_PATH)
    
    print(f"📝 Parsed {len(tasks)} tasks:")
    for task in tasks:
        start_readable = timestamp_to_readable(task["startTime"])
        end_readable = timestamp_to_readable(task["endTime"])
        print(f"   {task['id']}: {task['description']} ({start_readable} → {end_readable})")
    
    print("\n☁️  Uploading to Firestore...")
    upload_to_firestore(tasks, db)
    
    print("\n📋 Updating metadata...")
    update_metadata(db, file_hash, len(tasks))
    
    print(f"\n📊 Total operations: {len(tasks) + 1} writes, 1 read")


if __name__ == "__main__":
    main()