# 🖼️ Java Steganography Tool

A **Java-based Steganography application** that hides and retrieves secret messages inside images using the **Least Significant Bit (LSB)** algorithm.

The project includes a **Swing-based graphical user interface (GUI)** that allows users to apply watermarks, embed text files into images, and retrieve hidden messages.

---

## 📌 Features

* 🔐 Hide secret text inside images using **LSB Steganography**
* 🔍 Retrieve hidden messages from stego images
* 🖋 Apply **visible watermark text** on images
* 🖥 Simple and intuitive **Java Swing GUI**
* 📁 Supports embedding **text files into images**
* 📤 Export results as **lossless PNG images**

---

## 🧠 How It Works

The system uses **Least Significant Bit (LSB) steganography**.

In this method, the **least significant bit of image pixel bytes** is modified to store secret data.

Example:

```
Original Pixel Byte
10101100

Modified Pixel Byte
10101101
```

The visual change is almost **imperceptible to the human eye**.

The application stores:

```
[Message Length (4 Bytes)] + [Message Data]
```

inside the image byte stream.

---

## 🖥️ Application Modules

### 1️⃣ Watermark Module

Adds a visible watermark to an image.

Features:

* Adjustable watermark text
* Transparent overlay
* Centered watermark placement
* Export as PNG

---

### 2️⃣ Hide Data Module

Embeds a file inside an image using LSB encoding.

Steps:

1. Select a cover image
2. Select a text file to hide
3. The program embeds the file into the image
4. Output image is generated as:

```
originalNameMsg.png
```

---

### 3️⃣ Retrieve Data Module

Extracts hidden messages from a steganographic image.

Steps:

1. Select stego image
2. Click **Retrieve**
3. Hidden message appears in the GUI
4. Save extracted text as a `.txt` file

---

## 📂 Project Structure

```
steganography-java
│
├── src
│   ├── Stegenography.java
│   └── Stego_patched.java
│
├── sample
│   ├── input.png
│   └── secret.txt
│
├── screenshots
│   └── gui.png
│
├── README.md
└── LICENSE
```

---

## ⚙️ Installation & Setup

### 1️⃣ Clone the Repository

```
git clone https://github.com/yourusername/steganography-java.git
```

### 2️⃣ Navigate to the Source Folder

```
cd steganography-java/src
```

### 3️⃣ Compile the Program

```
javac Stegenography.java Stego_patched.java
```

### 4️⃣ Run the Application

```
java Stego_patched
```

---

## 🧪 Example Usage

### Hide Message

```
Input Image : cover.png
Hidden File : secret.txt
Output Image: coverMsg.png
```

### Retrieve Message

```
Input Image : coverMsg.png
Output      : Secret message displayed in GUI
```

---

## 🔒 Security Notes

* Uses **LSB steganography**
* Works best with **PNG images**
* Lossy formats like **JPG may corrupt hidden data**

---

## 🛠 Technologies Used

* Java
* Java Swing
* BufferedImage
* ImageIO
* DataBufferByte

---

## 🎓 Educational Purpose

This project demonstrates concepts of:

* Steganography
* Data hiding techniques
* Image processing
* Java GUI development
* Information security

---

## 👨‍💻 Author

**A Vishal**
**Aarushi Gupta**
**Shivani Acharya**
B.Tech – Information Technology

---

## 📜 License

This project is licensed under the **MIT License**.

You are free to use, modify, and distribute this software for educational or personal purposes with proper attribution.
