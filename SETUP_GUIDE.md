# TeamSync Setup Guide

Complete step-by-step guide to set up and run the TeamSync Android application.

## Step 1: Install Required Software

### Android Studio
1. Download Android Studio from: https://developer.android.com/studio
2. Install Android Studio Otter 2025.2.1 or later
3. During installation, make sure to install:
   - Android SDK
   - Android SDK Platform
   - Android Virtual Device (AVD)

### Java Development Kit (JDK)
- Android Studio typically includes JDK 17
- Verify by going to: File → Project Structure → SDK Location

## Step 2: Firebase Setup

### Create Firebase Project

1. **Go to Firebase Console**
   - Visit: https://console.firebase.google.com/
   - Sign in with your Google account

2. **Create a New Project**
   - Click "Add project"
   - Enter project name: "TeamSync" (or any name you prefer)
   - Accept terms and click "Continue"
   - Disable Google Analytics (optional for this project)
   - Click "Create project"

3. **Add Android App to Firebase**
   - In your Firebase project, click the Android icon
   - Register app with these details:
     - **Android package name:** `com.student.teamsync`
     - **App nickname:** TeamSync (optional)
     - **Debug signing certificate SHA-1:** Leave blank for now
   - Click "Register app"

4. **Download Configuration File**
   - Download the `google-services.json` file
   - This file contains your Firebase configuration
   - **IMPORTANT:** Keep this file secure, don't share it publicly

5. **Enable Authentication**
   - In Firebase Console, go to "Authentication" from the left menu
   - Click "Get started"
   - Click on "Sign-in method" tab
   - Enable "Email/Password" provider
   - Click "Save"

6. **(Optional) Set up Firestore**
   - Go to "Firestore Database" from the left menu
   - Click "Create database"
   - Choose "Start in test mode" (for development)
   - Select a Cloud Firestore location
   - Click "Enable"

## Step 3: Import Project into Android Studio

### Option A: From ZIP file
1. Extract the TeamSync.zip file
2. Open Android Studio
3. Click "Open" (not "New Project")
4. Navigate to the extracted TeamSync folder
5. Click "OK"

### Option B: From source files
1. Copy all project files to a folder named "TeamSync"
2. Open Android Studio
3. Click "Open"
4. Select the TeamSync folder
5. Click "OK"

## Step 4: Add Firebase Configuration

1. **Locate the google-services.json file**
   - This is the file you downloaded from Firebase Console

2. **Add to Project**
   - In Android Studio, switch to "Project" view (top-left dropdown)
   - Navigate to `TeamSync/app/` folder
   - Copy your `google-services.json` file here
   - The final path should be: `TeamSync/app/google-services.json`

3. **Verify Placement**
   - The file should be in the same directory as `build.gradle` (app-level)
   - If placed correctly, you'll see it in the app folder in Project view

## Step 5: Configure SDK Path

1. **Find your Android SDK location**
   - In Android Studio: File → Settings (or Preferences on Mac)
   - Go to: Appearance & Behavior → System Settings → Android SDK
   - Copy the "Android SDK Location" path

2. **Update local.properties**
   - Open the file `TeamSync/local.properties`
   - Replace `/path/to/your/Android/sdk` with your actual SDK path
   - Example:
     ```
     sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
     ```
   - On Windows, use double backslashes (\\) or forward slashes (/)
   - On Mac/Linux, use forward slashes (/)

## Step 6: Sync and Build Project

1. **Sync Gradle**
   - In Android Studio, click on "Sync Project with Gradle Files" button
   - Or: File → Sync Project with Gradle Files
   - Wait for the sync to complete (this may take a few minutes)

2. **Resolve Any Errors**
   - If you see errors, check:
     - Is `google-services.json` in the correct location?
     - Is your internet connection working?
     - Are all required SDK components installed?

3. **Build the Project**
   - Click Build → Make Project
   - Or press Ctrl+F9 (Cmd+F9 on Mac)
   - Wait for the build to complete

## Step 7: Run the Application

### Option A: Use Android Emulator

