package network.palace.dashboard.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 9/12/16
 */
public class Party {
    private UUID leader;
    private List<UUID> members;
    public String headerMessage = ChatColor.GOLD + "-----------------------------------------------------";
    public String footerMessage = ChatColor.GOLD + "-----------------------------------------------------";
    public String warpMessage = ChatColor.GREEN + "Your Party Leader has warped you to their server.";
    private UUID uuid = UUID.randomUUID();

    public Party(JsonObject obj) {
        UUID leader = UUID.fromString(obj.get("leader").getAsString());
        List<UUID> members = new ArrayList<>();
        JsonArray arr = obj.get("members").getAsJsonArray();
        for (JsonElement e : arr) {
            members.add(UUID.fromString(e.getAsString()));
        }
        this.leader = leader;
        this.members = members;
        if (!members.contains(leader)) {
            members.add(leader);
        }
    }

    public Party(UUID leader, List<UUID> members) {
        this.leader = leader;
        this.members = members;
        if (!members.contains(leader)) {
            members.add(leader);
        }
    }

    public Player getLeader() {
        Dashboard dashboard = Launcher.getDashboard();
        if (leader == null) {
            return null;
        }
        return dashboard.getPlayer(leader);
    }

    public List<UUID> getMembers() {
        return new ArrayList<>(members);
    }

    public void addMember(Player player) {
        if (members.contains(player.getUniqueId())) {
            return;
        }
        members.add(player.getUniqueId());
    }

    public void removeMember(Player player) {
        if (!members.contains(player.getUniqueId())) {
            return;
        }
        members.remove(player.getUniqueId());
    }

    public void close() {
        Dashboard dashboard = Launcher.getDashboard();
        String name = null;
        Player lead = dashboard.getPlayer(leader);
        if (lead != null) {
            name = lead.getUsername();
        }
        messageToAllMembers(ChatColor.RED + (name == null ? "The Party has been closed!" : name +
                " has closed the Party!"), true);
        for (UUID uuid : members) {
            Player tp = dashboard.getPlayer(uuid);
            if (tp == null) {
                continue;
            }
            if (tp.getChannel().equals("party")) {
                tp.sendMessage(ChatColor.GREEN + "You have been moved to the " + ChatColor.AQUA + "all " +
                        ChatColor.GREEN + "channel");
                tp.setChannel("all");
            }
        }
        members.clear();
        dashboard.getPartyUtil().removeParty(this);
    }

    public void warpToLeader() {
        Dashboard dashboard = Launcher.getDashboard();
        if (members.size() > 25) {
            dashboard.getPlayer(leader).sendMessage(ChatColor.RED + "Parties larger than 25 players cannot be warped!");
            return;
        }

        String server = dashboard.getPlayer(leader).getServer();
        for (UUID uuid : members) {
            if (uuid != leader) {
                Player player = dashboard.getPlayer(uuid);
                if (player == null) {
                    continue;
                }
                if (player.getUniqueId().equals(leader)) {
                    continue;
                }
                dashboard.getServerUtil().sendPlayer(player, server);
                messageMember(uuid, warpMessage, true);
            } else {
                messageMember(uuid, ChatColor.GOLD + "Warping your party to you...", false);
            }
        }
    }

    public boolean isLeader(Player player) {
        return leader.equals(player.getUniqueId());
    }

    public String getHeaderMessage() {
        return headerMessage;
    }

    public String getFooterMessage() {
        return footerMessage;
    }

    public void messageToAllMembers(String message, boolean bars) {
        Dashboard dashboard = Launcher.getDashboard();
        for (UUID tuuid : getMembers()) {
            Player tp = dashboard.getPlayer(tuuid);
            if (tp == null) {
                continue;
            }
            if (bars) {
                tp.sendMessage(headerMessage);
            }
            tp.sendMessage(message);
            if (bars) {
                tp.sendMessage(footerMessage);
            }
        }
    }

