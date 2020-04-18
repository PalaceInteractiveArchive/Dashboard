package network.palace.dashboard.commands;

import network.palace.dashboard.chat.*;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;

public class ApplyCommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(new ComponentBuilder("\nClick to see what positions we have available!\n")
        .color(ChatColor.YELLOW).underlined(false).bold(true)
        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://palnet.us/apply"))
        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, "Click me")).create());
    }
}
