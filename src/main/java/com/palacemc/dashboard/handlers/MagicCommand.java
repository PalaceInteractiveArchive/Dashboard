package com.palacemc.dashboard.handlers;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class MagicCommand {
    @Getter protected List<String> aliases = new ArrayList<>();
    @Getter private Rank rank;
    @Getter public boolean tabCompletePlayers = false;

    private Dashboard dashboard = Launcher.getDashboard();

    public MagicCommand() {
        rank = Rank.SETTLER;
    }

    public MagicCommand(Rank rank) {
        this.rank = rank;
    }

    public abstract void execute(Player player, String label, String[] args);

    public boolean canPerformCommand(Rank rank) {
        return rank.getRankId() >= this.rank.getRankId();
    }

    public Iterable<String> onTabComplete(Player sender, List<String> args) {
        List<String> players = new ArrayList<>();

        if (tabCompletePlayers) {
            for (Player player : dashboard.getOnlinePlayers()) {
                players.add(player.getUsername());
            }

            if (args.size() > 0) {
                String arg2 = args.get(args.size() - 1);
                List<String> autocompleted = new ArrayList<>();

                for (String username : players) {
                    if (username.toLowerCase().startsWith(arg2.toLowerCase())) {
                        autocompleted.add(username);
                    }
                }
                Collections.sort(autocompleted);
                return autocompleted;
            }
        }
        Collections.sort(players);
        return players;
    }
}