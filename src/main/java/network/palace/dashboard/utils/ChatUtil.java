package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.packets.dashboard.*;
import network.palace.dashboard.server.DashboardSocketChannel;
import network.palace.dashboard.server.WebSocketServerHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Marc on 7/15/16
 */
public class ChatUtil {
    public HashMap<String, Boolean> mutedServers = new HashMap<>();
    private HashMap<UUID, Long> time = new HashMap<>();
    private HashMap<UUID, String> messageCache = new HashMap<>();
    private int chatDelay = 2000;
    private HashMap<Character, ChatColor> chars = new HashMap<>();
    private List<String> swearList = new ArrayList<>();
    private List<String> specificList = new ArrayList<>();
    private List<String> spacesList = new ArrayList<>();
    private List<String> whitelist = new ArrayList<>();
    private List<String> allowedChars = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
            "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "=", "_", "+", "[", "]", "{", "}", "/",
            "\\", "?", "|", ",", ".", "<", ">", "`", "~", ";", ":", "'", "\"", "√", "˚", "≤", "≥", "™", "£", "¢", "∞",
            "•", " ");
    private Pattern linkPattern = Pattern.compile("((\\d{1,3}\\.){3}\\d{1,3}(:\\d+)?)|(([0-9a-z:/]+(\\.|\\(dot\\)\\(\\.\\" +
            ")))+(aero|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|pro|tel|travel|ac|ad|ae|" +
            "af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc" +
            "|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cu|cv|cx|cy|de|dj|dk|dm|do|dz|ec|ee|eg|er|es|et|eu|fi|fj|fk|fm|fo|f" +
            "r|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|" +
            "je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml" +
            "|mo|mp|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|nom|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|" +
            "pt|pw|py|qa|re|ra|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sk|sl|sm|sn|so|sr|st|su|sv|sy|sz|tc|td|tf|tg|th|tj" +
            "|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|yu|za|zm|zw|arpa)(:[0-" +
            "9]+)?((/([~0-9a-zA-Z#+%@./_-]+))?(/[0-9a-zA-Z+%@/&\\[\\];=_-]+)?)?)\\b");
    private HashMap<UUID, List<String>> messages = new HashMap<>();
    private List<String> mutedChats = new ArrayList<>();
    private boolean privateMessages = true;

    public ChatUtil() {
        reload();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try (Connection connection = Dashboard.sqlUtil.getConnection()) {
                    if (messages.isEmpty()) {
                        return;
                    }
                    int amount = 0;
                    for (Map.Entry<UUID, List<String>> entry : new HashSet<>(messages.entrySet())) {
                        for (String s : entry.getValue()) {
                            amount++;
                        }
                    }
                    StringBuilder statement = new StringBuilder("INSERT INTO chat (user, message) VALUES ");
                    int i = 0;
                    HashMap<Integer, String> lastList = new HashMap<>();
                    for (Map.Entry<UUID, List<String>> entry : new HashSet<>(messages.entrySet())) {
                        if (entry == null || entry.getKey() == null || messages == null) {
                            continue;
                        }
                        for (String s : new ArrayList<>(messages.remove(entry.getKey()))) {
                            statement.append("(?, ?)");
                            if (((i / 2) + 1) < amount) {
                                statement.append(", ");
                            }
                            lastList.put(i += 1, entry.getKey().toString());
                            lastList.put(i += 1, s);
                        }
                    }
                    statement.append(";");
                    PreparedStatement sql = connection.prepareStatement(statement.toString());
                    for (Map.Entry<Integer, String> entry : new HashSet<>(lastList.entrySet())) {
                        sql.setString(entry.getKey(), entry.getValue());
                    }
                    sql.execute();
                    sql.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 5000);
    }

    public void reload() {
        try (BufferedReader br = new BufferedReader(new FileReader("swears.txt"))) {
            String line = br.readLine();
            boolean swears = false;
            boolean specific = false;
            boolean spaces = false;
            while (line != null) {
                boolean header = false;
                if (line.startsWith("swears:")) {
                    swears = true;
                    specific = false;
                    spaces = false;
                    header = true;
                }
                if (line.startsWith("specific:")) {
                    swears = false;
                    specific = true;
                    spaces = false;
                    header = true;
                }
                if (line.startsWith("spaces:")) {
                    swears = false;
                    specific = false;
                    spaces = true;
                    header = true;
                }
                if (!header) {
                    if (swears) {
                        swearList.add(line);
                    } else if (specific) {
                        specificList.add(line);
                    } else if (spaces) {
                        spacesList.add(line);
                    }
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedReader br = new BufferedReader(new FileReader("links.txt"))) {
            String line = br.readLine();
            boolean whitelist = false;
            while (line != null) {
                if (line.startsWith("whitelist:")) {
                    whitelist = true;
                }
                if (whitelist) {
                    this.whitelist.add(line);
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void chatEvent(PacketPlayerChat packet) {
        UUID uuid = packet.getUniqueId();
        Player player = Dashboard.getPlayer(uuid);
        if (player == null) {
            return;
        }
        if (player.isNewGuest()) {
            return;
        }
        Rank rank = player.getRank();
        boolean special = rank.getRankId() >= Rank.SPECIALGUEST.getRankId();
        boolean squire = rank.getRankId() >= Rank.SQUIRE.getRankId();
        if (squire) {
            if (player.isAFK()) {
                player.setAFK(false);
                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Your AFK Timer has been reset!");
                PacketTitle title = new PacketTitle(player.getUniqueId(), ChatColor.RED + "" + ChatColor.BOLD + "Confirmed",
                        ChatColor.RED + "" + ChatColor.BOLD + "Your AFK Timer has been reset!", 10, 100, 20);
                player.send(title);
                player.afkAction();
                return;
            }
            player.afkAction();
        }
        boolean command = packet.getMessage().startsWith("/");
        if (player.isDisabled()) {
            if (command) {
                String m = packet.getMessage().replaceFirst("/", "");
                if (m.startsWith("staff")) {
                    Dashboard.commandUtil.handleCommand(player, m);
                }
            }
            return;
        }
        StringBuilder msg = new StringBuilder();
        String[] l = packet.getMessage().split(" ");
        for (int i = 0; i < l.length; i++) {
            if (l[i].equals("") || l[i].equals(" ")) {
                continue;
            }
            if (i < (l.length - 1)) {
                msg.append(l[i]).append(" ");
                continue;
            }
            msg.append(l[i]);
        }
        if (command) {
            if (!Dashboard.commandUtil.handleCommand(player, msg.toString().replaceFirst("/", ""))) {
                String s = msg.toString().toLowerCase().replaceFirst("/", "");
                if (rank.getRankId() < Rank.KNIGHT.getRankId() && (s.startsWith("/calc") || s.startsWith("/calculate") ||
                        s.startsWith("/eval") || s.startsWith("/evaluate") || s.startsWith("/solve") ||
                        s.startsWith("worldedit:/calc") || s.startsWith("worldedit:/calculate") ||
                        s.startsWith("worldedit:/eval") || s.startsWith("worldedit:/evaluate") ||
                        s.startsWith("worldedit:/solve"))) {
                    player.sendMessage(ChatColor.RED + "That command is disabled.");
                    return;
                }
                player.chat(msg.toString());
            }
            return;
        }
        if (!enoughTime(player)) {
            player.sendMessage(ChatColor.DARK_AQUA + "New Guests must be on the server for at least 15 minutes before " +
                    "talking in chat. Learn more at palnet.us/rules");
            return;
        }
        if (isMuted(player)) {
            return;
        }
        if (!squire) {
            //Muted Chat Check
            String server = player.getServer();
            if (Dashboard.getServer(server).isPark()) {
                server = "ParkChat";
            }
            if (mutedChats.contains(server)) {
                player.sendMessage(ChatColor.RED + "Chat is silenced right now!");
                return;
            }
            //ChatDelay Check
            if (time.containsKey(player.getUniqueId()) && System.currentTimeMillis() < time.get(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You have to wait " + chatDelay / 1000 + " seconds before chatting!");
                return;
            }
            time.put(player.getUniqueId(), System.currentTimeMillis() + chatDelay);
            msg = new StringBuilder(removeCaps(player, msg.toString()));
            if (containsSwear(player, packet.getMessage()) || isAdvert(player, packet.getMessage()) ||
                    spamCheck(player, packet.getMessage()) || containsUnicode(player, packet.getMessage())) {
                return;
            }
            String mm = packet.getMessage().toLowerCase().replace(".", "").replace("-", "").replace(",", "")
                    .replace("/", "").replace("_", "").replace(" ", "").replace(";", "");
            if (mm.contains("skype") || mm.contains(" skyp ") || mm.startsWith("skyp ") || mm.endsWith(" skyp") || mm.contains("skyp*")) {
                player.sendMessage(ChatColor.RED + "Please do not ask for Skype information!");
                return;
            }
            //Duplicate Message Check
            if (messageCache.containsKey(player.getUniqueId())) {
                if (msg.toString().equalsIgnoreCase(messageCache.get(player.getUniqueId()))) {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Please do not repeat the same message!");
                    return;
                }
            }
            messageCache.put(player.getUniqueId(), msg.toString());
        } else {
            if (msg.toString().startsWith(":warn-")) {
                Dashboard.warningUtil.handle(player, msg.toString());
                return;
            }
        }
        if (!player.getChannel().equals("all")) {
            switch (player.getChannel()) {
                case "party":
                    Dashboard.commandUtil.handleCommand(player, "pchat " + msg);
                    return;
                case "staff":
                    Dashboard.commandUtil.handleCommand(player, "sc " + msg);
                    return;
                case "admin":
                    Dashboard.commandUtil.handleCommand(player, "ho " + msg);
                    return;
            }
        }
        sendChat(player, msg.toString());
    }

    private boolean enoughTime(Player player) {
        return (((System.currentTimeMillis() - player.getLoginTime()) / 1000) + player.getOnlineTime()) >= 900;
    }

    public boolean isMuted(Player player) {
        Mute mute = player.getMute();
        if (mute != null && mute.isMuted()) {
            long releaseTime = mute.getRelease();
            Date currentTime = new Date();
            if (currentTime.getTime() > releaseTime) {
                Dashboard.sqlUtil.unmutePlayer(player.getUniqueId());
                player.getMute().setMuted(false);
            } else {
                String msg = ChatColor.RED + "You are silenced! You will be unsilenced in " +
                        DateUtil.formatDateDiff(mute.getRelease()) + ".";
                if (!mute.getReason().equals("")) {
                    msg += " Reason: " + player.getMute().getReason();
                }
                player.sendMessage(msg);
                return true;
            }
        }
        return false;
    }

    public void sendChat(Player player, String msg) {
        logMessage(player.getUniqueId(), msg);
        String sname = Dashboard.getServer(player.getServer()).getServerType();
        if (sname.startsWith("New")) {
            sname = sname.replaceAll("New", "");
        }
        if (Dashboard.getServer(player.getServer()).isPark()) {
            Rank rank = player.getRank();
            if (rank.getRankId() >= Rank.SQUIRE.getRankId()) {
                msg = ChatColor.translateAlternateColorCodes('&', msg);
            }
            String message = rank.getNameWithBrackets() + " " + ChatColor.GRAY + player.getName() + ": " +
                    rank.getChatColor() + msg;
            for (Player tp : Dashboard.getOnlinePlayers()) {
                if (tp.isNewGuest() || tp.isDisabled()) {
                    continue;
                }
                if (Dashboard.getServer(tp.getServer()).isPark()) {
                    String send = ChatColor.WHITE + "[" + ChatColor.GREEN + sname + ChatColor.WHITE + "] " + message;
                    boolean mention = false;
                    if (tp.hasMentions() && !tp.getUniqueId().equals(player.getUniqueId())) {
                        String possibleMention = send;
                        String name = tp.getName().toLowerCase();
                        if (possibleMention.contains(" " + name + " ") || possibleMention.startsWith(name + " ") ||
                                possibleMention.endsWith(" " + name) || possibleMention.equalsIgnoreCase(name) ||
                                possibleMention.contains(" " + name + ".") || possibleMention.startsWith(name + ".") ||
                                possibleMention.contains(" " + name + "!") || possibleMention.startsWith(name + "!")) {
                            mention = true;
                            send = ChatColor.BLUE + "* " + send;
                            break;
                        }
                    }
                    if (mention) {
                        tp.sendMessage(send);
                        tp.mention();
                    } else {
                        tp.sendMessage(send);
                    }
                }
            }
            return;
        }
        player.chat(msg);
    }

    public boolean containsSwear(Player player, String msg) {
        boolean bool = false;
        //omsg is the player's message with spaces
        final String omsg = msg.replace(".", "").replace("-", "")
                .replace(",", "").replace("/", "").replace("()", "o")
                .replace("0", "o").replace("_", "").replace("@", "a")
                .replace("$", "s").replace(";", "");
        //m is the player's message without spaces
        final String m = msg.replace(" ", "").replace(".", "")
                .replace("-", "").replace(",", "").replace("/", "")
                .replace("()", "o").replace("0", "o").replace("_", "")
                .replace("@", "a").replace("$", "s").replace(";", "");
        for (String s : swearList) {
            if (m.toLowerCase().contains(s)) {
                bool = true;
                break;
            }
        }
        if (!bool) {
            for (String s : spacesList) {
                if (omsg.equalsIgnoreCase(s) || omsg.toLowerCase().startsWith(s + " ") || omsg.toLowerCase().endsWith(" " + s)
                        || omsg.contains(" " + s + " ")) {
                    bool = true;
                    break;
                }
            }
        }
        if (!bool) {
            for (String s : specificList) {
                if (omsg.toLowerCase().contains(s)) {
                    bool = true;
                    break;
                }
            }
        }
        if (bool) {
            player.sendMessage(ChatColor.RED + "Please do not swear!");
            logMessage(player.getUniqueId(), msg);
            swearMessage(player.getName(), msg);
            return true;
        }
        return false;
    }

    public boolean containsUnicode(Player player, String msg) {
        List<Character> blocked = new ArrayList<>();
        for (Character c : msg.toLowerCase().toCharArray()) {
            if (!allowedChars.contains(c.toString()) && !blocked.contains(c)) {
                blocked.add(c);
            }
        }
        if (!blocked.isEmpty()) {
            StringBuilder text = new StringBuilder("Your message contains blocked characters! Click to read more about why your message was blocked.\n\nThe following character" +
                    (blocked.size() > 1 ? "s were" : " was") + " blocked:");
            for (Character c : blocked) {
                text.append("\n- '").append(c.toString()).append("'");
            }
            PacketLink packet = new PacketLink(player.getUniqueId(), "https://palnet.us/help/faq#blocked-chars", text.toString(),
                    ChatColor.AQUA, false);
            player.send(packet);
            return true;
        }
        return false;
    }

    public void logMessage(UUID uuid, String msg) {
        if (messages.containsKey(uuid)) {
            messages.get(uuid).add(msg);
            return;
        }
        List<String> list = new ArrayList<>();
        list.add(msg);
        messages.put(uuid, list);
    }

    private void swearMessage(String name, String msg) {
        UUID id = UUID.randomUUID();
        String response = "Please keep chat appropriate.";
        Warning warning = new Warning(id, name, msg, response, System.currentTimeMillis() + 300000);
        Dashboard.warningUtil.trackWarning(warning);
        PacketWarning packet = new PacketWarning(id, name, msg, "possibly swears");
        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel dash = (DashboardSocketChannel) o;
            if (!dash.getType().equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                continue;
            }
            dash.send(packet);
        }
    }

    private void advertMessage(String name, String msg) {
        UUID id = UUID.randomUUID();
        String response = "Please to not attempt to advertise or share links.";
        Warning warning = new Warning(id, name, msg, response, System.currentTimeMillis() + 300000);
        Dashboard.warningUtil.trackWarning(warning);
        PacketWarning packet = new PacketWarning(id, name, msg, "advertises");
        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel dash = (DashboardSocketChannel) o;
            if (!dash.getType().equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                continue;
            }
            dash.send(packet);
        }
    }

    public String removeCaps(Player player, String msg) {
        int size = msg.toCharArray().length;
        if (size < 10) {
            return msg;
        }
        int amount = 0;
        for (char c : msg.toCharArray()) {
            if (Character.isUpperCase(c)) {
                amount++;
            }
        }
        if (Math.floor((double) (100 * (((float) amount) / size))) >= 50.0) {
            player.sendMessage(ChatColor.RED + "Please do not use a lot of capitals in your messages.");
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < msg.length(); i++) {
                if (i == 0) {
                    s.append(msg.charAt(0));
                    continue;
                }
                s.append(Character.toLowerCase(msg.charAt(i)));
            }
            return s.toString();
        }
        return msg;
    }

    public boolean spamCheck(Player player, String msg) {
        if (Dashboard.getPlayer(msg) != null) {
            return false;
        }
        Character last = null;
        int amount = 0;
        StringBuilder word = new StringBuilder();
        boolean spam = false;
        for (char c : msg.toCharArray()) {
            if (last == null) {
                last = c;
                continue;
            }
            if (c == ' ') {
                if (Dashboard.getPlayer(word.toString().trim()) != null) {
                    spam = false;
                }
                word = new StringBuilder();
                continue;
            }
            if (c == last) {
                amount++;
            } else {
                amount = 0;
            }
            if (amount >= 4) {
                spam = true;
            }
            last = c;
            word.append(c);
        }
        if (Dashboard.getPlayer(word.toString().trim()) != null) {
            spam = false;
        }
        if (spam) {
            player.sendMessage(ChatColor.RED + "Please do not spam chat with excessive amounts of characters.");
            return true;
        }
        int numamount = 0;
        spam = false;
        word = new StringBuilder();
        for (char c : msg.toCharArray()) {
            if (c == ' ') {
                if (Dashboard.getPlayer(word.toString().trim()) != null) {
                    spam = false;
                }
                word = new StringBuilder();
                continue;
            }
            if (isInt(c)) {
                numamount++;
            }
            if (numamount >= 8) {
                spam = true;
            }
            word.append(c);
        }
        if (Dashboard.getPlayer(word.toString().trim()) != null) {
            spam = false;
        }
        if (spam) {
            player.sendMessage(ChatColor.RED + "Please do not spam chat with excessive amounts of numbers.");
            return true;
        }
        return false;
    }

    private boolean isInt(char c) {
        try {
            Integer.parseInt(String.valueOf(c));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean isAdvert(Player player, String msg) {
        Matcher m = linkPattern.matcher(msg.toLowerCase());
        if (m.find()) {
            if (isWhitelisted(m.toMatchResult().group())) {
                return false;
            }
            advertMessage(player.getName(), msg);
            player.sendMessage(ChatColor.RED + "Please do not attempt to advertise or share links.");
            return true;
        }
        return false;
    }

    private boolean isWhitelisted(String group) {
        for (String s : whitelist) {
            String m;
            if (group.startsWith("https://")) {
                m = group.replaceFirst("https://", "");
            } else if (group.startsWith("http://")) {
                m = group.replaceFirst("http://", "");
            } else {
                m = group;
            }
            if (m.startsWith(s) || m.endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    public void staffChatMessage(String msg) {
        for (Player player : Dashboard.getOnlinePlayers()) {
            if (player.getRank().getRankId() >= Rank.SQUIRE.getRankId() && !player.isDisabled()) {
                try {
                    player.sendMessage(msg);
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void socialSpyMessage(Player from, Player to, String message, String command) {
        if (Dashboard.getServer(from.getServer()).isPark()) {
            String msg = ChatColor.WHITE + from.getName() + ": /" + command + " " + to.getName() + " " + message;
            for (Player tp : Dashboard.getOnlinePlayers()) {
                if (tp.getRank().getRankId() < Rank.SQUIRE.getRankId() || tp.getServer() == null ||
                        tp.getUniqueId().equals(from.getUniqueId()) || tp.getUniqueId().equals(to.getUniqueId()) || tp.isDisabled()) {
                    continue;
                }
                if (Dashboard.getServer(tp.getServer()).isPark()) {
                    tp.sendMessage(msg);
                }
            }
        } else {
            String server = from.getServer();
            for (Player tp : Dashboard.getOnlinePlayers()) {
                if (tp.getRank().getRankId() < Rank.SQUIRE.getRankId() || tp.getServer() == null ||
                        tp.getUniqueId().equals(from.getUniqueId()) || tp.getUniqueId().equals(to.getUniqueId()) || tp.isDisabled()) {
                    continue;
                }
                if (tp.getServer().equals(server)) {
                    tp.sendMessage(from.getName() + ": /" + command + " " + to.getName() + " " + message);
                }
            }
        }
    }

    public void socialSpyParty(Player player, Party party, String message, String command) {
        if (Dashboard.getServer(player.getServer()).isPark()) {
            String msg = ChatColor.WHITE + player.getName() + ": /" + command + " " + party.getLeader().getName() +
                    " " + message;
            for (Player tp : Dashboard.getOnlinePlayers()) {
                if (tp.getRank().getRankId() < Rank.SQUIRE.getRankId() || tp.getServer() == null ||
                        party.getMembers().contains(tp.getUniqueId()) || tp.isDisabled()) {
                    continue;
                }
                if (Dashboard.getServer(tp.getServer()).isPark()) {
                    tp.sendMessage(msg);
                }
            }
        } else {
            String server = player.getServer();
            for (Player tp : Dashboard.getOnlinePlayers()) {
                if (tp.getRank().getRankId() < Rank.SQUIRE.getRankId() || tp.getServer() == null ||
                        party.getMembers().contains(tp.getUniqueId()) || tp.isDisabled()) {
                    continue;
                }
                if (tp.getServer().equals(server)) {
                    tp.sendMessage(player.getName() + ": /" + command + " " + party.getLeader().getName() + " " + message);
                }
            }
        }
    }

    public void setChatDelay(int i) {
        this.chatDelay = i;
    }

    public int getChatDelay() {
        return chatDelay;
    }

    public void muteChat(String server) {
        mutedChats.add(server);
    }

    public void unmuteChat(String server) {
        mutedChats.remove(server);
    }

    public boolean isChatMuted(String server) {
        return mutedChats.contains(server);
    }

    public boolean privateMessagesEnabled() {
        return privateMessages;
    }

    public void setPrivateMessages(boolean privateMessages) {
        this.privateMessages = privateMessages;
    }
}