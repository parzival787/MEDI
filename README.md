# 💊 Medi - Smart Pharmacy & Healthcare App

[![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-ffca28.svg)](https://firebase.google.com)
[![ML Kit](https://img.shields.io/badge/AI-ML%20Kit-blue.svg)](https://developers.google.com/ml-kit)

**Medi** is a powerful Android application designed to modernize the pharmacy experience. It connects users with pharmacists, featuring AI-driven prescription scanning, real-time inventory tracking, and secure digital payments.

---

## 🌟 Key Features

### 👤 Customer Experience
*   **🔍 Smart Search**: Instant medicine lookup with detailed descriptions and pricing.
*   **📄 OCR Prescription Scanner**: Powered by **Google ML Kit**, users can scan physical prescriptions to automatically extract medicine names.
*   **🤖 AI Assistance**: Integrated with **Google Generative AI (Gemini)** to provide helpful health insights and medicine information.
*   **🛒 Seamless Cart**: Add multiple medicines to a persistent cart and manage quantities easily.
*   **💳 Secure Payments**: Fully integrated with **Razorpay** for safe and fast online transactions.
*   **📦 Order Tracking**: Real-time status updates from "Order Placed" to "Delivered".

### 👨‍⚕️ Pharmacist Dashboard
*   **📋 Inventory Control**: Add new stock, update prices, or remove expired medications with a clean interface.
*   **⚡ Real-time Orders**: Receive instant notifications when a customer places an order.
*   **✅ Order Fulfillment**: Streamlined workflow to accept, process, and complete incoming requests.

---

## 🛠 Tech Stack

| Category | Tools/Libraries |
| :--- | :--- |
| **Language** | Java |
| **UI** | Material Design Components, XML, ViewBinding |
| **Backend** | Firebase Auth, Firestore, Firebase Storage |
| **AI/ML** | Google ML Kit (Text Recognition), Google Generative AI (Gemini) |
| **Payments** | Razorpay SDK |
| **Networking** | Volley, Glide (Image Loading) |
| **Security** | jBCrypt (Password Hashing), Credential Manager |

---

## 🚀 Installation & Setup

### Prerequisites
*   Android Studio (Ladybug or newer)
*   JDK 17
*   A Firebase Project

### Steps
1.  **Clone the Repo**
    ```bash
    git clone https://github.com/parzival787/MEDI.git
    ```
2.  **Firebase Configuration**
    *   Download `google-services.json` from your Firebase Console.
    *   Place it in the `app/` directory.
    *   Enable **Email/Google Auth**, **Firestore**, and **Storage**.
3.  **API Keys**
    *   Add your Razorpay Key to the payment module.
    *   Add your Gemini API Key for AI features.
4.  **Run the App**
    *   Sync Gradle and run on a physical device or emulator (API 24+).

---

## 📸 Screenshots
*(Coming Soon - Add your app screenshots here!)*

---

## 📄 License
This project is for educational purposes as part of the Mobile App Development curriculum.

---

## 🤝 Contact
**Project Maintainer**: [parzival787](https://github.com/parzival787)
