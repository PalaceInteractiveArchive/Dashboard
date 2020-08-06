package network.palace.dashboard.scheduler;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.Player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * Created by Marc on 9/26/16
 */
public class BroadcastClock extends TimerTask {
    private List<String> announcements = new ArrayList<>();
    private int i = 0;

    public BroadcastClock() {
        reload();
    }

    public void reload() {
        List<String> newList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("announcements.txt"))) {
            String line = br.readLine();
            boolean read = false;
            while (line != null) {
                if (read) {
                    newList.add(line);
                }
                if (line.startsWith("announcements:")) {
                    read = true;
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            Launcher.getDashboard().getLogger().error("Error loading announcements.txt", e);
        }
        if (!newList.equals(announcements)) {
            announcements = newList;
            i = 0;
        }
    }

    @Override
    public void run() {
        String msg = ChatColor.translateAlternateColorCodes('&', announcements.get(i));
        for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
            tp.sendMessage(msg);
        }
        i++;
        if (i >= announcements.size()) {
            i = 0;
        }
    }
}