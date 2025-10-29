# ChatGPT Proxy Android (XML)

This project pairs a simple Android chat client with a lightweight Node/Express proxy so your device can talk to the OpenAI Chat Completions API. The Android project is a classic XML layout app and targets SDK 34.

## Android client
1. Install the Android SDK and set the `sdk.dir` in a local `local.properties` (the file is ignored by Git). Example:
   ```properties
   sdk.dir=/Users/<you>/Library/Android/sdk
   ```
2. Open the project in Android Studio.
3. Let Gradle sync and run the *app* configuration on an emulator or device.
4. Ensure the backend proxy is reachable at `http://10.0.2.2:3000` (or change `BASE_URL` inside [`MainActivity.kt`](app/src/main/java/com/example/translateapp/MainActivity.kt)).

> **Note:** If you are testing on a physical device, replace `10.0.2.2` with your computer's LAN IP and add that domain to [`res/xml/network_security_config.xml`](app/src/main/res/xml/network_security_config.xml).

## Backend proxy
```bash
cd backend
npm install
cat <<'ENV' > .env
OPENAI_API_KEY=sk-your-key
ENV
npm start
```

The proxy exposes `POST /api/chat` and forwards the messages to OpenAI using the `gpt-4o-mini` model by default. Adjust the model or request payload inside [`backend/server.js`](backend/server.js) as needed.
