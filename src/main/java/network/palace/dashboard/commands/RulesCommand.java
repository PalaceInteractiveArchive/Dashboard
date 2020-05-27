package network.palace.dashboard.commands;

import network.palace.dashboard.chat.*;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;

public class RulesCommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(new ComponentBuilder("\nClick to view The Palace Network's rules!\n")
                .color(ChatColor.YELLOW).underlined(false).bold(true)
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://palnet.us/rules"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to visit https://palnet.us/rules").color(ChatColor.GREEN).create())).create());
    }
}
