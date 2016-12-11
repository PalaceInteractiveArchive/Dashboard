package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;
import com.palacemc.dashboard.packets.dashboard.PacketStaffListCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandStaffList extends MagicCommand {

    public CommandStaffList() {
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

        for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
            Rank r = tp.getRank();
            if (r.getRankId() >= Rank.SQUIRE.getRankId()) {
                switch (r) {
                    case SQUIRE:
                        squire.add(tp.getUsername() + ":" + tp.getServer());
                        break;
                    case KNIGHT:
                        knight.add(tp.getUsername() + ":" + tp.getServer());
                        break;
                    case PALADIN:
                        paladin.add(tp.getUsername() + ":" + tp.getServer());
                        break;
                    case WIZARD:
                        wizard.add(tp.getUsername() + ":" + tp.getServer());
                        break;
                    case EMPEROR:
                        emperor.add(tp.getUsername() + ":" + tp.getServer());
                        break;
                    case EMPRESS:
                        empress.add(tp.getUsername() + ":" + tp.getServer());
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
        PacketStaffListCommand packet = new PacketStaffListCommand(player.getUuid(), empress, emperor,
                wizard, paladin, knight, squire);
        player.send(packet);
    }
}