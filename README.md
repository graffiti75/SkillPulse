# SkillPulse

A real-time task management app built with Android, Firebase and Jetpack Compose.

## 📱 About

SkillPulse is a modern Android application designed for efficient task management with real-time synchronization capabilities. Built using the latest Android development technologies, it provides a smooth and intuitive user experience for managing your daily tasks and workflows.

## Demo

![SkillPulse Demo](https://raw.githubusercontent.com/graffiti75/SkillPulse/refs/heads/master/media/Screen%20Recording%202026-02-06%20at%2012.11.57.gif)

Also, you can check out the full video [here](https://www.youtube.com/shorts/j1Gzt7mbsvU).

## ✨ Features

- 🔄 **Real-time synchronization** - Tasks sync instantly across devices using Firebase Firestore
- 📝 **Task management** - Create, update, and delete tasks with ease
- 🎨 **Modern UI** - Built with Jetpack Compose for a beautiful, responsive interface
- 🔐 **Secure authentication** - Firebase Authentication integration
- 📊 **Task tracking** - Monitor your productivity and task completion

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Backend:** Firebase
  - Firestore Database
  - Firebase Authentication
- **IDE:** Android Studio
- **Architecture:** MVVM (Model-View-ViewModel)

## 🚀 Getting Started

### Prerequisites

- Android Studio (latest version)
- Android SDK
- Firebase account
- Minimum SDK: 24 (Android 7.0)

### Installation

1. Clone the repository
```bash
git clone https://github.com/graffiti75/SkillPulse.git
```

2. Open the project in Android Studio

3. Set up Firebase:
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app to your Firebase project
   - Download the `google-services.json` file
   - Place it in the `app/` directory

4. Sync the project with Gradle files

5. Run the app on an emulator or physical device

## 📂 Project Structure
```
SkillPulse/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   └── ...
│   └── build.gradle.kts
├── gradle/
├── scripts/
└── build.gradle.kts
```

## 🔧 Configuration

Make sure to add your `google-services.json` file to the `app/` directory. This file contains your Firebase configuration and should **not** be committed to version control.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

## 👤 Author

**Rodrigo Cericatto**
- GitHub: [@graffiti75](https://github.com/graffiti75)

## 🙏 Acknowledgments

- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Powered by [Firebase](https://firebase.google.com/)
- Made with ❤️ using Kotlin

---

**Note:** Remember to keep your Firebase credentials secure and never commit `google-services.json` to public repositories.
