package com.palacemc.dashboard.packets.bungee;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 12/3/16
 */
public class PacketPlayerListInfo extends BasePacket {
    @Getter private List<Player> players = new ArrayList<>();

    public PacketPlayerListInfo() {
        this(new ArrayList<>());
    }

    public PacketPlayerListInfo(List<Player> players) {
        this.id = PacketID.Bungee.PLAYERLISTINFO.getId();
        this.players = players;
    }

    public PacketPlayerListInfo fromJSON(JsonObject object) {
        this.id = object.get("id").getAsInt();
        JsonArray list = object.get("players").getAsJsonArray();

        for (JsonElement e : list) {
            Player p = fromString(e.toString());
            this.players.add(p);
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            Gson gson = new Gson();

            object.addProperty("id", this.id);
            List<String> list = new ArrayList<>();

            for (Player p : players) {
                list.add(p.toString());
            }

            object.add("players", gson.toJsonTree(list).getAsJsonArray());
        } catch (Exception e) {
            return null;
        }
        return object;
    }

    public Player fromString(String playerName) {
        Player player = new Player();
        playerName = playerName.replace("\"Player{", "").replace("}\"", "");
        String[] list = playerName.split(",");

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
                                player.setUuid(UUID.fromString(str));
                            } catch (Exception ignored) {
                            }
                            break;
                        case "username":
                            player.setUsername(str);
                            break;
                        case "address":
                            player.setAddress(str);
                            break;
                        case "server":
                            player.setServer(str);
                            break;
                        case "rank":
                            player.setRank(str);
                            break;
                    }
                }
            }
        }
        return player;
    }

    public static class Player {
        @Getter @Setter private UUID uuid;
        @Getter @Setter private String username;
        @Getter @Setter private String address;
        @Getter @Setter private String server;
        @Getter @Setter private String rank;

        public Player(UUID uuid, String username, String address, String server, String rank) {
            this.uuid = uuid;
            this.username = username;
            this.address = address;
            this.server = server;
            this.rank = rank;
        }

        public Player() { }

        @Override
        public String toString() {
            return "Player{uuid=" + uuid.toString() + ",username=" + username + ",address=" + address + ",server=" +
                    server + ",rank=" + rank + "}";
        }
    }
}