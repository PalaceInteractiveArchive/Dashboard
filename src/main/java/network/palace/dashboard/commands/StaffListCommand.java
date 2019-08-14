package network.palace.dashboard.commands;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.packets.dashboard.PacketStaffListCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StaffListCommand extends DashboardCommand {

    public StaffListCommand() {
        super(Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        List<String> director = new ArrayList<>();
        List<String> manager = new ArrayList<>();
        List<String> admin = new ArrayList<>();
        List<String> developer = new ArrayList<>();
        List<String> srmod = new ArrayList<>();
        List<String> architect = new ArrayList<>();
        List<String> builder = new ArrayList<>();
        List<String> mod = new ArrayList<>();
        List<String> trainee = new ArrayList<>();
        for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
            Rank r = tp.getRank();
            if (r.getRankId() >= Rank.TRAINEE.getRankId()) {
                switch (r) {
                    case TRAINEEBUILD:
                    case TRAINEE:
                        trainee.add(tp.getUsername() + ":" + tp.getServer());
                        break;
                    case MOD:
                        mod.add(tp.getUsername() + ":" + tp.getServer());
                        break;
                    case BUILDER:
                        builder.add(tp.getUsername() + ":" + tp.getServer());
                        break;
                    case ARCHITECT:
                        architect.add(tp.getUsername() + ":" + tp.getServer());
                        break;
                    case SRMOD:
                        srmod.add(tp.getUsername() + ":" + tp.getServer());
                        break;
                    case DEVELOPER:
                        developer.add(tp.getUsername() + ":" + tp.getServer());
                        break;
                    case ADMIN:
                        admin.add(tp.getUsername() + ":" + tp.getServer());
                        break;
                    case MANAGER:
                        manager.add(tp.getUsername() + ":" + tp.getServer());
                        break;
                    case DIRECTOR:
                        director.add(tp.getUsername() + ":" + tp.getServer());
                        break;
                }
            }
        }
        Collections.sort(director);
        Collections.sort(manager);
        Collections.sort(admin);
        Collections.sort(developer);
        Collections.sort(srmod);
        Collections.sort(architect);
        Collections.sort(builder);
        Collections.sort(mod);
        Collections.sort(trainee);
        PacketStaffListCommand packet = new PacketStaffListCommand(player.getUniqueId(), director, manager, admin,
                developer, srmod, architect, builder, mod, trainee);
        player.send(packet);
    }
}