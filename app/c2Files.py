import os

def merge_code_files(start_dir="src", output_file="alle_klassen.txt"):
    # Sicherstellen, dass wir im richtigen Verzeichnis suchen
    if not os.path.exists(start_dir):
        print(f"FEHLER: Der Ordner '{start_dir}' wurde nicht gefunden!")
        print(f"Ich suche in: {os.getcwd()}")
        return

    count = 0
    with open(output_file, "w", encoding="utf-8") as outfile:
        for root, dirs, files in os.walk(start_dir):
            for file in files:
                # HIER IST DIE ÄNDERUNG: Sucht nach .kt UND .java
                if file.endswith((".kt", ".java")):
                    full_path = os.path.join(root, file)
                    outfile.write(f"\n{'='*50}\n")
                    outfile.write(f"FILE: {full_path}\n")
                    outfile.write(f"{'='*50}\n")
                    
                    try:
                        with open(full_path, "r", encoding="utf-8") as infile:
                            outfile.write(infile.read())
                        count += 1
                    except Exception as e:
                        outfile.write(f"Error reading file: {e}\n")
                        
    print(f"Erfolg! {count} Kotlin/Java-Dateien wurden in '{output_file}' gespeichert.")

if __name__ == "__main__":
    merge_code_files()