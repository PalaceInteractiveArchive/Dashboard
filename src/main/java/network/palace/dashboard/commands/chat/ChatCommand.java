package network.palace.dashboard.commands.chat;

import com.mongodb.client.MongoCollection;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.ChatMessage;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 9/25/16
 */
public class ChatCommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        dashboard.getSchedulerManager().runAsync(() -> {
            MongoCollection<Document> chatCollection = dashboard.getMongoHandler().getChatCollection();
            player.sendMessage(ChatColor.GREEN + "Processing messages...");
            for (int i = 1; i < 500; i++) {
                List<ChatMessage> msgs = new ArrayList<>();
                player.sendMessage(ChatColor.YELLOW + "Loading from MongoDB, skipping " + ((i - 1) * 100000) + "...");
                for (Document doc : chatCollection.find().skip((i - 1) * 100000).limit(100000)) {
                    msgs.add(new ChatMessage(UUID.fromString(doc.getString("uuid")), doc.getString("message"), doc.getLong("time") * 1000));
                }
                try {
                    player.sendMessage(ChatColor.AQUA + "Retrieved, sending to SQL...");
                    dashboard.getSqlUtil().logChat(msgs);
                    player.sendMessage(ChatColor.RED + "Sent to SQL!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void test(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        List<String> list = new ArrayList<>();
        list.add("all");
        list.add("party");
        if (player.getRank().getRankId() >= Rank.TRAINEE.getRankId()) {
            list.add("staff");
            if (player.getRank().getRankId() >= Rank.LEAD.getRankId()) {
                list.add("admin");
            }
        }
        if (args.length <= 0) {
            StringBuilder m = new StringBuilder(ChatColor.AQUA + "You are currently in the " + ChatColor.GREEN + player.getChannel() +
                    ChatColor.AQUA + " channel. You can speak in the following channels:");
            for (String s : list) {
                m.append(ChatColor.GREEN).append("\n- ").append(ChatColor.AQUA).append(s);
            }
            m.append("\n\nExample: ").append(ChatColor.GREEN).append("/chat all ").append(ChatColor.AQUA).append("switches you to main chat");
            player.sendMessage(m.toString());
            return;
        }
        String channel = args[0].toLowerCase();
        if (!list.contains(channel)) {
            player.sendMessage(ChatColor.RED + "You can't join that channel, or it doesn't exist!");
            return;
        }
        if (channel.equals("party") && dashboard.getPartyUtil().findPartyForPlayer(player) == null) {
            player.sendMessage(ChatColor.RED + "You aren't in a party! Invite a player with " + ChatColor.GREEN + "/party [Username]");
            return;
        }
        player.setChannel(channel);
        player.sendMessage(ChatColor.GREEN + "You have selected the " + ChatColor.AQUA + channel +
                ChatColor.GREEN + " channel");
    }

    @Override
    public Iterable<String> onTabComplete(Player player, List<String> args) {
        List<String> list = new ArrayList<>();
        if (args.size() == 0) {
            list.add("all");
            list.add("party");
            if (player.getRank().getRankId() >= Rank.TRAINEE.getRankId()) {
                list.add("staff");
                if (player.getRank().getRankId() >= Rank.LEAD.getRankId()) {
                    list.add("admin");
                }
            }
            Collections.sort(list);
            return list;
        } else if (args.size() == 1) {
            list.add("all");
            list.add("party");
            if (player.getRank().getRankId() >= Rank.TRAINEE.getRankId()) {
                list.add("staff");
                if (player.getRank().getRankId() >= Rank.LEAD.getRankId()) {
                    list.add("admin");
                }
            }
            String arg = args.get(0);
            List<String> l2 = new ArrayList<>();
            for (String s : list) {
                if (s.toLowerCase().startsWith(arg.toLowerCase())) {
                    l2.add(s);
                }
            }
            Collections.sort(l2);
            return l2;
        }
        return list;
    }

    @Override
    public boolean doesTabComplete() {
        return true;
    }
}