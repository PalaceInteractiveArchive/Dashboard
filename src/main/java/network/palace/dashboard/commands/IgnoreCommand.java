package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.IgnoreData;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.text.DateFormatSymbols;
import java.util.*;

public class IgnoreCommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 1) {
            helpMenu(player);
            return;
        }
        Dashboard dashboard = Launcher.getDashboard();
        switch (args[0].toLowerCase()) {
            case "list": {
                List<IgnoreData> list = player.getIgnoreData();
                if (list.isEmpty()) {
                    player.sendMessage(ChatColor.GREEN + "No ignored players!");
                    return;
                }
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
                list.sort((o1, o2) -> {
                    String name1 = dashboard.getCachedName(o1.getIgnored());
                    String name2 = dashboard.getCachedName(o2.getIgnored());
                    if (name1 == null) name1 = dashboard.getMongoHandler().uuidToUsername(o1.getIgnored());
                    if (name2 == null) name2 = dashboard.getMongoHandler().uuidToUsername(o2.getIgnored());
                    return name1.toLowerCase().compareTo(name2.toLowerCase());
                });
                int listSize = list.size();
                int maxPage = (int) Math.ceil((double) listSize / 8);
                if (page > maxPage) page = maxPage;
                int startAmount = 8 * (page - 1);
                int endAmount;
                if (maxPage > 1) {
                    if (page < maxPage) {
                        endAmount = (8 * page);
                    } else {
                        endAmount = listSize;
                    }
                } else {
                    endAmount = listSize;
                }
                list = list.subList(startAmount, endAmount);
                StringBuilder msg = new StringBuilder(ChatColor.YELLOW + "Ignored Players (Page " + page + " of " + maxPage + "):\n");
                for (IgnoreData data : list) {
                    msg.append("- ").append(dashboard.getCachedName(data.getIgnored())).append(ChatColor.AQUA)
                            .append(" since ").append(format(data.getStarted())).append(ChatColor.YELLOW).append("\n");
                }
                player.sendMessage(msg.toString());
                break;
            }
            case "add": {
                if (args.length < 2) {
                    helpMenu(player);
                    return;
                }
                if (args[1].equalsIgnoreCase(player.getUsername())) {
                    player.sendMessage(ChatColor.RED + "You can't ignore yourself!");
                    return;
                }
                String name;
                UUID uuid = dashboard.getMongoHandler().usernameToUUID(args[1]);
                if (uuid == null) {
                    player.sendMessage(ChatColor.RED + "That player can't be found!");
                    return;
                }
                Rank rank = dashboard.getMongoHandler().getRank(uuid);
                if (rank.getRankId() >= Rank.CHARACTER.getRankId()) {
                    player.sendMessage(ChatColor.RED + "You can't ignore that player!");
                    return;
                }
                name = dashboard.getCachedName(uuid) == null ? dashboard.getMongoHandler().uuidToUsername(uuid) : dashboard.getCachedName(uuid);
                dashboard.addToCache(uuid, name);
                player.ignorePlayer(uuid);
                player.sendMessage(ChatColor.GREEN + "You have ignored " + name);
                if (dashboard.getServer(player.getServer()).getServerType().equals("Creative"))
                    player.sendServerIgnoreList();
                break;
            }
            case "remove": {
                if (args.length < 2) {
                    helpMenu(player);
                    return;
                }
                String name;
                UUID uuid = dashboard.getMongoHandler().usernameToUUID(args[1]);
                if (uuid == null) {
                    player.sendMessage(ChatColor.RED + "That player can't be found!");
                    return;
                }
                name = dashboard.getCachedName(uuid) == null ? dashboard.getMongoHandler().uuidToUsername(uuid) : dashboard.getCachedName(uuid);
                dashboard.addToCache(uuid, name);
                dashboard.getSchedulerManager().runAsync(() -> {
                    player.unignorePlayer(uuid);
                    player.sendMessage(ChatColor.GREEN + "You have unignored " + name);
                    if (dashboard.getServer(player.getServer()).getServerType().equals("Creative"))
                        player.sendServerIgnoreList();
                });
                break;
            }
            default: {
                helpMenu(player);
                break;
            }
        }
    }

    private String format(long started) {
        Calendar c = new GregorianCalendar();
        c.setTime(new Date(started));
        c.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        int hour = c.get(Calendar.HOUR_OF_DAY);
        String am = "am";
        if (hour > 12) {
            am = "pm";
            hour -= 12;
        } else if (hour == 0) {
            hour += 12;
        }
        String month = new DateFormatSymbols().getMonths()[c.get(Calendar.MONTH)].substring(0, 3);
        String min = String.valueOf(c.get(Calendar.MINUTE));
        if (min.length() < 2) {
            min = "0" + min;
        }
        return month + " " + c.get(Calendar.DAY_OF_MONTH) + " " +
                c.get(Calendar.YEAR) + " at " + hour + ":" + min + am;
    }

    public void helpMenu(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Use /ignore to hide messages from players\n" +
                ChatColor.GREEN + "Ignore Commands:\n" + ChatColor.YELLOW + "/ignore list [page] " +
                ChatColor.AQUA + "- List ignored players\n" + ChatColor.YELLOW + "/ignore add [player] " +
                ChatColor.AQUA + "- Ignore a player\n" + ChatColor.YELLOW + "/ignore remove [player] " +
                ChatColor.AQUA + "- Unignore a player\n" + ChatColor.YELLOW + "/ignore help " +
                ChatColor.AQUA + "- Show this help menu");
    }
}