    public void messageMember(UUID uuid, String message, boolean bars) {
        Dashboard dashboard = Launcher.getDashboard();
        Player player = dashboard.getPlayer(uuid);
        if (player == null) {
            return;
        }
        if (bars) {
            player.sendMessage(headerMessage);
        }
        player.sendMessage(message);
        if (bars) {
            player.sendMessage(footerMessage);
        }
    }

    public void listMembersToMember(Player player) {
        Dashboard dashboard = Launcher.getDashboard();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < members.size(); i++) {
            boolean l = members.get(i).equals(leader);
            if (i == (members.size() - 1)) {
                sb.append(l ? "*" : "").append(dashboard.getPlayer(members.get(i)).getUsername());
                continue;
            }
            sb.append(l ? "*" : "").append(dashboard.getPlayer(members.get(i)).getUsername()).append(", ");
        }
        String msg = ChatColor.YELLOW + "Members of your Party: " + sb.toString();
        player.sendMessage(headerMessage);
        player.sendMessage(msg);
        player.sendMessage(footerMessage);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public void leave(Player player) {
        removeMember(player);
        messageToAllMembers(ChatColor.RED + player.getUsername() + ChatColor.YELLOW + " has left the Party!", true);
        player.sendMessage(ChatColor.RED + "You have left the party!");
        if (player.getChannel().equals("party")) {
            player.sendMessage(ChatColor.GREEN + "You have been moved to the " + ChatColor.AQUA + "all " +
                    ChatColor.GREEN + "channel");
            player.setChannel("all");
        }
    }

    public void remove(Player tp) {
        Dashboard dashboard = Launcher.getDashboard();
        if (!getMembers().contains(tp.getUniqueId())) {
            dashboard.getPlayer(leader).sendMessage(ChatColor.YELLOW + "That player is not in your Party!");
            return;
        }
        removeMember(tp);
        messageToAllMembers(ChatColor.YELLOW + dashboard.getPlayer(leader).getUsername() + " has removed " +
                tp.getUsername() + " from the Party!", true);
        if (tp.getChannel().equals("party")) {
            tp.sendMessage(ChatColor.GREEN + "You have been moved to the " + ChatColor.AQUA + "all " +
                    ChatColor.GREEN + "channel");
            tp.setChannel("all");
        }
    }

    public void chat(Player player, String msg) {
        Dashboard dashboard = Launcher.getDashboard();
        Rank r = player.getRank();
        String m = ChatColor.BLUE + "[Party] " + (leader.equals(player.getUniqueId()) ? ChatColor.YELLOW + "* " : "") +
                r.getFormattedName() + ChatColor.GRAY + " " + player.getUsername() + ": " + ChatColor.WHITE +
                (r.getRankId() >= Rank.SQUIRE.getRankId() ? ChatColor.translateAlternateColorCodes('&', msg) : msg);
        for (UUID uuid : getMembers()) {
            if (uuid.equals(player.getUniqueId())) {
                continue;
            }
            Player tp = dashboard.getPlayer(uuid);
            if (tp != null && tp.hasMentions()) {
                tp.mention();
            }
        }
        messageToAllMembers(m, false);
        dashboard.getChatUtil().socialSpyParty(player, this, msg, "pchat");
    }

    public void promote(Player player, Player tp) {
        messageToAllMembers(ChatColor.YELLOW + player.getUsername() + " promoted " + tp.getUsername() +
                " to Party Leader!", true);
        leader = tp.getUniqueId();
    }

    public void takeover(Player player) {
        if (!members.contains(player.getUniqueId())) {
            members.add(player.getUniqueId());
        }
        leader = player.getUniqueId();
        messageToAllMembers(ChatColor.YELLOW + player.getUsername() + " has taken over the Party!", true);
    }

    @Override
    public String toString() {
        JsonObject o = new JsonObject();
        JsonArray arr = new JsonArray();
        for (UUID uuid : getMembers()) {
            arr.add(uuid.toString());
        }
        o.addProperty("leader", leader.toString());
        o.add("members", arr);
        return o.toString();
    }
}