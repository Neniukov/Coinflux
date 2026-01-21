# Technical Evaluation Report: WebView-Based Player Implementation
---

## 1. Executive Summary
This report evaluates the feasibility of using Android WebView as the primary engine for a new multimedia player. While WebView offers unparalleled flexibility for UI design and remote updates, it is a resource-intensive component. To achieve industrial-grade stability (24/7 uptime), a **Hybrid Architecture** (Localhost + Native Asset Management) is required to mitigate inherent performance risks.

---

## 2. Test Setup & Methodology
* **Hardware:** Mid-range Android SoC (Exynos/MediaCodec architecture).
* **Content Profile:** Dynamic Multi-zone Grids (Fullscreen H.264 Video, Static Images, HTML5 Widgets).
* **Duration:** 2-hour continuous stress test session.
* **Infrastructure:** Files served via a **Local HTTP Server** (localhost) using **Service Workers** for offline persistence.

---

## 3. Measured Resource Usage (Comparative Data)

| Resource | Fullscreen/Grid WebView | Native Player (ExoPlayer) | Evaluation |
| :--- | :--- | :--- | :--- |
| **CPU Load** | **15% – 25%** | **5% – 10%** | **Moderate:** Higher overhead due to JS execution and DOM rendering. |
| **RAM Usage** | **400MB – 650MB** | **120MB – 200MB** | **Critical:** WebView triggers multiple processes (Browser, Renderer, GPU). |
| **GPU Load** | **High** | **Low** | **Moderate:** Constant composition of UI layers over the video stream. |
| **Battery Drain** | **~25% / hour** | **~12% / hour** | **High:** The Chromium engine is significantly more power-hungry. |

> **Key Technical Discovery:** Log analysis confirms that WebView successfully utilizes **Hardware Acceleration** (`c2.exynos.h264.decoder`). The primary bottleneck is the **Memory (RAM) footprint**, not the CPU.

---

## 4. Risks & Limitations Identification

### **A. Memory Pressure & "Bloat"**
WebView accumulates memory in the Renderer process over long sessions. 
* **Risk:** Unexpected "White Screen" (Renderer Process Crash) or the OS killing the app due to OOM (Out of Memory).

### **B. Thermal Throttling**
The Chromium engine generates more heat than a native player during continuous playback.
* **Risk:** Upon reaching thermal limits, the device will downclock the CPU/GPU, resulting in **dropped frames (stuttering)** and UI lag.

### **C. Hardware Decoder Limits**
Android devices have a hard limit on concurrent hardware decoders (typically 2–4 instances).
* **Risk:** Complex grids with 4+ videos will result in black screens or a fallback to software decoding, which will freeze the device.
---

## 5. Comparison vs. Current Solution
* **Native Approach:** Highly stable and lightweight but rigid. Development of new layouts is slow and requires frequent APK updates.
* **WebView Approach:** Allows for rapid UI iteration and remote layout updates without APK deployment. Supports complex CSS animations and micro-frontends (iFrames) but requires more robust hardware and native "wrapping."

---

## 6. Final Recommendation: **GO WITH CONSTRAINTS**

We recommend proceeding with the WebView implementation **only if** the following engineering constraints are met:

### **Required Implementation Conditions:**
1.  **Localhost Serving (Mandatory):** All media must be served via a built-in Android HTTP server to ensure efficient video buffering and bypass CORS issues.
2.  **PDF-to-Image Pipeline:** The backend must convert PDF pages into images (JPEG/WebP). Rendering raw PDFs in the WebView is strictly prohibited.
3.  **Hardware Baseline:** Targeted hardware must have at least **3GB of RAM**.
4.  **Watchdog System:** The Android native wrapper must implement a "Watchdog" to monitor WebView health and perform a "soft reload" if memory usage exceeds 80%.
5.  **Offline-First Architecture:** All assets and manifests must be stored locally to ensure 100% uptime during network outages.

---

## 7. Acceptance Criteria Verification
* **[✔] Resource Usage:** Documented and compared against native benchmarks.
* **[✔] Key Risks:** Identified (Thermal, Memory, Codec limits).
* **[✔] Concrete Recommendation:** Provided (Go with constraints).
