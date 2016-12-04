package com.palacemc.dashboard.utils;

import com.palacemc.dashboard.commands.*;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.packets.dashboard.PacketTabComplete;

import java.util.*;

public class CommandUtil {
    private HashMap<String, MagicCommand> commands = new HashMap<>();

    public CommandUtil() {
        initialize();
    }

    private void initialize() {
        register("audio", new CommandAudio());
        register("b", new CommandDB());
        register("ban", new CommandBan());
        register("banip", new CommandBanIP());
        register("bseen", new CommandBSeen());
        register("bug", new CommandBug());
        register("cc", new CommandCC());
        register("charlist", new CommandCharList());
        register("chat", new CommandChat());
        register("chatdelay", new CommandChatDelay());
        register("chatreload", new CommandChatReload());
        register("cmds", new CommandCmds());
        register("find", new CommandFind());
        register("friend", new CommandFriend());
        register("ho", new CommandHo());
        register("ip", new CommandIP());
        register("ipseen", new CommandIPSeen());
        register("join", new CommandJoin());
        register("kick", new CommandKick());
        register("kickall", new CommandKickAll());
        register("maintenance", new CommandMaintenance());
        register("mentions", new CommandMentions());
        register("modlog", new CommandModLog());
        register("motdrl", new CommandMotdrl());
        register("msg", new CommandMsg());
        register("msgtoggle", new CommandMsgToggle());
        register("mumble", new CommandMumble());
        register("mute", new CommandMute());
        register("mutechat", new CommandMuteChat());
        register("namecheck", new CommandNameCheck());
        register("oc", new CommandDoc());
        register("party", new CommandParty());
        register("parties", new CommandParties());
        register("pchat", new CommandPChat());
        //register("pin", new CommandPin());
        register("pmtoggle", new CommandPmToggle());
        register("processes", new CommandProcesses());
        register("reboot", new CommandReboot());
        register("reply", new CommandReply());
        register("sc", new CommandSC());
        register("send", new CommandSend());
        register("server", new CommandServer());
        register("social", new CommandSocial());
        register("stafflist", new CommandStaffList());
        register("store", new CommandStore());
        register("tempban", new CommandTempBan());
        register("unban", new CommandUnban());
        register("unbanip", new CommandUnbanIP());
        register("unmute", new CommandUnmute());
        register("uptime", new CommandUptime());
        register("whereami", new CommandWhereAmI());
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
            for (String s : list) {
                results.add(s);
            }
        }
        PacketTabComplete packet = new PacketTabComplete(player.getUniqueId(), command, args, results);
        player.send(packet);
    }

    public List<String> getCommandsAndAliases() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, MagicCommand> entry : commands.entrySet()) {
            list.add(entry.getKey());
            for (String s : entry.getValue().getAliases()) {
                list.add(s);
            }
        }
        return list;
    }
}