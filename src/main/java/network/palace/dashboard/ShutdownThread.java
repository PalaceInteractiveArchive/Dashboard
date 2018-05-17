package network.palace.dashboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.slack.SlackAttachment;
import network.palace.dashboard.slack.SlackMessage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

/**
 * @author Marc
 * @since 6/15/17
 */
public class ShutdownThread extends Thread {

    @Override
    public void run() {
        Dashboard dashboard = Launcher.getDashboard();
        dashboard.getLogger().warn("Shutting down Dashboard...");
        for (Player p : dashboard.getOnlinePlayers()) {
            if (!p.getChannel().equalsIgnoreCase("all")) {
                p.setChannel("all");
                p.sendMessage(ChatColor.GREEN + "You have been moved to the " + ChatColor.AQUA + "all " +
                        ChatColor.GREEN + "channel");
            }
        }
        File inventories = new File("inventories.txt");
        try {
            inventories.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(inventories, false));
            for (InventoryCache cache : dashboard.getInventoryUtil().getCachedInventories().values()) {
                JsonObject o = new JsonObject();
                o.addProperty("uuid", cache.getUuid().toString());
                JsonArray e = new JsonArray();
                for (ResortInventory inv : cache.getResorts().values()) {
                    JsonObject ob = new JsonObject();
                    ob.addProperty("resort", inv.getResort().getId());
                    ob.addProperty("packJSON", inv.getBackpackJSON());
                    ob.addProperty("packHash", inv.getBackpackHash());
                    ob.addProperty("dbPackHash", inv.getDbBackpackHash());
                    ob.addProperty("packsize", inv.getBackpackSize());
                    ob.addProperty("lockerJSON", inv.getLockerJSON());
                    ob.addProperty("lockerHash", inv.getLockerHash());
                    ob.addProperty("dbLockerHash", inv.getDbLockerHash());
                    ob.addProperty("lockersize", inv.getLockerSize());
                    ob.addProperty("hotbarJSON", inv.getHotbarJSON());
                    ob.addProperty("hotbarHash", inv.getHotbarHash());
                    ob.addProperty("dbHotbarHash", inv.getDbHotbarHash());
                    e.add(ob);
                }
                o.add("resorts", e);
                bw.write(o.toString());
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File chat = new File("chat.txt");
        try {
            chat.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(chat, false));
            for (String s : dashboard.getChatUtil().getMutedChats()) {
                bw.write(s);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File parties = new File("parties.txt");
        try {
            parties.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(parties, false));
            for (Party p : dashboard.getPartyUtil().getParties()) {
                bw.write(p.toString());
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dashboard.getVoteUtil().stop();
        dashboard.getSlackUtil().sendDashboardMessage(new SlackMessage(),
                Collections.singletonList(new SlackAttachment("Dashboard went offline! #devs").color("danger")));
    }
}
