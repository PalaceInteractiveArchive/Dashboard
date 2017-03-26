package network.palace.dashboard.utils;

import network.palace.dashboard.commands.*;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.packets.dashboard.PacketTabComplete;

import java.util.*;

public class CommandUtil {
    private HashMap<String, MagicCommand> commands = new HashMap<>();

    public CommandUtil() {
        initialize();
    }

    private void initialize() {
        register("audio", new Commandaudio());
        register("b", new Commandb());
        register("ban", new Commandban());
        register("banip", new Commandbanip());
        register("bseen", new Commandbseen());
        register("bug", new Commandbug());
        register("cc", new Commandcc());
        register("charlist", new Commandcharlist());
        register("chat", new Commandchat());
        register("chatdelay", new Commandchatdelay());
        register("chatreload", new Commandchatreload());
        register("chatstatus", new Commandchatstatus());
        register("cmds", new Commandcmds());
        register("discord", new CommandDiscord());
        register("find", new Commandfind());
        register("friend", new Commandfriend());
        register("ho", new Commandho());
        register("ip", new Commandip());
        register("ipseen", new Commandipseen());
        register("join", new Commandjoin());
        register("kick", new Commandkick());
        register("kickall", new Commandkickall());
        register("link", new Commandlink());
        register("maintenance", new Commandmaintenance());
        register("mentions", new Commandmentions());
        register("modlog", new Commandmodlog());
        register("motdrl", new Commandmotdrl());
        register("msg", new Commandmsg());
        register("msgtoggle", new Commandmsgtoggle());
        register("mumble", new Commandmumble());
        register("mute", new Commandmute());
        register("mutechat", new Commandmutechat());
        register("namecheck", new Commandnamecheck());
        register("oc", new Commandoc());
        register("party", new Commandparty());
        register("parties", new Commandparties());
        register("pchat", new Commandpchat());
        //register("pin", new CommandPin());
        register("pmtoggle", new Commandpmtoggle());
        register("processes", new Commandprocesses());
        register("reboot", new Commandreboot());
        register("reply", new Commandreply());
        register("sc", new Commandsc());
        register("send", new Commandsend());
        register("server", new Commandserver());
        register("social", new Commandsocial());
        register("stafflist", new Commandstafflist());
        register("store", new Commandstore());
        register("tempban", new Commandtempban());
        register("unban", new Commandunban());
        register("unbanip", new Commandunbanip());
        register("unmute", new Commandunmute());
        register("updatehashes", new Commandupdatehashes());
        register("uptime", new Commanduptime());
        register("whereami", new Commandwhereami());
    }

    public boolean handleCommand(Player player, String message) {
        try {
            String[] parts = message.split(" ");
            String command = parts[0].toLowerCase();
            String[] args = new String[parts.length - 1];
            int i = 0;
            boolean first = true;
            for (String s : parts) {
                if (first) {
                    first = false;
                    continue;
                }
                args[i] = s;
                i++;
            }
            MagicCommand cmd = null;
            if (!commands.containsKey(command)) {
                for (MagicCommand c : new ArrayList<>(commands.values())) {
                    if (c.getAliases().contains(command)) {
                        cmd = c;
                        break;
                    }
                }
            } else {
                cmd = commands.get(command);
            }
            if (cmd == null) {
                return false;
            }
            execute(player, cmd, command, args);
            return true;
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "An internal error occured whilst executing this command.");
            e.printStackTrace();
            return true;
        }
    }

    public TreeMap<String, MagicCommand> getCommands() {
        return new TreeMap<>(commands);
    }

    public MagicCommand getCommand(String label) {
        return commands.get(label);
    }

    private void execute(Player player, MagicCommand c, String command, String[] args) {
        if (!c.canPerformCommand(player.getRank())) {
            player.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
            return;
        }
        c.execute(player, command, args);
    }

    public void register(String label, MagicCommand command) {
        if (commands.containsKey(label.toLowerCase())) {
            commands.remove(label.toLowerCase());
        }
        commands.put(label.toLowerCase(), command);
    }

    public void tabComplete(Player player, String command, List<String> args, List<String> results) {
        MagicCommand cmd = null;
        if (!commands.containsKey(command)) {
            for (MagicCommand c : new ArrayList<>(commands.values())) {
                if (c.getAliases().contains(command)) {
                    cmd = c;
                    break;
                }
            }
        } else {
            cmd = commands.get(command);
        }
        if (cmd == null) {
            return;
        }
        Iterable<String> l = cmd.onTabComplete(player, args);
        List<String> list = new ArrayList<>();
        for (String s : l) {
            list.add(s);
        }
        if (!list.isEmpty()) {
            results.clear();
            results.addAll(list);
        }
        PacketTabComplete packet = new PacketTabComplete(player.getUniqueId(), command, args, results);
        player.send(packet);
    }

    public List<String> getCommandsAndAliases() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, MagicCommand> entry : commands.entrySet()) {
            list.add(entry.getKey());
            list.addAll(entry.getValue().getAliases());
        }
        return list;
    }
}