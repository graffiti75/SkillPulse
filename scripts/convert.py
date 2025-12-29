import re
from datetime import datetime

def parse_date(date_str):
    """Convert DD/MM/YYYY to YYYYMMDD format."""
    day, month, year = date_str.split('/')
    return f"{year}{month}{day}"

def parse_time(time_str):
    """Convert time format to HH:MM, keeping the time but removing +X notation."""
    time_str = time_str.strip()
    
    # Handle times like "9h50", "16h55", "0h10", "11h   +5", "11h +5"
    # Extract just the time part (everything before +X if present, or the whole string)
    # First, try to match the time pattern anywhere in the string
    match = re.search(r'(\d+)h(\d*)', time_str)
    if match:
        hour = match.group(1).zfill(2)
        minute = match.group(2) if match.group(2) else '00'
        minute = minute.zfill(2)
        return f"{hour}:{minute}"
    
    return None

def convert_tasks_to_csv(input_file, output_file):
    """Convert task log from input.txt to CSV format in output.txt."""
    
    with open(input_file, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    output_lines = []
    current_date = None
    task_counter = 0
    
    # Date pattern: DD/MM/YYYY
    date_pattern = re.compile(r'^\d{2}/\d{2}/\d{4}$')
    
    for line in lines:
        line = line.strip()
        
        # Skip empty lines
        if not line:
            continue
        
        # Check if line is a date
        if date_pattern.match(line):
            current_date = parse_date(line)
            task_counter = 0
            continue
        
        # Skip lines without a current date
        if not current_date:
            continue
        
        # Parse task line (format: description \t time)
        # First, try to extract time from the end of the line
        time_match = re.search(r'(\d+h\d*\s*(?:\+\d+)?)\s*$', line)
        
        if time_match:
            time_part = time_match.group(1)
            # Get description by removing the time part from the end
            description = line[:time_match.start()].strip()
            
            # Parse time (this will extract time and ignore +X)
            formatted_time = parse_time(time_part)
            
            # Always include the line if we can parse a time
            if formatted_time and description:
                task_counter += 1
                task_id = f"{current_date}_{task_counter}"
                
                # Create CSV line
                csv_line = f"{task_id};{description};{formatted_time}"
                output_lines.append(csv_line)
    
    # Write to output file
    with open(output_file, 'w', encoding='utf-8') as f:
        # Write header
        f.write("id;description;task_end_time\n")
        # Write data
        for line in output_lines:
            f.write(line + "\n")
    
    print(f"Conversion complete! Created {output_file} with {len(output_lines)} tasks.")

# Run the conversion
if __name__ == "__main__":
    convert_tasks_to_csv('input.txt', 'output.txt')