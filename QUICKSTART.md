# TeamSync - Quick Start Guide

## What You Have

Your complete Android Studio project for TeamSync is ready! You have:

1. **TeamSync.zip** - Complete project archive
2. **TeamSync/** folder - Full project structure with all files

## Before You Begin - CRITICAL STEP

⚠️ **You MUST add a Firebase configuration file before the app will run!**

### Get Your Firebase Configuration

1. Go to https://console.firebase.google.com/
2. Create a new project (or use existing)
3. Add an Android app with package name: `com.student.teamsync`
4. Download the `google-services.json` file
5. Place it in the `TeamSync/app/` directory

**Without this file, the project will not compile!**

## 3-Minute Setup

### Step 1: Open in Android Studio (1 min)
- Extract TeamSync.zip (if using zip)
- Open Android Studio
- Click "Open" → Select TeamSync folder

### Step 2: Add Firebase File (1 min)
- Copy your `google-services.json` to `TeamSync/app/`
- Enable Email/Password authentication in Firebase Console

### Step 3: Run (1 min)
- Click "Sync Project with Gradle Files"
- Click "Run" button (green triangle)
- Create a test account and explore!

## What Works Right Now

✅ **Fully Functional:**
- User Registration (Sign Up)
- User Login
- Session Management
- Logout
- Task List View (with sample data)
- Team Accountability View (with sample data)
- Bottom Navigation
- All UI screens

🚧 **Coming Soon (needs database):**
- Creating/editing tasks
- Real-time chat
- File uploads
- Multi-project support

## File Structure Overview

```
TeamSync/
├── app/
│   ├── src/main/
│   │   ├── java/              # All Java code
│   │   ├── res/               # UI layouts, images, strings
│   │   └── AndroidManifest.xml
│   ├── build.gradle           # App dependencies
│   └── [google-services.json] # ADD THIS FILE HERE!
├── build.gradle               # Project config
├── README.md                  # Full documentation
├── SETUP_GUIDE.md            # Detailed setup instructions
└── QUICKSTART.md             # This file

```

## Test Credentials

After signing up with your own credentials, you can test:
- Email: anything@example.com
- Password: minimum 6 characters

## Need Help?

- **Detailed Instructions:** See SETUP_GUIDE.md
- **Full Documentation:** See README.md
- **Build Issues:** Check that google-services.json is in the right place
- **Auth Issues:** Verify Firebase Authentication is enabled

## Technologies Used

- Java 17
- Android SDK 24+
- Firebase Authentication
- Material Design Components
- RecyclerView for lists

## Your Team

- Lincuna, Eden Grace R.
- Matugas, Christine Fel A.
- Sabido, Irish
- Sobradil, Aliyah Margaret

**Section:** BSIT3B
**Course:** IT57 Human Computer Interaction

---

**Ready to code!** 🚀

The app is production-ready for authentication and basic UI. Database integration is the next step to unlock full functionality.
