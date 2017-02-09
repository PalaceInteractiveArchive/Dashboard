package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.packets.dashboard.PacketAddServer;
import network.palace.dashboard.packets.dashboard.PacketConnectionType;
import network.palace.dashboard.packets.dashboard.PacketRemoveServer;
import network.palace.dashboard.server.DashboardSocketChannel;
import network.palace.dashboard.server.WebSocketServerHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class Commandserver extends MagicCommand {

    public Commandserver() {
        super(Rank.SQUIRE);
    }

    @Override
    public void execute(final Player player, String label, String[] args) {
        if (args.length == 6 && args[0].equalsIgnoreCase("add") && player.getRank().getRankId() >= Rank.WIZARD.getRankId()) {
            final String name = args[1];
            final String address = args[2];
            final int port;
            final boolean park;
            final String type = args[5];
            try {
                port = Integer.parseInt(args[3]);
            } catch (Exception ignored) {
                player.sendMessage(ChatColor.RED + "Please use a number for the port!");
                return;
            }
            try {
                park = Boolean.parseBoolean(args[4]);
            } catch (Exception ignored) {
                player.sendMessage(ChatColor.RED + "Please use true or false to state if it is a Park server or not!");
                return;
            }
            if (Dashboard.serverUtil.getServer(name) != null) {
                player.sendMessage(ChatColor.RED + "A server already exists called '" + name + "'!");
                return;
            }
            Server s = new Server(name, address, port, park, 0, type);
            Dashboard.serverUtil.addServer(s);
            Dashboard.schedulerManager.runAsync(() -> {
                try (Connection connection = Dashboard.sqlUtil.getConnection()) {
                    PreparedStatement sql = connection.prepareStatement("INSERT INTO " + (Dashboard.isTestNetwork() ?
                            "playground" : "") + "servers (name,address,port,park,type) VALUES (?,?,?,?,?)");
                    sql.setString(1, name);
                    sql.setString(2, address);
                    sql.setInt(3, port);
                    sql.setInt(4, park ? 1 : 0);
                    sql.setString(5, type);
                    sql.execute();
                    sql.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                player.sendMessage(ChatColor.GREEN + "'" + name + "' successfully created! Notifying Bungees...");
                PacketAddServer packet = new PacketAddServer(name, address, port);
                for (Object o : WebSocketServerHandler.getGroup()) {
                    DashboardSocketChannel dash = (DashboardSocketChannel) o;
                    if (!dash.getType().equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                        continue;
                    }
                    dash.send(packet);
                }
                player.sendMessage(ChatColor.GREEN + "All Bungees notified! Server '" + name + "' can now be joined.");
            });
            return;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("remove") && player.getRank().getRankId() >= Rank.WIZARD.getRankId()) {
            final String name = args[1];
            final Server s = Dashboard.serverUtil.getServer(name);
            if (s == null) {
                player.sendMessage(ChatColor.RED + "No server exists called '" + name + "'!");
                return;
            }
            player.sendMessage(ChatColor.GREEN + "Emptying server " + s.getName() + "...");
            s.emptyServer();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (s.getCount() <= 0) {
                        player.sendMessage(ChatColor.GREEN + s.getName() + " has been emptied! Removing server...");
                        cancel();
                        Dashboard.serverUtil.removeServer(name);
                        Dashboard.schedulerManager.runAsync(() -> {
                            try (Connection connection = Dashboard.sqlUtil.getConnection()) {
                                //TODO Change this back to the regular table
                                PreparedStatement sql = connection.prepareStatement("DELETE FROM " +
                                        (Dashboard.isTestNetwork() ? "playground" : "") + "servers WHERE name=?");
                                sql.setString(1, s.getName());
                                sql.execute();
                                sql.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            player.sendMessage(ChatColor.GREEN + "'" + name + "' successfully removed! Notifying Bungees...");
                            PacketRemoveServer packet = new PacketRemoveServer(name);
                            for (Object o : WebSocketServerHandler.getGroup()) {
                                DashboardSocketChannel dash = (DashboardSocketChannel) o;
                                if (!dash.getType().equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                                    continue;
                                }
                                dash.send(packet);
                            }
                            player.sendMessage(ChatColor.GREEN + "All Bungees notified! Server '" + name + "' has been removed.");
                        });
                    }
                }
            }, 1000);
            return;
        }
        if (args.length == 1) {
            if (player.getRank().getRankId() >= Rank.WIZARD.getRankId()) {
                if (args[0].equalsIgnoreCase("help")) {
                    player.sendMessage(ChatColor.GREEN + "Server Commands:");
                    player.sendMessage(ChatColor.GREEN + "/server list " + ChatColor.AQUA +
                            "- List all servers and addresses");
                    player.sendMessage(ChatColor.GREEN + "/server add [Name] [IP Address] [Port] [True/False] [Type] " +
                            ChatColor.AQUA + "- Add a new server to all Bungees");
                    player.sendMessage(ChatColor.GREEN + "/server remove [Name] " + ChatColor.AQUA +
                            "- Remove a server from all Bungees");
                    return;
                } else if (args[0].equalsIgnoreCase("list")) {
                    String msg = ChatColor.GREEN + "Server List:\n";
                    List<Server> servers = Dashboard.getServers();
                    servers.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
                    for (int i = 0; i < servers.size(); i++) {
                        Server s = servers.get(i);
                        ChatColor c = s.isOnline() ? ChatColor.GREEN : ChatColor.RED;
                        msg += "- " + c + s.getName() + ChatColor.GREEN + " - " + s.getAddress() + ":" + s.getPort() +
                                " - " + s.getServerType();
                        if (i < (servers.size() - 1)) {
                            msg += "\n";
                        }
                    }
                    player.sendMessage(msg);
                    return;
                }
            }
            Server server = Dashboard.serverUtil.getServer(args[0]);
            if (server == null) {
                player.sendMessage(ChatColor.RED + "That server doesn't exist!");
                return;
            }
            Dashboard.serverUtil.sendPlayer(player, server.getName());
            return;
        }
        if (args.length == 0) {
            player.sendMessage(ChatColor.GREEN + "You are currently on " + player.getServer());
            String msg = "The following servers exist: ";
            List<Server> servers = Dashboard.getServers();
            List<String> names = new ArrayList<>();
            for (Server s : servers) {
                names.add(s.getName());
            }
            Collections.sort(names);
            for (int i = 0; i < names.size(); i++) {
                msg += names.get(i);
                if (i < (names.size() - 1)) {
                    msg += ", ";
                }
            }
            player.sendMessage(ChatColor.GREEN + msg);
        }
    }

    @Override
    public Iterable<String> onTabComplete(Player sender, List<String> args) {
        List<String> list = new ArrayList<>();
        for (Server server : Dashboard.getServers()) {
            list.add(server.getName());
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
}