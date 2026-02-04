"""
Check Firestore Usage
=====================

This script retrieves Firestore usage metrics (reads, writes, deletes) for the current day
using the Google Cloud Monitoring API.

Requirements:
    pip install google-cloud-monitoring

Setup:
    1. Enable the Cloud Monitoring API in Google Cloud Console
       https://console.cloud.google.com/apis/library/monitoring.googleapis.com
    2. Add "Monitoring Viewer" role to your service account in IAM
       https://console.cloud.google.com/iam-admin/iam
    3. Place serviceAccountKey.json in the parent directory

Usage:
    python check_firestore_usage.py

Note:
    Metrics may have a delay of 1-3 minutes. For real-time data, use Firebase Console.
"""

import os
from datetime import datetime, timedelta, timezone

os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "../serviceAccountKey.json"


def get_firestore_usage():
    """Fetch Firestore read, write, and delete counts for today from Cloud Monitoring API."""
    try:
        from google.cloud import monitoring_v3
    except ImportError:
        print("Error: google-cloud-monitoring not installed.")
        print("Run: pip install google-cloud-monitoring")
        return None
    
    import json
    with open("../serviceAccountKey.json") as f:
        project_id = json.load(f)["project_id"]
    
    client = monitoring_v3.MetricServiceClient()
    project_name = f"projects/{project_id}"
    
    now = datetime.now(timezone.utc)
    start_time = now - timedelta(hours=24)
    
    interval = monitoring_v3.TimeInterval({
        "end_time": {"seconds": int(now.timestamp())},
        "start_time": {"seconds": int(start_time.timestamp())},
    })
    
    metrics = {
        "reads": "firestore.googleapis.com/document/read_count",
        "writes": "firestore.googleapis.com/document/write_count",
        "deletes": "firestore.googleapis.com/document/delete_count",
    }
    
    print(f"📊 Firestore Usage (last 24 hours)")
    print(f"   Project: {project_id}")
    print(f"   Time: {start_time.strftime('%Y-%m-%d %H:%M')} → {now.strftime('%Y-%m-%d %H:%M')} UTC")
    print()
    
    for name, metric_type in metrics.items():
        try:
            results = client.list_time_series(
                request={
                    "name": project_name,
                    "filter": f'metric.type = "{metric_type}"',
                    "interval": interval,
                    "view": monitoring_v3.ListTimeSeriesRequest.TimeSeriesView.FULL,
                }
            )
            
            total = 0
            for series in results:
                for point in series.points:
                    total += point.value.int64_value
            
            print(f"   {name.capitalize():10} {total:,}")
            
        except Exception as e:
            print(f"   {name.capitalize():10} Error: {e}")
    
    print()
    print("ℹ️  Free tier daily limits: 50,000 reads | 20,000 writes | 20,000 deletes")


if __name__ == "__main__":
    get_firestore_usage()