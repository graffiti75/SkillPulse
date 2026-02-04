"""
Convert Tasks to CSV
====================

This script converts a daily task log from a custom format to CSV format.
It handles midnight crossing detection to calculate correct timestamps.

Requirements:
    Python 3.6+ (no external dependencies)

Setup:
    Place your input.txt file in the parent directory (or update the path in main)

Usage:
    python convert_tasks.py

Input format (input.txt):
    DD/MM/YYYY
    + Task description                  HHhMM
    + Another task                      HHh
    + Task with duration                HHhMM +X
    
    Example:
    01/11/2025
    + Acordar                           6h45
    + Almoçar                           12h30 +15
    + Cochilo                           1h15

Output format (output.txt):
    ID;DESCRIPTION;TIMESTAMP
    Example: 20251101_1;Acordar;2025-11-01T06:45:00-03:00

Notes:
    - Timestamps are in GMT-3 (São Paulo timezone)
    - Tasks after midnight are automatically assigned to the next day
    - The +X notation (duration) is ignored in the output
"""

import re
from datetime import datetime, timedelta


def parse_date(date_str):
    """Convert DD/MM/YYYY to YYYYMMDD format."""
    day, month, year = date_str.split('/')
    return f"{year}{month}{day}"


def parse_time(time_str):
    """Convert time format (e.g., '9h50', '16h', '0h10') to HH:MM format."""
    time_str = time_str.strip()
    time_str = re.sub(r'\+\d+', '', time_str).strip()
    
    match = re.match(r'(\d+)h(\d*)', time_str)
    if match:
        hour = match.group(1).zfill(2)
        minute = match.group(2) if match.group(2) else '00'
        minute = minute.zfill(2)
        return f"{hour}:{minute}"
    
    return None


def time_to_minutes(time_str):
    """Convert HH:MM to minutes since midnight for comparison."""
    hours, minutes = map(int, time_str.split(':'))
    return hours * 60 + minutes


def convert_tasks_to_csv(input_file, output_file):
    """
    Parse input file and convert to CSV format.
    Detects midnight crossing by checking if time goes backwards by more than 6 hours.
    """
    with open(input_file, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    output_lines = []
    current_date = None
    current_date_for_timestamp = None
    task_counter = 0
    previous_time_minutes = None
    
    date_pattern = re.compile(r'^\d{2}/\d{2}/\d{4}$')
    
    for line in lines:
        line = line.rstrip('\n\r')
        
        if not line.strip():
            continue
        
        if date_pattern.match(line.strip()):
            current_date = parse_date(line.strip())
            current_date_for_timestamp = current_date
            task_counter = 0
            previous_time_minutes = None
            continue
        
        if not current_date:
            continue
        
        if not line.strip().startswith('+ '):
            continue
        
        line = line.strip()[2:]
        
        time_match = re.search(r'(\d{1,2}h\d{0,2})\s*(\+\d+)?.*$', line)
        
        if time_match:
            time_part = time_match.group(1)
            description = line[:time_match.start()].strip()
            formatted_time = parse_time(time_part)
            
            if formatted_time and description:
                current_time_minutes = time_to_minutes(formatted_time)
                
                if previous_time_minutes is not None:
                    if previous_time_minutes - current_time_minutes > 360:
                        dt = datetime.strptime(current_date_for_timestamp, '%Y%m%d')
                        dt += timedelta(days=1)
                        current_date_for_timestamp = dt.strftime('%Y%m%d')
                
                previous_time_minutes = current_time_minutes
                task_counter += 1
                task_id = f"{current_date}_{task_counter}"
                
                year = current_date_for_timestamp[:4]
                month = current_date_for_timestamp[4:6]
                day = current_date_for_timestamp[6:8]
                timestamp = f"{year}-{month}-{day}T{formatted_time}:00-03:00"
                
                csv_line = f"{task_id};{description};{timestamp}"
                output_lines.append(csv_line)
    
    with open(output_file, 'w', encoding='utf-8') as f:
        for line in output_lines:
            f.write(line + "\n")
    
    print(f"Conversion complete! Created {output_file} with {len(output_lines)} tasks.")


if __name__ == "__main__":
    convert_tasks_to_csv('../input.txt', 'output.txt')