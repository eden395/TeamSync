# TeamSync - Student Project Management Tracker

A comprehensive Android application for managing student projects with team collaboration features.

## Features

- **User Authentication** - Firebase Authentication with email/password
- **Task Management** - Create, assign, and track tasks with priority levels
- **Team Collaboration** - Real-time team accountability and contribution metrics
- **File Sharing** - Upload and manage project files
- **Team Chat** - Communicate with team members
- **Responsive UI** - Clean, modern interface based on Material Design

## Prerequisites

- Android Studio Otter 2025.2.1 or later
- JDK 17
- Android SDK (API Level 24 or higher)
- Firebase account

## Setup Instructions

### 1. Clone or Open the Project

Open Android Studio and import this project directory.

### 2. Configure Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use an existing one
3. Add an Android app to your Firebase project
   - Package name: `com.student.teamsync`
4. Download the `google-services.json` file
5. Place it in the `app/` directory of this project

**IMPORTANT:** The app will not compile without a valid `google-services.json` file.

### 3. Enable Firebase Authentication

1. In Firebase Console, go to Authentication
2. Click "Get Started"
3. Enable "Email/Password" sign-in method

### 4. (Optional) Enable Firebase Firestore

For future database integration:
1. In Firebase Console, go to Firestore Database
2. Click "Create Database"
3. Start in test mode (or production mode with proper security rules)

### 5. Update local.properties

Edit `local.properties` file and set your Android SDK path:
```
sdk.dir=/path/to/your/Android/sdk
```

### 6. Sync and Build

1. In Android Studio, click "Sync Project with Gradle Files"
2. Wait for the sync to complete
3. Click "Run" or press Shift+F10

## Project Structure

```
TeamSync/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/student/teamsync/
│   │       │   ├── activities/
│   │       │   │   ├── LoginActivity.java
│   │       │   │   ├── SignUpActivity.java
│   │       │   │   └── MainActivity.java
│   │       │   ├── fragments/
│   │       │   │   ├── TasksFragment.java
│   │       │   │   ├── ChatsFragment.java
│   │       │   │   ├── FilesFragment.java
│   │       │   │   └── TeamFragment.java
│   │       │   ├── adapters/
│   │       │   │   ├── TaskAdapter.java
│   │       │   │   └── TeamMemberAdapter.java
│   │       │   ├── models/
│   │       │   │   ├── Task.java
│   │       │   │   └── TeamMember.java
│   │       │   └── utils/
│   │       │       └── SessionManager.java
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   ├── drawable/
│   │       │   ├── values/
│   │       │   └── menu/
│   │       └── AndroidManifest.xml
│   ├── build.gradle
│   └── google-services.json (You need to add this)
├── build.gradle
└── settings.gradle
```

## Key Technologies

- **Language:** Java 17
- **UI:** Android XML Layouts, Material Design Components
- **Architecture:** MVVM pattern with ViewModels
- **Authentication:** Firebase Authentication
- **Session Management:** SharedPreferences
- **Future Integration:** SQLite (offline), Firebase Firestore (realtime)

## Current Implementation Status

### ✅ Completed
- Login and Sign Up UI
- Firebase Authentication integration
- Session Management
- Main Dashboard with Bottom Navigation
- Tasks screen with sample data
- Team Accountability screen with sample data
- Chats screen (UI only)
- Files screen (UI only)
- Responsive layouts

### 🚧 Coming Soon (Requires Database Integration)
- Create/Edit/Delete tasks
- Assign tasks to team members
- Real-time chat functionality
- File upload and management
- Project creation and management
- Notifications
- SQLite offline storage
- Firebase Firestore sync

## Testing the App

### Test User Creation

1. Run the app
2. Click "Sign up" on the login screen
3. Fill in the form with:
   - Full name: Your name
   - Email: test@example.com
   - Password: test123 (minimum 6 characters)
   - Confirm Password: test123
4. Click "Sign Up"
5. You'll be automatically logged in

### Features to Test

- **Login/Logout:** Test authentication flow
- **Task List:** View sample tasks with different priorities
- **Team View:** See team member accountability metrics
- **Navigation:** Switch between tabs using bottom navigation

## Troubleshooting

### Build Issues

**Error: "google-services.json is missing"**
- Solution: Add your Firebase `google-services.json` file to the `app/` directory

**Error: "SDK location not found"**
- Solution: Update `local.properties` with your Android SDK path

**Gradle sync failed**
- Solution: Make sure you have internet connection
- Solution: File → Invalidate Caches → Restart

### Runtime Issues

**App crashes on startup**
- Check Logcat for error messages
- Verify Firebase configuration is correct
- Ensure minimum SDK version is met (API 24)

**Authentication not working**
- Verify Firebase Authentication is enabled
- Check package name matches in Firebase Console
- Ensure `google-services.json` is properly placed

## Team Members

- Lincuna, Eden Grace R.
- Matugas, Christine Fel A.
- Sabido, Irish
- Sobradil, Aliyah Margaret

**Section:** BSIT3B

## License

This project is for educational purposes as part of IT57 Human Computer Interaction course.
