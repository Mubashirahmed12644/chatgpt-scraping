package com.example.translateapp

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    companion object {
        // Emulator -> host machine: http://10.0.2.2:3000
        private const val BASE_URL = "http://10.0.2.2:3000"
        private const val CHAT_PATH = "/api/chat"
        private const val TIMEOUT_MS = 30_000
    }

    private val io = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val input = findViewById<EditText>(R.id.input)
        val send = findViewById<Button>(R.id.sendBtn)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val result = findViewById<TextView>(R.id.result)

        fun setLoading(show: Boolean) {
            progress.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
            send.isEnabled = !show
        }

        fun appendLine(line: String) {
            result.append(line + "\n\n")
            val scroll = result.parent as ScrollView
            scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
        }

        fun sendPrompt(prompt: String) {
            if (prompt.isBlank()) return
            input.setText("")
            setLoading(true)
            appendLine("You: $prompt")
            io.execute {
                val (text, err) = callBackend(prompt)
                runOnUiThread {
                    setLoading(false)
                    if (err != null) appendLine("Error: $err")
                    else appendLine("Assistant: ${text ?: "No response"}")
                }
            }
        }

        send.setOnClickListener { sendPrompt(input.text.toString().trim()) }
        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendPrompt(input.text.toString().trim())
                true
            } else false
        }
    }

    private fun callBackend(prompt: String): Pair<String?, String?> {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(BASE_URL + CHAT_PATH)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                doInput = true
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }

            val body = JSONObject().apply {
                put("messages", JSONArray().put(
                    JSONObject().put("role", "user").put("content", prompt)
                ))
            }.toString()

            BufferedWriter(OutputStreamWriter(conn.outputStream, Charsets.UTF_8)).use {
                it.write(body)
            }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val resp = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { br ->
                buildString {
                    var line: String?
                    while (br.readLine().also { line = it } != null) append(line)
                }
            }

            if (code !in 200..299) return Pair(null, "HTTP $code: $resp")

            val obj = JSONObject(resp)
            val choices = obj.optJSONArray("choices")
            val first = choices?.optJSONObject(0)
            val message = first?.optJSONObject("message")
            val content = message?.optString("content")

            val fallbackText = first?.optString("text", null)
            Pair(content ?: fallbackText, null)
        } catch (e: Exception) {
            Pair(null, e.message ?: "unknown error")
        } finally {
            conn?.disconnect()
        }
    }
}