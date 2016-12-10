package com.palacemc.dashboard.handlers;

import com.palacemc.dashboard.Launcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class MagicCommand {
    protected List<String> aliases = new ArrayList<>();
    private Rank rank;
    public boolean tabCompletePlayers = false;

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

    public List<String> getAliases() {
        return new ArrayList<>(aliases);
    }

    public boolean doTabCompletePlayers() {
        return tabCompletePlayers;
    }

    public Iterable<String> onTabComplete(Player sender, List<String> args) {
        List<String> list = new ArrayList<>();
        if (tabCompletePlayers) {
            for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
                list.add(tp.getName());
            }
            if (args.size() > 0) {
                String arg2 = args.get(args.size() - 1);
                List<String> l2 = new ArrayList<>();
                for (String s : list) {
                    if (s.toLowerCase().startsWith(arg2.toLowerCase())) {
                        l2.add(s);
                    }
                }
                Collections.sort(l2);
                return l2;
            }
        }
        Collections.sort(list);
        return list;
    }

    public Rank getRank() {
        return rank;
    }
}