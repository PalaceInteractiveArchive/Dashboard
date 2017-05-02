package network.palace.dashboard.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import network.palace.dashboard.handlers.Player;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * @author Innectic
 * @since 5/2/2017
 */
public class MCLeakUtil {

    public static boolean checkPlayer(Player player) {
        String url = "https://mcleaks.themrgong.xyz/api/v3/isuuidmcleaks/" + player.getUuid().toString();

        try (InputStream inputStream = new URL(url).openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String text = readAll(reader);
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(text).getAsJsonObject();

            return object.has("isMcleaks") && object.get("isMcleaks").getAsBoolean();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String readAll(Reader rd) {
        StringBuilder sb = new StringBuilder();
        int cp;
        try {
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
