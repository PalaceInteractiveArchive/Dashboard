package network.palace.dashboard.commands;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
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
import java.util.HashMap;

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
                    list.put(doc.getString("name"), new ResourcePack(doc.getString("name"),
                            doc.getString("url"), doc.getString("hash"), false));
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
                    try {
                        player.sendMessage(ChatColor.GREEN + "Downloading " + pack.getName() + " from " + pack.getUrl());
                        String path = "packs/" + pack.getName() + System.currentTimeMillis();
                        URL website = new URL(pack.getUrl());
                        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                        FileOutputStream fos = new FileOutputStream(path);
                        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                        File f = new File(path);
                        if (!f.exists()) {
                            player.sendMessage(ChatColor.RED + "There was an error downloading " + pack.getName() + "!");
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
                            if (!hash.equals(pack.getHash())) {
                                pack.setHash(hash);
                                pack.setUpdated(true);
                            }
                            player.sendMessage(ChatColor.GREEN + "SHA-1 hash for " + pack.getName() + " is " + hash);
                        }
                    } catch (IOException | NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + e.getClass().getName() + e.getMessage());
                    }
                }
                for (ResourcePack pack : list.values()) {
                    if (!pack.isUpdated()) continue;
                    dashboard.getMongoHandler().getResourcePackCollection().updateOne(Filters.eq("name",
                            pack.getName()), Updates.set("hash", pack.getHash()));
                    player.sendMessage(ChatColor.YELLOW + "Updated hash for " + pack.getName());
                }
                player.sendMessage(ChatColor.GREEN + "Clearing download folder...");
                for (File file : dir.listFiles()) {
                    if (!file.isDirectory()) {
                        file.delete();
                    }
                }
                player.sendMessage(ChatColor.BLUE + "Task complete");
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "There was an error processing the resource packs!");
            }
        });
    }

    @AllArgsConstructor
    private class ResourcePack {
        @Getter private String name;
        @Getter private String url;
        @Getter @Setter private String hash;
        @Getter @Setter private boolean updated;
    }
}
