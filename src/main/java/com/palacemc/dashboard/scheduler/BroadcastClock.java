package com.palacemc.dashboard.scheduler;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.Player;

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
            e.printStackTrace();
        }
        if (!newList.equals(announcements)) {
            announcements = newList;
            i = 0;
        }
    }

    @Override
    public void run() {
        String msg = ChatColor.translateAlternateColorCodes('&', announcements.get(i));
        for (Player tp : Dashboard.getOnlinePlayers()) {
            tp.sendMessage(msg);
        }
        i++;
        if (i >= announcements.size()) {
            i = 0;
        }
    }
}