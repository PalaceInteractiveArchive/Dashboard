package network.palace.dashboard.handlers;

import lombok.Getter;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class DashboardCommand {
    protected List<String> aliases = new ArrayList<>();
    private Rank rank;
    private RankTag tag;
    @Getter protected boolean tabCompletePlayers = false;

    public DashboardCommand() {
        this(Rank.SETTLER, null);
    }

    public DashboardCommand(Rank rank) {
        this(rank, null);
    }

    public DashboardCommand(Rank rank, RankTag tag) {
        this.rank = rank;
        this.tag = tag;
    }

    public abstract void execute(Player player, String label, String[] args);

    public boolean canPerformCommand(Player player) {
        return player.getRank().getRankId() >= rank.getRankId() || (tag != null && player.hasTag(tag));
    }

    public List<String> getAliases() {
        return new ArrayList<>(aliases);
    }

    public boolean doesTabComplete() {
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