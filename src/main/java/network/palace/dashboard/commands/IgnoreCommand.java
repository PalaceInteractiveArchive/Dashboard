package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;

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
                    if (name1 == null) name1 = dashboard.getSqlUtil().usernameFromUUID(o1.getIgnored());
                    if (name2 == null) name2 = dashboard.getSqlUtil().usernameFromUUID(o2.getIgnored());
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
                UUID uuid = dashboard.getSqlUtil().uuidFromUsername(args[1]);
                if (uuid == null) {
                    player.sendMessage(ChatColor.RED + "That player can't be found!");
                    return;
                }
                Rank rank = dashboard.getSqlUtil().getRank(uuid);
                if (rank.getRankId() >= Rank.CHARACTER.getRankId()) {
                    player.sendMessage(ChatColor.RED + "You can't ignore that player!");
                    return;
                }
                name = dashboard.getCachedName(uuid) == null ? dashboard.getSqlUtil().usernameFromUUID(uuid) : dashboard.getCachedName(uuid);
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
                UUID uuid = dashboard.getSqlUtil().uuidFromUsername(args[1]);
                if (uuid == null) {
                    player.sendMessage(ChatColor.RED + "That player can't be found!");
                    return;
                }
                name = dashboard.getCachedName(uuid) == null ? dashboard.getSqlUtil().usernameFromUUID(uuid) : dashboard.getCachedName(uuid);
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
        c.setTime(new Date(started * 1000));
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

    @Override
    public Iterable<String> onTabComplete(Player sender, List<String> args) {
        Dashboard dashboard = Launcher.getDashboard();
        List<String> list = new ArrayList<>();
        for (IgnoreData data : sender.getIgnoreData()) {
            UUID uuid = data.getIgnored();
            String name = dashboard.getCachedName(uuid) == null ? dashboard.getSqlUtil().usernameFromUUID(uuid) : dashboard.getCachedName(uuid);
            list.add(name);
        }
        Collections.sort(list);
        if (args.size() == 0) {
            return list;
        }
        List<String> l2 = new ArrayList<>();
        String arg = args.get(args.size() - 1);
        for (String s : list) {
            if (s.toLowerCase().startsWith(arg.toLowerCase())) {
                l2.add(s);
            }
        }
        Collections.sort(l2);
        return l2;
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
