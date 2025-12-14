<p align="center"><img src="app/src/main/res/drawable/logo_app.jpg" alt="Logo app " width="500"/></p>

# 💊 MediNotify - Ứng dụng Quản lý & Nhắc Uống Thuốc

> **Trợ lý cá nhân đồng hành giúp bạn quản lý và theo dõi quá trình uống thuốc hiệu quả.**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-green.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-Backend-orange.svg)](https://firebase.google.com)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue.svg)](https://developer.android.com/topic/architecture)

## 📖 Giới thiệu (Introduction)

**MediNotify** là ứng dụng di động được xây dựng trên nền tảng Android nhằm giải quyết vấn đề quên uống thuốc hoặc uống sai liều lượng – một thách thức lớn đối với người cao tuổi và những người bận rộn. 
Trong nhịp sống bận rộn hiện nay, việc quên uống thuốc hoặc nhầm lẫn liều lượng xảy ra thường xuyên, gây ảnh hưởng trực tiếp đến hiệu quả điều trị và sức khỏe người bệnh. **MediNotify** giải quyết triệt để vấn đề này bằng cách kết hợp khả năng quản lý danh sách thuốc chặt chẽ với hệ thống nhắc nhở tự động thông minh.


## ✨ Tính năng chính (Key Features)

Dựa trên phân tích yêu cầu hệ thống, ứng dụng cung cấp các chức năng cốt lõi:

* **🔐 Đăng ký & Bảo mật:**
    * Đăng nhập linh hoạt qua Email/Mật khẩu hoặc **Google Sign-In**.
    * Tự động đồng bộ dữ liệu người dùng khi đăng nhập trên thiết bị mới.
* **📋 Quản lý Thuốc (Medicine Management):**
    * Thêm, sửa, xóa thông tin thuốc chi tiết (Tên, liều lượng, loại thuốc: viên nén, siro, v.v.).
    * Kiểm tra trùng lặp tên thuốc để tránh sai sót.
* **⏰ Hệ thống Nhắc nhở Thông minh:**
    * Thiết lập lịch uống linh hoạt: Hàng ngày, ngày cụ thể trong tuần, hoặc khoảng cách ngày (Interval).
    * **Hoạt động Offline:** Sử dụng `WorkManager` để gửi thông báo cục bộ ngay cả khi không có Internet.
* **📊 Theo dõi & Lịch sử:**
    * Ghi lại trạng thái: "Đã uống" (Taken) hoặc "Bỏ qua" (Skipped).
    * Xem lịch sử tuân thủ điều trị theo ngày/tháng.
* **☁️ Đồng bộ Dữ liệu (Cloud Sync):**
    * Cơ chế **Offline-first**: Dữ liệu lưu tại máy (Room Database) và tự động đồng bộ lên Firebase Firestore khi có mạng.

## 🛠 Công nghệ sử dụng (Tech Stack)

Dự án áp dụng kiến trúc hiện đại và các thư viện mới nhất của Android:

* **Ngôn ngữ:** Kotlin.
* **Giao diện (UI):** Jetpack Compose.
* **Kiến trúc:** MVVM (Model-View-ViewModel) + Repository Pattern.
* **Lưu trữ cục bộ (Local DB):** Room Database (SQLite).
* **Backend & Cloud:**
    * Firebase Authentication (Xác thực).
    * Firebase Firestore (NoSQL Database).
    * Firebase Cloud Messaging (FCM).
* **Lập lịch (Scheduling):** Android WorkManager (Đảm bảo thông báo chính xác).

## 👥 Nhóm thực hiện (Contributors)

* **Kiều Trần Thu Uyên** - 064305005016
* **Hoàng Mai Kiều** - 067305001315
* **Lương Thị Ánh Tuyết** - 067305001563
# 🚀 Cài Đặt và Chạy Thử

**Để build và chạy thử dự án, bạn cần thực hiện các bước sau:**

**_- Yêu cầu:_**

- Android Studio Iguana | 2023.2.1 hoặc mới hơn.
- JDK 17.

**Các bước cài đặt:**

***1.Clone Repository:***

- git clone https://github.com/HoangKiew/Medicine-Reminder-Tracker-App.git
- cd Medicine-Reminder-Tracker-App

***2.Kết nối với Firebase:***

- Truy cập Firebase Console.
- Tạo một dự án Firebase mới.
- Thêm một ứng dụng Android vào dự án Firebase (Hãy kiểm tra package name trong file `app/build.gradle.kts` để nhập chính xác).
- *Lưu ý:* Để sử dụng đăng nhập Google, bạn cần thêm mã SHA-1 (lấy bằng lệnh `gradlew signingReport`) vào cấu hình dự án trên Console.
- Tải về file `google-services.json` và đặt nó vào thư mục `app/`.
- Trong Firebase Console, kích hoạt các dịch vụ sau:
  - Authentication: Bật phương thức đăng nhập bằng Email/Password và Google.
  - Firestore Database: Tạo một database ở chế độ production (hoặc test mode).
  - Storage: (Nếu cần) Tạo một bucket lưu trữ.

***3.Build Dự Án:***

- Mở dự án bằng Android Studio.
- Android Studio sẽ tự động đồng bộ Gradle. Quá trình này có thể mất vài phút.
- Nếu gặp lỗi `org.gradle.java.home`, hãy vào File -> Settings -> Build, Execution, Deployment -> Build Tools -> Gradle và chọn một Gradle JDK là jbr-17 hoặc Embedded JDK 17.

***4.Chạy Ứng Dụng:***

- Kết nối một thiết bị Android thật hoặc khởi động một máy ảo (Emulator).
- Nhấn nút Run 'app' (▶️) trên thanh công cụ của Android Studio.
---
*Đồ án thực tế - Viện CNTT - Trường ĐH Giao Thông Vận Tải TP.HCM (UTH)*
