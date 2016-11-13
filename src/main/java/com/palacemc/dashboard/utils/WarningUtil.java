package com.palacemc.dashboard.utils;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.Warning;

import java.util.*;

/**
 * Created by Marc on 9/22/16
 */
public class WarningUtil {
    private HashMap<UUID, Warning> warnings = new HashMap<>();

    public WarningUtil() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (Warning w : new ArrayList<>(warnings.values())) {
                    if (w.getExpiration() < System.currentTimeMillis()) {
                        warnings.remove(w.getId());
                    }
                }
            }
        }, 0, 60000);
    }

    private Warning getWarning(UUID id) {
        return warnings.get(id);
    }

    public void trackWarning(Warning w) {
        warnings.put(w.getId(), w);
    }

    public void handle(Player player, String msg) {
        try {
            UUID id = UUID.fromString(msg.replace(":warn-", ""));
            Warning warning = getWarning(id);
            if (warning == null) {
                player.sendMessage(ChatColor.RED + "The warning token you used has expired or never existed!");
                return;
            }
            warnings.remove(warning.getId());
            if (Dashboard.getPlayer(warning.getName()) == null) {
                player.sendMessage(ChatColor.RED + "That player has logged off!");
                return;
            }
            List<String> list = new ArrayList<>();
            list.add(warning.getName());
            for (String s : warning.getResponse().split(" ")) {
                list.add(s);
            }
            String[] args = new String[list.size()];
            list.toArray(args);
            Dashboard.commandUtil.getCommand("msg").execute(player, "msg", args);
        } catch (Exception ignored) {
            ignored.printStackTrace();
            player.sendMessage(ChatColor.RED + "There was an error processing that warning!");
        }
    }
}