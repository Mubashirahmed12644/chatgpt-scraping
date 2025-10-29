import express from "express";
import fetch from "node-fetch";
import dotenv from "dotenv";

dotenv.config();

const app = express();
app.use(express.json());

const OPENAI_KEY = process.env.OPENAI_API_KEY;
if (!OPENAI_KEY) {
    console.error("Missing OPENAI_API_KEY in environment or .env file");
    process.exit(1);
}

app.get("/health", (_req, res) => {
    res.json({ status: "ok" });
});

app.post("/api/chat", async (req, res) => {
    try {
        const messages = req.body?.messages;
        const fallbackPrompt = req.body?.prompt ?? "";
        const payload = {
            model: req.body?.model || "gpt-4o-mini",
            messages: Array.isArray(messages) && messages.length > 0
                ? messages
                : [{ role: "user", content: fallbackPrompt }],
            max_tokens: req.body?.max_tokens || 800,
            temperature: req.body?.temperature ?? 0.7
        };

        const response = await fetch("https://api.openai.com/v1/chat/completions", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${OPENAI_KEY}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errText = await response.text();
            return res.status(response.status).json({
                error: "OpenAI error",
                status: response.status,
                body: errText
            });
        }

        const data = await response.json();
        res.json(data);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: error.message || "Unknown error" });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Proxy running on http://localhost:${PORT}`);
});