1. **Create Virtual Device**
   - Click on "Device Manager" icon (phone with Android logo)
   - Click "Create Device"
   - Select a device definition (e.g., Pixel 4)
   - Click "Next"
   - Select a system image (API 24 or higher, recommended: API 33)
   - Download the system image if needed
   - Click "Next" then "Finish"

2. **Start Emulator**
   - Select your virtual device
   - Click the play button to start the emulator
   - Wait for the emulator to fully boot

3. **Run App**
   - Click the "Run" button (green triangle) in Android Studio
   - Or press Shift+F10
   - Select your emulator from the list
   - Click "OK"

### Option B: Use Physical Device

1. **Enable Developer Options on Your Phone**
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Developer options will be enabled

2. **Enable USB Debugging**
   - Go to Settings → Developer Options
   - Enable "USB Debugging"

3. **Connect Your Device**
   - Connect your phone to computer via USB
   - Allow USB debugging when prompted on phone
   - Select "Always allow from this computer"

4. **Run App**
   - Click the "Run" button in Android Studio
   - Select your physical device
   - Click "OK"

## Step 8: Test the Application

### Create Test Account

1. **Launch App**
   - The app should open to the Login screen

2. **Sign Up**
   - Click "Sign up" text at the bottom
   - Fill in the form:
     - Full name: Your name
     - Email: test@teamsync.com
     - Password: test123456
     - Confirm Password: test123456
   - Click "Sign Up" button

3. **Automatic Login**
   - After successful signup, you'll be automatically logged in
   - You should see the main dashboard

### Test Features

1. **Explore Task List**
   - You should see sample tasks
   - Try checking/unchecking tasks
   - Click "Add Task" (placeholder message will show)

2. **Check Team View**
   - Tap the "Team" icon in bottom navigation
   - View team member accountability metrics

3. **Test Other Tabs**
   - Chats (UI only, database not connected)
   - Files (UI only, database not connected)

4. **Test Logout**
   - Click the logout icon (top right)
   - You should be returned to login screen

5. **Test Login**
   - Use the credentials you created during signup
   - Click "Login"
   - You should be logged in successfully

## Common Issues and Solutions

### Issue: "google-services.json is missing"
**Solution:**
- Verify the file is in `TeamSync/app/` directory
- Check the file name is exactly `google-services.json`
- Re-download from Firebase Console if needed

### Issue: "Sync failed: SDK location not found"
**Solution:**
- Open `local.properties`
- Update `sdk.dir` with your actual Android SDK path
- Use proper path format for your OS

### Issue: Build fails with dependency errors
**Solution:**
- Make sure you have internet connection
- File → Invalidate Caches → Restart
- Sync project again

### Issue: App crashes on startup
**Solution:**
- Check Logcat for detailed error messages
- Verify Firebase Authentication is enabled
- Ensure app package name matches Firebase configuration

### Issue: Authentication fails
**Solution:**
- Verify Email/Password is enabled in Firebase Console
- Check if `google-services.json` is correctly placed
- Ensure device/emulator has internet connection

### Issue: Emulator is slow
**Solution:**
- Use x86 system image (faster than ARM)
- Enable hardware acceleration in BIOS
- Allocate more RAM to emulator in AVD settings
- Use a physical device instead

## Next Steps

After successfully running the app:

1. **Explore the Code**
   - Review activity files in `activities/` folder
   - Check fragment implementations
   - Study the authentication flow

2. **Future Enhancements**
   - Implement SQLite database for offline storage
   - Add Firebase Firestore for real-time sync
   - Implement actual chat functionality
   - Add file upload feature

3. **Customize**
   - Modify colors in `res/values/colors.xml`
   - Update strings in `res/values/strings.xml`
   - Enhance UI layouts

## Support

For issues or questions:
- Check Android Studio Logcat for error messages
- Review Firebase Console for authentication logs
- Consult Android Developer documentation: https://developer.android.com/

## Project Information

- **Course:** IT57 Human Computer Interaction
- **Section:** BSIT3B
- **Team Members:**
  - Lincuna, Eden Grace R.
  - Matugas, Christine Fel A.
  - Sabido, Irish
  - Sobradil, Aliyah Margaret
