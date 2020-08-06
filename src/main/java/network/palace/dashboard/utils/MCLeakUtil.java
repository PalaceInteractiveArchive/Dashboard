package network.palace.dashboard.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.Player;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Innectic
 * @since 5/2/2017
 */
public class MCLeakUtil {

    public static boolean checkPlayer(Player player) {
        String url = "https://mcleaks.themrgong.xyz/api/v3/isuuidmcleaks/" + player.getUuid().toString();

        try (InputStream inputStream = new URL(url).openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String text = readAll(reader);
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(text).getAsJsonObject();

            return object.has("isMcleaks") && object.get("isMcleaks").getAsBoolean();
        } catch (IOException e) {
            Launcher.getDashboard().getLogger().error("Error checking player with MCLeaks", e);
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
            Launcher.getDashboard().getLogger().error("Error reading stream", e);
        }
        return sb.toString();
    }

}
