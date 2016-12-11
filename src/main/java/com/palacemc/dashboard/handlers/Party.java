package com.palacemc.dashboard.handlers;

import com.palacemc.dashboard.Launcher;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 9/12/16
 */
public class Party {
    @Getter private UUID leader;
    @Getter  private List<UUID> members;
    @Getter private UUID uuid = UUID.randomUUID();

    public String headerMessage = ChatColor.GOLD + "-----------------------------------------------------";
    public String footerMessage = ChatColor.GOLD + "-----------------------------------------------------";
    public String warpMessage = ChatColor.GREEN + "Your Party Leader has warped you to their server.";

    public Party(UUID leader, List<UUID> members) {
        this.leader = leader;
        this.members = members;

        if (!members.contains(leader)) {
            members.add(leader);
        }
    }

    public List<UUID> getMembers() {
        return new ArrayList<>(members);
    }

    public void addMember(Player player) {
        if (members.contains(player.getUuid())) {
            return;
        }

        members.add(player.getUuid());
    }

    public void removeMember(Player player) {
        if (!members.contains(player.getUuid())) {
            return;
        }

        members.remove(player.getUuid());
    }

    public void close() {
        String name = null;
        Player lead = Launcher.getDashboard().getPlayer(leader);

        if (lead != null) {
            name = lead.getUsername();
        }

        messageToAllMembers(ChatColor.RED + (name == null ? "The Party has been closed!" : name +
                " has closed the Party!"), true);
        for (UUID uuid : members) {
            Player tp = Launcher.getDashboard().getPlayer(uuid);
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
        Launcher.getDashboard().getPartyUtil().removeParty(this);
    }

    public void warpToLeader() {
        if (members.size() > 25) {
            Launcher.getDashboard().getPlayer(leader).sendMessage(ChatColor.RED + "Parties larger than 25 players cannot be warped!");
            return;
        }

        String server = Launcher.getDashboard().getPlayer(leader).getServer();

        for (UUID memberUUID : members) {
            Player member = Launcher.getDashboard().getPlayer(memberUUID);
            if (member == null) {
                return;
            }

            if (member.getUuid().equals(leader)) {
                continue;
            }

            Launcher.getDashboard().getServerUtil().sendPlayer(member, server);
        }
        messageToAllMembers(warpMessage, true);
    }

    public boolean isLeader(Player player) {
        return leader.equals(player.getUuid());
    }

    public String getHeaderMessage() {
        return headerMessage;
    }

    public String getFooterMessage() {
        return footerMessage;
    }

    public void messageToAllMembers(String message, boolean bars) {
        for (UUID tuuid : getMembers()) {
            Player tp = Launcher.getDashboard().getPlayer(tuuid);

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
                sb.append(l ? "*" : "").append(Launcher.getDashboard().getPlayer(members.get(i)).getUsername());
                continue;
            }
            sb.append(l ? "*" : "").append(Launcher.getDashboard().getPlayer(members.get(i)).getUsername()).append(", ");
        }

        String msg = ChatColor.YELLOW + "Members of your Party: " + sb.toString();
        player.sendMessage(headerMessage);
        player.sendMessage(msg);
        player.sendMessage(footerMessage);
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
        if (!getMembers().contains(tp.getUuid())) {
            Launcher.getDashboard().getPlayer(leader).sendMessage(ChatColor.YELLOW + "That player is not in your Party!");
            return;
        }

        removeMember(tp);
        messageToAllMembers(ChatColor.YELLOW + Launcher.getDashboard().getPlayer(leader).getUsername() + " has removed " +
                tp.getUsername() + " from the Party!", true);

        if (tp.getChannel().equals("party")) {
            tp.sendMessage(ChatColor.GREEN + "You have been moved to the " + ChatColor.AQUA + "all " +
                    ChatColor.GREEN + "channel");
            tp.setChannel("all");
        }
    }

    public void chat(Player player, String msg) {
        Rank r = player.getRank();
        String m = ChatColor.BLUE + "[Party] " + (leader.equals(player.getUuid()) ? ChatColor.YELLOW + "* " : "") +
                r.getNameWithBrackets() + ChatColor.GRAY + " " + player.getUsername() + ": " + ChatColor.WHITE +
                (r.getRankId() >= Rank.SQUIRE.getRankId() ? ChatColor.translateAlternateColorCodes('&', msg) : msg);

        for (UUID uuid : getMembers()) {
            if (uuid.equals(player.getUuid())) {
                continue;
            }

            Player tp = Launcher.getDashboard().getPlayer(uuid);
            if (tp != null && tp.isMentions()) {
                tp.mention();
            }
        }

        messageToAllMembers(m, false);
        Launcher.getDashboard().getChatUtil().socialSpyParty(player, this, msg, "pchat");
    }

    public void promote(Player player, Player tp) {
        messageToAllMembers(ChatColor.YELLOW + player.getUsername() + " promoted " + tp.getUsername() +
                " to Party Leader!", true);
        leader = tp.getUuid();
    }

    public void takeover(Player player) {
        if (!members.contains(player.getUuid())) {
            members.add(player.getUuid());
        }

        leader = player.getUuid();
        messageToAllMembers(ChatColor.YELLOW + player.getUsername() + " has taken over the Party!", true);
    }

    public Player getLeader() {
        return Launcher.getDashboard().getPlayer(uuid);
    }
}