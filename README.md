# 📚 Library Management System

## Overview

The **Library Management System** is a robust, Java-based application designed to efficiently manage:

- 📘 Books
- 👤 Users
- 📑 Loans
- ⭐ Reviews
- 💸 Fines
- 🔔 Notifications

This project includes a **Graphical User Interface (GUI)** and supports **CSV-based data persistence**, making it perfect for small-scale library operations.

---

## ✨ Features

### 📘 Book Management

- Add, update, and display books.
- Search for books by title or author.
- View the top three most popular books.

### 👤 User Management

- Add and manage users with roles (`ADMIN` or `MEMBER`).
- Switch between users and view individual data such as loans and fines.

### 📑 Loan Management

- Loan books to users and manage deadlines.
- Return books and update loan statuses.
- Automatically notify users about overdue loans.

### 💸 Fine Management

- Generate and manage fines for overdue books.
- Pay fines or modify fine details (admin-only functionality).

### ⭐ Review System

- Users can leave and view reviews for books (after borrowing).

### 🔔 Notifications

- Notify users of overdue books or fines.
- View unread notifications and mark them as read.

### 🌐 User Session Management

- Maintain the current user session globally using the `SessionManager` class.
- Track selected user ID and name for GUI operations.

### 📂 Data Persistence

- Store data for books, users, loans, fines, and reviews in CSV files.

---

## 🔧 Prerequisites

1. **Java JDK 22 or higher**

   - Install from [Oracle](https://www.oracle.com/java/technologies/javase-downloads.html).

2. **Visual Studio Code (VS Code)**

   - Download from [VS Code](https://code.visualstudio.com/).
   - Install extensions:
     - [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack).

3. **Git**

   - Install from [Git](https://git-scm.com/).

---

## 📂 Project Structure

```
src/
├── LibraryGUI.java         # Main GUI for user interaction
├── Main.java               # Console entry point for debugging
├── LibraryService.java     # Core service handling library operations
├── Library.java            # Main library class (manages books, users, loans, etc.)
├── AuditService.java       # Logs system actions
├── Book.java               # Book entity
├── Loan.java               # Loan entity
├── Fine.java               # Fine entity
├── FinesManager.java       # Manages fines
├── NotificationService.java # Manages notifications
├── Review.java             # Review entity
├── Search.java             # Provides book search functionality
├── CSVService.java         # Handles data persistence in CSV files
├── Exceptii.java           # Custom exception handling
├── User.java               # User entity with role support
├── UserRole.java           # Enum for user roles (ADMIN, MEMBER)
├── SessionManager.java     # Manages global user session
├── Date/                   # Directory for CSV data files
└── README.md               # Documentation (this file)
```

---

## 🚀 Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/username/LibraryManagementSystem.git
cd LibraryManagementSystem
```

### 2. Open in Visual Studio Code

```bash
code .
```

### 3. Configure Java Runtime

1. Install the Java extension in VS Code.
2. Open the command palette (`Ctrl+Shift+P`) and search for `Java: Configure Java Runtime`.
3. Set the JDK path to your installed Java version.

---

## 🖥️ Running the Application

### Using the Graphical User Interface (GUI)

1. Open `LibraryGUI.java` in VS Code.
2. Click **Run** or press `F5`.
3. Use the menu options to interact with the system:
   - Manage books, users, loans, fines, and notifications.

### Running Console-Based Tests

1. Open `Main.java` in VS Code.
2. Click **Run** or press `F5` to execute tests and debug functionality.

---

## 📊 CSV Files

All application data is stored in the following files:

- ``: Stores book details.
- ``: Stores user information.
- ``: Tracks loan records.
- ``: Maintains fine details.
- ``: Stores reviews left by users.

---

## 🌟 Development Highlights

### 🔔 Notifications

- Automatically notify users about overdue loans or unpaid fines.
- Notifications can be marked as read.

### 💸 Fine Management

- Fines are generated for overdue loans at a rate of \$5/day.
- Admin users can modify fine amounts or mark fines as paid.

### 📊 Top Books

- View the most popular books based on loan frequency and recent loan dates.

### ⚠️ Custom Exceptions

- Ensure robust error handling with exceptions like:
  - `BookNotFound`
  - `UnauthorizedAccessException`
  - `FineAlreadyPaidException`

### 🌐 User Role Management

- `ADMIN`: Full control over library operations, including adding books and modifying fines.
- `MEMBER`: Limited access to borrowing and reviewing books.

