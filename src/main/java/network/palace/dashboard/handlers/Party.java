package network.palace.dashboard.handlers;

import network.palace.dashboard.Dashboard;

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

    public Party(UUID leader, List<UUID> members) {
        this.leader = leader;
        this.members = members;
        if (!members.contains(leader)) {
            members.add(leader);
        }
    }

    public Player getLeader() {
        if (leader == null) {
            return null;
        }
        return Dashboard.getPlayer(leader);
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
        String name = null;
        Player lead = Dashboard.getPlayer(leader);
        if (lead != null) {
            name = lead.getName();
        }
        messageToAllMembers(ChatColor.RED + (name == null ? "The Party has been closed!" : name +
                " has closed the Party!"), true);
        for (UUID uuid : members) {
            Player tp = Dashboard.getPlayer(uuid);
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
        Dashboard.partyUtil.removeParty(this);
    }

    public void warpToLeader() {
        if (members.size() > 25) {
            Dashboard.getPlayer(leader).sendMessage(ChatColor.RED + "Parties larger than 25 players cannot be warped!");
            return;
        }
        String server = Dashboard.getPlayer(leader).getServer();
        for (UUID tuuid : members) {
            Player tp = Dashboard.getPlayer(tuuid);
            if (tp == null) {
                continue;
            }
            if (tp.getUniqueId().equals(leader)) {
                continue;
            }
            Dashboard.serverUtil.sendPlayer(tp, server);
        }
        messageToAllMembers(warpMessage, true);
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
        for (UUID tuuid : getMembers()) {
            Player tp = Dashboard.getPlayer(tuuid);
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

    public void listMembersToMember(Player player) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < members.size(); i++) {
            boolean l = members.get(i).equals(leader);
            if (i == (members.size() - 1)) {
                sb.append(l ? "*" : "").append(Dashboard.getPlayer(members.get(i)).getName());
                continue;
            }
            sb.append(l ? "*" : "").append(Dashboard.getPlayer(members.get(i)).getName()).append(", ");
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
        messageToAllMembers(ChatColor.RED + player.getName() + ChatColor.YELLOW + " has left the Party!", true);
        player.sendMessage(ChatColor.RED + "You have left the party!");
        if (player.getChannel().equals("party")) {
            player.sendMessage(ChatColor.GREEN + "You have been moved to the " + ChatColor.AQUA + "all " +
                    ChatColor.GREEN + "channel");
            player.setChannel("all");
        }
    }

    public void remove(Player tp) {
        if (!getMembers().contains(tp.getUniqueId())) {
            Dashboard.getPlayer(leader).sendMessage(ChatColor.YELLOW + "That player is not in your Party!");
            return;
        }
        removeMember(tp);
        messageToAllMembers(ChatColor.YELLOW + Dashboard.getPlayer(leader).getName() + " has removed " +
                tp.getName() + " from the Party!", true);
        if (tp.getChannel().equals("party")) {
            tp.sendMessage(ChatColor.GREEN + "You have been moved to the " + ChatColor.AQUA + "all " +
                    ChatColor.GREEN + "channel");
            tp.setChannel("all");
        }
    }

    public void chat(Player player, String msg) {
        Rank r = player.getRank();
        String m = ChatColor.BLUE + "[Party] " + (leader.equals(player.getUniqueId()) ? ChatColor.YELLOW + "* " : "") +
                r.getNameWithBrackets() + ChatColor.GRAY + " " + player.getName() + ": " + ChatColor.WHITE +
                (r.getRankId() >= Rank.SQUIRE.getRankId() ? ChatColor.translateAlternateColorCodes('&', msg) : msg);
        for (UUID uuid : getMembers()) {
            if (uuid.equals(player.getUniqueId())) {
                continue;
            }
            Player tp = Dashboard.getPlayer(uuid);
            if (tp != null && tp.hasMentions()) {
                tp.mention();
            }
        }
        messageToAllMembers(m, false);
        Dashboard.chatUtil.socialSpyParty(player, this, msg, "pchat");
    }

    public void promote(Player player, Player tp) {
        messageToAllMembers(ChatColor.YELLOW + player.getName() + " promoted " + tp.getName() +
                " to Party Leader!", true);
        leader = tp.getUniqueId();
    }

    public void takeover(Player player) {
        if (!members.contains(player.getUniqueId())) {
            members.add(player.getUniqueId());
        }
        leader = player.getUniqueId();
        messageToAllMembers(ChatColor.YELLOW + player.getName() + " has taken over the Party!", true);
    }
}