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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            List<String> director = new ArrayList<>();
            List<String> manager = new ArrayList<>();
            List<String> admin = new ArrayList<>();
            List<String> developer = new ArrayList<>();
            List<String> coordinator = new ArrayList<>();
            List<String> technician = new ArrayList<>();
            List<String> builder = new ArrayList<>();
            List<String> mod = new ArrayList<>();
            List<String> trainee = new ArrayList<>();
            for (Player tp : dashboard.getOnlinePlayers()) {
                Rank r = tp.getRank();
                if (r.getRankId() >= Rank.TRAINEE.getRankId()) {
                    switch (r) {
                        case TRAINEEBUILD:
                        case TRAINEETECH:
                        case TRAINEE:
                            trainee.add(tp.getUsername());
                            break;
                        case BUILDER:
                            builder.add(tp.getUsername());
                            break;
                        case TECHNICIAN:
                            technician.add(tp.getUsername());
                            break;
                        case MOD:
                            mod.add(tp.getUsername());
                            break;
                        case COORDINATOR:
                            coordinator.add(tp.getUsername());
                            break;
                        case DEVELOPER:
                            developer.add(tp.getUsername());
                            break;
                        case LEAD:
                            admin.add(tp.getUsername());
                            break;
                        case MANAGER:
                            manager.add(tp.getUsername());
                            break;
                        case DIRECTOR:
                            director.add(tp.getUsername());
                    }
                }
            }
            Collections.sort(director);
            Collections.sort(manager);
            Collections.sort(admin);
            Collections.sort(developer);
            Collections.sort(coordinator);
            Collections.sort(technician);
            Collections.sort(builder);
            Collections.sort(mod);
            Collections.sort(trainee);
            JsonArray array = new JsonArray();
            int n = 0;
            if (!director.isEmpty()) {
                StringBuilder names = new StringBuilder();
                for (int i = 0; i < director.size(); i++) {
                    names.append(director.get(i));
                    if (i < (director.size() - 1)) {
                        names.append(", ");
                    }
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("title", "Director");
                obj.addProperty("text", names.toString());
                obj.addProperty("color", getColor(Rank.DIRECTOR));
                array.add(obj);
            }
            if (!manager.isEmpty()) {
                StringBuilder names = new StringBuilder();
                for (int i = 0; i < manager.size(); i++) {
                    names.append(manager.get(i));
                    if (i < (manager.size() - 1)) {
                        names.append(", ");
                    }
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("title", "Manager");
                obj.addProperty("text", names.toString());
                obj.addProperty("color", getColor(Rank.MANAGER));
                array.add(obj);
            }
            if (!admin.isEmpty()) {
                StringBuilder names = new StringBuilder();
                for (int i = 0; i < admin.size(); i++) {
                    names.append(admin.get(i));
                    if (i < (admin.size() - 1)) {
                        names.append(", ");
                    }
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("title", "Admin");
                obj.addProperty("text", names.toString());
                obj.addProperty("color", getColor(Rank.LEAD));
                array.add(obj);
            }
            if (!developer.isEmpty()) {
                StringBuilder names = new StringBuilder();
                for (int i = 0; i < developer.size(); i++) {
                    names.append(developer.get(i));
                    if (i < (developer.size() - 1)) {
                        names.append(", ");
                    }
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("title", "Developer");
                obj.addProperty("text", names.toString());
                obj.addProperty("color", getColor(Rank.DEVELOPER));
                array.add(obj);
            }
            if (!coordinator.isEmpty()) {
                StringBuilder names = new StringBuilder();
                for (int i = 0; i < coordinator.size(); i++) {
                    names.append(coordinator.get(i));
                    if (i < (coordinator.size() - 1)) {
                        names.append(", ");
                    }
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("title", "Coordinator");
                obj.addProperty("text", names.toString());
                obj.addProperty("color", getColor(Rank.COORDINATOR));
                array.add(obj);
            }
            if (!technician.isEmpty()) {
                StringBuilder names = new StringBuilder();
                for (int i = 0; i < technician.size(); i++) {
                    names.append(technician.get(i));
                    if (i < (technician.size() - 1)) {
                        names.append(", ");
                    }
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("title", "Technician");
                obj.addProperty("text", names.toString());
                obj.addProperty("color", getColor(Rank.TECHNICIAN));
                array.add(obj);
            }
            if (!builder.isEmpty()) {
                StringBuilder names = new StringBuilder();
                for (int i = 0; i < builder.size(); i++) {
                    names.append(builder.get(i));
                    if (i < (builder.size() - 1)) {
                        names.append(", ");
                    }
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("title", "Builder");
                obj.addProperty("text", names.toString());
                obj.addProperty("color", getColor(Rank.BUILDER));
                array.add(obj);
            }
            if (!mod.isEmpty()) {
                StringBuilder names = new StringBuilder();
                for (int i = 0; i < mod.size(); i++) {
                    names.append(mod.get(i));
                    if (i < (mod.size() - 1)) {
                        names.append(", ");
                    }
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("title", "Mod");
                obj.addProperty("text", names.toString());
                obj.addProperty("color", getColor(Rank.MOD));
                array.add(obj);
            }
            if (!trainee.isEmpty()) {
                StringBuilder names = new StringBuilder();
                for (int i = 0; i < trainee.size(); i++) {
                    names.append(trainee.get(i));
                    if (i < (trainee.size() - 1)) {
                        names.append(", ");
                    }
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("title", "Trainee");
                obj.addProperty("text", names.toString());
                obj.addProperty("color", getColor(Rank.TRAINEE));
                array.add(obj);
            }
            JsonObject obj = new JsonObject();
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
            case LEAD:
            case MANAGER:
            case DIRECTOR:
                return "#FF5050";
            case DEVELOPER:
                return "#FFAA00";
            case COORDINATOR:
            case ARCHITECT:
                return "#FFFF00";
            case BUILDER:
            case MOD:
                return "#00FF00";
            case TRAINEE:
                return "#009933";
        }
        return "good";
    }
}