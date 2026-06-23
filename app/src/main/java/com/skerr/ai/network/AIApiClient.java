package com.skerr.ai.network;

import okhttp3.*;
import org.json.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AIApiClient {

    // Groq - gr√°tis, sem cart√£o. Crie sua key em console.groq.com
    private static final String GROQ_BASE_URL = "https://api.groq.com/openai/v1/chat/completions";
    // OpenRouter - fallback gr√°tis. Crie sua key em openrouter.ai
    private static final String OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1/chat/completions";

    private final OkHttpClient client;
    private String groqApiKey;
    private String openRouterApiKey;
    private String selectedModel = "llama3-70b-8192"; // padr√£o Groq
    private boolean useOpenRouter = false;

    public interface StreamCallback {
        void onToken(String token);
        void onThinking(String thought);
        void onComplete(String fullResponse);
        void onError(String error);
    }

    public AIApiClient(String groqApiKey, String openRouterApiKey) {
        this.groqApiKey = groqApiKey;
        this.openRouterApiKey = openRouterApiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void setModel(String model) { this.selectedModel = model; }
    public void setUseOpenRouter(boolean use) { this.useOpenRouter = use; }

    public void sendMessageStream(List<JSONObject> history, String systemPrompt, StreamCallback callback) {
        new Thread(() -> {
            try {
                JSONObject requestBody = buildRequestBody(history, systemPrompt, true);
                String url = useOpenRouter ? OPENROUTER_B@T—W’TìàGROQ_BASE_URL;
                String key = useOpenRouter ? openRouterApiKey : groqApiKey;

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + key)
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
                        .build();

                StringBuilder fullResponse = new StringBuilder();
                StringBuilder thinkingBuffer = new StringBuilder();
                boolean inThinking = false;

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        callback.onError("Erro HTTP: " + response.code());
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) { callback.onError("Resposta vazia"); return; }

                    java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(body.byteStream()));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6).trim();
                            if (data.equals("[DONE]")) break;
                            try {
                                JSONObject chunk = new JSONObject(data);
                                JSONArray choices = chunk.getJSONArray("choices");
                                if (choices.length() > 0) {
                                    JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                                    if (delta.has("content")) {
                                        String token = delta.getString("content");
                                        // detecta bloco <think> (DeepSeek R1 style)
                                        if (token.contains("<think>")) inThinking = true;
                                        if (inThinking) {
                                            thinkingBuffer.append(token);
                                            callback.onThinking(thinkingBuffer.toString());
                                            if (token.contains("</think>")) inThinking = false;
                                        } else {
                                            fullResponse.append(token);
                                            callback.onToken(token);
                                        }
                                    }
                                }
                            } catch (JSONException ignored) {}
                        }
                    }
                    callback.onComplete(fullResponse.toString());
                }
            } catch (IOException e) {
                callback.onError("Erro de conex√£o: " + e.getMessage());
            }
        }).start();
    }

    private JSONObject buildRequestBody(List<JSONObject> history, String systemPrompt, boolean stream) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("model", useOpenRouter ? "deepseek/deepseek-r1:free" : selectedModel);
        body.put("stream", stream);
        body.put("temperature", 0.7);
        body.put("max_tokens", 4096);

        JSONArray messages = new JSONArray();
        JSONObject sys = new JSONObject();
        sys.put("role", "system");
        sys.put("content", systemPrompt);
        messages.put(sys);
        for (JSONObject msg : history) messages.put(msg);
        body.put("messages", messages);
        return body;
    }

    // Busca web via DuckDuckGo (sem API key)
    public void webSearch(String query, WebSearchCallback callback) {
        new Thread(() -> {
            try {
                String encoded = java.net.URLEncoder.encode(query, "UTF-8");
                String url = "https://api.duckduckgo.com/?q=" + encoded + "&format=json&no_redirect=1&no_html=1";
                Request req = new Request.Builder().url(url).get().build();
                try (Response res = client.newCall(req).execute()) {
                    if (res.body() != null) {
                        JSONObject json = new JSONObject(res.body().string());
                        StringBuilder result = new StringBuilder();
                        String abstract_ = json.optString("Abstract", "");
                        if (!abstract_.isEmpty()) result.append(abstract_).append("\n\n");
                        JSONArray related = json.optJSONArray("RelatedTopics");
                        if (related != null) {
                            for (int i = 0; i < Math.min(5, related.length()); i++) {
                                JSONObject topic = related.optJSONObject(i);
                                if (topic != null) {
                                    String text = topic.optString("Text", "");
                                    if (!text.isEmpty()) result.append("‚Ä¢ ").append(text).append("\n");
                                }
                            }
                        }
                        callback.onResult(result.toString().isEmpty() ? "Sem resultados para: " + query : result.toString());
                    }
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public interface WebSearchCallback {
        void onResult(String result);
        void onError(String error);
    }
}
