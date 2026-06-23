package com.skerr.ai.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.skerr.ai.model.Conversation;
import com.skerr.ai.model.Message;
import org.json.*;
import java.util.*;

public class ConversationRepository {
    private static final String PREFS_NAME = "skerr_convs";
    private static final String KEY_CONV_LIST = "conv_list";
    private final SharedPreferences prefs;
    private final Map<String, Conversation> cache = new LinkedHashMap<>();

    public ConversationRepository(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadAll();
    }

    private void loadAll() {
        String json = prefs.getString(KEY_CONV_LIST, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Conversation conv = new Conversation(obj.getString("id"), obj.getString("title"));
                JSONArray msgs = obj.optJSONArray("messages");
                if (msgs != null) {
                    for (int j = 0; j < msgs.length(); j++) {
                        JSONObject m = msgs.getJSONObject(j);
                        Message msg = new Message(
                            m.getString("id"), m.getString("content"),
                            m.getInt("type"), m.getLong("timestamp")
                        );
                        conv.addMessage(msg);
                    }
                }
                cache.put(conv.getId(), conv);
            }
        } catch (JSONException e) { e.printStackTrace(); }
    }

    public void saveAll() {
        try {
            JSONArray arr = new JSONArray();
            for (Conversation conv : cache.values()) {
                JSONObject obj = new JSONObject();
                obj.put("id", conv.getId());
                obj.put("title", conv.getTitle());
                obj.put("createdAt", conv.getCreatedAt());
                obj.put("updatedAt", conv.getUpdatedAt());
                JSONArray msgs = new JSONArray();
                for (Message m : conv.getMessages()) {
                    JSONObject mo = new JSONObject();
                    mo.put("id", m.getId());
                    mo.put("content", m.getContent());
                    mo.put("type", m.getType());
                    mo.put("timestamp", m.getTimestamp());
                    msgs.put(mo);
                }
                obj.put("messages", msgs);
                arr.put(obj);
            }
            prefs.edit().putString(KEY_CONV_LIST, arr.toString()).apply();
        } catch (JSONException e) { e.printStackTrace(); }
    }

    public Conversation createConversation(String title) {
        String id = UUID.randomUUID().toString();
        Conversation conv = new Conversation(id, title);
        cache.put(id, conv);
        saveAll();
        return conv;
    }

    public List<Conversation> getAllConversations() {
        List<Conversation> list = new ArrayList<>(cache.values());
        Collections.sort(list, (a, b) -> Long.compare(b.getUpdatedAt(), a.getUpdatedAt()));
        return list;
    }

    public Conversation getConversation(String id) {
        return cache.get(id);
    }

    public void deleteConversation(String id) {
        cache.remove(id);
        saveAll();
    }

    public void updateConversation(Conversation conv) {
        cache.put(conv.getId(), conv);
        saveAll();
    }
}
