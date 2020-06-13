package network.palace.dashboard.utils;

import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.commands.*;
import network.palace.dashboard.commands.admin.*;
import network.palace.dashboard.commands.chat.*;
import network.palace.dashboard.commands.guide.GuideAnnounceCommand;
import network.palace.dashboard.commands.guide.GuideHelpCommand;
import network.palace.dashboard.commands.guide.GuideListCommand;
import network.palace.dashboard.commands.guide.HelpMeCommand;
import network.palace.dashboard.commands.moderation.*;
import network.palace.dashboard.commands.staff.*;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.packets.dashboard.PacketCommandList;
import network.palace.dashboard.packets.dashboard.PacketTabComplete;

import java.util.*;

public class CommandUtil {
    private HashMap<String, DashboardCommand> commands = new HashMap<>();

    public CommandUtil() {
        initialize();
    }

    private void initialize() {
        /* Admin */
        register("bconfig", new BConfigCommand());
        register("bungeecounts", new BungeeCountsCommand());
        register("cmds", new CmdsCommand());
        register("dashboardversion", new DashboardVersion());
        register("glog", new GuideLogCommand());
        register("kickall", new KickAllCommand());
        register("maintenance", new MaintenanceCommand());
        register("msgtoggle", new MsgToggleCommand());
        register("processes", new ProcessesCommand());
        register("reboot", new RebootCommand());
        register("reloadicon", new ReloadIconCommand());
        register("send", new SendCommand());
        register("toggleserverqueue", new ToggleServerQueueCommand());
        register("updatehashes", new UpdateHashesCommand());
        /* Chat */
        register("cc", new ClearChatCommand());
        register("chat", new ChatCommand());
        register("chatdelay", new ChatDelayCommand());
        register("chatreload", new ChatReloadCommand());
        register("chatstatus", new ChatStatusCommand());
        register("gc", new GuideChatCommand());
        register("ho", new AdminChatCommand());
        register("pchat", new PartyChatCommand());
        register("sc", new StaffChatCommand());
        /* Guide */
        register("gannounce", new GuideAnnounceCommand());
        register("h", new GuideHelpCommand());
        register("guidelist", new GuideListCommand());
        register("helpme", new HelpMeCommand());
        /* Moderation */
        register("altaccounts", new AltAccountsCommand());
        register("ban", new BanCommand());
        register("banip", new BanIPCommand());
        register("bannedproviders", new BannedProvidersCommand());
        register("banprovider", new BanProviderCommand());
        register("bseen", new BseenCommand());
        register("find", new FindCommand());
        register("ip", new IPCommand());
        register("kick", new KickCommand());
        register("modlog", new ModlogCommand());
        register("motdrl", new MotdReloadCommand());
        register("mute", new MuteCommand());
        register("mutechat", new MuteChatCommand());
        register("namecheck", new NamecheckCommand());
        register("parties", new PartiesCommand());
        register("pmtoggle", new PMToggleCommand());
        register("spamip", new SpamIPCommand());
        register("strict", new StrictCommand());
        register("tempban", new TempBanCommand());
        register("unban", new UnbanCommand());
        register("unbanip", new UnbanIPCommand());
        register("unbanprovider", new UnbanProviderCommand());
        register("unmute", new UnmuteCommand());
        register("warn", new WarnCommand());
        /* Staff */
        register("b", new BroadcastCommand());
        register("charlist", new CharListCommand());
        register("mocap", new MotionCaptureCommand());
        register("multishow", new MultiShowCommand());
        register("server", new ServerCommand());
        register("sglist", new SGListCommand());
        register("staff", new StaffCommand());
        register("stafflist", new StaffListCommand());
        /* General */
        register("apply", new ApplyCommand());
        register("audio", new AudioCommand());
        register("bug", new BugCommand());
        register("discord", new DiscordCommand());
        register("friend", new FriendCommand());
        register("ignore", new IgnoreCommand());
        register("join", new JoinCommand());
        register("leavedashqueue", new LeaveServerQueueCommand());
        register("link", new LinkCommand());
        register("mentions", new MentionsCommand());
        register("msg", new MsgCommand());
        register("oc", new OnlineCountCommand());
        register("party", new PartyCommand());
        register("punishments", new PunishmentsCommand());
        register("reply", new ReplyCommand());
        register("rules", new RulesCommand());
        register("social", new SocialCommand());
        register("store", new StoreCommand());
        register("uptime", new UptimeCommand());
        register("whereami", new WhereAmICommand());
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
            DashboardCommand cmd = findCommand(command);
            if (cmd == null) {
                return false;
            }
            execute(player, cmd, command, args);
            return true;
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "An internal error occurred whilst executing this command.");
            e.printStackTrace();
            return true;
        }
    }

    public TreeMap<String, DashboardCommand> getCommands() {
        return new TreeMap<>(commands);
    }

    public DashboardCommand getCommand(String label) {
        return commands.get(label);
    }

    private void execute(Player player, DashboardCommand c, String command, String[] args) {
        if (!c.canPerformCommand(player)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
            return;
        }
        c.execute(player, command, args);
    }

    public void register(String label, DashboardCommand command) {
        commands.put(label.toLowerCase(), command);
    }

    public void tabComplete(Player player, int transactionId, String command, List<String> args, List<String> results) {
        DashboardCommand cmd = findCommand(command);
        if (cmd == null) {
            return;
        }
        Iterable<String> l = cmd.onTabComplete(player, args);
        List<String> list = new ArrayList<>();
        for (String s : l) {
            if (s.isEmpty()) continue;
            list.add(s);
        }
        if (!list.isEmpty()) {
            results.clear();
            results.addAll(list);
        }
        PacketTabComplete packet = new PacketTabComplete(player.getUniqueId(), transactionId, command, args, results);
        player.send(packet);
    }

    private DashboardCommand findCommand(String s) {
        DashboardCommand cmd = null;
        if (!commands.containsKey(s)) {
            for (DashboardCommand c : new ArrayList<>(commands.values())) {
                if (c.getAliases().contains(s)) {
                    cmd = c;
                    break;
                }
            }
        } else {
            cmd = commands.get(s);
        }
        return cmd;
    }

    public PacketCommandList getTabCompleteCommandPacket() {
        List<String> tabPlayerCommands = new ArrayList<>();
        List<String> generalTabCommands = new ArrayList<>();
        for (Map.Entry<String, DashboardCommand> entry : commands.entrySet()) {
            DashboardCommand cmd = entry.getValue();
            if (cmd.isTabCompletePlayers()) {
                tabPlayerCommands.add(entry.getKey());
                tabPlayerCommands.addAll(cmd.getAliases());
            } else if (cmd.doesTabComplete()) {
                generalTabCommands.add(entry.getKey());
                generalTabCommands.addAll(cmd.getAliases());
            }
        }
        return new PacketCommandList(tabPlayerCommands, generalTabCommands);
    }
}