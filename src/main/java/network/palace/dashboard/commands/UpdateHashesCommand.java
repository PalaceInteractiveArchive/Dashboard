package network.palace.dashboard.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.utils.ErrorUtil;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;

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
            Optional<Connection> optConnection = dashboard.getSqlUtil().getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
                player.sendMessage(ChatColor.GREEN + "Requesting Resource Pack list from database...");
                PreparedStatement sql = connection.prepareStatement("SELECT * FROM resource_packs;");
                ResultSet result = sql.executeQuery();
                HashMap<String, ResourcePack> list = new HashMap<>();
                while (result.next()) {
                    list.put(result.getString("name"), new ResourcePack(result.getString("name"),
                            result.getString("url"), result.getString("hash"), false));
                }
                result.close();
                sql.close();
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
                            player.sendMessage(ChatColor.RED + "There was an error in the download!");
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
                    if (!pack.isUpdated()) {
                        continue;
                    }
                    PreparedStatement sql1 = connection.prepareStatement("UPDATE resource_packs SET hash=? WHERE name=?");
                    sql1.setString(1, pack.getHash());
                    sql1.setString(2, pack.getName());
                    sql1.execute();
                    sql1.close();
                    player.sendMessage(ChatColor.YELLOW + "Updated hash for " + pack.getName());
                }
                player.sendMessage(ChatColor.GREEN + "Clearing download folder...");
                for (File file : dir.listFiles()) {
                    if (!file.isDirectory()) {
                        file.delete();
                    }
                }
                player.sendMessage(ChatColor.BLUE + "Task complete");
            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "There was an error in the SQL query!");
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
