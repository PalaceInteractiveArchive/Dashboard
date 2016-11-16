package com.palacemc.dashboard.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.handlers.Player;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.palacemc.dashboard.handlers.Rank;

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
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
        String ip = in.readLine();
        HttpServer server = HttpServer.create(new InetSocketAddress(ip, 7319), 0);
        server.createContext("/", this);
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            List<String> owners = new ArrayList<>();
            List<String> mayors = new ArrayList<>();
            List<String> managers = new ArrayList<>();
            List<String> developers = new ArrayList<>();
            List<String> coordinators = new ArrayList<>();
            List<String> castmembers = new ArrayList<>();
            List<String> earningmyears = new ArrayList<>();
            for (Player tp : Dashboard.getOnlinePlayers()) {
                Rank r = tp.getRank();
                if (r.getRankId() >= Rank.SQUIRE.getRankId()) {
                    switch (r) {
                        case SQUIRE:
                            earningmyears.add(tp.getName());
                            break;
                        case KNIGHT:
                            castmembers.add(tp.getName());
                            break;
                        case PALADIN:
                            coordinators.add(tp.getName());
                            break;
                        case WIZARD:
                            developers.add(tp.getName());
                            break;
                        case EMPEROR:
                            managers.add(tp.getName());
                            break;
                        case EMPRESS:
                            managers.add(tp.getName());
                            break;
                    }
                }
            }
            Collections.sort(owners);
            Collections.sort(mayors);
            Collections.sort(managers);
            Collections.sort(developers);
            Collections.sort(coordinators);
            Collections.sort(castmembers);
            Collections.sort(earningmyears);
            JsonArray array = new JsonArray();
            int n = 0;
            if (!owners.isEmpty()) {
                String names = "";
                for (int i = 0; i < owners.size(); i++) {
                    names += owners.get(i);
                    if (i < (owners.size() - 1)) {
                        names += ", ";
                    }
                }
            }
            if (!mayors.isEmpty()) {
                String names = "";
                for (int i = 0; i < mayors.size(); i++) {
                    names += mayors.get(i);
                    if (i < (mayors.size() - 1)) {
                        names += ", ";
                    }
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("title", "Mayor");
                obj.addProperty("text", names);
                obj.addProperty("color", getColor(Rank.EMPRESS));
                array.add(obj);
            }
            if (!managers.isEmpty()) {
                String names = "";
                for (int i = 0; i < managers.size(); i++) {
                    names += managers.get(i);
                    if (i < (managers.size() - 1)) {
                        names += ", ";
                    }
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("title", "Managers");
                obj.addProperty("text", names);
                obj.addProperty("color", getColor(Rank.EMPEROR));
                array.add(obj);
            }
            if (!developers.isEmpty()) {
                String names = "";
                for (int i = 0; i < developers.size(); i++) {
                    names += developers.get(i);
                    if (i < (developers.size() - 1)) {
                        names += ", ";
                    }
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("title", "Developers");
                obj.addProperty("text", names);
                obj.addProperty("color", getColor(Rank.WIZARD));
                array.add(obj);
            }
            if (!coordinators.isEmpty()) {
                String names = "";
                for (int i = 0; i < coordinators.size(); i++) {
                    names += coordinators.get(i);
                    if (i < (coordinators.size() - 1)) {
                        names += ", ";
                    }
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("title", "Coordinators");
                obj.addProperty("text", names);
                obj.addProperty("color", getColor(Rank.PALADIN));
                array.add(obj);
            }
            if (!castmembers.isEmpty()) {
                String names = "";
                for (int i = 0; i < castmembers.size(); i++) {
                    names += castmembers.get(i);
                    if (i < (castmembers.size() - 1)) {
                        names += ", ";
                    }
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("title", "Cast Members");
                obj.addProperty("text", names);
                obj.addProperty("color", getColor(Rank.KNIGHT));
                array.add(obj);
            }
            if (!earningmyears.isEmpty()) {
                String names = "";
                for (int i = 0; i < earningmyears.size(); i++) {
                    names += earningmyears.get(i);
                    if (i < (earningmyears.size() - 1)) {
                        names += ", ";
                    }
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("title", "Earning My Ears");
                obj.addProperty("text", names);
                obj.addProperty("color", getColor(Rank.SQUIRE));
                array.add(obj);
            }
            JsonObject obj = new JsonObject();
            if (array.size() > 0) {
                try {
                    obj.addProperty("text", "Current online Cast Members");
                    obj.addProperty("response_type", "ephemeral");
                    obj.add("attachments", array);
                } catch (Exception e) {
                    return;
                }
            } else {
                try {
                    obj.addProperty("text", "No Cast Members are currently online!");
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
            e.printStackTrace();
        }
    }

    private String getColor(Rank rank) {
        switch (rank) {
            case EMPEROR:
                return "#FFAA00";
            case EMPRESS:
                return "#FFAA00";
            case WIZARD:
                return "#FFAA00";
            case PALADIN:
                return "#55FF55";
            case KNIGHT:
                return "#55FF55";
            case SQUIRE:
                return "#55FF55";
        }
        return "good";
    }
}