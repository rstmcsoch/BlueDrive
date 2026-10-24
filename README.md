# BlueDrive ☁️

**Your personal cloud file manager for Android, powered by Telegram.**

BlueDrive transforms a private Telegram channel into a convenient cloud-backed file library. Upload, organize, browse, download, and share your files directly from your Android device through a clean and intuitive interface.

Instead of searching through Telegram messages manually, BlueDrive gives you a dedicated space to manage your files, track storage usage, filter content by type, and access your saved media whenever you need it.

---

## 📖 Table of Contents

- [About BlueDrive](#about-bluedrive)
- [Key Features](#key-features)
- [How It Works](#how-it-works)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Step 1: Create a Telegram Bot](#step-1-create-a-telegram-bot)
  - [Step 2: Create a Private Channel](#step-2-create-a-private-channel)
  - [Step 3: Get Your Chat ID](#step-3-get-your-chat-id)
  - [Step 4: Configure BlueDrive](#step-4-configure-bluedrive)
- [How to Use BlueDrive](#how-to-use-bluedrive)
- [Technical Details](#technical-details)
- [Privacy and Limitations](#privacy-and-limitations)
- [License](#license)

---

## 🌐 About BlueDrive

BlueDrive is designed to make personal cloud storage simple and accessible.

It uses the Telegram Bot API to store files in a private Telegram channel while providing an Android interface for browsing and managing them. BlueDrive also maintains a local index of your files, so you can find your content without manually searching through your Telegram channel.

The file index is intended to be stored in a text file named `bluedrive_files.txt` inside your device's `Documents/BlueDrive` directory. This helps preserve your organized file list independently of the app installation, provided the file remains on your device.

**The idea is simple:** your Telegram channel acts as the storage backend, while BlueDrive acts as your personal file manager.

---

## ✨ Key Features

- **⚙️ Easy Configuration:** Enter your Telegram Bot Token and Chat ID directly in the Settings screen without modifying source code.
- **🌍 Multi-Language Support:** Switch between English, Arabic (العربية), and French (Français) from the app settings.
- **📁 File Uploads:** Select documents, images, videos, and other supported file types from your Android device and upload them to your private Telegram channel.
- **📊 Upload Progress:** Track active uploads and cancel an upload in progress when supported.
- **🖼️ Image Viewer:** Open images in a full-screen viewer with zoom support.
- **▶️ Video Playback:** Download videos temporarily and open them using your device's video player.
- **🖼️ Media Thumbnails:** Identify images and videos more easily with thumbnail previews.
- **📥 File Downloads:** Save files from your cloud library to `Documents/BlueDrive/Download`.
- **🔗 Native Sharing:** Share available files through Android's native sharing interface.
- **🗑️ File Management:** Remove file entries and request deletion of their corresponding Telegram messages, subject to Telegram API restrictions.
- **🗂️ Persistent File Index:** Keep your organized file list in `Documents/BlueDrive/bluedrive_files.txt`.
- **📈 Storage Insights:** View file counts and storage usage, including categories such as images, videos, PDFs, and other files.
- **🔍 Smart Filtering:** Filter the library by file category to find specific content faster.
- **🎨 Clean Android Interface:** Manage your files through an intuitive interface built with Android UI components.
- **🔐 Personal Storage Control:** Connect BlueDrive to your own Telegram bot and private channel instead of relying on a separate cloud-storage account.

---

## 🔧 How It Works

BlueDrive uses the Telegram Bot API to manage your cloud-backed file library.

### 1. Upload

When you select a file, BlueDrive sends it to your Telegram channel through the Bot API's `sendDocument` endpoint.

### 2. File Tracking

After a successful upload, Telegram returns file information such as a `file_id` and `message_id`. BlueDrive can store the relevant metadata in its local index, `bluedrive_files.txt`, so the file can be listed and managed inside the app.

### 3. Download

When you request a download, BlueDrive uses the stored file information with the Telegram Bot API's `getFile` endpoint and retrieves the file using the returned file path.

### 4. Organization

BlueDrive presents your saved files in an organized interface, with category filters, thumbnails, file counts, and storage information.

### 5. Deletion

BlueDrive can use Telegram's `deleteMessage` endpoint to request deletion of a file's message from the connected channel. Telegram's permissions and API restrictions apply.

---

## 🚀 Getting Started

Follow the steps below to connect BlueDrive to your own Telegram storage.

### Prerequisites

- An active Telegram account.
- An Android device.
- A Telegram bot created through BotFather.
- A private Telegram channel in which your bot has the required permissions.

### Step 1: Create a Telegram Bot

1. Open Telegram and search for **[@BotFather](https://t.me/BotFather)**.
2. Send the `/newbot` command.
3. Follow the instructions to choose a display name for your bot.
4. Create a unique username ending in `bot`, such as `my_bluedrive_bot`.
5. Copy the API token provided by BotFather.

Keep this token private. It provides access to your bot.

Your token will be used as `BOT_TOKEN`.

### Step 2: Create a Private Channel

1. Create a new Telegram channel.
2. Give it a name, such as **BlueDrive Cloud Storage**.
3. Set the channel visibility to **Private**.
4. Add your newly created bot as an administrator.
5. Grant the permissions required for posting messages and deleting messages.

Your channel will serve as the storage location for files uploaded through BlueDrive.

### Step 3: Get Your Chat ID

1. Find a Telegram bot or utility that can identify a channel's numeric chat ID.
2. Follow its instructions to retrieve the ID of your private channel.
3. Confirm that the returned ID belongs to the correct channel. Channel IDs commonly begin with `-100`.
4. Save the value for use in BlueDrive.

This value will be used as `CHAT_ID`.

**Security note:** Never publish your bot token in your README, source code, screenshots, or public repository.

### Step 4: Configure BlueDrive

1. Install and open BlueDrive on your Android device.
2. Navigate to **Settings** or the app's configuration screen.
3. Enter your `BOT_TOKEN` and `CHAT_ID`.
4. Tap **Check Connection** to test the configuration.
5. If the connection succeeds, save the settings.
6. Open the main screen and upload your first file.

BlueDrive is now connected to your configured Telegram channel.

---

## 📱 How to Use BlueDrive

### Upload a File

1. Open BlueDrive.
2. Tap the `+` floating action button.
3. Select a file from your device.
4. Wait for the upload to finish.

### Browse Your Library

Open the main screen to view your saved files and available storage statistics. Use category cards to filter images, videos, PDFs, or other files.

### View Images and Videos

Tap a supported image to open it in the built-in viewer. Videos can be downloaded temporarily and opened using a compatible video player.

### Download a File

1. Find the file in your library.
2. Open its options menu.
3. Select **Download**.
4. Look for the downloaded file in `Documents/BlueDrive/Download`.

### Share a File

Open the file's options menu and choose **Share File** to access Android's sharing interface, where supported by the app implementation.

### Remove a File

1. Open the file's options menu.
2. Select the available removal or deletion option.
3. Confirm the action when prompted.

Deletion from Telegram depends on the bot's permissions and Telegram's message-deletion rules.

### Change the App Language

Go to **Settings** and select an available language.

---

## 🛠️ Technical Details

- **Platform:** Android.
- **Architecture:** Multiple activities for the main library, settings, filtered files, and image viewing.
- **Networking:** `HttpURLConnection` for Telegram Bot API requests.
- **File Uploads:** Multipart requests through Telegram's `sendDocument` endpoint.
- **File Downloads:** Telegram's `getFile` endpoint and its returned file path.
- **UI Components:** `RecyclerView` and `CardView` for displaying files and their metadata.
- **Image Viewer:** PhotoView for zoomable image viewing.
- **Thumbnail Loading:** Glide for image and video thumbnail handling.
- **Localization:** Android application locale support and language helper utilities.
- **Persistence:** Pipe-delimited (`|`) local text database, intended to be stored as `bluedrive_files.txt`.
- **Storage Directory:** `Documents/BlueDrive/`.
- **Download Directory:** `Documents/BlueDrive/Download/`.
- **File Sharing:** Android `FileProvider` for sharing local files through secure `content://` URIs.
- **Permissions:** Runtime storage permissions and applicable Android storage-access handling.

---

## 🔐 Privacy and Limitations

BlueDrive connects to the Telegram bot and channel configured by the user. Your files are hosted on Telegram's infrastructure, and the app uses Telegram's API to interact with them.

Keep the following points in mind:

- **Protect Your Credentials:** A bot token should never be committed to a public repository.
- **Channel Privacy Matters:** Use a private channel and grant your bot only the permissions it requires.
- **Telegram Limitations Apply:** Upload size, downloads, message deletion, request limits, and other operations are subject to Telegram's current API restrictions.
- **Local Index:** Your file index is stored on your device. Back it up if you need to preserve it independently of the app.
- **Network Access:** Uploading, downloading, and synchronizing file information require an internet connection.

BlueDrive does not eliminate Telegram's own storage, privacy, or API limitations.
