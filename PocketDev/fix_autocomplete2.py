import re

path = './PocketDev/app/src/main/java/com/pocketdev/app/editor/AutocompleteEngine.kt'
with open(path, 'r') as f:
    content = f.read()

# Replace HTML tags with offset
def replace_tag_with_offset(match):
    p1, p2, p3 = match.groups()
    tag_match = re.match(r'^(<[^>]+>(?:</[^>]+>)?)', p2)
    if tag_match:
        tag = tag_match.group(1).replace('\n', '\\n')
        # Escape quotes in tag
        tag = tag.replace('"', '\\"')
        return f'AutocompleteItem("{p1}", "{p2}", CompletionType.TAG, {p3}, "{tag}")'
    return match.group(0)

content = re.sub(
    r'AutocompleteItem\("([^"]+)", "(.*?)", CompletionType\.TAG, (-[0-9]+)\)',
    replace_tag_with_offset,
    content
)

# Replace HTML tags without offset
def replace_tag_no_offset(match):
    p1, p2 = match.groups()
    tag_match = re.match(r'^(<[^>]+>(?:</[^>]+>)?)', p2)
    if tag_match:
        tag = tag_match.group(1).replace('\n', '\\n')
        tag = tag.replace('"', '\\"')
        return f'AutocompleteItem("{p1}", "{p2}", CompletionType.TAG, 0, "{tag}")'
    return match.group(0)

content = re.sub(
    r'AutocompleteItem\("([^"]+)", "(.*?)", CompletionType\.TAG\)',
    replace_tag_no_offset,
    content
)

with open(path, 'w') as f:
    f.write(content)

print("Done")
