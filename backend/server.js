// Simple OpenAI proxy
// Usage:
//   npm init -y
//   npm i express node-fetch dotenv
//   node server.js
import express from "express";
import fetch from "node-fetch";
import dotenv from "dotenv";
dotenv.config();

const app = express();
app.use(express.json());

const OPENAI_KEY = process.env.OPENAI_API_KEY;
if (!OPENAI_KEY) {
    console.error("Set OPENAI_API_KEY in .env");
    process.exit(1);
}

app.post("/api/chat", async (req, res) => {
    try {
        const { messages } = req.body;
        const r = await fetch("https://api.openai.com/v1/chat/completions", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${OPENAI_KEY}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                model: "gpt-4o-mini", // or any available chat model
                messages: messages || [{ role: "user", content: req.body.prompt || "" }],
                max_tokens: 800
            })
        });
        const data = await r.json();
        res.json(data);
    } catch (e) {
        console.error(e);
        res.status(500).json({ error: e.message });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log("Proxy running on", PORT));