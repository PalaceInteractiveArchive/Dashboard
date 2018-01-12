package network.palace.dashboard.handlers;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class DashboardCommand {
    protected List<String> aliases = new ArrayList<>();
    private Rank rank;
    public boolean tabCompletePlayers = false;

    public DashboardCommand() {
        rank = Rank.SETTLER;
    }

    public DashboardCommand(Rank rank) {
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
            Dashboard dashboard = Launcher.getDashboard();
            for (Player tp : dashboard.getOnlinePlayers()) {
                list.add(tp.getUsername());
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