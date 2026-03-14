package com.pocketdev.app.utils

import com.pocketdev.app.data.models.Language

data class CodeExample(
    val title: String,
    val description: String,
    val language: Language,
    val code: String
)

object CodeExamples {

    val pythonExamples = listOf(
        CodeExample(
            title = "Hello World & Variables",
            description = "Learn about printing output and storing values",
            language = Language.PYTHON,
            code = """# Python - Hello World & Variables
# This is a comment - Python ignores these lines

# Print a message to the console
print("Hello, World!")

# Variables store data
name = "Alice"         # String (text)
age = 16               # Integer (whole number)
height = 5.6           # Float (decimal number)
is_student = True      # Boolean (True or False)

# Using variables in print statements
print(f"My name is {name}")
print(f"I am {age} years old")
print(f"My height is {height} feet")
print(f"Am I a student? {is_student}")

# Basic arithmetic
x = 10
y = 3
print(f"\n{x} + {y} = {x + y}")
print(f"{x} - {y} = {x - y}")
print(f"{x} * {y} = {x * y}")
print(f"{x} / {y} = {x / y:.2f}")  # :.2f means 2 decimal places
print(f"{x} // {y} = {x // y}")    # Integer division
print(f"{x} % {y} = {x % y}")      # Remainder (modulo)
print(f"{x} ** {y} = {x ** y}")    # Power/exponent
"""
        ),
        CodeExample(
            title = "Loops & Conditionals",
            description = "Control flow with if/else and for/while loops",
            language = Language.PYTHON,
            code = """# Python - Loops & Conditionals

# If/elif/else statements
score = 85

if score >= 90:
    grade = "A"
elif score >= 80:
    grade = "B"
elif score >= 70:
    grade = "C"
elif score >= 60:
    grade = "D"
else:
    grade = "F"

print(f"Score: {score}, Grade: {grade}")

# For loop - repeat for each item
print("\nCounting from 1 to 5:")
for i in range(1, 6):
    print(f"  Number: {i}")

# For loop through a list
fruits = ["apple", "banana", "cherry", "mango"]
print("\nMy favorite fruits:")
for fruit in fruits:
    print(f"  - {fruit}")

# While loop
print("\nCountdown:")
count = 5
while count > 0:
    print(f"  {count}...")
    count -= 1
print("  Blast off! 🚀")

# Loop with break and continue
print("\nEven numbers from 1-10:")
for num in range(1, 11):
    if num % 2 != 0:  # Skip odd numbers
        continue
    print(f"  {num}", end=" ")
print()  # New line
"""
        ),
        CodeExample(
            title = "Functions & Parameters",
            description = "Create reusable code blocks with functions",
            language = Language.PYTHON,
            code = """# Python - Functions & Parameters

# Basic function definition
def greet(name):
    '''Say hello to someone.'''
    print(f"Hello, {name}! Welcome!")

# Call the function
greet("Alice")
greet("Bob")

# Function with return value
def add_numbers(a, b):
    '''Add two numbers and return the result.'''
    result = a + b
    return result

sum_result = add_numbers(15, 7)
print(f"\n15 + 7 = {sum_result}")

# Function with default parameter
def introduce(name, age=18):
    '''Introduce a person with optional age.'''
    print(f"Hi! I'm {name} and I'm {age} years old.")

introduce("Carol")           # Uses default age
introduce("Dave", age=25)    # Uses custom age

# Function that returns multiple values
def get_circle_info(radius):
    '''Calculate area and circumference of a circle.'''
    import math
    area = math.pi * radius ** 2
    circumference = 2 * math.pi * radius
    return area, circumference

area, circumference = get_circle_info(5)
print(f"\nCircle with radius 5:")
print(f"  Area: {area:.2f}")
print(f"  Circumference: {circumference:.2f}")

# Recursive function
def factorial(n):
    '''Calculate factorial using recursion.'''
    if n <= 1:
        return 1
    return n * factorial(n - 1)

print(f"\nFactorial of 5 = {factorial(5)}")
print(f"Factorial of 8 = {factorial(8)}")
"""
        ),
        CodeExample(
            title = "Lists & Dictionaries",
            description = "Work with collections of data",
            language = Language.PYTHON,
            code = """# Python - Lists & Dictionaries

# === LISTS ===
# Lists are ordered collections
numbers = [3, 1, 4, 1, 5, 9, 2, 6, 5]
print("Original list:", numbers)

# Common list operations
numbers.append(7)           # Add to end
numbers.insert(0, 0)        # Insert at position
numbers.remove(1)           # Remove first occurrence
print("After changes:", numbers)

# Sorting
numbers.sort()
print("Sorted:", numbers)

# List slicing [start:end:step]
print("First 4 items:", numbers[:4])
print("Last 3 items:", numbers[-3:])
print("Every 2nd item:", numbers[::2])

# List comprehension (powerful Python feature)
squares = [x**2 for x in range(1, 6)]
print("\nSquares 1-5:", squares)

even_squares = [x**2 for x in range(1, 11) if x % 2 == 0]
print("Even squares 1-10:", even_squares)

# === DICTIONARIES ===
# Dictionaries store key-value pairs
student = {
    "name": "Emma",
    "age": 17,
    "grade": "11th",
    "subjects": ["Math", "Science", "English"]
}

print(f"\nStudent: {student['name']}")
print(f"Age: {student['age']}")
print(f"Grade: {student['grade']}")
print(f"Subjects: {', '.join(student['subjects'])}")

# Adding and updating dictionary values
student["gpa"] = 3.8
student["age"] = 18

# Iterating over a dictionary
print("\nAll student info:")
for key, value in student.items():
    print(f"  {key}: {value}")

# Checking if key exists
if "gpa" in student:
    print(f"\nGPA: {student['gpa']}")
"""
        ),
        CodeExample(
            title = "Classes & Objects",
            description = "Object-oriented programming with classes",
            language = Language.PYTHON,
            code = """# Python - Classes & Objects (OOP)

# Define a class
class Animal:
    # Class variable (shared by all instances)
    kingdom = "Animalia"

    # Constructor method
    def __init__(self, name, species, sound):
        # Instance variables (unique to each object)
        self.name = name
        self.species = species
        self.sound = sound
        self.energy = 100

    # Instance method
    def speak(self):
        print(f"{self.name} says: {self.sound}!")

    def eat(self, food):
        self.energy += 10
        print(f"{self.name} eats {food}. Energy: {self.energy}")

    # String representation
    def __str__(self):
        return f"{self.name} ({self.species})"


# Inheritance - Dog extends Animal
class Dog(Animal):
    def __init__(self, name, breed):
        super().__init__(name, "Canis lupus familiaris", "Woof")
        self.breed = breed
        self.tricks = []

    def learn_trick(self, trick):
        self.tricks.append(trick)
        print(f"{self.name} learned: {trick}!")

    def show_tricks(self):
        if self.tricks:
            print(f"{self.name}'s tricks: {', '.join(self.tricks)}")
        else:
            print(f"{self.name} doesn't know any tricks yet.")


# Create objects (instances of classes)
cat = Animal("Whiskers", "Felis catus", "Meow")
dog = Dog("Buddy", "Golden Retriever")

# Use objects
print(f"=== {cat} ===")
cat.speak()
cat.eat("tuna")

print(f"\n=== {dog} ===")
dog.speak()
dog.learn_trick("sit")
dog.learn_trick("shake hands")
dog.learn_trick("roll over")
dog.show_tricks()
dog.eat("kibble")

# Class variable access
print(f"\nAll animals belong to kingdom: {Animal.kingdom}")
"""
        )
    )

