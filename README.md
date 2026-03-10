# ScanX - AR Product Detection

An Android application developed to accurately identify products on a retail shelf and visually mark them using live on-camera Augmented Reality (AR). 

## 🚀 Core Features

* **Real-Time AR Overlay:** Uses a custom Canvas overlay mapped to the live camera feed to draw green bounding boxes and checkmarks directly over detected products. Processing happens 100% on-device and live on-camera.
* **Continuous Scanning:** Utilizes Google ML Kit's Object Detection in `STREAM_MODE` to continuously identify newly visible products as the user pans the camera.
* **Smart Non-Duplication Logic:** Implements a custom "Spatial Memory" algorithm using bounding box intersection heuristics. If the camera pans away and returns, the system recognizes the spatial coordinates of previously scanned items, preventing duplicate tracking IDs and re-marking.
* **UX Enhancements:** Features haptic feedback upon new product detection and a "Reset Scan" button to quickly clear the session without restarting the app.

## 🛠️ Tech Stack

* **Language:** Kotlin
* **Camera API:** Google CameraX (Preview & ImageAnalysis use cases)
* **Machine Learning:** Google ML Kit (Object Detection & Tracking API)
* **UI/AR:** Custom `View` with Android `Canvas` for precise coordinate mapping.

## 🧠 How the Non-Duplication Logic Works

Because standard 2D ML trackers assign new IDs when an object leaves and re-enters the frame, this app maintains a memory of `KnownItem`s. When ML Kit detects a "new" ID, the app checks for spatial overlap (`Rect.intersects`) against previously known bounding boxes. If an overlap occurs, it assumes the tracker simply dropped the frame, updates the ID, and does *not* log it as a duplicate item.
