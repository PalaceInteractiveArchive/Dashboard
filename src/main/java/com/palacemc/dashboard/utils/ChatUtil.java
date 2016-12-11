package com.palacemc.dashboard.utils;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.*;
import com.palacemc.dashboard.packets.dashboard.*;
import com.palacemc.dashboard.server.DashboardSocketChannel;
import com.palacemc.dashboard.server.WebSocketServerHandler;

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
    private List<String> whitelist = new ArrayList<>();
    private List<String> allowedChars = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
            "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "=", "_", "+", "[", "]", "{", "}", "/",
            "\\", "?", "|", ",", ".", "<", ">", "`", "~", ";", ":", "'", "\"", "√", "˚", "≤", "≥", "™", "£", "¢", "∞",
            "•", " ");
    private Pattern linkPattern = Pattern.compile("((\\d{1,3}\\.){3}\\d{1,3}(:\\d+)?)|(([0-9a-z:/]+(\\.|\\(dot\\)\\(\\.\\" +
            ")))+(aero|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|pro|tel|travel|ac|ad|ae|" +
            "af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc" +
            "|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cu|cv|cx|cy|cz|cz|de|dj|dk|dm|do|dz|ec|ee|eg|er|es|et|eu|fi|fj|fk|fm|fo|f" +
            "r|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|" +
            "je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mn|mn" +
            "|mo|mp|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|nom|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|" +
            "pt|pw|py|qa|re|ra|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sj|sk|sl|sm|sn|so|sr|st|su|sv|sy|sz|tc|td|tf|tg|th|tj" +
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
                try (Connection connection = Launcher.getDashboard().getSqlUtil().getConnection()) {
                    if (messages.isEmpty()) {
                        return;
                    }
                    int amount = 0;
                    for (Map.Entry<UUID, List<String>> entry : new HashSet<>(messages.entrySet())) {
                        for (String s : entry.getValue()) {
                            amount++;
                        }
                    }
                    String statement = "INSERT INTO chat (user, message) VALUES ";
                    int i = 0;
                    HashMap<Integer, String> lastList = new HashMap<>();
                    for (Map.Entry<UUID, List<String>> entry : new HashSet<>(messages.entrySet())) {
                        if (entry == null || entry.getKey() == null || messages == null) {
                            continue;
                        }
                        for (String s : new ArrayList<>(messages.remove(entry.getKey()))) {
                            statement += "(?, ?)";
                            if (((i / 2) + 1) < amount) {
                                statement += ", ";
                            }
                            lastList.put(i += 1, entry.getKey().toString());
                            lastList.put(i += 1, s);
                        }
                    }
                    statement += ";";
                    PreparedStatement sql = connection.prepareStatement(statement);
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
            while (line != null) {
                if (line.startsWith("swears:")) {
                    swears = true;
                    specific = false;
                }
                if (line.startsWith("specific:")) {
                    swears = false;
                    specific = true;
                }
                if (swears) {
                    swearList.add(line);
                } else if (specific) {
                    specificList.add(line);
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
        UUID uuid = packet.getUuid();
        Player player = Launcher.getDashboard().getPlayer(uuid);
        if (player == null) {
            return;
        }
        if (player.isNewGuest()) {
            return;
        }
        Rank rank = player.getRank();
        boolean special = rank.getRankId() >= Rank.SPECIALGUEST.getRankId();
        boolean eme = rank.getRankId() >= Rank.SQUIRE.getRankId();
        if (eme) {
            if (player.isAFK()) {
                player.setAFK(false);
                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Your AFK Timer has been reset!");
                PacketTitle title = new PacketTitle(player.getUuid(), ChatColor.RED + "" + ChatColor.BOLD + "Confirmed",
                        ChatColor.RED + "" + ChatColor.BOLD + "Your AFK Timer has been reset!", 10, 100, 20);
                player.send(title);
                player.afkAction();
                return;
            }
            player.afkAction();
        }
        boolean command = packet.getMessage().startsWith("/");
        String msg = "";
        String[] l = packet.getMessage().split(" ");
        for (int i = 0; i < l.length; i++) {
            if (l[i].equals("") || l[i].equals(" ")) {
                continue;
            }
            if (i < (l.length - 1)) {
                msg += l[i] + " ";
                continue;
            }
            msg += l[i];
        }
        if (command) {
            if (!Launcher.getDashboard().getCommandUtil().handleCommand(player, msg.replaceFirst("/", ""))) {
                player.chat(msg);
            }
            return;
        }
        if (!enoughTime(player)) {
            player.sendMessage(ChatColor.DARK_AQUA + "New Guests must be on the server for at least 15 minutes before " +
                    "talking in chat. Learn more at mcmagic.us/rules#chat");
            return;
        }
        if (isMuted(player)) {
            return;
        }
        if (!eme) {
            //Muted Chat Check
            String server = player.getServer();
            if (Launcher.getDashboard().getServer(server).isPark()) {
                server = "ParkChat";
            }
            if (mutedChats.contains(server)) {
                player.sendMessage(ChatColor.RED + "Chat is silenced right now!");
                return;
            }
            //ChatDelay Check
            if (time.containsKey(player.getUuid()) && System.currentTimeMillis() < time.get(player.getUuid())) {
                player.sendMessage(ChatColor.RED + "You have to wait " + chatDelay / 1000 + " seconds before chatting!");
                return;
            }
            time.put(player.getUuid(), System.currentTimeMillis() + chatDelay);
            msg = removeCaps(player, msg);
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
            if (messageCache.containsKey(player.getUuid())) {
                if (msg.equalsIgnoreCase(messageCache.get(player.getUuid()))) {
                    player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Please do not repeat the same message!");
                    return;
                }
            }
            messageCache.put(player.getUuid(), msg);
        } else {
            if (msg.startsWith(":warn-")) {
                Launcher.getDashboard().getWarningUtil().handle(player, msg);
                return;
            }
        }
        if (!player.getChannel().equals("all")) {
            switch (player.getChannel()) {
                case "party":
                    Launcher.getDashboard().getCommandUtil().handleCommand(player, "pchat " + msg);
                    return;
                case "staff":
                    Launcher.getDashboard().getCommandUtil().handleCommand(player, "sc " + msg);
                    return;
                case "admin":
                    Launcher.getDashboard().getCommandUtil().handleCommand(player, "ho " + msg);
                    return;
            }
        }
        sendChat(player, msg);
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
                Launcher.getDashboard().getSqlUtil().unmutePlayer(player.getUuid());
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
        logMessage(player.getUuid(), msg);
        String sname = Launcher.getDashboard().getServer(player.getServer()).getServerType();
        if (sname.startsWith("New")) {
            sname = sname.replaceAll("New", "");
        }
        if (Launcher.getDashboard().getServer(player.getServer()).isPark()) {
            Rank rank = player.getRank();
            if (rank.getRankId() >= Rank.SQUIRE.getRankId()) {
                msg = ChatColor.translateAlternateColorCodes('&', msg);
            }
            String message = rank.getNameWithBrackets() + " " + ChatColor.GRAY + player.getUsername() + ": " +
                    rank.getChatColor() + msg;
            for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
                if (tp.isNewGuest()) {
                    continue;
                }
                if (Launcher.getDashboard().getServer(tp.getServer()).isPark()) {
                    String send = ChatColor.WHITE + "[" + ChatColor.GREEN + sname + ChatColor.WHITE + "] " + message;
                    boolean mention = false;
                    if (tp.isMentions() && !tp.getUuid().equals(player.getUuid())) {
                        String possibleMention = send;
                        String name = tp.getUsername().toLowerCase();
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
        final String omsg = msg.replace(".", "").replace("-", "")
                .replace(",", "").replace("/", "").replace("()", "o")
                .replace("0", "o").replace("_", "").replace("@", "a")
                .replace("$", "s").replace(";", "");
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
        if (omsg.equalsIgnoreCase("ass") || omsg.toLowerCase().startsWith("ass ") || omsg.toLowerCase().endsWith(" ass")
                || omsg.contains(" ass ")) {
            bool = true;
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
            logMessage(player.getUuid(), msg);
            swearMessage(player.getUsername(), msg);
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
            String text = "Your message contains blocked characters! Click to read more about why your message was blocked.\n\nThe following character" +
                    (blocked.size() > 1 ? "s were" : " was") + " blocked:";
            for (Character c : blocked) {
                text += "\n- '" + c.toString() + "'";
            }
            PacketLink packet = new PacketLink(player.getUuid(), "https://mcmagic.us/help/faq#blocked-chars", text,
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
        Launcher.getDashboard().getWarningUtil().trackWarning(warning);
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
            String s = "";
            for (int i = 0; i < msg.length(); i++) {
                if (i == 0) {
                    s += msg.charAt(0);
                    continue;
                }
                s += Character.toLowerCase(msg.charAt(i));
            }
            return s;
        }
        return msg;
    }

    public boolean spamCheck(Player player, String msg) {
        if (Launcher.getDashboard().getPlayer(msg) != null) {
            return false;
        }
        Character last = null;
        int amount = 0;
        String word = "";
        boolean spam = false;
        for (char c : msg.toCharArray()) {
            if (last == null) {
                last = c;
                continue;
            }
            if (c == ' ') {
                if (Launcher.getDashboard().getPlayer(word.trim()) != null) {
                    spam = false;
                }
                word = "";
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
            word += c;
        }
        if (Launcher.getDashboard().getPlayer(word.trim()) != null) {
            spam = false;
        }
        if (spam) {
            player.sendMessage(ChatColor.RED + "Please do not spam chat with excessive amounts of characters.");
            return true;
        }
        int numamount = 0;
        spam = false;
        word = "";
        for (char c : msg.toCharArray()) {
            if (c == ' ') {
                if (Launcher.getDashboard().getPlayer(word.trim()) != null) {
                    spam = false;
                }
                word = "";
                continue;
            }
            if (isInt(c)) {
                numamount++;
            }
            if (numamount >= 8) {
                spam = true;
            }
            word += c;
        }
        if (Launcher.getDashboard().getPlayer(word.trim()) != null) {
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
            advertMessage(player.getUsername(), msg);
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
        for (Player player : Launcher.getDashboard().getOnlinePlayers()) {
            if (player.getRank().getRankId() >= Rank.SQUIRE.getRankId()) {
                try {
                    player.sendMessage(msg);
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void socialSpyMessage(Player from, Player to, String message, String command) {
        if (Launcher.getDashboard().getServer(from.getServer()).isPark()) {
            String msg = ChatColor.WHITE + from.getUsername() + ": /" + command + " " + to.getUsername() + " " + message;
            for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
                if (tp.getRank().getRankId() < Rank.SQUIRE.getRankId() || tp.getServer() == null ||
                        tp.getUuid().equals(from.getUuid()) || tp.getUuid().equals(to.getUuid())) {
                    continue;
                }
                if (Launcher.getDashboard().getServer(tp.getServer()).isPark()) {
                    tp.sendMessage(msg);
                }
            }
        } else {
            String server = from.getServer();
            for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
                if (tp.getRank().getRankId() < Rank.SQUIRE.getRankId() || tp.getServer() == null ||
                        tp.getUuid().equals(from.getUuid()) || tp.getUuid().equals(to.getUuid())) {
                    continue;
                }
                if (tp.getServer().equals(server)) {
                    tp.sendMessage(from.getUsername() + ": /" + command + " " + to.getUsername() + " " + message);
                }
            }
        }
    }

    public void socialSpyParty(Player player, Party party, String message, String command) {
        if (Launcher.getDashboard().getServer(player.getServer()).isPark()) {
            String msg = ChatColor.WHITE + player.getUsername() + ": /" + command + " " + party.getLeader().getUsername() +
                    " " + message;
            for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
                if (tp.getRank().getRankId() < Rank.SQUIRE.getRankId() || tp.getServer() == null ||
                        party.getMembers().contains(tp.getUuid())) {
                    continue;
                }
                if (Launcher.getDashboard().getServer(tp.getServer()).isPark()) {
                    tp.sendMessage(msg);
                }
            }
        } else {
            String server = player.getServer();
            for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
                if (tp.getRank().getRankId() < Rank.SQUIRE.getRankId() || tp.getServer() == null ||
                        party.getMembers().contains(tp.getUuid())) {
                    continue;
                }
                if (tp.getServer().equals(server)) {
                    tp.sendMessage(player.getUsername() + ": /" + command + " " + party.getLeader().getUsername() + " " + message);
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