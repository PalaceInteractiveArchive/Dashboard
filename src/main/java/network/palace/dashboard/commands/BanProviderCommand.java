package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;

public class BanProviderCommand extends DashboardCommand {

    public BanProviderCommand() {
        super(Rank.MOD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/banprovider [Provider]");
            return;
        }
        StringBuilder provider = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            provider.append(args[i]);
            if (i < (args.length - 1)) {
                provider.append(" ");
            }
        }
        ProviderBan ban = new ProviderBan(provider.toString(), player.getUsername());
        dashboard.getSchedulerManager().runAsync(() -> {
            ProviderBan existing = dashboard.getMongoHandler().getProviderBan(provider.toString());
            if (existing != null) {
                player.sendMessage(ChatColor.RED + "This provider is already banned!");
                return;
            }
            dashboard.getMongoHandler().banProvider(ban);
            for (Player tp : dashboard.getOnlinePlayers()) {
                if (tp.getIsp().trim().equalsIgnoreCase(provider.toString().trim())) {
                    try {
                        tp.kickPlayer(ChatColor.RED + "Your ISP (Internet Service Provider) Has Been Blocked From Our Network");
                    } catch (Exception ignored) {
                    }
                }
            }
            dashboard.getModerationUtil().announceBan(ban);
        });
    }
}