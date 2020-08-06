package network.palace.dashboard.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Created by Marc on 3/5/17.
 */
public class IPUtil {
    private static HashMap<String, Match> cache = new HashMap<>();
    private static int count = 0;
    private static long lastReset = System.currentTimeMillis();

    public IPUtil() {
        /*
        If 5+ accounts connect within 5 minutes
         *
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
            }
        }, 0L, 1000L);*/
    }

    public static ProviderData getProviderData(String address) {
        Dashboard dashboard = Launcher.getDashboard();
        if (address.isEmpty()) {
            dashboard.getLogger().info("Empty address!");
            return null;
        }
        if (System.currentTimeMillis() - (60 * 1000) > lastReset) {
            lastReset = System.currentTimeMillis();
            count = 0;
            dashboard.getLogger().info("Over one minute");
        }
        if (count >= 150) {
            dashboard.getLogger().info("count >= 150");
            return null;
        }
        if (cache.containsKey(address)) {
            Match m = cache.get(address);
            if (System.currentTimeMillis() - (6 * 60 * 60 * 1000) < m.getTime()) {
                dashboard.getLogger().info("Cached value: " + address + " -> " + m.getData().toString());
                return m.getData();
            }
            cache.remove(address);
        }
        count++;
        Match m = new Match(request(address));
        if (m == null) {
            dashboard.getLogger().info("Error requesting provider info for " + address + "!");
            return null;
        }
        cache.put(address, m);
        dashboard.getLogger().info("New request: " + address + " -> " + m.getData().toString());
        return m.getData();
    }

    private static ProviderData request(String address) {
        String url = "http://ip-api.com/json/" + address + "?fields=33550";
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(jsonText).getAsJsonObject();
            if (obj.has("message")) {
                return null;
            }
            return new ProviderData(obj.get("isp").getAsString(), obj.get("countryCode").getAsString(),
                    obj.get("region").getAsString(), obj.get("regionName").getAsString(),
                    obj.get("timezone").getAsString());
        } catch (IOException e) {
            Launcher.getDashboard().getLogger().error("Error retrieving IP address", e);
            return null;
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    @AllArgsConstructor
    public static class ProviderData {
        @Getter private String isp;
        @Getter private String country;
        @Getter private String region;
        @Getter private String regionName;
        @Getter private String timezone;

        @Override
        public String toString() {
            return "isp:" + isp + ";country:" + country + ";region:" + region + ";regionName:" + regionName +
                    ";timezone:" + timezone;
        }
    }

    private static class Match {
        @Getter private ProviderData data;
        @Getter private long time;

        public Match(ProviderData data) {
            this.data = data;
            this.time = System.currentTimeMillis();
        }
    }
}
