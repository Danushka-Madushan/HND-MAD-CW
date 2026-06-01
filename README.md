<div align="center">

<img src="https://raw.githubusercontent.com/Danushka-Madushan/HND-MAD-CW/refs/heads/master/.idea/icon.svg" width="96" height="96" alt="SnapShop Icon"/>

# SnapShop

**Point. Snap. Shop.**

A utility-driven Android app that identifies any physical product from a photo and instantly surfaces verified online purchase links.

[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Android API](https://img.shields.io/badge/API%2028%2B-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)
[![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black)](https://firebase.google.com/)
[![Course](https://img.shields.io/badge/NIBM-HND--SE%20%7C%20Batch%2026.1F-0F6E56?style=flat-square)](https://www.nibm.lk/)

</div>

## Overview

SnapShop bridges the gap between **physical observation** and **online purchasing**. When you encounter a product you can't name - a specialized tool, a hardware component, an interesting gadget - SnapShop automates the entire pipeline from raw image capture to actionable purchase results.

No guessing. No scrolling through irrelevant search results. Just point, snap, and shop.

## How It Works

```
📷 Capture  →  🤖 ML Kit Crop  →  ☁️ Imgbb CDN  →  🔍 SerpAPI Lens
                                                            ↓
🛒 Shopping Results  ←  🛍️ SerpAPI Shopping  ←  ✨ Gemini Distillation
```

1. **Capture** - Aim the in-app camera at any item and tap the shutter
2. **Crop** - Google ML Kit Object Detection isolates the subject and removes background noise on-device
3. **Upload** - The clean cropped image is uploaded to Imgbb to generate a public CDN URL
4. **Visual Analysis** - SerpAPI's Google Lens endpoint extracts the top 5 matching product title candidates
5. **Query Distillation** - Gemini Flash-Lite filters clutter, translates foreign terms, and outputs a single optimized English product name
6. **Market Indexing** - The refined query hits SerpAPI Google Shopping to return live store links, names, and active pricing

## Features

### Core Identification Pipeline
| Feature | Description |
|---|---|
| 📷 In-App Camera | Native high-resolution capture with a simple shutter control |
| ✂️ Smart Background Removal | On-device ML Kit bounding-box crop - strips visual noise before any network call |
| 🔍 Visual Product Recognition | SerpAPI Google Lens returns the top 5 structural product matches |
| 🛒 E-Commerce Indexing | Live retail listings, store names, and direct merchant checkout links |

### Data & Identity
| Feature | Description |
|---|---|
| 💾 Local History & Caching | SQLite stores every scan result for instant offline access, shielding expensive API quota |
| 🔐 User Authentication | Firebase Auth handles secure sign-up, sign-in, and session state |
| ☁️ Cloud Synchronization | Cloud Firestore backs up scan history across devices automatically |

## Tech Stack

### Platform & UI
- **Kotlin** - Primary language with Coroutines and Flows for non-blocking async execution
- **Jetpack Compose** - Declarative UI with Material 3 components
- **Android SDK API 28+** - Modern Android compatibility

### On-Device Processing
- **Google ML Kit** (Object Detection & Tracking) - Real-time subject isolation and bounding-box crop

### Networking
- **OkHttp3** - REST client for header management, request configuration, and JSON serialization
- **Imgbb API** - CDN image hosting to generate public URLs for downstream APIs

### Cloud Intelligence
- **SerpAPI Google Lens** - Deep reverse-image search returning top 5 product metadata records
- **Google Gemini Flash-Lite** - Structured output query distillation at low temperature (`0.1`) via REST, returning strict JSON
- **SerpAPI Google Shopping** - Live e-commerce listings from the distilled product name

### Storage & Auth
- **Room** (SQLite abstraction) - Local relational caching of scan history
- **Firebase Authentication** - Account lifecycle and credential management
- **Cloud Firestore** - Scalable remote backup and cross-device sync

## Architecture

SnapShop follows **Clean Architecture** principles with clear separation across data, domain, and presentation layers - ensuring testability, scalability, and maintainability throughout the development lifecycle.

## Project Structure

```
snapshop/
├── app/
│   ├── data/           # Room DB, Firebase, API clients
│   ├── domain/         # Use cases and business logic
│   ├── presentation/   # Compose screens and ViewModels
│   └── di/             # Dependency injection modules
├── gradle/
└── README.md
```

---

## Team

**Group 01 - Batch 26.1F | HND Software Engineering | NIBM**

| Student ID | Name | Role |
|---|---|---|
| MAHNDSE261F-001 | D.P.D. Madushan | DevOps & Project Management |
| MAHNDSE261F-005 | K.L. Thineth Geevinda | Business Logic & Internal Methods |
| MAHNDSE261F-006 | K.K.N. Naveesha | UI Design |
| MAHNDSE261F-010 | G.A. Disanayaka | UX Design |

> Every team member is responsible for implementing the full application. Role designations reflect areas of primary expertise and review responsibility - not exclusive ownership.

## Feature Prioritization

**Tier 1 - Core Identification Pipeline** *(Highest Priority)*
Camera integration → on-device crop → multi-stage SerpAPI + Gemini search → query distillation → market indexing

**Tier 2 - Data Persistence & Session Management** *(Medium Priority)*
SQLite caching → Firebase Authentication → Cloud Firestore sync

**Tier 3 - Deployment & Administrative Tooling** *(Lowest Priority)*
Repository documentation, Clean Architecture packaging, and dependency injection module write-up

## Target Audience

- **Technicians & Engineers** - Quickly identify and source specific mechanical parts or electronics on-site
- **General Consumers** - Spot interesting gadgets or goods and check pricing and availability instantly
- **DIY Enthusiasts** - Identify hardware and materials without knowing the exact industry name

<div align="center">

*National Institute of Business Management · Mobile Application Development · 2026*

</div>