    val javascriptExamples = listOf(
        CodeExample(
            title = "Variables & Data Types",
            description = "Learn about variables, const, let, and data types",
            language = Language.JAVASCRIPT,
            code = """// JavaScript - Variables & Data Types

// Modern JavaScript uses 'const' and 'let' (not 'var')
const PI = 3.14159;          // const: cannot be changed
let counter = 0;             // let: can be changed
counter = 1;                 // OK - let can be reassigned

// Data Types
const name = "Alice";            // String
const age = 17;                  // Number (integers and decimals)
const height = 5.6;              // Number (float)
const isStudent = true;          // Boolean
const nothing = null;            // Null (intentional absence)
let notAssigned;                 // Undefined (no value assigned)

console.log("Name:", name);
console.log("Age:", age);
console.log("Is student:", isStudent);
console.log("Type of name:", typeof name);
console.log("Type of age:", typeof age);
console.log("Type of nothing:", typeof nothing);

// Template literals (string interpolation)
const greeting = `Hello! My name is ${'$'}{name} and I am ${'$'}{age} years old.`;
console.log(greeting);

// String methods
const message = "Hello, World!";
console.log("\nString operations:");
console.log("Uppercase:", message.toUpperCase());
console.log("Lowercase:", message.toLowerCase());
console.log("Length:", message.length);
console.log("Includes 'World':", message.includes("World"));
console.log("Replace:", message.replace("World", "JavaScript"));
console.log("Slice:", message.slice(7, 12));

// Number operations
console.log("\nMath operations:");
console.log("Max:", Math.max(10, 20, 5, 15));
console.log("Min:", Math.min(10, 20, 5, 15));
console.log("Round:", Math.round(4.7));
console.log("Floor:", Math.floor(4.7));
console.log("Ceil:", Math.ceil(4.3));
console.log("Random 1-10:", Math.floor(Math.random() * 10) + 1);
"""
        ),
        CodeExample(
            title = "Functions & Arrow Functions",
            description = "Regular functions vs arrow functions",
            language = Language.JAVASCRIPT,
            code = """// JavaScript - Functions & Arrow Functions

// Traditional function declaration
function greet(name) {
    return `Hello, ${'$'}{name}!`;
}
console.log(greet("Alice"));

// Function expression
const multiply = function(a, b) {
    return a * b;
};
console.log("3 × 4 =", multiply(3, 4));

// Arrow function (concise syntax)
const add = (a, b) => a + b;
console.log("5 + 7 =", add(5, 7));

// Arrow function with body
const calculateGrade = (score) => {
    if (score >= 90) return "A";
    else if (score >= 80) return "B";
    else if (score >= 70) return "C";
    else if (score >= 60) return "D";
    else return "F";
};

const scores = [95, 82, 73, 61, 45];
scores.forEach(score => {
    console.log(`Score: ${'$'}{score} → Grade: ${'$'}{calculateGrade(score)}`);
});

// Higher-order functions (functions that take/return functions)
const numbers = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

// map: transform each element
const squared = numbers.map(n => n * n);
console.log("\nSquared:", squared);

// filter: keep matching elements
const evens = numbers.filter(n => n % 2 === 0);
console.log("Even numbers:", evens);

// reduce: combine all elements into one value
const sum = numbers.reduce((total, n) => total + n, 0);
console.log("Sum:", sum);

// Chaining methods
const result = numbers
    .filter(n => n % 2 === 0)   // keep evens: [2,4,6,8,10]
    .map(n => n * n)             // square them: [4,16,36,64,100]
    .reduce((sum, n) => sum + n, 0); // sum: 220
console.log("Sum of squares of even numbers:", result);
"""
        ),
        CodeExample(
            title = "Arrays & Objects",
            description = "Working with arrays and objects in JavaScript",
            language = Language.JAVASCRIPT,
            code = """// JavaScript - Arrays & Objects

// === ARRAYS ===
const fruits = ["apple", "banana", "cherry"];

// Array methods
fruits.push("mango");           // Add to end
fruits.unshift("strawberry");   // Add to beginning
const removed = fruits.pop();   // Remove from end
console.log("Fruits:", fruits);
console.log("Removed:", removed);

// Spread operator
const moreFruits = [...fruits, "grape", "kiwi"];
console.log("More fruits:", moreFruits);

// Array destructuring
const [first, second, ...rest] = moreFruits;
console.log("First:", first);
console.log("Second:", second);
console.log("Rest:", rest);

// === OBJECTS ===
const student = {
    name: "Emma",
    age: 17,
    grade: "11th",
    subjects: ["Math", "Science", "English"],
    address: {
        city: "New York",
        state: "NY"
    }
};

// Accessing properties
console.log("\nStudent:", student.name);
console.log("City:", student.address.city);

// Object destructuring
const { name, age, subjects } = student;
console.log(`${'$'}{name} is ${'$'}{age} years old`);
console.log("Studies:", subjects.join(", "));

// Object spread (copy and modify)
const updatedStudent = {
    ...student,
    age: 18,
    gpa: 3.9
};
console.log("\nUpdated student GPA:", updatedStudent.gpa);

// Object methods (shorthand)
const calculator = {
    value: 0,
    add(n) { this.value += n; return this; },
    subtract(n) { this.value -= n; return this; },
    multiply(n) { this.value *= n; return this; },
    result() { return this.value; }
};

// Method chaining
const calcResult = calculator.add(10).multiply(5).subtract(8).result();
console.log("\nCalculation result:", calcResult);
"""
        ),
        CodeExample(
            title = "Promises & Async/Await",
            description = "Handle asynchronous code elegantly",
            language = Language.JAVASCRIPT,
            code = """// JavaScript - Promises & Async/Await

// Promises represent future values
function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

function fetchUserData(userId) {
    return new Promise((resolve, reject) => {
        // Simulate API call
        setTimeout(() => {
            if (userId > 0) {
                resolve({
                    id: userId,
                    name: "Alice Johnson",
                    email: "alice@example.com",
                    score: 95
                });
            } else {
                reject(new Error("Invalid user ID"));
            }
        }, 100);
    });
}

// Using .then().catch() chain
fetchUserData(1)
    .then(user => {
        console.log("User found:", user.name);
        console.log("Email:", user.email);
        return user;
    })
    .then(user => {
        console.log("Score:", user.score);
    })
    .catch(error => {
        console.error("Error:", error.message);
    });

// Async/Await (cleaner syntax for promises)
async function getUserInfo(userId) {
    try {
        console.log(`\nFetching user ${'$'}{userId}...`);
        const user = await fetchUserData(userId);
        console.log(`Got user: ${'$'}{user.name}`);

        // Simulate another async operation
        await delay(50);
        console.log("Processing complete!");

        return user;
    } catch (error) {
        console.error("Failed to get user:", error.message);
        return null;
    }
}

// Promise.all - run multiple promises in parallel
async function fetchMultipleUsers() {
    console.log("\nFetching multiple users in parallel...");
    const userPromises = [fetchUserData(1), fetchUserData(2), fetchUserData(3)];

    try {
        const users = await Promise.all([
            fetchUserData(1),
            fetchUserData(2),
            fetchUserData(3)
        ]);
        console.log(`Fetched ${'$'}{users.length} users successfully!`);
        users.forEach(u => console.log(`  - ${'$'}{u.name}`));
    } catch (error) {
        console.error("One or more requests failed:", error.message);
    }
}

// Run the async functions
getUserInfo(1);
fetchMultipleUsers();
"""
        ),
        CodeExample(
            title = "Classes & Modules Pattern",
            description = "Object-oriented JavaScript with classes",
            language = Language.JAVASCRIPT,
            code = """// JavaScript - Classes & OOP

// Base class
class Shape {
    constructor(color = "black") {
        this.color = color;
    }

    // Getter
    get info() {
        return `${'$'}{this.constructor.name} - Color: ${'$'}{this.color}`;
    }

    area() {
        throw new Error("area() must be implemented by subclass");
    }

    toString() {
        return `${'$'}{this.info}, Area: ${'$'}{this.area().toFixed(2)}`;
    }
}

// Inheritance with extends
class Circle extends Shape {
    constructor(radius, color) {
        super(color); // Call parent constructor
        this.radius = radius;
    }

    area() {
        return Math.PI * this.radius ** 2;
    }

    circumference() {
        return 2 * Math.PI * this.radius;
    }
}

class Rectangle extends Shape {
    constructor(width, height, color) {
        super(color);
        this.width = width;
        this.height = height;
    }

    area() {
        return this.width * this.height;
    }

    perimeter() {
        return 2 * (this.width + this.height);
    }
}

class Triangle extends Shape {
    constructor(base, height, color) {
        super(color);
        this.base = base;
        this.height = height;
    }

    area() {
        return 0.5 * this.base * this.height;
    }
}

// Create instances
const shapes = [
    new Circle(5, "red"),
    new Rectangle(4, 6, "blue"),
    new Triangle(3, 8, "green"),
    new Circle(3, "purple")
];

console.log("=== All Shapes ===");
shapes.forEach(shape => console.log(shape.toString()));

// Find largest shape
const largest = shapes.reduce((max, shape) =>
    shape.area() > max.area() ? shape : max
);
console.log("\nLargest shape:", largest.info);
console.log("Area:", largest.area().toFixed(2));

// Static methods
class MathHelper {
    static factorial(n) {
        if (n <= 1) return 1;
        return n * MathHelper.factorial(n - 1);
    }

    static fibonacci(n) {
        if (n <= 1) return n;
        return MathHelper.fibonacci(n - 1) + MathHelper.fibonacci(n - 2);
    }
}

console.log("\n5! =", MathHelper.factorial(5));
console.log("Fibonacci(8) =", MathHelper.fibonacci(8));
"""
        )
    )

