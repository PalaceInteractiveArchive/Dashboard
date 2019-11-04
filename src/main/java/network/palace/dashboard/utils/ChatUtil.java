package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.DashboardConstants;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.dashboard.*;
import network.palace.dashboard.packets.park.PacketMuteChat;
import network.palace.dashboard.server.DashboardSocketChannel;
import network.palace.dashboard.server.WebSocketServerHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Created by Marc on 7/15/16
 */
public class ChatUtil {
    private HashMap<UUID, Long> time = new HashMap<>();
    private HashMap<UUID, ChatMessage> messageCache = new HashMap<>();
    private LinkedList<ChatMessage> messages = new LinkedList<>();
    private List<String> mutedChats = new ArrayList<>();

    private List<String> swearList = new ArrayList<>();
    private List<String> specificList = new ArrayList<>();
    private List<String> spacesList = new ArrayList<>();
    private List<String> whitelist = new ArrayList<>();
    private List<String> allowedChars = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
            "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "=", "_", "+", "[", "]", "{", "}", "/",
            "\\", "?", "|", ",", ".", "<", ">", "`", "~", ";", ":", "'", "\"", "√", "˚", "≤", "≥", "™", "£", "¢", "∞",
            "•", " ");

    private boolean privateMessages = true;
    private int chatDelay = 2000;

    public ChatUtil() {
        Dashboard dashboard = Launcher.getDashboard();
        File f = new File("chat.txt");
        if (!f.exists()) {
            return;
        }
        try {
            Scanner scanner = new Scanner(new FileReader(f));
            while (scanner.hasNextLine()) {
                String server = scanner.nextLine();
                mutedChats.add(server);
            }
        } catch (Exception e) {
            dashboard.getLogger().error("An exception occurred while parsing chat.txt - " + e.getMessage());
            ErrorUtil.logError("Error parsing chat.txt", e);
        }
        f.delete();
        reload();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    int size = messages.size();
                    List<ChatMessage> localMessages = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        if (messages.isEmpty()) break;
                        try {
                            ChatMessage msg = messages.pop();
                            localMessages.add(msg);
                        } catch (NoSuchElementException e2) {
                            e2.printStackTrace();
                            messages.clear();
                            break;
                        }
                    }
                    for (ChatMessage msg : localMessages) {
                        dashboard.getMongoHandler().logChat(msg);
                    }
