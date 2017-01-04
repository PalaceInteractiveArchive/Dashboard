package network.palace.dashboard.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marc on 9/10/16
 */
public class NameUtil {

    public static List<String> getNames(String name) {
        List<String> names = new ArrayList<>();
        try {
            String webData = readUrl("https://api.mojang.com/users/profiles/minecraft/" + name);
            Gson gson = new Gson();
            JsonObject uuidData = gson.fromJson(webData, JsonObject.class);
            String uuid = "";
            if (uuidData != null) {
                uuid = uuidData.get("id").getAsString();
            }
            if (!uuid.equals("")) {
                List<String> list = getNames(name, uuid);
                for (String s : list) {
                    names.add(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return names;
    }

    public static List<String> getNames(String n, String uuid) throws Exception {
        Gson gson = new Gson();
        List<String> names = new ArrayList<>();
        String namesData = readUrl("https://api.mojang.com/user/profiles/" + uuid + "/names");
        JsonArray pastNames = gson.fromJson(namesData, JsonArray.class);
        names.add(uuid);
        for (JsonElement pastName : pastNames) {
            JsonElement element = gson.fromJson(pastName, JsonElement.class);
            JsonObject nameObj = element.getAsJsonObject();
            String name = nameObj.get("name").getAsString();
            names.add(name);
        }
        return names;
    }

    public static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) buffer.append(chars, 0, read);
            return buffer.toString();
        } finally {
            if (reader != null) reader.close();
        }
    }
}