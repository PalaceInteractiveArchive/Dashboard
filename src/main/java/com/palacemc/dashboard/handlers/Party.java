package com.palacemc.dashboard.handlers;

import com.palacemc.dashboard.Dashboard;
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

    @Getter public String headerMessage = ChatColor.GOLD + "-----------------------------------------------------";
    @Getter public String footerMessage = ChatColor.GOLD + "-----------------------------------------------------";
    public String warpMessage = ChatColor.GREEN + "Your Party Leader has warped you to their server.";

    private Dashboard dashboard = Launcher.getDashboard();

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
        String username = null;
        Player leader = dashboard.getPlayer(this.leader);

        if (leader != null) {
            username = leader.getUsername();
        }

        messageToAllMembers(ChatColor.RED + (username == null ? "The Party has been closed!" : username +
                " has closed the Party!"), true);

        for (UUID uuid : members) {
            Player player = dashboard.getPlayer(uuid);
            if (player == null) continue;

            if (player.getChannel().equals("party")) {
                player.sendMessage(ChatColor.GREEN + "You have been moved to the " + ChatColor.AQUA + "all " +
                        ChatColor.GREEN + "channel");
                player.setChannel("all");
            }
        }

        members.clear();
        dashboard.getPartyUtil().removeParty(this);
    }

    public void warpToLeader() {
        if (members.size() > 25) {
            dashboard.getPlayer(leader).sendMessage(ChatColor.RED + "Parties larger than 25 players cannot be warped!");
            return;
        }

        String server = dashboard.getPlayer(leader).getServer();

        for (UUID memberUUID : members) {
            Player member = dashboard.getPlayer(memberUUID);

            if (member == null) {
                return;
            }

            if (member.getUuid().equals(leader)) {
                continue;
            }

            dashboard.getServerUtil().sendPlayer(member, server);
        }
        messageToAllMembers(warpMessage, true);
    }

    public boolean isLeader(Player player) {
        return leader.equals(player.getUuid());
    }

    public void messageToAllMembers(String message, boolean bars) {
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

    public void listMembersToMember(Player player) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < members.size(); i++) {
            boolean l = members.get(i).equals(leader);

            if (i == (members.size() - 1)) {
                stringBuilder.append(l ? "*" : "").append(dashboard.getPlayer(members.get(i)).getUsername());
                continue;
            }
            stringBuilder.append(l ? "*" : "").append(dashboard.getPlayer(members.get(i)).getUsername()).append(", ");
        }

        String message = ChatColor.YELLOW + "Members of your Party: " + stringBuilder.toString();
        player.sendMessage(headerMessage);
        player.sendMessage(message);
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

    public void remove(Player player) {
        if (!getMembers().contains(player.getUuid())) {
            dashboard.getPlayer(leader).sendMessage(ChatColor.YELLOW + "That player is not in your Party!");
            return;
        }

        removeMember(player);
        messageToAllMembers(ChatColor.YELLOW + dashboard.getPlayer(leader).getUsername() + " has removed " +
                player.getUsername() + " from the Party!", true);

        if (player.getChannel().equals("party")) {
            player.sendMessage(ChatColor.GREEN + "You have been moved to the " + ChatColor.AQUA + "all " +
                    ChatColor.GREEN + "channel");
            player.setChannel("all");
        }
    }

    public void chat(Player player, String msg) {
        Rank rank = player.getRank();

        String message = ChatColor.BLUE + "[Party] " + (
                leader.equals(player.getUuid()) ? ChatColor.YELLOW + "* " : "") +
                rank.getNameWithBrackets() + ChatColor.GRAY + " " + player.getUsername() + ": " + ChatColor.WHITE +
                (rank.getRankId() >= Rank.SQUIRE.getRankId() ? ChatColor.translateAlternateColorCodes('&', msg) : msg);

        for (UUID uuid : getMembers()) {
            if (uuid.equals(player.getUuid())) continue;

            Player member = dashboard.getPlayer(uuid);
            if (member != null && member.isMentions()) {
                member.mention();
            }
        }

        messageToAllMembers(message, false);
        dashboard.getChatUtil().socialSpyParty(player, this, msg, "pchat");
    }

    public void promote(Player leader, Player newLeader) {
        messageToAllMembers(ChatColor.YELLOW + leader.getUsername() + " promoted " + newLeader.getUsername() +
                " to Party Leader!", true);
        this.leader = newLeader.getUuid();
    }

    public void takeover(Player player) {
        if (!members.contains(player.getUuid())) {
            members.add(player.getUuid());
        }

        leader = player.getUuid();
        messageToAllMembers(ChatColor.YELLOW + player.getUsername() + " has taken over the Party!", true);
    }

    public Player getLeader() {
        return dashboard.getPlayer(uuid);
    }
}