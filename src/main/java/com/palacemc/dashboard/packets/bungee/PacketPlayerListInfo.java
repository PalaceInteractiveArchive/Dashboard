package com.palacemc.dashboard.packets.bungee;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 12/3/16
 */
public class PacketPlayerListInfo extends BasePacket {
    private List<Player> players = new ArrayList<>();

    public PacketPlayerListInfo() {
        this(new ArrayList<Player>());
    }

    public PacketPlayerListInfo(List<Player> players) {
        this.id = PacketID.Bungee.PLAYERLISTINFO.getID();
        this.players = players;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public PacketPlayerListInfo fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        JsonArray list = obj.get("players").getAsJsonArray();
        for (JsonElement e : list) {
            Player p = fromString(e.toString());
            this.players.add(p);
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            Gson gson = new Gson();
            List<String> list = new ArrayList<>();
            for (Player p : players) {
                list.add(p.toString());
            }
            obj.add("players", gson.toJsonTree(list).getAsJsonArray());
        } catch (Exception e) {
            return null;
        }
        return obj;
    }

    public Player fromString(String s) {
        Player p = new Player();
        String sr = s.replace("\"Player{", "").replace("}\"", "");
        String[] list = sr.split(",");
        for (String st : list) {
            String[] list2 = st.split("=");
            String next = "";
            boolean first = true;
            for (String str : list2) {
                if (first) {
                    next = str;
                    first = false;
                } else {
                    switch (next.toLowerCase()) {
                        case "uuid":
                            try {
                                p.setUniqueId(UUID.fromString(str));
                            } catch (Exception ignored) {
                            }
                            break;
                        case "username":
                            p.setUsername(str);
                            break;
                        case "address":
                            p.setAddress(str);
                            break;
                        case "server":
                            p.setServer(str);
                            break;
                        case "rank":
                            p.setRank(str);
                            break;
                    }
                }
            }
        }
        return p;
    }

    public static class Player {
        private UUID uuid;
        private String username;
        private String address;
        private String server;
        private String rank;

        public Player(UUID uuid, String username, String address, String server, String rank) {
            this.uuid = uuid;
            this.username = username;
            this.address = address;
            this.server = server;
            this.rank = rank;
        }

        public Player() {
        }

        public UUID getUniqueId() {
            return uuid;
        }

        public String getUsername() {
            return username;
        }

        public String getAddress() {
            return address;
        }

        public String getServer() {
            return server;
        }

        public String getRank() {
            return rank;
        }

        public void setUniqueId(UUID uuid) {
            this.uuid = uuid;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public void setRank(String rank) {
            this.rank = rank;
        }

        @Override
        public String toString() {
            return "Player{uuid=" + uuid.toString() + ",username=" + username + ",address=" + address + ",server=" +
                    server + ",rank=" + rank + "}";
        }
    }
}