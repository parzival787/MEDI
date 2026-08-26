# 💊 Medi - Smart Pharmacy & Healthcare App

[![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-ffca28.svg)](https://firebase.google.com)
[![ML Kit](https://img.shields.io/badge/AI-ML%20Kit-blue.svg)](https://developers.google.com/ml-kit)

**Medi** is a comprehensive Android application designed to bridge the gap between patients and pharmacies. It leverages AI-driven text recognition (OCR) for prescriptions, Generative AI for medical insights, and a robust real-time synchronization system for inventory and order management.

---

## 🔄 App Workflow & Logic

The application is built using a **Modular Activity-based Architecture** centered around a Firebase real-time ecosystem.

### 1. User Entry & Identity (`MainActivity`, `Signin` modules)
*   **Role-Based Access**: The app starts with `MainActivity` where users select their role. This bifurcates the experience into **Customer** and **Pharmacist** portals.
*   **Authentication Logic**: 
    *   **Customers**: `UserSignin` handles Firebase Auth with support for Google One-Tap Sign-In.
    *   **Pharmacists**: `PharmacySignin` manages secure login. Passwords for pharmacist accounts are hashed using **jBCrypt** for enhanced security.

### 2. The Customer Workflow (Procurement)
*   **Discovery**: Users browse medicines via `UserActivity`. Search queries are handled by `MedicineSearchUserAdapter`, filtering Firestore data in real-time.
*   **Smart Scanning (OCR)**: 
    *   The `Uploadprescription` module opens the camera, captures a prescription, and passes the image to **Google ML Kit**.
    *   **Logic**: The extracted text is parsed to automatically identify medicines available in the inventory, allowing users to add them directly to their cart.
*   **AI Integration**: Users can get medicine insights where the app consults **Google Gemini (Generative AI)** to provide immediate information about medications.
*   **Transaction**: Items are added to a `CartManager` (Singleton Pattern). Checkout is handled by `PaymentActivity`, integrating the **Razorpay** payment gateway for secure transactions.
*   **Orders**: `MyOrdersActivity` allows users to track their order history using the `MyOrdersAdapter`.

### 3. The Pharmacist Workflow (Supply Chain)
*   **Inventory Control**: Pharmacists use `addmedicine` to upload product data (images to Firebase Storage, metadata to Firestore). 
*   **Stock Management**: `ViewInventoryActivity` and `EditInventoryActivity` allow for real-time price updates and stock adjustments via `InventoryAdapter`.
*   **Order Fulfillment**: `OrdersReceivedActivity` listens for new orders. Pharmacists use `OrdersReceivedAdapter` to view and update the status of incoming requests.

---

## 🛠 Technical Implementation (The Codes)

### Core Components
| Module | Primary Responsibility | Key Classes |
| :--- | :--- | :--- |
| **Auth** | Identity & Security | `UserSignin.java`, `PharmacySignin.java` |
| **Inventory** | CRUD Operations | `addmedicine.java`, `ViewInventoryActivity.java`, `Medicine.java` |
| **Smart Engine** | OCR & Generative AI | `Uploadprescription.java`, `OcrResultsAdapter.java` |
| **Commerce** | Cart & Payments | `CartManager.java`, `CartActivity.java`, `PaymentActivity.java` |
| **Orders** | Tracking & History | `MyOrdersActivity.java`, `OrdersReceivedActivity.java`, `Order.java` |

### Data Models
*   **`Medicine.java`**: Defines the structure for drugs (Name, Price, Category, Image URL).
*   **`Order.java` & `PharmacistOrder.java`**: Structures for transaction data and fulfillment status.
*   **`CartItem.java`**: Manages the local state of items selected for purchase.

---

## 📂 Project Structure

*   **`com.example.medi`**: Contains all activity logic and business rules.
*   **Adapters**: Specialized classes (e.g., `CartAdapter`, `InventoryAdapter`) for binding Firestore data to UI.
*   **Resources**: 
    *   `layout/`: XML-based Material Design interfaces.
    *   `navigation/`: Defines the navigation graph for the app flow.

---

## 🚀 Installation & Setup

1.  **Clone the Repo**: `git clone https://github.com/parzival787/MEDI.git`
2.  **Firebase Configuration**: 
    *   Add `google-services.json` to the `app/` folder.
    *   Enable Email/Google Auth, Firestore, and Storage in the Firebase Console.
3.  **API Keys**: 
    *   Add your **Razorpay Key** in `PaymentActivity.java`.
    *   Add your **Gemini API Key** in the AI module.
4.  **Build**: Open in Android Studio Ladybug and Sync Gradle.

---

## 🤝 Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

---

## 📄 License
This project is for educational purposes as part of the Mobile App Development curriculum.

**Developer**: [parzival787](https://github.com/parzival787)
