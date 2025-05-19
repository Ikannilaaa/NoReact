from pathlib import Path
import csv

# Set the base directory (adjust this to your actual path)
BASE_DIR = Path(r"D:\SEMESTER-6\PPB\NoReact\Data\Food-101\images")

# Output CSV path
CSV_OUTPUT = BASE_DIR.parent / "food101_labels.csv"

def extract_labels_to_csv(base_dir, csv_path):
    rows = []

    for class_folder in base_dir.iterdir():
        if class_folder.is_dir():
            label = class_folder.name
            for image_file in class_folder.glob("*.*"):
                if image_file.suffix.lower() in ['.jpg', '.jpeg', '.png']:
                    # Build relative path from base_dir.parent so it looks like "images/apple_pie/img.jpg"
                    rel_path = image_file.relative_to(base_dir.parent).as_posix()
                    rows.append([rel_path, label])

    # Save to CSV
    with open(csv_path, "w", newline='', encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["image_path", "labels"])
        writer.writerows(rows)

    print(f"✅ CSV saved at: {csv_path}, total {len(rows)} entries.")

# Run it
extract_labels_to_csv(BASE_DIR, CSV_OUTPUT)
