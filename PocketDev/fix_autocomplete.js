const fs = require('fs');
const path = './PocketDev/app/src/main/java/com/pocketdev/app/editor/AutocompleteEngine.kt';
let content = fs.readFileSync(path, 'utf8');

// Replace Python functions/classes with -1 offset
content = content.replace(/AutocompleteItem\("([^"]+)", "([^"]+)", CompletionType\.(FUNCTION|CLASS), -1\)/g, 
    'AutocompleteItem("$1", "$2", CompletionType.$3, -1, "$1()")');

// Replace HTML tags
content = content.replace(/AutocompleteItem\("([^"]+)", "([^"]+)", CompletionType\.TAG, (-[0-9]+)\)/g, (match, p1, p2, p3) => {
    // Extract the tag from description, e.g., "<h1></h1> — Heading 1" -> "<h1></h1>"
    const tagMatch = p2.match(/^(<[^>]+>(?:<\/[^>]+>)?)/);
    if (tagMatch) {
        return `AutocompleteItem("${p1}", "${p2}", CompletionType.TAG, ${p3}, "${tagMatch[1].replace(/\n/g, '\\n')}")`;
    }
    return match;
});

// Also replace HTML tags without offset
content = content.replace(/AutocompleteItem\("([^"]+)", "([^"]+)", CompletionType\.TAG\)/g, (match, p1, p2) => {
    const tagMatch = p2.match(/^(<[^>]+>(?:<\/[^>]+>)?)/);
    if (tagMatch) {
        return `AutocompleteItem("${p1}", "${p2}", CompletionType.TAG, 0, "${tagMatch[1].replace(/\n/g, '\\n')}")`;
    }
    return match;
});

fs.writeFileSync(path, content);
console.log('Done');
