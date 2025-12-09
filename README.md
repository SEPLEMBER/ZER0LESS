# ZER0LESS 😄

While AI is busy generating secure code, one introverted person sips tea in peace. Welcome to ZER0LESS, a secure text encryption app for Android, forked from the awesome SimpleTextCrypt by Aidin Gharibnavaz. This project brings modern encryption, multilingual support, and a sleek AMOLED theme to keep your secrets safe (or as safe as 2025 allows 😉). 

## Features
- Secure Encryption: AES-256-GCM with PBKDF2 key derivation for robust text encryption.
- Multilingual Interface: Supports Russian, English, Ukrainian, Japanese, Spanish, German, Italian, and French.
- Theme Options: Light, Dark, and AMOLED themes for a comfortable user experience.
- Lock Timeout: Configurable lock timeout to protect your data.
- Secure Password Handling: Uses char[] for passwords to minimize memory leaks.

## Cryptographic Parameters
ZER0LESS uses the following parameters for encryption (implemented in Crypter.java):
- Cipher: AES/GCM/NoPadding with 256-bit keys, ensuring confidentiality and integrity.
- Salt: 32 bytes (256 bits) for strong key derivation with PBKDF2.
- Initialization Vector (IV): 12 bytes (96 bits), standard for AES-GCM.
- Authentication Tag: 128 bits for robust data integrity.
- PBKDF2 Iterations: 75,000 with HmacSHA256 for secure key derivation.
- Format: v1:[Base64(salt)]:[Base64(iv)]:[Base64(ciphertext)] for compatibility and extensibility.

These parameters provide a strong foundation for secure text encryption, suitable for most use cases in 2025.

## Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/ZER0LESS.git
   ```
2. Build with Gradle:
   ```bash
   ./gradlew build
   ```
3. Install the APK:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Disclaimer: No System Is Safe
As MRX said in *Who Am I* (2014): "No System Is Safe." In the spirit of Zero Trust, we acknowledge that no encryption system is immune to risks. *Lethargic sigh:* Well... Leaks are possible, but the risk is still lower than chatting in plain text. Plus, the source code is open for scrutiny and improvement. The Crypter code itself has been repeatedly checked for leaks and vulnerabilities, and nothing particularly critical was found. What little was found will be fixed someday. While ZER0LESS uses modern cryptographic standards (AES-256-GCM, PBKDF2), users should remain vigilant, use strong passwords, and avoid sharing sensitive data on untrusted devices. That said, the algorithm is robust and reliable for 2025 standards.

## Acknowledgments
HUGE thanks to Aidin Gharibnavaz 💖, the original author of SimpleTextCrypt, for creating an awesome foundation for this fork. Your work inspired us to build something secure and user-friendly!

## License
This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.
