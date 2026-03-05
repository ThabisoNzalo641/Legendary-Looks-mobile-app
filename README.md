# Legendary-Looks-mobile-app
Legendary Looks is a mobile booking application designed to help independent beauty professionals manage their services, appointments, and clients more efficiently.
The application provides a centralized platform where clients can browse available services and book appointments, while service providers can manage their schedules and customer interactions. The system is designed to reduce scheduling conflicts, improve client communication, and streamline daily business operations.
This project was developed as part of a collaborative software engineering group project.
---
## Problem
Many independent beauty professionals rely on manual booking methods such as phone calls, messaging apps, or handwritten appointment books. These approaches often result in:
- Scheduling conflicts  
- Poor client communication  
- Lack of centralized client records  
- Administrative inefficiencies  
---
## Solution
Legendary Looks provides a digital solution that allows beauty professionals to manage appointments and services through a mobile application.
The system offers:
- Centralized appointment booking
- Service management
- Client history tracking
- Secure cloud-based data storage
- Improved scheduling reliability
---
## Technologies Used
### Mobile Application
- Java  
- Kotlin  
- Android Studio  
- Material Design Components  
### Backend
- Firebase Firestore  
- Firebase Authentication  
### Development Tools
- Git  
- GitHub  
- Android Emulator / Physical Android Device  
---
## Core Features
- User authentication and login
- Appointment booking system
- Service catalog browsing
- Client record management
- Booking conflict detection
- Role-based access within the application
- Firebase cloud data storage
---
## System Architecture
The application follows a layered architecture consisting of:
### Presentation Layer
The Android mobile application developed using Java and Kotlin. This layer provides the user interface and allows clients to interact with the system.
### Business Logic Layer
Handles the booking workflows, validation rules, and application logic such as preventing conflicting booking times.
### Data Layer
Uses Firebase Firestore and Firebase Authentication to securely manage user accounts and store appointment data.
---
## Project Structure
Legendary-Looks-mobile-app

java/  
Contains the application logic written in Java and Kotlin, including UI components, models, and Firebase integration.

res/  
Contains Android resources such as layouts, drawables, and UI styling values used by the application.

AndroidManifest.xml  
Defines application configuration, permissions, and activity declarations.
---
## My Contribution
This project was developed as a collaborative group project.
My main contributions included:
- Developing parts of the mobile client application using Java and Kotlin
- Designing and implementing frontend user interface screens
- Implementing navigation flows for the appointment booking system
- Integrating the mobile application with Firebase backend services
- Assisting with implementing booking validation logic and data synchronization
---
## How to Run the Application
### Prerequisites
Ensure the following software is installed:
- Android Studio
- Java Development Kit (JDK)
- Git
- Android Emulator or physical Android device
- Internet connection (required for Firebase services)
---
### 1. Clone the Repository
Clone the project from GitHub:
git clone https://github.com/ThabisoNzalo641/Legendary-Looks-mobile-app.git
---
### 2. Open the Project in Android Studio
1. Open Android Studio  
2. Select Open an Existing Project  
3. Navigate to the following folder:
Legendary-Looks-mobile-app/LL/bookingApp
4. Select the bookingApp folder and open it.
---
### 3. Sync Gradle
Once the project is opened:
- Click Sync Project with Gradle Files
- Allow Android Studio to download required dependencies.
---
### 4. Configure Firebase
Ensure the Firebase configuration file is present in the project:
app/google-services.json
If the file is missing, it must be downloaded from the Firebase Console and added to the app directory.
---
### 5. Run the Application
1. Start an Android emulator or connect a physical Android device.
2. Click the Run ▶ button in Android Studio.
3. Select your device or emulator.
4. The application will build and launch automatically.
---
## Development Approach
The project was developed using Agile software development principles, with collaborative teamwork and version control managed through Git and GitHub.
---
## Project Status
This project was developed for academic purposes as part of a software engineering project and demonstrates practical application of mobile development, backend integration, and collaborative development practices.

