package com.palacemc.dashboard.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Marc on 9/25/16
 */
public class SiteUtil implements HttpHandler {

    private Dashboard dashboard = Launcher.getDashboard();

    public SiteUtil() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 7319), 0);

        server.createContext("/", this);
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            List<String> empress = new ArrayList<>();
            List<String> emperors = new ArrayList<>();
            List<String> wizards = new ArrayList<>();
            List<String> paladins = new ArrayList<>();
            List<String> architects = new ArrayList<>();
            List<String> knights = new ArrayList<>();
            List<String> squires = new ArrayList<>();

            for (Player tp : dashboard.getOnlinePlayers()) {
                Rank r = tp.getRank();
                if (r.getRankId() >= Rank.SQUIRE.getRankId()) {
                    switch (r) {
                        case SQUIRE:
                            squires.add(tp.getUsername());
                            break;
                        case KNIGHT:
                            knights.add(tp.getUsername());
                            break;
                        case ARCHITECT:
                            architects.add(tp.getUsername());
                            break;
                        case PALADIN:
                            paladins.add(tp.getUsername());
                            break;
                        case WIZARD:
                            wizards.add(tp.getUsername());
                            break;
                        case EMPEROR:
                            emperors.add(tp.getUsername());
                            break;
                        case EMPRESS:
                            empress.add(tp.getUsername());
                    }
                }
            }
            Collections.sort(empress);
            Collections.sort(emperors);
            Collections.sort(wizards);
            Collections.sort(paladins);
            Collections.sort(architects);
            Collections.sort(knights);
            Collections.sort(squires);

            JsonArray array = new JsonArray();

            int n = 0;
            if (!empress.isEmpty()) {
                String names = "";

                for (int i = 0; i < empress.size(); i++) {
                    names += empress.get(i);
                    if (i < (empress.size() - 1)) {
                        names += ", ";
                    }
                }

                JsonObject obj = new JsonObject();

                obj.addProperty("title", "Empress");
                obj.addProperty("text", names);
                obj.addProperty("color", getColor(Rank.EMPRESS));
                array.add(obj);
            }

            if (!emperors.isEmpty()) {
                String names = "";

                for (int i = 0; i < emperors.size(); i++) {
                    names += emperors.get(i);
                    if (i < (emperors.size() - 1)) {
                        names += ", ";
                    }
                }

                JsonObject obj = new JsonObject();

                obj.addProperty("title", "Emperor");
                obj.addProperty("text", names);
                obj.addProperty("color", getColor(Rank.EMPEROR));
                array.add(obj);
            }

            if (!wizards.isEmpty()) {
                String names = "";

                for (int i = 0; i < wizards.size(); i++) {
                    names += wizards.get(i);
                    if (i < (wizards.size() - 1)) {
                        names += ", ";
                    }
                }

                JsonObject obj = new JsonObject();

                obj.addProperty("title", "Wizard");
                obj.addProperty("text", names);
                obj.addProperty("color", getColor(Rank.WIZARD));
                array.add(obj);
            }

            if (!paladins.isEmpty()) {
                String names = "";

                for (int i = 0; i < paladins.size(); i++) {
                    names += paladins.get(i);
                    if (i < (paladins.size() - 1)) {
                        names += ", ";
                    }
                }

                JsonObject obj = new JsonObject();

                obj.addProperty("title", "Paladin");
                obj.addProperty("text", names);
                obj.addProperty("color", getColor(Rank.PALADIN));
                array.add(obj);
            }

            if (!architects.isEmpty()) {
                String names = "";

                for (int i = 0; i < architects.size(); i++) {
                    names += architects.get(i);
                    if (i < (architects.size() - 1)) {
                        names += ", ";
                    }
                }

                JsonObject obj = new JsonObject();

                obj.addProperty("title", "Architect");
                obj.addProperty("text", names);
                obj.addProperty("color", getColor(Rank.ARCHITECT));
                array.add(obj);
            }
            if (!knights.isEmpty()) {
                String names = "";

                for (int i = 0; i < knights.size(); i++) {
                    names += knights.get(i);
                    if (i < (knights.size() - 1)) {
                        names += ", ";
                    }
                }

                JsonObject obj = new JsonObject();

                obj.addProperty("title", "Knight");
                obj.addProperty("text", names);
                obj.addProperty("color", getColor(Rank.KNIGHT));
                array.add(obj);
            }

            if (!squires.isEmpty()) {
                String names = "";
                for (int i = 0; i < squires.size(); i++) {
                    names += squires.get(i);
                    if (i < (squires.size() - 1)) {
                        names += ", ";
                    }
                }
                JsonObject obj = new JsonObject();
                obj.addProperty("title", "Squire");
                obj.addProperty("text", names);
                obj.addProperty("color", getColor(Rank.SQUIRE));
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
            e.printStackTrace();
        }
    }

    private String getColor(Rank rank) {
        switch (rank) {
            case EMPRESS:
            case EMPEROR:
                return "#FFAA00";
            case WIZARD:
                return "#FFAA00";
            case PALADIN:
                return "#FFAA00";
            case ARCHITECT:
            case KNIGHT:
                return "#FFAA00";
            case SQUIRE:
                return "#55FF55";
        }
        return "good";
    }
}