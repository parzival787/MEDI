# Medi — Android Pharmacy App

Medi is an Android prototype connecting patients with pharmacies. Patients search available medicines or scan prescription text, build a cart, and use a Razorpay checkout flow. Pharmacists manage inventory and view incoming orders.

## Start here

| Role | What to explore |
| --- | --- |
| Patient | Sign in, search medicines across pharmacies, scan a prescription image, add items to the cart, and view orders |
| Pharmacist | Register a pharmacy profile, add or edit medicines, view inventory, and manage received orders |

## Features in the code

- Patient and pharmacist sign-in using Firebase Authentication.
- Pharmacy profiles stored in Firestore.
- Medicine search across pharmacy `my_inventory` subcollections.
- Prescription-image text extraction using Google ML Kit OCR, followed by inventory lookup.
- Shared cart state through `CartManager`.
- Razorpay SDK checkout and client-side success/failure handling.
- Firestore order and inventory workflows, with image handling through Firebase Storage/Glide.

The project declares Gemini and jBCrypt dependencies, but the tracked Java application classes do not currently implement a Gemini assistant or jBCrypt password hashing. They are therefore not listed as completed features.

## Example workflow

1. A pharmacist creates a profile and adds medicines to inventory.
2. A patient signs in and searches by medicine name or selects a prescription image.
3. OCR extracts text; the app looks up matching names across pharmacy inventories.
4. The patient opens medicine details, adds items to the cart, and enters checkout.
5. Checkout callbacks lead to the order workflow, and the pharmacist can view received orders.

Prescription OCR depends on image quality and the matching text; it is not a clinical interpretation of a prescription.

## Technology

| Part | Technology |
| --- | --- |
| Language | Java, with Java 11 source compatibility |
| Android UI | XML layouts, Material components, RecyclerView, ViewBinding |
| Identity and data | Firebase Authentication and Cloud Firestore |
| Images | Firebase Storage and Glide |
| OCR | Google ML Kit Text Recognition |
| Checkout | Razorpay Android SDK |
| Android requirements | Compile/target SDK 36; minimum API 24 |

## Setup

```sh
git clone https://github.com/parzival787/MEDI.git
cd MEDI
```

1. Open the project in Android Studio and install Android SDK 36.
2. Create your own Firebase Android app using package `com.example.medi`.
3. Add its configuration to `app/google-services.json`. Configure Authentication, Firestore, and Storage.
4. Configure a Razorpay **test-mode key ID** in [PaymentActivity.java](app/src/main/java/com/example/medi/PaymentActivity.java) for a checkout demonstration. A payment key secret must not be embedded in an Android client.
5. Sync Gradle and run the `app` module on an API 24+ device or emulator.
6. Add sample inventory using a pharmacy account, then test search and ordering with a patient account.

The application uses Firestore collection-group queries on `my_inventory`; create any indexes requested by Firestore while running those queries.

## Build and test

This is a native Android Gradle project. The root npm dependencies do not provide a React Native app or an `npm start` workflow.

The wrapper uses Gradle 9.3.1 with a JDK 21 daemon configuration; the Java 11 setting describes source compatibility. After completing Firebase and SDK setup, run from the repository root:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

Use `bash ./gradlew` on macOS/Linux. Connected tests require a device/emulator. Find the debug APK in `app/build/outputs/apk/debug/`. These commands do not by themselves establish that OCR, payment callbacks or order writes work against your services.

## Data and verification

| Data | Location |
| --- | --- |
| Patient profiles and orders | Firestore `users/{uid}` and its `orders` subcollection |
| Pharmacy inventory | Firestore `pharmacists/{uid}/my_inventory` |
| Received pharmacy orders | Firestore `pharmacists/{uid}/ordersReceived` |
| Current cart | In-memory `CartManager` singleton; not durable across process termination |

Use separate patient and pharmacist test accounts. Add a medicine, find it by name, try a clear prescription image, and compare the cart total with test checkout. After a successful test callback, inspect both the patient's order and the pharmacy's received order. Also try cancellation and an empty cart. Use Razorpay test mode throughout.

## Repository map

- [UserSignin.java](app/src/main/java/com/example/medi/UserSignin.java) and [PharmacySignin.java](app/src/main/java/com/example/medi/PharmacySignin.java): account workflows.
- [UserActivity.java](app/src/main/java/com/example/medi/UserActivity.java): medicine search.
- [Uploadprescription.java](app/src/main/java/com/example/medi/Uploadprescription.java): image selection, OCR, and inventory lookup.
- [CartManager.java](app/src/main/java/com/example/medi/CartManager.java) and [CartActivity.java](app/src/main/java/com/example/medi/CartActivity.java): cart model and screen.
- [PaymentActivity.java](app/src/main/java/com/example/medi/PaymentActivity.java): Razorpay checkout and order handling.
- [PharmacistActivity.java](app/src/main/java/com/example/medi/PharmacistActivity.java): pharmacy dashboard.
- [OrdersReceivedActivity.java](app/src/main/java/com/example/medi/OrdersReceivedActivity.java): received orders.
- [app/build.gradle.kts](app/build.gradle.kts): build configuration and dependencies.

## Prototype boundaries

A successful client callback is not a substitute for server-side payment verification. The repository does not include a separate payment-verification backend. Configure Firebase access rules, validate checkout behavior, and review inventory/order consistency before using the app with real customers or payments.

This documentation describes the tracked implementation; it does not claim measured performance, clinical accuracy, or production readiness.
