package network.palace.dashboard.utils;

import com.google.gson.JsonParser;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.chat.ClickEvent;
import network.palace.dashboard.chat.ComponentBuilder;
import network.palace.dashboard.chat.HoverEvent;
import network.palace.dashboard.handlers.Party;
import network.palace.dashboard.handlers.Player;

import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Created by Marc on 9/12/16
 */
public class PartyUtil {
    public List<Party> partyList = new ArrayList<>();
    public HashMap<UUID, Party> timerList = new HashMap<>();

    public PartyUtil() {
        Dashboard dashboard = Launcher.getDashboard();
        File f = new File("parties.txt");
        if (!f.exists()) {
            return;
        }
        try {
            Scanner scanner = new Scanner(new FileReader(f));
            while (scanner.hasNextLine()) {
                String json = scanner.nextLine();
                partyList.add(new Party(new JsonParser().parse(json).getAsJsonObject()));
            }
        } catch (Exception e) {
            dashboard.getLogger().error("An exception occurred while parsing parties.txt - " + e.getMessage());
            e.printStackTrace();
        }
        f.delete();
    }

    public Party findPartyForPlayer(Player player) {
        return findPartyForPlayer(player.getUniqueId());
    }

    public Party findPartyForPlayer(UUID uuid) {
        for (Party p : new ArrayList<>(partyList)) {
            if (p == null) continue;
            if (p.getLeader() == null) {
                p.close();
                continue;
            }
            if (p.getMembers().contains(uuid) || p.getLeader().getUuid().equals(uuid)) {
                return p;
            }
        }
        return null;
    }

    public void invitePlayer(final Party party, final Player tp) {
        if (timerList.containsKey(tp.getUniqueId())) {
            party.getLeader().sendMessage(ChatColor.GREEN + "This player already has a party request pending!");
            return;
        }
        Party currentParty = findPartyForPlayer(tp.getUniqueId());
        if (currentParty != null) {
            if (currentParty.getMembers().size() > 1 || hasTimer(currentParty)) {
                party.getLeader().sendMessage(ChatColor.RED + "This player is already in a Party!");
                return;
            }
            partyList.remove(currentParty);
        }
        if (party.getMembers().contains(tp.getUniqueId())) {
            party.getLeader().sendMessage(ChatColor.RED + "This player is already in your party!");
            return;
        }
        timerList.put(tp.getUniqueId(), party);

        tp.sendMessage(new ComponentBuilder(party.getLeader().getUsername()).color(ChatColor.YELLOW)
                .append(" has invited you to their Party! ").color(ChatColor.GREEN).append("Click here to join the Party.")
                .color(ChatColor.GOLD).bold(true).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to join this Party!").color(ChatColor.AQUA).create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept"))
                .append(" This invite will expire in 5 minutes.", ComponentBuilder.FormatRetention.NONE)
                .color(ChatColor.GREEN).create());

        party.messageToAllMembers(ChatColor.YELLOW + party.getLeader().getUsername() + " has asked " + tp.getUsername() +
                " to join their party, they have 5 minutes to accept!", true);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (timerList.containsKey(tp.getUniqueId()) && timerList.get(tp.getUniqueId()).getUniqueId()
                        .equals(party.getUniqueId())) {
                    timerList.remove(tp.getUniqueId());
                    tp.sendMessage(ChatColor.YELLOW + party.getLeader().getUsername() + "'s party request has expired!");
                    party.messageToAllMembers(ChatColor.YELLOW + party.getLeader().getUsername() + "'s request to " +
                            tp.getUsername() + " has expired!", true);
                }
            }
        }, 300000);
    }

    private boolean hasTimer(Party p) {
        for (Map.Entry<UUID, Party> entry : timerList.entrySet()) {
            if (entry.getValue().getUniqueId().equals(p.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public void logout(Player player) {
        Party party = findPartyForPlayer(player);
        if (party == null) {
            return;
        }
        if (party.isLeader(player)) {
            party.close();
            partyList.remove(party);
            return;
        }
        party.leave(player);
    }

    public Party createParty(Player player) {
        Party party = new Party(player.getUniqueId(), new ArrayList<>());
        partyList.add(party);
        return party;
    }

    public void removeParty(Party party) {
        partyList.remove(party);
    }

    public void acceptRequest(Player player) {
        if (!timerList.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You have no pending Party Requests!");
            return;
        }
        Party party = timerList.remove(player.getUniqueId());
        party.addMember(player);
        party.messageToAllMembers(ChatColor.YELLOW + player.getUsername() + " has accepted the Party Request!", true);
    }

    public void denyRequest(Player player) {
        if (!timerList.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You have no pending Party Requests!");
            return;
        }
        timerList.remove(player.getUniqueId());
        player.sendMessage(ChatColor.RED + "You have denied the Party Request!");
    }

    public List<Party> getParties() {
        return new ArrayList<>(partyList);
    }
}