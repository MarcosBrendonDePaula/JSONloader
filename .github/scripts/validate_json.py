import json
import os
import jsonschema
import sys

def validate_json_file(file_path, schema_path=None):
    """Validate a JSON file against a schema if provided, or just check if it's valid JSON."""
    print(f"Validating {file_path}...")
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        if schema_path:
            with open(schema_path, 'r', encoding='utf-8') as f:
                schema = json.load(f)
            jsonschema.validate(data, schema)
        
        print(f"✅ {file_path} is valid JSON")
        return True
    except json.JSONDecodeError as e:
        print(f"❌ {file_path} is not valid JSON: {e}")
        return False
    except jsonschema.exceptions.ValidationError as e:
        print(f"❌ {file_path} does not match schema: {e}")
        return False
    except Exception as e:
        print(f"❌ Error validating {file_path}: {e}")
        return False

def main():
    # Define paths to JSON files and their schemas
    json_files = [
        {"file": "src/main/resources/assets/jsonloader/blocks.json"},
        {"file": "src/main/resources/assets/jsonloader/items.json"},
        {"file": "src/main/resources/assets/jsonloader/drops.json"}
    ]
    
    # Validate each JSON file
    success = True
    for json_file in json_files:
        file_path = json_file["file"]
        schema_path = json_file.get("schema")
        
        if not os.path.exists(file_path):
            print(f"⚠️ {file_path} does not exist, skipping")
            continue
        
        if schema_path and not os.path.exists(schema_path):
            print(f"⚠️ Schema {schema_path} does not exist, validating JSON syntax only")
            schema_path = None
        
        if not validate_json_file(file_path, schema_path):
            success = False
    
    # Exit with appropriate status code
    if not success:
        print("❌ JSON validation failed")
        sys.exit(1)
    else:
        print("✅ All JSON files are valid")
        sys.exit(0)

if __name__ == "__main__":
    main()
