## Setup Instructions

This project is an Android application built with **Kotlin** and **Android Studio**, using **Firebase** for authentication and data storage.

---

### 1. Prerequisites

Make sure you have the following installed:

- **Android Studio** (latest stable version recommended)
- **JDK 17** (typically bundled with Android Studio)
- **Android SDK** (installed via Android Studio)

---

### 2. Clone the Repository

```bash
git clone git@github.com:Freemankn/firebase-reddit-app.git
cd firebase-reddit-app
```

### 3. Open in Android Studio
- Open Android Studio
- Click "Open"
- Select the project folder you just cloned
- Wait for Gradle sync to complete

If prompted:
- Click "Trust Project"
- Click "Sync Now"

### 4. Firebase Setup

This project uses Firebase, but the `google-services.json` file is not included for security reasons.

To run the project:

1. Create your own Firebase project
2. Register an Android app in Firebase
3. Download `google-services.json`
4. Place it in:

```text
app/google-services.json
```
### 5. Enable Firebase Services

In the Firebase Console:

- Authentication
    - Enable sign-in methods (e.g., Email/Password)
- Firestore Database
    - Create a database (start in test mode for development)


### 6. Build and Run the App
- Connect a physical Android device or start an emulator
- Click Run ▶ in Android Studio

## Notes
- The app will not run without a valid google-services.json
- The package name must match Firebase exactly
- Do not commit google-services.json to version control