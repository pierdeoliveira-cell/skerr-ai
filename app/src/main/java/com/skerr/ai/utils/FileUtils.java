package com.skerr.ai.utils;

import android.content.Context;
import android.net.Uri;
import java.io.*;

public class FileUtils {
    public static String readTextFromUri(Context context, Uri uri) {
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is == null) return null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getFileName(Context context, Uri uri) {
        String name = uri.getLastPathSegment();
        if (name == null) name = "arquivo";
        return name;
    }

    public static boolean isImage(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public static boolean isPdf(String mimeType) {
        return "application/pdf".equals(mimeType);
    }

    public static boolean isAudio(String mimeType) {
        return mimeType != null && mimeType.startsWith("audio/");
    }
}
