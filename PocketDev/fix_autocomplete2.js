const fs = require('fs');
const path = './PocketDev/app/src/main/java/com/pocketdev/app/editor/AutocompleteEngine.kt';
let content = fs.readFileSync(path, 'utf8');

// Replace HTML tags with offset
content = content.replace(/AutocompleteItem\("([^"]+)", "(.*?)", CompletionType\.TAG, (-[0-9]+)\)/g, (match, p1, p2, p3) => {
    const tagMatch = p2.match(/^(<[^>]+>(?:<\/[^>]+>)?)/);
    if (tagMatch) {
        let tag = tagMatch[1].replace(/\n/g, '\\n');
        return `AutocompleteItem("${p1}", "${p2}", CompletionType.TAG, ${p3}, "${tag}")`;
    }
    return match;
});

// Also replace HTML tags without offset
content = content.replace(/AutocompleteItem\("([^"]+)", "(.*?)", CompletionType\.TAG\)/g, (match, p1, p2) => {
    const tagMatch = p2.match(/^(<[^>]+>(?:<\/[^>]+>)?)/);
    if (tagMatch) {
        let tag = tagMatch[1].replace(/\n/g, '\\n');
        return `AutocompleteItem("${p1}", "${p2}", CompletionType.TAG, 0, "${tag}")`;
    }
    return match;
});

fs.writeFileSync(path, content);
console.log('Done');
