package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.chat.ChatColor;
import network.palace.dashboard.handlers.ClickWarning;
import network.palace.dashboard.handlers.DefaultFontInfo;
import network.palace.dashboard.handlers.Player;

import java.util.*;

/**
 * Created by Marc on 9/22/16
 */
public class WarningUtil {
    private HashMap<UUID, ClickWarning> warnings = new HashMap<>();
    private final static int CENTER_PX = 120;

    public static String getCenteredText(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messageSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '§') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                DefaultFontInfo info = DefaultFontInfo.getDefaultFontInfo(c);
                messageSize += isBold ? info.getBoldLength() : info.getLength();
                messageSize++;
            }
        }

        int halvedMessageSize = messageSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder builder = new StringBuilder();
        while (compensated < toCompensate) {
            builder.append(" ");
            compensated += spaceLength;
        }
        return builder.toString() + message;
    }


    public WarningUtil() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (ClickWarning w : new ArrayList<>(warnings.values())) {
                    if (w.getExpiration() < System.currentTimeMillis()) {
                        warnings.remove(w.getId());
                    }
                }
            }
        }, 0, 60000);
    }

    private ClickWarning getWarning(UUID id) {
        return warnings.get(id);
    }

    public void trackWarning(ClickWarning w) {
        warnings.put(w.getId(), w);
    }

    public void handle(Player player, String msg) {
        Dashboard dashboard = Launcher.getDashboard();
        try {
            UUID id = UUID.fromString(msg.replace(":warn-", ""));
            ClickWarning warning = getWarning(id);
            if (warning == null) {
                player.sendMessage(ChatColor.RED + "The warning token you used has expired or never existed!");
                return;
            }
            warnings.remove(warning.getId());
            if (dashboard.getPlayer(warning.getName()) == null) {
                player.sendMessage(ChatColor.RED + "That player has logged off!");
                return;
            }
            List<String> list = new ArrayList<>();
            list.add(warning.getName());
            Collections.addAll(list, warning.getResponse().split(" "));
            String[] args = new String[list.size()];
            list.toArray(args);
            dashboard.getCommandUtil().getCommand("warn").execute(player, "warn", args);
        } catch (Exception ignored) {
            player.sendMessage(ChatColor.RED + "There was an error processing that warning!");
        }
    }
}