# ChatGPT Proxy Android (XML)

**Run Android:**
1) Open this folder in Android Studio.
2) Sync Gradle.
3) Start emulator.
4) Ensure backend is running at http://10.0.2.2:3000 (or edit BASE_URL in `MainActivity.kt`).

**Run Backend:**
```bash
cd backend
npm init -y
npm i express node-fetch dotenv
echo "OPENAI_API_KEY=sk-xxxxx" > .env
node server.js
```

Physical device: set BASE_URL to your PC LAN IP (e.g. http://192.168.1.10:3000) and allow it in `res/xml/network_security_config.xml`.