# Running-Tracker-App

## Overview

This Running Tracker App is an Android application developed in Java, designed to empower users to track, save, visualiae, and analyse their running data across multiple sessions. It adheres to the MVVM (Model-View-ViewModel) design pattern, ensuring scalability, maintainability, and reliability. Here's a brief overview of its key features and technologies used:

## Features

1. Tracking Runs
   The app allows users to initiate and track their running sessions in real-time using the device's GPS capabilities. It ensures accurate location tracking by integrating Google Play services location API.

2. Visualising Routes
   Users can visualise their running routes on an interactive map, thanks to the integration of the Google Maps SDK for Android. This feature provides a clear overview of their running path.

3. Saving Run Data
   The app saves essential run statistics, including distance, duration, and route information, in an SQLite database. To enhance data retrieval and manipulation, a ContentProvider is used, ensuring a seamless user experience.

## Installation

Clone the repository to your local machine.
Open the project in Android Studio.
Build and run the app on an Android emulator or a physical device with GPS capabilities.

## Getting Started

Launch the app on your Android device.
Start a new running session by clicking the "Start Run" button.
During the run, your route will be tracked and displayed on the map.
Once the run is complete, save the session, and your data will be stored in the SQLite database.

## Requirements

Android Studio (latest version)
Android SDK (minimum API level 21)

## Technologies Used

Java for Android app development.
MVVM design pattern for improved scalability and maintainability.
Google Play services location API for accurate location tracking.
Google Maps SDK for Android for interactive route visualisation.
SQLite database with ContentProvider for efficient data storage and retrieval.
