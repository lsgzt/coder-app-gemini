const fs = require('fs');
const path = './PocketDev/app/src/main/java/com/pocketdev/app/editor/AutocompleteEngine.kt';
let content = fs.readFileSync(path, 'utf8');

// Replace HTML tags with offset
content = content.replace(/AutocompleteItem\("([^"]+)", "((?:[^"\\]|\\.)*)", CompletionType\.TAG, (-[0-9]+), "(?:[^"\\]|\\.)*"\)/g, (match, p1, p2, p3) => {
    // Extract everything before " — "
    let tag = p2.split(' — ')[0];
    return `AutocompleteItem("${p1}", "${p2}", CompletionType.TAG, ${p3}, "${tag}")`;
});

// Also replace HTML tags without offset
content = content.replace(/AutocompleteItem\("([^"]+)", "((?:[^"\\]|\\.)*)", CompletionType\.TAG, 0, "(?:[^"\\]|\\.)*"\)/g, (match, p1, p2) => {
    let tag = p2.split(' — ')[0];
    return `AutocompleteItem("${p1}", "${p2}", CompletionType.TAG, 0, "${tag}")`;
});

fs.writeFileSync(path, content);
console.log('Done');
