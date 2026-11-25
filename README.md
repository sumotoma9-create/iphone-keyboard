# iOS-style Bilingual Keyboard — Dark Edition with Typing Sounds

An Android Input Method Editor (IME) starter project with a dark, iPhone-inspired keyboard, English/Arabic switching, and original locally generated key-click sound effects.

## Included
- Dark iPhone-inspired key layout
- English QWERTY and Arabic layouts
- Globe key to switch languages
- Shift, backspace, space, punctuation, and enter
- Number and symbol pages
- Emoji shortcut
- Distinct synthesized sounds for normal keys, delete, space, and enter
- On-keyboard **Sound on / Sound off** toggle (preference is saved)
- No internet permission and no network functionality

## About the sounds
The sound effects are short, original synthesized clicks designed to feel subtle and tactile. They are iPhone-inspired, not Apple's exact proprietary recordings.

## Build an APK
This workspace cannot compile Android APKs because the Android SDK/Gradle build environment is not installed. To create the APK:
1. Install Android Studio on a computer.
2. Open this project folder in Android Studio.
3. Allow Gradle sync to complete; install Android SDK Platform 35 if requested.
4. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Transfer the generated APK to your Android phone and install it.

## Enable on Android
On Samsung phones, open **Settings > General management > Keyboard list and default**.
Enable **iOS Style Keyboard**, then select it under **Default keyboard**.

## Notes
- This is an independent iPhone-inspired design, not an Apple product and not Apple's proprietary keyboard.
- This is a starter keyboard, not a production-grade predictive keyboard. It does not currently provide autocorrect, swipe typing, clipboard history, or a full emoji picker.
- The source does not request internet access and does not transmit typed text.
