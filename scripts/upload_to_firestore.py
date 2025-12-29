import firebase_admin
from firebase_admin import credentials, firestore
from datetime import datetime

# === CONFIGURATION ===
TXT_FILE_PATH = "../tasks.txt"
SERVICE_ACCOUNT_PATH = "../serviceAccountKey.json"  # Your service account key file
COLLECTION_NAME = "tasks"

def parse_date_from_id(task_id: str) -> str:
    """Extract date from ID like '20251102_1' -> '2025-11-02'"""
    date_part = task_id.split("_")[0]
    year = date_part[0:4]
    month = date_part[4:6]
    day = date_part[6:8]
    return f"{year}-{month}-{day}"

def datetime_to_timestamp(date_str: str, time_str: str) -> int:
    datetime_str = f"{date_str} {time_str}"
    dt = datetime.strptime(datetime_str, "%Y-%m-%d %H:%M")
    return int(dt.timestamp())

def timestamp_to_readable(timestamp: int) -> str:
    return datetime.fromtimestamp(timestamp).strftime("%Y-%m-%d %H:%M")

def parse_txt_file(file_path: str) -> list[dict]:
    """Read and parse the TXT file into a list of task dictionaries"""
    tasks = []
    
    with open(file_path, "r", encoding="utf-8") as f:
        lines = [line.strip() for line in f if line.strip()]
    
    for i, line in enumerate(lines):
        parts = line.split(";")
        task_id = parts[0]
        description = parts[1]
        time_str = parts[2]
        
        date_str = parse_date_from_id(task_id)
        start_time = datetime_to_timestamp(date_str, time_str)
        
        # Calculate endTime: use next row's time, or same as startTime if last row
        if i + 1 < len(lines):
            next_parts = lines[i + 1].split(";")
            next_time_str = next_parts[2]
            next_date_str = parse_date_from_id(next_parts[0])
            end_time = datetime_to_timestamp(next_date_str, next_time_str)
        else:
            end_time = start_time  # Last row: endTime = startTime
        
        tasks.append({
            "id": task_id,
            "description": description,
            "startTime": start_time,
            "endTime": end_time,
            "timestamp": None  # Will be set when uploading
        })
    
    return tasks

def upload_to_firestore(tasks: list[dict]):
    """Upload tasks to Firestore"""
    # Initialize Firebase
    cred = credentials.Certificate(SERVICE_ACCOUNT_PATH)
    firebase_admin.initialize_app(cred)
    db = firestore.client()
    
    collection_ref = db.collection(COLLECTION_NAME)
    
    for task in tasks:
        doc_ref = collection_ref.document(task["id"])
        
        # Set timestamp to current time (server timestamp)
        task_data = {
            "id": task["id"],
            "description": task["description"],
            "startTime": task["startTime"],
            "endTime": task["endTime"],
            "timestamp": int(datetime.now().timestamp())
        }
        
        doc_ref.set(task_data)
        print(f"✓ Uploaded: {task['id']} - {task['description']}")
    
    print(f"\n✅ Successfully uploaded {len(tasks)} tasks to Firestore!")

def main():
    print("📖 Reading TXT file...")
    tasks = parse_txt_file(TXT_FILE_PATH)
    
    print(f"📝 Parsed {len(tasks)} tasks:")
    for task in tasks:
        start_readable = timestamp_to_readable(task["startTime"])
        end_readable = timestamp_to_readable(task["endTime"])
        print(f"   {task['id']}: {task['description']} ({start_readable} → {end_readable})")
    
    
    print("\n☁️  Uploading to Firestore...")
    upload_to_firestore(tasks)

if __name__ == "__main__":
    main()