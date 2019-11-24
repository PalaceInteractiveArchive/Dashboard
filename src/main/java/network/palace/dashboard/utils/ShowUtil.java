package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.Player;
import org.bson.Document;

import java.util.HashMap;
import java.util.UUID;

public class ShowUtil {
    private HashMap<UUID, Long> requests = new HashMap<>();

    public boolean checkPlayer(Player player) {
        if (requests.containsKey(player.getUniqueId())) {
            long lastRequest = requests.get(player.getUniqueId());
            if ((System.currentTimeMillis() / 1000) - lastRequest < 600) {
                player.sendMessage(ChatColor.RED + "You must wait at least 10 minutes before sending another show request!");
                return false;
            }
        }
        Dashboard dashboard = Launcher.getDashboard();
        Document doc = dashboard.getMongoHandler().getPlayer(player.getUniqueId(), new Document("showRequests", 1));
        if (doc != null && doc.containsKey("showRequests")) {
            Document requests = (Document) doc.get("showRequests");
            String lastShow = requests.getString("lastShow");
            long lastRan = requests.getLong("lastRan");
            if ((System.currentTimeMillis() / 1000) - lastRan < 86400) {
                player.sendMessage(ChatColor.RED + "You already ran a show within the last 24 hours, try again soon!");
                return false;
            }
        }
        requests.put(player.getUniqueId(), System.currentTimeMillis() / 1000);
        return true;
    }
}