//                    HashMap<UUID, List<String>> localMessages = new HashMap<>(messages);
//                    messages.clear();
//                    for (Map.Entry<UUID, List<String>> entry : new HashSet<>(localMessages.entrySet())) {
//                        dashboard.getMongoHandler().logChat(entry.getKey(), entry.getValue());
//                    }
                } catch (Exception e) {
                    messages.clear();
                    e.printStackTrace();
                    dashboard.getErrors().error("Error logging chat: " + e.getMessage());
                }
            }
        }, 0, 5000);
    }

    public void reload() {
        swearList.clear();
        specificList.clear();
        spacesList.clear();
        whitelist.clear();
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
        Dashboard dashboard = Launcher.getDashboard();
        UUID uuid = packet.getUniqueId();
        Player player = dashboard.getPlayer(uuid);
        String message = packet.getMessage();
        dashboard.getLogger().info((player == null ? "PLAYER IS NULL " : "") + "CHAT MESSAGE FROM " + uuid.toString() + ": '" + message + "'");

        if (player == null) return;
        if (player.isNewGuest() && !message.startsWith("/")) return;

        Rank rank = player.getRank();
        SponsorTier tier = player.getSponsorTier();
        boolean trainee = rank.getRankId() >= Rank.TRAINEE.getRankId();
        if (trainee) {
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
        boolean command = message.startsWith("/");
        if (player.isDisabled()) {
            if (command) {
                String m = message.replaceFirst("/", "");
                if (m.startsWith("staff")) {
                    dashboard.getCommandUtil().handleCommand(player, m);
                }
            }
            return;
        }
        StringBuilder msg = new StringBuilder();
        String[] l = message.split(" ");
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
            if (!dashboard.getCommandUtil().handleCommand(player, msg.toString().replaceFirst("/", ""))) {
                String s = msg.toString().toLowerCase().replaceFirst("/", "");
                if (rank.getRankId() < Rank.TRAINEEBUILD.getRankId() && (s.startsWith("/calc") || s.startsWith("/calculate") ||
                        s.startsWith("/eval") || s.startsWith("/evaluate") || s.startsWith("/solve") ||
                        s.startsWith("worldedit:/calc") || s.startsWith("worldedit:/calculate") ||
                        s.startsWith("worldedit:/eval") || s.startsWith("worldedit:/evaluate") ||
                        s.startsWith("worldedit:/solve") || s.startsWith("train") || s.startsWith("cart"))) {
                    player.sendMessage(ChatColor.RED + "That command is disabled.");
                    return;
                }
                player.chat(msg.toString());
            }
            return;
        }
        if (player.getOnlineTime() == 0) {
            player.sendMessage(ChatColor.RED + "We're currently loading your chat settings, try chatting again in a few seconds!");
            return;
        }
        if (notEnoughTime(player)) {
            player.sendMessage(DashboardConstants.NEW_GUEST);
            return;
        }
        if (isMuted(player)) {
            dashboard.getLogger().info("CANCELLED CHAT EVENT PLAYER MUTED");
            return;
        }
        if (!trainee) {
            //Muted Chat Check
            String server = player.getServer();
            if (dashboard.getServer(server).isPark()) {
                server = "ParkChat";
            }
            if (isChatMuted(server) && !server.equals("Creative")) {
                player.sendMessage(DashboardConstants.MUTED_CHAT);
                dashboard.getLogger().info("CANCELLED CHAT EVENT CHAT MUTED");
                return;
            }

            if (dashboard.isStrictMode() && !messageCache.isEmpty() && message.length() >= 10) {
                ChatMessage chatMessage = null;
                for (ChatMessage cached : messageCache.values()) {
                    if (chatMessage == null) {
                        chatMessage = cached;
                        continue;
                    }
                    if (cached.getTime() > chatMessage.getTime()) {
                        chatMessage = cached;
                    }
                }

//                ChatMessage chatMessage = (ChatMessage) this.messageCache.values().toArray()[messageCache.size() - 1];

                //Only strict-check messages said within the last 10 seconds
                if (chatMessage != null && System.currentTimeMillis() - chatMessage.getTime() < 10 * 1000) {
                    String lastMessage = chatMessage.getMessage();
                    double distance = dashboard.getChatAlgorithm().similarity(message, lastMessage);
                    if (distance >= dashboard.getStrictThreshold()) {
                        player.sendMessage(ChatColor.RED + "Your message was similar to another recently said in chat and was marked as spam. We apologize if this was done in error, we're constantly improving our chat filter.");
//                    swearMessage(player.getUsername(), message);
                        dashboard.getModerationUtil().announceSpamMessage(player.getUsername(), message);
                        dashboard.getLogger().info("CANCELLED CHAT EVENT STRICT MODE");
                        return;
                    }
                }
                /*String secondLastMessage = (String) this.messageCache.values().toArray()[messageCache.size() - 2];
                double secondDistance = dashboard.getChatAlgorithm().similarity(message, secondLastMessage);
                //Slightly less strict second check
                if (secondDistance >= (dashboard.getStrictThreshold() * 1.4)) {
                    swearMessage(player.getUsername(), message);
                    dashboard.getLogger().info("CANCELLED CHAT EVENT SECOND STRICT MODE");
                    return;
                }*/
            }

            //ChatDelay Check
            if (rank.getRankId() < Rank.CHARACTER.getRankId() && time.containsKey(player.getUniqueId()) && System.currentTimeMillis() - time.get(player.getUniqueId()) < chatDelay) {
                String response = DashboardConstants.CHAT_DELAY.replaceAll("<TIME>", String.valueOf(chatDelay / 1000));
                player.sendMessage(response);
                dashboard.getLogger().info("CANCELLED CHAT EVENT CHAT DELAY");
                return;
            }
            time.put(player.getUniqueId(), System.currentTimeMillis());

            msg = new StringBuilder(removeCaps(player, msg.toString()));
            String msgString = msg.toString();

            //Duplicate Message Check
            if (messageCache.containsKey(player.getUniqueId())) {
                ChatMessage cachedMessage = messageCache.get(player.getUniqueId());
                //Block saying the same message within a minute
                if ((System.currentTimeMillis() - cachedMessage.getTime() < 60 * 1000) && msgString.equalsIgnoreCase(cachedMessage.getMessage())) {
                    player.sendMessage(DashboardConstants.MESSAGE_REPEAT);
                    dashboard.getLogger().info("CANCELLED CHAT EVENT DUPLICATE");
                    return;
                }
            }

            String temp = message.trim();
            if (containsSwear(player, temp) || isAdvert(player, temp) ||
                    spamCheck(player, temp) || containsUnicode(player, temp)) {
                dashboard.getLogger().info("CANCELLED CHAT EVENT SWEAR,ADVERT,SPAM,UNICODE");
                return;
            }

            //TODO Remove skype check?
            String mm = message.toLowerCase().replace(".", "").replace("-", "").replace(",", "")
                    .replace("/", "").replace("_", "").replace(" ", "").replace(";", "");
            if (mm.contains("skype") || mm.contains(" skyp ") || mm.startsWith("skyp ") || mm.endsWith(" skyp") || mm.contains("skyp*")) {
                player.sendMessage(DashboardConstants.SKYPE_INFORMATION);
                dashboard.getLogger().info("CANCELLED CHAT EVENT SKYPE");
                return;
            }

            messageCache.put(player.getUniqueId(), new ChatMessage(msgString, System.currentTimeMillis()));
        } else {
            if (msg.toString().startsWith(":warn-")) {
                dashboard.getWarningUtil().handle(player, msg.toString());
                dashboard.getLogger().info("CANCELLED CHAT EVENT WARNING CLICK");
                return;
            }
        }

        if (!player.getChannel().equals("all")) {
            switch (player.getChannel()) {
                case "party":
                    dashboard.getCommandUtil().handleCommand(player, "pchat " + msg);
                    dashboard.getLogger().info("CANCELLED CHAT EVENT PARTY CHAT");
                    return;
                case "staff":
                    dashboard.getCommandUtil().handleCommand(player, "sc " + msg);
                    dashboard.getLogger().info("CANCELLED CHAT EVENT STAFF CHAT");
                    return;
                case "admin":
                    dashboard.getCommandUtil().handleCommand(player, "ho " + msg);
                    dashboard.getLogger().info("CANCELLED CHAT EVENT ADMIN CHAT");
                    return;
            }
        }

        String m = msg.toString();
        logMessage(player.getUniqueId(), m);

        String emoji;
        try {
            emoji = dashboard.getEmojiUtil().convertMessage(player, m);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + e.getMessage());
            return;
        }

        if (!emoji.equals(m)) m = emoji;

        String sname = dashboard.getServer(player.getServer()).getServerType();
        if (sname.startsWith("New")) {
            sname = sname.replaceAll("New", "");
        }
        if (dashboard.getServer(player.getServer()).isPark()) {
            if (rank.getRankId() >= Rank.TRAINEE.getRankId()) {
                m = ChatColor.translateAlternateColorCodes('&', m);
            }
            String m2 = tier.getChatTag(true) + rank.getFormattedName() + " " + ChatColor.GRAY + player.getUsername() + ": " +
                    rank.getChatColor() + m;
            for (Player tp : dashboard.getOnlinePlayers()) {
                if (tp.isNewGuest() || tp.isDisabled() ||
                        (rank.getRankId() < Rank.CHARACTER.getRankId() && tp.isIgnored(player.getUniqueId()) && tp.getRank().getRankId() < Rank.CHARACTER.getRankId()))
                    continue;
                if (dashboard.getServer(tp.getServer()).isPark()) {
                    String send = ChatColor.WHITE + "[" + ChatColor.GREEN + sname + ChatColor.WHITE + "] " + m2;
//                    String send = m2;
                    boolean mention = false;
                    if (tp.hasMentions() && !tp.getUniqueId().equals(player.getUniqueId())) {
                        String possibleMention = m.toLowerCase();
                        String name = tp.getUsername().toLowerCase();
                        if (possibleMention.contains(" " + name + " ") || possibleMention.startsWith(name + " ") ||
                                possibleMention.endsWith(" " + name) || possibleMention.equalsIgnoreCase(name) ||
                                possibleMention.contains(" " + name + ".") || possibleMention.startsWith(name + ".") ||
                                possibleMention.contains(" " + name + "!") || possibleMention.startsWith(name + "!")) {
                            mention = true;
                            send = ChatColor.BLUE + "* " + send;
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
        player.chat(m);
    }

    public static boolean notEnoughTime(Player player) {
        return (((System.currentTimeMillis() - player.getLoginTime()) / 1000) + player.getOnlineTime()) < 600;
    }

    public boolean isMuted(Player player) {
        Dashboard dashboard = Launcher.getDashboard();
        Mute mute = player.getMute();
        if (mute != null && mute.isMuted()) {
            long releaseTime = mute.getExpires();
            Date currentTime = new Date();
            if (currentTime.getTime() > releaseTime) {
                dashboard.getMongoHandler().unmutePlayer(player.getUniqueId());
                player.getMute().setMuted(false);
            } else {
                String response = DashboardConstants.MUTED_PLAYER;
                response = response.replaceAll("<TIME>", DateUtil.formatDateDiff(mute.getExpires()));
                if (!mute.getReason().isEmpty())
                    response += DashboardConstants.MUTE_REASON.replaceAll("<REASON>", player.getMute().getReason());
                player.sendMessage(response);
                return true;
            }
        }
        return false;
    }

    public boolean containsSwear(Player player, String msg) {
        boolean containsSwear = false;
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
                containsSwear = true;
                break;
            }
        }
        if (!containsSwear) {
            for (String s : spacesList) {
                if (omsg.equalsIgnoreCase(s) || omsg.toLowerCase().startsWith(s + " ") || omsg.toLowerCase().endsWith(" " + s)
                        || omsg.contains(" " + s + " ")) {
                    containsSwear = true;
                    break;
                }
            }
        }
        if (!containsSwear) {
            for (String s : specificList) {
                if (omsg.toLowerCase().contains(s)) {
                    containsSwear = true;
                    break;
                }
            }
        }
        if (containsSwear) {
            player.sendMessage(DashboardConstants.SWEAR_RESPONSE);
            logMessage(player.getUniqueId(), msg);
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
        messages.add(new ChatMessage(uuid, msg, System.currentTimeMillis() / 1000));
//        if (messages.containsKey(uuid)) {
//            List<String> msgs = messages.get(uuid);
//            msgs.add(msg);
//            messages.put(uuid, msgs);
//            return;
//        }
//        List<String> list = new ArrayList<>();
//        list.add(msg);
//        messages.put(uuid, list);
    }

    private void swearMessage(String name, String msg) {
        Dashboard dashboard = Launcher.getDashboard();
        UUID id = UUID.randomUUID();
        String response = DashboardConstants.SWEAR_WARNING;
        ClickWarning warning = new ClickWarning(id, name, msg, response, System.currentTimeMillis() + 300000);
        dashboard.getWarningUtil().trackWarning(warning);
        PacketWarning packet = new PacketWarning(id, name, msg, "possibly swears");
        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel dash = (DashboardSocketChannel) o;
            if (!dash.getType().equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                continue;
            }
            dash.send(packet);
        }
        dashboard.getSchedulerManager().runAsync(() -> dashboard.getMongoHandler().logInfraction(name, msg));
    }

    private void advertMessage(String name, String msg) {
        Dashboard dashboard = Launcher.getDashboard();
        UUID id = UUID.randomUUID();
        String response = DashboardConstants.LINK_WARNING;
        ClickWarning warning = new ClickWarning(id, name, msg, response, System.currentTimeMillis() + 300000);
        dashboard.getWarningUtil().trackWarning(warning);
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
        if (Math.floor(100 * (((float) amount) / size)) >= 50.0) {
            player.sendMessage(DashboardConstants.EXCESSIVE_CAPS);
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
        Dashboard dashboard = Launcher.getDashboard();
        if (dashboard.getPlayer(msg) != null) return false;
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
                if (dashboard.getPlayer(word.toString().trim()) != null) {
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
        if (dashboard.getPlayer(word.toString().trim()) != null) {
            spam = false;
        }
        if (spam) {
            player.sendMessage(DashboardConstants.SPAM_WARNING);
            return true;
        }
        int numamount = 0;
        spam = false;
        word = new StringBuilder();
        for (char c : msg.toCharArray()) {
            if (c == ' ') {
                if (dashboard.getPlayer(word.toString().trim()) != null) {
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
        if (dashboard.getPlayer(word.toString().trim()) != null) {
            spam = false;
        }
        if (spam) {
            player.sendMessage(DashboardConstants.SPAM_WARNING);
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
        Matcher m = DashboardConstants.LINK_PATTERN.matcher(msg.toLowerCase());
        if (m.find()) {
            if (isWhitelisted(m.toMatchResult().group())) {
                return false;
            }
            advertMessage(player.getUsername(), msg);
            player.sendMessage(DashboardConstants.LINK_WARNING);
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
        Dashboard dashboard = Launcher.getDashboard();
        for (Player player : dashboard.getOnlinePlayers()) {
            if (player.getRank().getRankId() >= Rank.TRAINEE.getRankId() && !player.isDisabled()) {
                try {
                    player.sendMessage(msg);
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void socialSpyMessage(Player from, Player to, String message, String command) {
        Dashboard dashboard = Launcher.getDashboard();
        if (dashboard.getServer(from.getServer()).isPark()) {
            String msg = ChatColor.WHITE + from.getUsername() + ": /" + command + " " + to.getUsername() + " " + message;
            for (Player tp : dashboard.getOnlinePlayers()) {
                if (tp.getRank().getRankId() < Rank.TRAINEE.getRankId() || tp.getServer() == null ||
                        tp.getUniqueId().equals(from.getUniqueId()) || tp.getUniqueId().equals(to.getUniqueId()) || tp.isDisabled()) {
                    continue;
                }
                if (dashboard.getServer(tp.getServer()).isPark()) {
                    tp.sendMessage(msg);
                }
            }
        } else {
            String server = from.getServer();
            for (Player tp : dashboard.getOnlinePlayers()) {
                if (tp.getRank().getRankId() < Rank.TRAINEE.getRankId() || tp.getServer() == null ||
                        tp.getUniqueId().equals(from.getUniqueId()) || tp.getUniqueId().equals(to.getUniqueId()) || tp.isDisabled()) {
                    continue;
                }
                if (tp.getServer().equals(server)) {
                    tp.sendMessage(from.getUsername() + ": /" + command + " " + to.getUsername() + " " + message);
                }
            }
        }
    }

    public void socialSpyParty(Player player, Party party, String message, String command) {
        Dashboard dashboard = Launcher.getDashboard();
        if (dashboard.getServer(player.getServer()).isPark()) {
            String msg = "" + ChatColor.BOLD + ChatColor.YELLOW + "[P] " + ChatColor.LIGHT_PURPLE + player.getUsername() + ": /" + command + " " + party.getLeader().getUsername() +
                    " " + message;
            for (Player tp : dashboard.getOnlinePlayers()) {
                if (tp.getRank().getRankId() < Rank.TRAINEE.getRankId() || tp.getServer() == null ||
                        party.getMembers().contains(tp.getUniqueId()) || tp.isDisabled()) {
                    continue;
                }
                if (dashboard.getServer(tp.getServer()).isPark()) {
                    tp.sendMessage(msg);
                }
            }
        } else {
            String server = player.getServer();
            for (Player tp : dashboard.getOnlinePlayers()) {
                if (tp.getRank().getRankId() < Rank.TRAINEE.getRankId() || tp.getServer() == null ||
                        party.getMembers().contains(tp.getUniqueId()) || tp.isDisabled()) {
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
        if (server.equals("Creative")) {
            PacketMuteChat packet = new PacketMuteChat(server, true, "");
            sendToServer(server, packet);
        }
    }

    public void unmuteChat(String server) {
        mutedChats.remove(server);
        if (server.equals("Creative")) {
            PacketMuteChat packet = new PacketMuteChat(server, false, "");
            sendToServer(server, packet);
        }
    }

    public void sendToServer(String server, BasePacket packet) {
        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel dash = (DashboardSocketChannel) o;
            if (!dash.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) continue;
            if (dash.getServerName().equals(server)) {
                dash.send(packet);
            }
        }
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

    public List<String> getMutedChats() {
        return new ArrayList<>(mutedChats);
    }

    public void logout(UUID uuid) {
        messageCache.remove(uuid);
        time.remove(uuid);
    }
}