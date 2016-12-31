package com.palacemc.dashboard.utils;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Warning;

import java.util.*;

/**
 * Created by Marc on 9/22/16
 */
public class WarningUtil {
    private HashMap<UUID, Warning> warnings = new HashMap<>();

    private Dashboard dashboard = Launcher.getDashboard();

    public WarningUtil() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (Warning w : new ArrayList<>(warnings.values())) {
                    if (w.getExpiration() < System.currentTimeMillis()) {
                        warnings.remove(w.getUuid());
                    }
                }
            }
        }, 0, 60000);
    }

    private Warning getWarning(UUID id) {
        return warnings.get(id);
    }

    public void trackWarning(Warning w) {
        warnings.put(w.getUuid(), w);
    }

    public void handle(Player player, String msg) {
        try {
            UUID id = UUID.fromString(msg.replace(":warn-", ""));
            Warning warning = getWarning(id);

            if (warning == null) {
                player.sendMessage(ChatColor.RED + "The warning token you used has expired or never existed!");
                return;
            }

            warnings.remove(warning.getUuid());
            if (dashboard.getPlayer(warning.getName()) == null) {
                player.sendMessage(ChatColor.RED + "That player has logged off!");
                return;
            }

            List<String> warnings = new ArrayList<>();
            warnings.add(warning.getName());
            Collections.addAll(warnings, warning.getResponse().split(" "));

            String[] args = new String[warnings.size()];
            warnings.toArray(args);

            dashboard.getCommandUtil().getCommand("msg").execute(player, "msg", args);
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "There was an error processing that warning!");
        }
    }
}