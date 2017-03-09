package network.palace.dashboard.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import network.palace.dashboard.Dashboard;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Created by Marc on 3/5/17.
 */
public class IPUtil {
    private static HashMap<String, Match> cache = new HashMap<>();
    private static int count = 0;
    private static long lastReset = System.currentTimeMillis();

    public static ProviderData getProviderData(String address) {
        if (address.isEmpty()) {
            Dashboard.getLogger().info("Empty address!");
            return null;
        }
        if (System.currentTimeMillis() - (60 * 1000) > lastReset) {
            lastReset = System.currentTimeMillis();
            count = 0;
            Dashboard.getLogger().info("Over one minute");
        }
        if (count >= 149) {
            Dashboard.getLogger().info("count >= 149");
            return null;
        }
        if (cache.containsKey(address)) {
            Match m = cache.get(address);
            if (System.currentTimeMillis() - (6 * 60 * 60 * 1000) < m.getTime()) {
                Dashboard.getLogger().info("Cached value: " + address + " -> " + m.getData().toString());
                return m.getData();
            }
            cache.remove(address);
        }
        count++;
        Match m = new Match(request(address));
        cache.put(address, m);
        Dashboard.getLogger().info("New request: " + address + " -> " + m.getData().toString());
        return m.getData();
    }

    private static ProviderData request(String address) {
        String url = "http://ip-api.com/json/" + address + "?fields=33550";
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
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
            e.printStackTrace();
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