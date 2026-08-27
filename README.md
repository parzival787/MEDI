# 💊 Medi - Smart Pharmacy & Healthcare App

[![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-ffca28.svg)](https://firebase.google.com)
[![ML Kit](https://img.shields.io/badge/AI-ML%20Kit-blue.svg)](https://developers.google.com/ml-kit)
[![Gemini](https://img.shields.io/badge/AI-Gemini-blueviolet.svg)](https://deepmind.google/technologies/gemini/)

**Medi** is a high-performance Android ecosystem that digitizes the pharmacy experience. It bridges the gap between healthcare providers and patients through AI-driven prescription parsing, Generative AI medical assistance, and a robust real-time supply chain management system.

---

## 🔄 Technical Workflow & System Logic

The application architecture is built on a **Modular Activity-based System** with a centralized Firebase event-driven backend.

### 1. The Core Engine (Architecture)
*   **Role-Based Separation**: The app implements a dual-interface logic. `MainActivity.java` acts as the primary traffic controller, routing intents based on user role selection (Customer vs. Pharmacist).
*   **State Persistence**: Uses a **Singleton Pattern** in `CartManager.java` to maintain a globally accessible, thread-safe shopping cart state across different Activity lifecycles.

### 2. Patient Procurement Workflow (The Code Logic)
1.  **Identity Verification**: `UserSignin.java` handles session persistence via Firebase Auth. Google One-Tap integration is implemented for low-friction onboarding.
2.  **Smart Prescription Analysis (`Uploadprescription.java`)**:
    *   **Capture**: Uses `ActivityResultLauncher` to interface with the system's media picker.
    *   **AI OCR**: The image is processed via `Google ML Kit TextRecognizer`. Raw blocks of text are extracted into a string buffer.
    *   **Heuristic Matching**: A Regex-based algorithm (`[a-zA-Z]{4,}`) filters the OCR output to identify drug names, which are then passed to a Firestore `collectionGroup` query to find matches across *all* pharmacy inventories simultaneously.
3.  **GenAI Assistance**: Integrated with the **Google Gemini API**, providing a chatbot-like interface where patients can query drug interactions, side effects, and general health advice.
4.  **Transaction Lifecycle (`PaymentActivity.java`)**:
    *   Integrates **Razorpay SDK**. The workflow handles the `PaymentResultListener` callbacks.
    *   Upon successful verification, the cart is flushed, and a new `Order` document is atomized into the Firestore `orders` collection.

### 3. Supply Chain & Fulfillment (Pharmacist Logic)
1.  **Inventory Management**: `addmedicine.java` handles binary data upload (images) to **Firebase Storage** and metadata (price, dosage) to **Firestore**.
2.  **Real-Time Dashboard**: `OrdersReceivedActivity` uses a `SnapshotListener`. When a customer places an order, the pharmacist's UI updates instantly without requiring a refresh.
3.  **Fulfillment Logic**: Updating the `status` field in a Firestore document triggers a real-time update in the customer's `MyOrdersActivity`, keeping the patient informed of their order's progress.

---

## 🛠 Tech Stack Implementation Details

| Component | Technology | Implementation Detail |
| :--- | :--- | :--- |
| **Language** | Java 11 | Utilizes modern Java features like Lambda expressions and `Stream` APIs for data processing. |
| **Backend** | Firebase | **Firestore**: NoSQL Real-time DB. **Auth**: Identity Management. **Storage**: Image BLOB storage. |
| **AI/ML** | ML Kit & Gemini | **ML Kit**: Text-to-Data conversion. **Gemini**: Generative conversational AI for health insights. |
| **Payments** | Razorpay | End-to-end encrypted payment processing with success/failure callback handling. |
| **Security** | jBCrypt | Salted password hashing for pharmacist accounts to prevent rainbow table attacks. |
| **Image Handling** | Glide | Used for efficient memory management and lazy-loading of medicine thumbnails. |

---

## 📂 Key Code Modules & Design Patterns

*   **Singleton Pattern**: Implemented in `CartManager.java` to ensure a single source of truth for the shopping cart.
*   **Adapter Pattern**: Multiple `RecyclerView.Adapter` implementations (e.g., `InventoryAdapter`, `MyOrdersAdapter`) bridge complex Firestore data objects to UI components.
*   **Model-View-Binding**: Extensive use of `ViewBinding` to ensure null-safe UI interaction and cleaner code.
*   **POJO Modeling**: `Medicine.java`, `Order.java`, and `CartItem.java` define the schema for all database transactions.

---

## 📝 Detailed Project Summary

**Medi** is a sophisticated healthcare and pharmacy management ecosystem designed to modernize the traditional medicine procurement process. By bridging the gap between patients and pharmacists, the application facilitates a seamless end-to-end digital workflow. Patients can utilize high-precision **AI-driven OCR** to scan physical prescriptions, which are automatically parsed and matched against a real-time, cross-pharmacy inventory powered by **Firebase Firestore**. To enhance the patient experience, the platform integrates **Generative AI (Google Gemini)**, providing a conversational interface for medical guidance, drug interaction checks, and health advice. 

On the supply side, pharmacists are equipped with a robust dashboard to manage stock levels, update pricing, and fulfill orders through an event-driven notification system. The application's architecture emphasizes reliability and security, employing the **Singleton design pattern** for consistent state management across the shopping experience, **jBCrypt** for salted password hashing, and the **Razorpay SDK** for secure, encrypted financial transactions. This holistic approach ensures that Medi is not just a marketplace, but a high-performance healthcare companion that prioritizes speed, security, and intelligent automation.

---

## 🚀 Setup & Installation

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/parzival787/MEDI.git
    ```
2.  **Firebase Integration**:
    *   Place your `google-services.json` in the `app/` folder.
    *   Enable **Authentication**, **Cloud Firestore**, and **Storage** in the Firebase Console.
3.  **API Key Configuration**:
    *   Add your **Razorpay Key** in `PaymentActivity.java`.
    *   Inject your **Gemini API Key** into the AI helper module.
4.  **Build**:
    *   Open in Android Studio Ladybug (or newer).
    *   Gradle Sync & Run on API 24+.

---

## 🤝 Contributing
Contributions are welcome! If you're looking to improve the AI matching algorithm or UI, please submit a Pull Request.

**Developer**: [parzival787](https://github.com/parzival787)
