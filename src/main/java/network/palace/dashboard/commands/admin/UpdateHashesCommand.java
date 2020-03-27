package network.palace.dashboard.commands.admin;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import org.bson.Document;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Marc on 3/26/17.
 */
public class UpdateHashesCommand extends DashboardCommand {

    public UpdateHashesCommand() {
        super(Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        dashboard.getSchedulerManager().runAsync(() -> {
            player.sendMessage(ChatColor.GREEN + "Requesting Resource Pack list from database...");
            try {
                HashMap<String, ResourcePack> list = new HashMap<>();

                for (Document doc : dashboard.getMongoHandler().getResourcePackCollection().find()) {
                    String name = doc.getString("name");
                    ResourcePack pack = new ResourcePack(name);
                    List<ResourcePack.Version> versions = new ArrayList<>();

                    if (doc.containsKey("versions")) {
                        for (Object o : doc.get("versions", ArrayList.class)) {
                            Document version = (Document) o;
                            int protocolId = version.getInteger("id");

                            versions.add(pack.generateVersion(protocolId, version.getString("url"), version.containsKey("hash") ? version.getString("hash") : ""));
                        }
                    } else {
                        versions.add(pack.generateVersion(-1, doc.getString("url"), doc.containsKey("hash") ? doc.getString("hash") : ""));
                    }
                    pack.setVersions(versions);

                    list.put(name, pack);
                }

                File dir = new File("packs");
                if (!dir.exists()) {
                    dir.mkdir();
                    player.sendMessage(ChatColor.GREEN + "Creating download folder...");
                } else {
                    if (!dir.isDirectory()) {
                        dir.delete();
                        dir.mkdir();
                    }
                    player.sendMessage(ChatColor.GREEN + "Clearing download folder...");
                    for (File file : dir.listFiles()) {
                        if (!file.isDirectory()) {
                            file.delete();
                        }
                    }
                }

                for (ResourcePack pack : list.values()) {
                    for (ResourcePack.Version version : pack.getVersions()) {
                        try {
                            player.sendMessage(ChatColor.GREEN + "Downloading " + version.getName() + " from " + version.getUrl());
                            String path = "packs/" + version.getName() + System.currentTimeMillis();
                            URL website = new URL(version.getUrl());
                            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                            FileOutputStream fos = new FileOutputStream(path);
                            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                            File f = new File(path);
                            if (!f.exists()) {
                                player.sendMessage(ChatColor.RED + "There was an error downloading " + version.getName() + "!");
                            } else {
                                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                                InputStream fis = new FileInputStream(f);
                                int n = 0;
                                byte[] buffer = new byte[8192];
                                while (n != -1) {
                                    n = fis.read(buffer);
                                    if (n > 0) {
                                        digest.update(buffer, 0, n);
                                    }
                                }
                                String hash = DatatypeConverter.printHexBinary(digest.digest()).toLowerCase();
                                if (!hash.equals(version.getHash())) {
                                    version.setHash(hash);
                                    version.setUpdated(true);
                                }
                                player.sendMessage(ChatColor.GREEN + "SHA-1 hash for " + version.getName() + " is " + hash);
                            }
                        } catch (IOException | NoSuchAlgorithmException e) {
                            e.printStackTrace();
                            player.sendMessage(ChatColor.RED + e.getClass().getName() + e.getMessage());
                        }
                    }
                }

                for (ResourcePack pack : list.values()) {
                    for (ResourcePack.Version version : pack.getVersions()) {
                        if (!version.isUpdated()) continue;
                        if (version.getProtocolId() == -1) {
                            dashboard.getMongoHandler().getResourcePackCollection().updateOne(Filters.eq("name",
                                    pack.getName()), Updates.set("hash", version.getHash()));
                        } else {
                            dashboard.getMongoHandler().getResourcePackCollection()
                                    .updateOne(Filters.and(
                                            Filters.eq("name", pack.getName()),
                                            Filters.elemMatch("versions",
                                                    Filters.eq("id", version.getProtocolId()))
                                            ),
                                            Updates.set("versions.$.hash", version.getHash()));
                        }
                        player.sendMessage(ChatColor.YELLOW + "Updated hash for " + version.getName());
                    }
                }

                player.sendMessage(ChatColor.GREEN + "Clearing download folder...");
                for (File file : dir.listFiles()) {
                    if (!file.isDirectory()) {
                        file.delete();
                    }
                }
                dir.delete();
                player.sendMessage(ChatColor.BLUE + "Task complete");
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "There was an error processing the resource packs!");
            }
        });
    }

    @Getter
    @Setter
    private class ResourcePack {
        private String name;
        private List<Version> versions = new ArrayList<>();

        public ResourcePack(String name) {
            this.name = name;
        }

        public Version generateVersion(int protocolId, String url, String hash) {
            return new Version(protocolId, url, hash, false);
        }

        @Getter
        @AllArgsConstructor
        protected class Version {
            private int protocolId; //This is the highest protocol id this pack works for
            private String url;
            @Setter private String hash;
            @Setter private boolean updated;

            public String getName() {
                if (protocolId != -1) {
                    return ResourcePack.this.name + "_" + protocolId;
                }
                return ResourcePack.this.name;
            }
        }
    }
}
