# Legendary-Looks-mobile-app
Legendary Looks is a multi-tenant mobile booking application designed to help independent beauty professionals manage appointments, services, and clients digitally.
The platform allows beauticians to operate their businesses through a centralized system while enabling clients to easily browse services and book appointments through a mobile application.
---
## Problem
Many independent beauty professionals rely on manual booking methods such as phone calls, text messages, or handwritten appointment books. This often leads to:
- Scheduling conflicts
- Poor client communication
- Lack of centralized client history
- Administrative inefficiency
---
## Solution
Legendary Looks provides a centralized mobile platform where:
- Clients can browse available services and book appointments
- Beauty professionals can manage services, schedules, and clients
- Appointment data is stored securely in the cloud
- Businesses can operate independently within the same platform
The system uses a **multi-tenant architecture**, allowing multiple beauticians to use the same system while keeping their data isolated.
---
## Technologies Used
# Mobile Client Application
- Kotlin
- Android Studio
- Material Design UI
# Admin Dashboard
- JavaScript
- HTML / CSS
# Backend
- Firebase Firestore
- Firebase Authentication
# Development Tools
- Git
- GitHub
- Agile Development Methodology
---
# System Architecture
The application follows a **multi-tenant SaaS architecture**, where:
- Multiple businesses share the same infrastructure
- Each tenant's data is isolated using a unique tenant ID
- Clients and administrators have different system views and permissions
This architecture allows the system to remain scalable, secure, and cost-efficient.
---
# Core Features
- User authentication (Admin and Client roles)
- Appointment booking system
- Service management
- Client history tracking
- Role-based login system
- Booking conflict detection
- Data isolation between tenants
---
## My Contribution
This project was developed as a **collaborative group project**.
My main contributions included:
- Developing the **mobile client application using Kotlin**
- Designing and implementing **user interface screens** in Android Studio
- Implementing **navigation flows for appointment booking**
- Connecting the mobile client to the **Firebase backend**
- Assisting with development of the **JavaScript admin dashboard**
- Helping integrate booking data between the client and admin systems
---
## Challenges Solved
# Conflicting Booking Time Slots
Implemented a system that checks the database before confirming bookings to prevent multiple users from selecting the same time slot.
# Role-Based Login
Developed logic to separate **admin and client logins**, ensuring users are redirected to the correct interface.
# Data Isolation
Implemented tenant-based data filtering to ensure businesses only see their own appointments and clients.
---
## Future Improvements
Potential improvements for future versions include:
- Multi-vendor marketplace for beauty professionals
- Integrated online payment system
- Advanced service search functionality
- Analytics dashboard for business insights
---
## Project Status
This project was developed as part of an academic group project and demonstrates the application of **mobile development, backend integration, and system architecture principles**.
---
## License
This project is for educational purposes.
