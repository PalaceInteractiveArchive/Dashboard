package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.packets.dashboard.PacketAudioCommand;

public class Commandaudio extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        PacketAudioCommand packet = new PacketAudioCommand(player.getUniqueId(), player.setAudioAuth());
        player.send(packet);
    }
}