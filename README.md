# Event Organizer App

## Project Description

Event Organizer is a Kotlin-based Android application built using the MVVM architecture and clean coding principles. This app is designed to allow users to seamlessly create, manage, and join events with features tailored for both admins and attendees. Users can authenticate via Firebase, view event details, join events, and get notified about upcoming events. This application also supports user profile management, event location mapping, and includes local data persistence and synchronization with Firebase Firestore.

---

## Table of Contents
1. [Features](#features)
2. [Tech Stack](#tech-stack)
3. [Architecture](#architecture)
4. [Setup and Installation](#setup-and-installation)
5. [Code Structure](#code-structure)
6. [Important Concepts and Practices](#important-concepts-and-practices)
7. [Screenshots](#screenshots)
8. [Project Evaluation](#project-evaluation)
9. [License](#license)

---

## Features

### 1. User Authentication
- **Firebase Authentication** for secure sign-up, login, and password reset.
- Role-based user sign-up differentiates between **Admin** and **Attendee** roles.

### 2. User Roles
- **Admin**:
  - Create, edit, and delete events.
  - View and manage all events and attendees.
- **Attendee**:
  - View, join, and leave events.
  - View their joined events in their profile.

### 3. Event Management
- **Admin**:
  - Event creation with details like title, description, date, time, location, and category.
  - Image upload support.
- **Attendee**:
  - View a list of upcoming events with search and filter functionalities.
  - Join or leave events and see event details on a map.

### 4. User Profile
- View and edit user profile information.
- Display lists of events the user has created or joined.

### 5. Notifications
- Local notifications to remind users of upcoming events.
- Option to enable or disable notifications for each event.

### 6. Data Persistence
- Local data storage using Room.
- Firebase Firestore for data synchronization.

### 7. Responsive UI
- Auto layout to support various screen sizes and orientations.

---

## Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM with Clean Architecture
- **Firebase**: Authentication, Firestore
- **UI**: Jetpack Compose (optional) or traditional Views
- **Data Persistence**: Room database for offline caching
- **Networking**: Retrofit with Coroutine support for asynchronous calls
- **Dependency Injection**: Hilt
- **Local Notifications**: Android Notification Manager
- **Mapping**: Google Maps API for event location display
- **GitHub**: Version control with feature branching, pull requests, and code reviews.

---

## Architecture

This project follows the MVVM (Model-View-ViewModel) pattern with clean architecture principles:
1. **Model**: Responsible for data management and logic, with entities representing core data structures.
2. **ViewModel**: Manages UI-related data and business logic to prepare it for the view.
3. **View**: Observes data changes from the ViewModel and updates the UI.

---

## Setup and Installation

### Prerequisites
- **Android Studio Bumblebee (or later)**
- **Firebase Project Setup**: Configure Firebase Authentication and Firestore.

### Steps
1. Clone the repository.
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Add the `google-services.json` file to the project.
5. Configure Firebase Authentication and Firestore rules.
6. Build and run the app on an emulator or device.

---

## Code Structure

├── data │ ├── model # Data models (User, Event) │ ├── repository # Data repositories (Firebase, Room) ├── di # Hilt DI modules ├── ui # UI components │ ├── view # Activities and Fragments │ ├── viewmodel # ViewModels │ ├── adapters # RecyclerView adapters ├── utils # Utility classes and constants ├── service # Services like notifications, data sync, etc. └── network # Network layer for API calls

---

## Important Concepts and Practices

### 1. Observers and Flows
- **LiveData** and **StateFlow** are used to observe data changes from the ViewModel to the View, ensuring efficient state management.

### 2. Data Synchronization and Local Storage
- **Room Database** is implemented to cache data offline.
- **Firestore Syncing** ensures real-time data updates for events.
- **WorkManager** handles background data sync tasks.

### 3. Broadcast Receivers
- Custom BroadcastReceivers handle actions like connectivity changes to manage data synchronization seamlessly.

### 4. Notifications
- Local notifications are sent to remind users of upcoming events.
- Notifications are handled with **Notification Manager** and scheduled using **WorkManager** for specific event reminders.

### 5. Networking
- Retrofit with Coroutines to perform API calls efficiently.
- A repository pattern manages data retrieval and updates from both Room and Firestore.

---

## Project Evaluation

### Functionality
- All features are implemented as specified and rigorously tested to ensure no crashes or critical bugs.

### Architecture
- MVVM with a clean architecture is implemented, following best practices for Android development.

### Code Quality
- The code follows Kotlin best practices, with clear naming conventions, reusable components, and proper error handling.

### User Interface
- Responsive UI with adaptable layouts that look good in both portrait and landscape orientations.

### Git Usage
- Follows Git best practices with feature branching, clean commit messages, and organized pull requests.


--- 

### Note
Feel free to reach out with questions or for assistance in setting up this project!
