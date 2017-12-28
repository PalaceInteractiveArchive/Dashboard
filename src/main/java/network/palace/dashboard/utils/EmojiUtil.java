package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.packets.dashboard.PacketTabComplete;

import java.util.*;

public class EmojiUtil {
    private HashMap<String, String> translations = new HashMap<>();

    public EmojiUtil() {
        translations.put(":heart:", "❤");
        translations.put(":checkmark:", "✔");
        translations.put(":music:", "♫");
        translations.put(":snowman:", "☃");
        translations.put(":shrug:", "¯\\_(ツ)_/¯");
        translations.put(":tableflip:", "(╯°□°）╯︵ ┻━┻");
        translations.put(":sqrt:", "√");
        translations.put("<3", "❤");
    }

    public String convertMessage(Player player, String msg) {
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            if (msg.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
            if (msg.startsWith(entry.getKey() + " ")) {
                msg = msg.replaceFirst(entry.getKey() + " ", entry.getValue() + " ");
            }
            if (msg.endsWith(" " + entry.getKey())) {
                int start = msg.lastIndexOf(entry.getKey());
                msg = msg.substring(0, start) + entry.getValue();
            }
            msg = msg.replace(" " + entry.getKey() + " ", " " + entry.getValue() + " ");
        }
        return msg;
    }

    public void tabComplete(Player player, String cmd, List<String> args, List<String> results) {
        Dashboard dashboard = Launcher.getDashboard();

        Iterable<String> l;

        String arg2 = args.isEmpty() ? cmd : args.get(args.size() - 1);
        List<String> l2 = new ArrayList<>();
        for (String s : translations.keySet()) {
            if (!s.startsWith(":")) continue;
            if (s.toLowerCase().startsWith(arg2.toLowerCase())) {
                l2.add(s);
            }
        }
        Collections.sort(l2);
        l = l2;

        List<String> list = new ArrayList<>();
        for (String s : l) {
            list.add(s);
        }
        if (!list.isEmpty()) {
            results.clear();
            results.addAll(list);
        }
        PacketTabComplete packet = new PacketTabComplete(player.getUniqueId(), cmd, args, results);
        player.send(packet);
    }
}
