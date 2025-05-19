from pathlib import Path
import csv

# Base directory of your dataset
BASE_DIR = Path("D:/SEMESTER-6/PPB/NoReact/Data/Allergen30")

# List of class names in order of their indices
class_names = [
    'alcohol', 'alcohol_glass', 'almond', 'avocado', 'blackberry', 'blueberry', 
    'bread', 'bread_loaf', 'capsicum', 'cheese', 'chocolate', 'cooked_meat', 'dates', 
    'egg', 'eggplant', 'icecream', 'milk', 'milk_based_beverage', 'mushroom', 
    'non_milk_based_beverage', 'pasta', 'pineapple', 'pistachio', 'pizza', 
    'raw_meat', 'roti', 'spinach', 'strawberry', 'tomato', 'whole_egg_boiled'
]

def extract_labels(split):
    images_dir = BASE_DIR / 'images' / split
    labels_dir = BASE_DIR / 'labels' / split
    entries = []

    for label_file in labels_dir.glob("*.txt"):
        # Find matching image file with common extensions
        image_file = None
        for ext in ['.jpg', '.jpeg', '.png']:
            possible_file = images_dir / label_file.with_suffix(ext).name
            if possible_file.exists():
                image_file = possible_file
                break
        if not image_file:
            print(f"[WARNING] No image found for label: {label_file.name} in {split}")
            continue

        with open(label_file, 'r') as f:
            lines = f.read().strip().splitlines()
            if not lines:
                continue
            # Collect unique class indices from label file
            class_indices = {int(line.split()[0]) for line in lines if line.strip()}

        # Convert indices to class names and join with comma
        labels = [class_names[idx] for idx in sorted(class_indices)]

        # Relative path for the image (relative to BASE_DIR)
        rel_path = str(image_file.relative_to(BASE_DIR)).replace("\\", "/")
        entries.append((rel_path, ",".join(labels)))

    return entries

def main():
    splits = ['train', 'valid', 'test']
    all_entries = []

    for split in splits:
        print(f"Extracting labels from split: {split}")
        split_entries = extract_labels(split)
        all_entries.extend(split_entries)

    # Write to CSV
    csv_file = BASE_DIR / "allergen30_labels.csv"
    with open(csv_file, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(['image_path', 'labels'])  # Header
        writer.writerows(all_entries)

    print(f"Extraction completed. CSV saved at: {csv_file}")

if __name__ == "__main__":
    main()