    val htmlExamples = listOf(
        CodeExample(
            title = "Basic HTML Structure",
            description = "Learn the fundamental HTML page structure",
            language = Language.HTML,
            code = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My First Webpage</title>
    <style>
        body {
            font-family: 'Segoe UI', Arial, sans-serif;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background: linear-gradient(135deg, #1a1a2e, #16213e);
            color: #e0e0e0;
            min-height: 100vh;
        }
        h1 { color: #4fc3f7; text-align: center; }
        h2 { color: #81c784; }
        p { line-height: 1.6; }
        .highlight { background: #2a2a5a; padding: 10px; border-radius: 8px; border-left: 4px solid #4fc3f7; margin: 10px 0; }
        a { color: #64b5f6; }
        strong { color: #ffcc02; }
        em { color: #f48fb1; }
    </style>
</head>
<body>
    <!-- This is an HTML comment -->
    <h1>🌟 Welcome to HTML!</h1>

    <!-- Headings h1 to h6 -->
    <h2>Text Elements</h2>
    <p>This is a <strong>paragraph</strong> with <em>italic text</em> and a <a href="#">link</a>.</p>
    <p>HTML stands for <strong>H</strong>yper<strong>T</strong>ext <strong>M</strong>arkup <strong>L</strong>anguage.</p>

    <!-- Unordered list -->
    <h2>Unordered List</h2>
    <ul>
        <li>🍎 Apples</li>
        <li>🍌 Bananas</li>
        <li>🍒 Cherries</li>
    </ul>

    <!-- Ordered list -->
    <h2>Ordered List</h2>
    <ol>
        <li>Learn HTML basics</li>
        <li>Add CSS styling</li>
        <li>Add JavaScript interactivity</li>
        <li>Build awesome websites! 🚀</li>
    </ol>

    <!-- Blockquote -->
    <div class="highlight">
        <blockquote>
            "The best way to learn programming is by writing code." — Every programmer ever
        </blockquote>
    </div>

    <!-- Image (placeholder) -->
    <h2>Image Element</h2>
    <img src="https://via.placeholder.com/300x150/4fc3f7/1a1a2e?text=HTML+Image"
         alt="Sample image"
         style="border-radius: 8px; max-width: 100%;">

    <p>Congratulations! You've seen the basic HTML structure. 🎉</p>
</body>
</html>"""
        ),
        CodeExample(
            title = "Forms & Inputs",
            description = "Create interactive forms with various input types",
            language = Language.HTML,
            code = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HTML Forms</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: Arial, sans-serif;
            background: #1a1a2e;
            color: #e0e0e0;
            padding: 20px;
        }
        .form-container {
            max-width: 500px;
            margin: 0 auto;
            background: #16213e;
            padding: 30px;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.3);
        }
        h2 { color: #4fc3f7; margin-bottom: 20px; text-align: center; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; color: #81c784; font-weight: bold; }
        input, select, textarea {
            width: 100%;
            padding: 10px;
            border: 1px solid #333;
            border-radius: 6px;
            background: #0f3460;
            color: #e0e0e0;
            font-size: 14px;
        }
        input:focus, select:focus, textarea:focus {
            outline: none;
            border-color: #4fc3f7;
        }
        .checkbox-group { display: flex; align-items: center; gap: 10px; }
        .checkbox-group input { width: auto; }
        button {
            width: 100%;
            padding: 12px;
            background: #4fc3f7;
            color: #1a1a2e;
            border: none;
            border-radius: 6px;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
            transition: background 0.3s;
        }
        button:hover { background: #81c784; }
        #result { margin-top: 15px; padding: 10px; background: #0f3460; border-radius: 6px; display: none; }
    </style>
</head>
<body>
    <div class="form-container">
        <h2>📝 Student Registration</h2>

        <form id="registrationForm">
            <!-- Text input -->
            <div class="form-group">
                <label for="name">Full Name *</label>
                <input type="text" id="name" placeholder="Enter your full name" required>
            </div>

            <!-- Email input -->
            <div class="form-group">
                <label for="email">Email Address *</label>
                <input type="email" id="email" placeholder="student@school.edu" required>
            </div>

            <!-- Number input -->
            <div class="form-group">
                <label for="age">Age</label>
                <input type="number" id="age" min="10" max="100" placeholder="Your age">
            </div>

            <!-- Select dropdown -->
            <div class="form-group">
                <label for="grade">Grade Level</label>
                <select id="grade">
                    <option value="">-- Select Grade --</option>
                    <option value="middle">Middle School</option>
                    <option value="high">High School</option>
                    <option value="college">College</option>
                    <option value="other">Other</option>
                </select>
            </div>

            <!-- Textarea -->
            <div class="form-group">
                <label for="message">Why do you want to learn coding?</label>
                <textarea id="message" rows="3" placeholder="Tell us your motivation..."></textarea>
            </div>

            <!-- Checkbox -->
            <div class="form-group">
                <div class="checkbox-group">
                    <input type="checkbox" id="terms" required>
                    <label for="terms">I agree to the terms and conditions</label>
                </div>
            </div>

            <button type="submit">Register Now 🚀</button>
        </form>

        <div id="result"></div>
    </div>

    <script>
        document.getElementById('registrationForm').addEventListener('submit', function(e) {
            e.preventDefault();

            const name = document.getElementById('name').value;
            const email = document.getElementById('email').value;
            const grade = document.getElementById('grade').value;

            const result = document.getElementById('result');
            result.innerHTML = `
                <strong style="color: #81c784;">✅ Registration Successful!</strong><br>
                Welcome, <strong>${'$'}{name}</strong>!<br>
                We'll send confirmation to: ${'$'}{email}<br>
                ${'$'}{grade ? `Grade: ${'$'}{grade}` : ''}
            `;
            result.style.display = 'block';
        });
    </script>
</body>
</html>"""
        ),
        CodeExample(
            title = "CSS Styling",
            description = "Beautiful styling with CSS",
            language = Language.HTML,
            code = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CSS Showcase</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }

        body {
            font-family: 'Segoe UI', sans-serif;
            background: #0d1117;
            color: #c9d1d9;
            min-height: 100vh;
            padding: 20px;
        }

        h1 {
            text-align: center;
            color: transparent;
            background: linear-gradient(135deg, #58a6ff, #79c0ff, #56d364);
            -webkit-background-clip: text;
            background-clip: text;
            font-size: 2em;
            margin-bottom: 20px;
            animation: fadeIn 1s ease-in;
        }

        .grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 16px;
            margin-bottom: 20px;
        }

        .card {
            background: #161b22;
            border: 1px solid #30363d;
            border-radius: 12px;
            padding: 20px;
            text-align: center;
            transition: transform 0.3s, box-shadow 0.3s;
            cursor: pointer;
        }

        .card:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 30px rgba(88, 166, 255, 0.3);
            border-color: #58a6ff;
        }

        .card .icon { font-size: 2.5em; margin-bottom: 10px; }
        .card h3 { color: #58a6ff; margin-bottom: 8px; }

        .progress-section { margin-bottom: 20px; }
        .progress-label {
            display: flex;
            justify-content: space-between;
            margin-bottom: 5px;
            font-size: 0.9em;
        }
        .progress-bar {
            height: 10px;
            background: #21262d;
            border-radius: 5px;
            margin-bottom: 12px;
            overflow: hidden;
        }
        .progress-fill {
            height: 100%;
            border-radius: 5px;
            background: linear-gradient(90deg, #238636, #56d364);
            transition: width 1s ease;
        }

        .badge {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 0.8em;
            font-weight: bold;
            margin: 3px;
        }
        .badge-blue { background: #1f6feb; color: white; }
        .badge-green { background: #238636; color: white; }
        .badge-orange { background: #9e6a03; color: white; }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-20px); }
            to { opacity: 1; transform: translateY(0); }
        }
    </style>
</head>
<body>
    <h1>🎨 CSS Styling Showcase</h1>

    <div class="grid">
        <div class="card">
            <div class="icon">🐍</div>
            <h3>Python</h3>
            <p>Beginner-friendly programming language</p>
        </div>
        <div class="card">
            <div class="icon">🟨</div>
            <h3>JavaScript</h3>
            <p>The language of the web browser</p>
        </div>
        <div class="card">
            <div class="icon">🌐</div>
            <h3>HTML/CSS</h3>
            <p>Building beautiful web pages</p>
        </div>
    </div>

    <div class="progress-section">
        <h3 style="color: #58a6ff; margin-bottom: 15px;">📊 Learning Progress</h3>
        <div class="progress-label"><span>Python</span><span>75%</span></div>
        <div class="progress-bar"><div class="progress-fill" style="width: 75%"></div></div>

        <div class="progress-label"><span>JavaScript</span><span>60%</span></div>
        <div class="progress-bar"><div class="progress-fill" style="width: 60%; background: linear-gradient(90deg, #1f6feb, #58a6ff)"></div></div>

        <div class="progress-label"><span>HTML/CSS</span><span>90%</span></div>
        <div class="progress-bar"><div class="progress-fill" style="width: 90%; background: linear-gradient(90deg, #9e6a03, #e3b341)"></div></div>
    </div>

    <div>
        <h3 style="color: #58a6ff; margin-bottom: 10px;">🏷️ Skills</h3>
        <span class="badge badge-blue">HTML5</span>
        <span class="badge badge-blue">CSS3</span>
        <span class="badge badge-orange">JavaScript</span>
        <span class="badge badge-green">Python</span>
        <span class="badge badge-blue">Responsive Design</span>
        <span class="badge badge-green">Git</span>
    </div>
</body>
</html>"""
        ),
        CodeExample(
            title = "JavaScript Integration",
            description = "Make HTML interactive with JavaScript",
            language = Language.HTML,
            code = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Interactive JavaScript</title>
    <style>
        body { font-family: Arial, sans-serif; background: #1a1a2e; color: #e0e0e0; padding: 20px; }
        .container { max-width: 500px; margin: 0 auto; }
        h2 { color: #4fc3f7; }
        .card { background: #16213e; border-radius: 12px; padding: 20px; margin-bottom: 20px; }
        button {
            padding: 10px 20px; margin: 5px;
            border: none; border-radius: 8px;
            cursor: pointer; font-weight: bold;
            transition: transform 0.1s;
        }
        button:active { transform: scale(0.95); }
        .btn-primary { background: #4fc3f7; color: #1a1a2e; }
        .btn-success { background: #81c784; color: #1a1a2e; }
        .btn-danger { background: #e57373; color: white; }
        input { padding: 10px; width: 100%; margin: 5px 0 10px; background: #0f3460; border: 1px solid #4fc3f7; border-radius: 6px; color: white; }
        #counter-display { font-size: 3em; text-align: center; color: #4fc3f7; padding: 20px; }
        #output { background: #0f3460; padding: 10px; border-radius: 6px; min-height: 40px; }
        li { padding: 5px 0; border-bottom: 1px solid #333; }
        li:last-child { border: none; }
    </style>
</head>
<body>
    <div class="container">
        <h2>⚡ Interactive JavaScript</h2>

        <!-- Counter -->
        <div class="card">
            <h3>🔢 Counter</h3>
            <div id="counter-display">0</div>
            <div style="text-align: center;">
                <button class="btn-danger" onclick="changeCount(-1)">− Decrease</button>
                <button class="btn-primary" onclick="changeCount(0)">↺ Reset</button>
                <button class="btn-success" onclick="changeCount(1)">+ Increase</button>
            </div>
        </div>

        <!-- Calculator -->
        <div class="card">
            <h3>🧮 Calculator</h3>
            <input type="number" id="num1" placeholder="First number" value="10">
            <input type="number" id="num2" placeholder="Second number" value="5">
            <button class="btn-primary" onclick="calculate('+')">+</button>
            <button class="btn-primary" onclick="calculate('-')">−</button>
            <button class="btn-primary" onclick="calculate('*')">×</button>
            <button class="btn-primary" onclick="calculate('/')">÷</button>
            <div id="calc-result" style="margin-top: 10px; color: #81c784; font-size: 1.2em;"></div>
        </div>

        <!-- Todo List -->
        <div class="card">
            <h3>📋 Todo List</h3>
            <input type="text" id="todo-input" placeholder="Add a task..." onkeypress="if(event.key==='Enter') addTodo()">
            <button class="btn-success" onclick="addTodo()">Add Task</button>
            <ul id="todo-list" style="list-style: none; padding: 0; margin-top: 10px;"></ul>
        </div>
    </div>

    <script>
        // Counter
        let count = 0;
        function changeCount(delta) {
            if (delta === 0) count = 0;
            else count += delta;
            document.getElementById('counter-display').textContent = count;
        }

        // Calculator
        function calculate(op) {
            const n1 = parseFloat(document.getElementById('num1').value) || 0;
            const n2 = parseFloat(document.getElementById('num2').value) || 0;
            let result;
            switch(op) {
                case '+': result = n1 + n2; break;
                case '-': result = n1 - n2; break;
                case '*': result = n1 * n2; break;
                case '/': result = n2 !== 0 ? (n1 / n2).toFixed(4) : 'Error: divide by zero'; break;
            }
            document.getElementById('calc-result').textContent = `= ${'$'}{result}`;
        }

        // Todo List
        const todos = [];
        function addTodo() {
            const input = document.getElementById('todo-input');
            const text = input.value.trim();
            if (!text) return;
            todos.unshift({ text, done: false, id: Date.now() });
            input.value = '';
            renderTodos();
        }

        function toggleTodo(id) {
            const todo = todos.find(t => t.id === id);
            if (todo) todo.done = !todo.done;
            renderTodos();
        }

        function deleteTodo(id) {
            const idx = todos.findIndex(t => t.id === id);
            if (idx > -1) todos.splice(idx, 1);
            renderTodos();
        }

        function renderTodos() {
            const list = document.getElementById('todo-list');
            list.innerHTML = todos.map(t => `
                <li style="display:flex; justify-content:space-between; align-items:center;">
                    <span onclick="toggleTodo(${'$'}{t.id})" style="cursor:pointer; ${'$'}{t.done ? 'text-decoration:line-through; color:#666' : ''}">
                        ${'$'}{t.done ? '✅' : '⬜'} ${'$'}{t.text}
                    </span>
                    <button onclick="deleteTodo(${'$'}{t.id})" style="background:#e57373; color:white; border:none; border-radius:4px; padding:2px 8px; cursor:pointer;">✕</button>
                </li>
            `).join('');
        }
    </script>
</body>
</html>"""
        ),
        CodeExample(
            title = "Responsive Layout",
            description = "Build layouts that work on any screen",
            language = Language.HTML,
            code = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Responsive Portfolio</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        :root {
            --primary: #667eea;
            --secondary: #764ba2;
            --dark: #1a1a2e;
            --card: #16213e;
        }
        body { font-family: Arial, sans-serif; background: var(--dark); color: #e0e0e0; }

        /* Navigation */
        nav {
            background: rgba(22, 33, 62, 0.95);
            padding: 15px 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            position: sticky;
            top: 0;
            backdrop-filter: blur(10px);
        }
        .logo { font-size: 1.5em; font-weight: bold; background: linear-gradient(135deg, var(--primary), var(--secondary)); -webkit-background-clip: text; color: transparent; }
        .nav-links a { color: #a0aec0; text-decoration: none; margin-left: 20px; }
        .nav-links a:hover { color: var(--primary); }

        /* Hero */
        .hero {
            text-align: center;
            padding: 60px 20px;
            background: linear-gradient(135deg, #667eea22, #764ba222);
        }
        .hero h1 { font-size: 2.5em; margin-bottom: 10px; }
        .hero h1 span { background: linear-gradient(135deg, var(--primary), var(--secondary)); -webkit-background-clip: text; color: transparent; }
        .hero p { color: #a0aec0; margin-bottom: 20px; max-width: 400px; margin: 0 auto 20px; }
        .btn { padding: 12px 30px; background: linear-gradient(135deg, var(--primary), var(--secondary)); color: white; border: none; border-radius: 25px; cursor: pointer; font-size: 1em; }

        /* Projects Grid */
        .section { padding: 40px 20px; }
        .section h2 { text-align: center; color: var(--primary); margin-bottom: 30px; }
        .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; }
        .project-card {
            background: var(--card);
            border-radius: 12px;
            overflow: hidden;
            transition: transform 0.3s;
        }
        .project-card:hover { transform: translateY(-5px); }
        .card-img { height: 120px; display: flex; align-items: center; justify-content: center; font-size: 3em; }
        .card-body { padding: 15px; }
        .card-body h3 { color: #e0e0e0; margin-bottom: 8px; }
        .card-body p { color: #a0aec0; font-size: 0.9em; }
        .tag { display: inline-block; background: #1a1a3e; padding: 3px 10px; border-radius: 12px; font-size: 0.75em; color: var(--primary); margin: 3px 3px 0 0; }
    </style>
</head>
<body>
    <nav>
        <div class="logo">PocketDev</div>
        <div class="nav-links">
            <a href="#">Home</a>
            <a href="#">Projects</a>
            <a href="#">About</a>
        </div>
    </nav>

    <div class="hero">
        <h1>Learn to <span>Code</span> 🚀</h1>
        <p>Start your programming journey with PocketDev — code anywhere, anytime!</p>
        <br>
        <button class="btn">Get Started</button>
    </div>

    <div class="section">
        <h2>🛠️ Featured Projects</h2>
        <div class="grid">
            <div class="project-card">
                <div class="card-img" style="background: linear-gradient(135deg, #667eea22, #764ba222)">🐍</div>
                <div class="card-body">
                    <h3>Python Calculator</h3>
                    <p>A command-line calculator with advanced operations.</p>
                    <span class="tag">Python</span>
                    <span class="tag">Math</span>
                </div>
            </div>
            <div class="project-card">
                <div class="card-img" style="background: linear-gradient(135deg, #f6d36544, #fda08544)">🟨</div>
                <div class="card-body">
                    <h3>Todo App</h3>
                    <p>An interactive todo list with local storage.</p>
                    <span class="tag">JavaScript</span>
                    <span class="tag">HTML</span>
                </div>
            </div>
            <div class="project-card">
                <div class="card-img" style="background: linear-gradient(135deg, #4fc3f744, #81c78444)">🌐</div>
                <div class="card-body">
                    <h3>Portfolio Site</h3>
                    <p>A responsive portfolio website like this one!</p>
                    <span class="tag">HTML</span>
                    <span class="tag">CSS</span>
                </div>
            </div>
        </div>
    </div>
</body>
</html>"""
        )
    )

    fun getAllExamples(): List<CodeExample> = pythonExamples + javascriptExamples + htmlExamples

    fun getExamplesForLanguage(language: Language): List<CodeExample> {
        return when (language) {
            Language.PYTHON -> pythonExamples
            Language.JAVASCRIPT -> javascriptExamples
            Language.HTML -> htmlExamples
            else -> emptyList()
        }
    }
}
