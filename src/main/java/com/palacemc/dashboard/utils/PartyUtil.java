package com.palacemc.dashboard.utils;

import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.Party;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.packets.dashboard.PacketPartyRequest;

import java.util.*;

/**
 * Created by Marc on 9/12/16
 */
public class PartyUtil {
    public List<Party> partyList = new ArrayList<>();
    public HashMap<UUID, Party> timerList = new HashMap<>();

    public Party findPartyForPlayer(Player player) {
        return findPartyForPlayer(player.getUuid());
    }

    public Party findPartyForPlayer(UUID uuid) {
        for (Party p : partyList) {
            if (p == null) {
                try {
                    partyList.remove(p);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                continue;
            }

            if (p.getLeader() == null) {
                p.close();
            }

            if (p.getMembers().contains(uuid) || p.getLeader().getUuid().equals(uuid)) {
                return p;
            }
        }
        return null;
    }

    public void invitePlayer(final Party party, final Player tp) {
        if (timerList.containsKey(tp.getUuid())) {
            party.getLeader().sendMessage(ChatColor.GREEN + "This player already has a party request pending!");
            return;
        }

        Party p = findPartyForPlayer(tp.getUuid());

        if (p != null) {
            if (p.getMembers().size() > 1 || hasTimer(p)) {
                party.getLeader().sendMessage(ChatColor.RED + "This player is already in a Party!");
                return;

            }
            partyList.remove(p);
        }

        if (party.getMembers().contains(tp.getUuid())) {
            party.getLeader().sendMessage(ChatColor.RED + "This player is already in your party!");
            return;
        }

        timerList.put(tp.getUuid(), party);
        PacketPartyRequest packet = new PacketPartyRequest(tp.getUuid(), party.getLeader().getUsername());
        tp.send(packet);

        party.messageToAllMembers(ChatColor.YELLOW + party.getLeader().getUsername() + " has asked " + tp.getUsername() +
                " to join their party, they have 5 minutes to accept!", true);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (timerList.containsKey(tp.getUuid()) && timerList.get(tp.getUuid()).getUuid()
                        .equals(party.getUuid())) {
                    timerList.remove(tp.getUuid());

                    tp.sendMessage(ChatColor.YELLOW + party.getLeader().getUsername() + "'s party request has expired!");

                    party.messageToAllMembers(ChatColor.YELLOW + party.getLeader().getUsername() + "'s request to " +
                            tp.getUsername() + " has expired!", true);
                }
            }
        }, 300000);
    }

    private boolean hasTimer(Party p) {
        for (Map.Entry<UUID, Party> entry : timerList.entrySet()) {
            if (entry.getValue().getUuid().equals(p.getUuid())) {
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
        Party party = new Party(player.getUuid(), new ArrayList<>());
        partyList.add(party);

        return party;
    }

    public void removeParty(Party party) {
        partyList.remove(party);
    }

    public void acceptRequest(Player player) {
        if (!timerList.containsKey(player.getUuid())) {
            player.sendMessage(ChatColor.RED + "You have no pending Party Requests!");
            return;
        }

        Party party = timerList.remove(player.getUuid());
        party.addMember(player);
        party.messageToAllMembers(ChatColor.YELLOW + player.getUsername() + " has accepted the Party Request!", true);
    }

    public void denyRequest(Player player) {
        if (!timerList.containsKey(player.getUuid())) {
            player.sendMessage(ChatColor.RED + "You have no pending Party Requests!");
            return;
        }
        timerList.remove(player.getUuid());
        player.sendMessage(ChatColor.RED + "You have denied the Party Request!");
    }

    public List<Party> getParties() {
        return new ArrayList<>(partyList);
    }
}