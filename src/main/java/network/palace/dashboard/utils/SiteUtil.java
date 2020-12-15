package network.palace.dashboard.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.*;

/**
 * Created by Marc on 9/25/16
 */
public class SiteUtil implements HttpHandler {

    public SiteUtil() throws IOException {
        URL whatismyip = new URL("https://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
        String ip = in.readLine();
        HttpServer server = HttpServer.create(new InetSocketAddress(ip, 7319), 0);
        server.createContext("/", this);
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Dashboard dashboard = Launcher.getDashboard();
        try {
            JsonObject obj = new JsonObject();
            TreeMap<Rank, List<String>> users = new TreeMap<>(Comparator.comparingInt(Enum::ordinal));
            for (Player tp : dashboard.getOnlinePlayers()) {
                Rank r = tp.getRank();
                if (r.getRankId() < Rank.TRAINEE.getRankId()) continue;
                if (!users.containsKey(r)) {
                    List<String> names = new ArrayList<>();
                    names.add(tp.getUsername());
                    users.put(r, names);
                } else {
                    users.get(r).add(tp.getUsername());
                }
            }
            JsonArray array = new JsonArray();
            for (Map.Entry<Rank, List<String>> entry : users.entrySet()) {
                List<String> list = entry.getValue();
                list.sort(Comparator.comparing(String::toLowerCase));

                StringBuilder names = new StringBuilder();
                for (int i = 0; i < list.size(); i++) {
                    names.append(list.get(i));
                    if (i < (list.size() - 1)) {
                        names.append(", ");
                    }
                }
                JsonObject o = new JsonObject();
                o.addProperty("title", entry.getKey().getName());
                o.addProperty("text", names.toString());
                o.addProperty("color", getColor(entry.getKey()));
                array.add(o);
            }
            if (array.size() > 0) {
                try {
                    obj.addProperty("text", "Current online Staff Members");
                    obj.addProperty("response_type", "ephemeral");
                    obj.add("attachments", array);
                } catch (Exception e) {
                    return;
                }
            } else {
                try {
                    obj.addProperty("text", "No Staff Members are currently online!");
                    obj.addProperty("response_type", "ephemeral");
                } catch (Exception e) {
                    return;
                }
            }
            Headers headers = httpExchange.getResponseHeaders();
            headers.add("Content-Type", "application/json");
            String response = obj.toString();
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (Exception e) {
            Launcher.getDashboard().getLogger().error("Error responding to stafflist API", e);
        }
    }

    private String getColor(Rank rank) {
        switch (rank) {
            case MANAGER:
            case DIRECTOR:
            case OWNER:
                // red
                return "#FF5050";
            case LEAD:
                // gold
                return "#FFAA00";
            case TRAINEETECH:
            case TRAINEEBUILD:
            case MEDIA:
            case BUILDER:
            case TECHNICIAN:
            case DEVELOPER:
                // blue
                return "#0000FF";
            case COORDINATOR:
                // yellow
                return "#FFFF00";
            case MOD:
                // green
                return "#00FF00";
            case TRAINEE:
                // dark green
                return "#009933";
        }
        // default
        return "good";
    }
}