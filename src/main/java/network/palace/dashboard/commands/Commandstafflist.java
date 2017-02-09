package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.packets.dashboard.PacketStaffListCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Commandstafflist extends MagicCommand {

    public Commandstafflist() {
        super(Rank.SQUIRE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        List<String> empress = new ArrayList<>();
        List<String> emperor = new ArrayList<>();
        List<String> wizard = new ArrayList<>();
        List<String> paladin = new ArrayList<>();
        List<String> knight = new ArrayList<>();
        List<String> squire = new ArrayList<>();
        for (Player tp : Dashboard.getOnlinePlayers()) {
            Rank r = tp.getRank();
            if (r.getRankId() >= Rank.SQUIRE.getRankId()) {
                switch (r) {
                    case SQUIRE:
                        squire.add(tp.getName() + ":" + tp.getServer());
                        break;
                    case KNIGHT:
                        knight.add(tp.getName() + ":" + tp.getServer());
                        break;
                    case PALADIN:
                        paladin.add(tp.getName() + ":" + tp.getServer());
                        break;
                    case WIZARD:
                        wizard.add(tp.getName() + ":" + tp.getServer());
                        break;
                    case EMPEROR:
                        emperor.add(tp.getName() + ":" + tp.getServer());
                        break;
                    case EMPRESS:
                        empress.add(tp.getName() + ":" + tp.getServer());
                        break;
                }
            }
        }
        Collections.sort(emperor);
        Collections.sort(empress);
        Collections.sort(wizard);
        Collections.sort(paladin);
        Collections.sort(knight);
        Collections.sort(squire);
        PacketStaffListCommand packet = new PacketStaffListCommand(player.getUniqueId(), empress, emperor,
                wizard, paladin, knight, squire);
        player.send(packet);
    }
}