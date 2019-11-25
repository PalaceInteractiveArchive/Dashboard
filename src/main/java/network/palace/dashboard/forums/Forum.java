package network.palace.dashboard.forums;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.mongo.MongoHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Marc on 12/12/16.
 */
public class Forum {
    private BoneCP connectionPool = null;
    private Random random;

    public Forum() throws IOException, SQLException {
        initialize();
    }

    public void initialize() throws IOException, SQLException {
        String address = "";
        String database = "";
        String username = "";
        String password = "";
        try (BufferedReader br = new BufferedReader(new FileReader("websql.txt"))) {
            String line = br.readLine();
            while (line != null) {
                if (line.startsWith("address:")) {
                    address = line.split("address:")[1];
                }
                if (line.startsWith("username:")) {
                    username = line.split("username:")[1];
                }
                if (line.startsWith("password:")) {
                    password = line.split("password:")[1];
                }
                if (line.startsWith("database:")) {
                    database = line.split("database:")[1];
                }
                line = br.readLine();
            }
        }
        BoneCPConfig config = new BoneCPConfig();
        config.setJdbcUrl("jdbc:mysql://" + address + ":3306/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setMinConnectionsPerPartition(3);
        config.setMaxConnectionsPerPartition(30);
        config.setPartitionCount(1);
        config.setIdleConnectionTestPeriod(300, TimeUnit.SECONDS);
        connectionPool = new BoneCP(config);
    }

    public boolean isConnected() {
        return connectionPool != null && !connectionPool.getDbIsDown().get();
    }

    public void stop() {
        connectionPool.shutdown();
    }

    public void linkAccount(Player player, String email) {
        if (!isConnected()) {
            player.sendMessage(ChatColor.RED + "Could not connect to database, please try again later!");
            return;
        }
        try (Connection connection = connectionPool.getConnection()) {
            //Check Minecraft account isn't already linked to a forum account.
            PreparedStatement checkMcNotLinkedSql = connection.prepareStatement("SELECT * FROM core_pfields_content WHERE field_3=?");
            checkMcNotLinkedSql.setString(1, player.getUniqueId().toString());
            ResultSet checkMcNotLinkedResult = checkMcNotLinkedSql.executeQuery();
            if (checkMcNotLinkedResult.next()) {
                player.sendMessage(ChatColor.RED + "Your Minecraft account is already linked to a Forum account. To unlink, type /link cancel.");
                checkMcNotLinkedResult.close();
                checkMcNotLinkedSql.close();
                return;
            }

            //Check forum account exists with provided email.
            PreparedStatement memberIdSql = connection.prepareStatement("SELECT member_id FROM core_members WHERE email=?");
            memberIdSql.setString(1, email);
            ResultSet memberIdResult = memberIdSql.executeQuery();
            if (!memberIdResult.next()) {
                player.sendMessage(ChatColor.RED + "There is no forum account with that email address!");
                player.sendMessage(ChatColor.YELLOW + "Register for our forums here: " + ChatColor.GOLD + "https://forums.palace.network/register/");
                memberIdResult.close();
                memberIdSql.close();
                return;
            }
            int member_id = memberIdResult.getInt("member_id");
            memberIdResult.close();
            memberIdSql.close();

            //Check forum account isn't already linked to a Minecraft account.
            PreparedStatement checkNotLinkedSql = connection.prepareStatement("SELECT * FROM core_pfields_content WHERE member_id=?");
            checkNotLinkedSql.setInt(1, member_id);
            ResultSet checkNotLinkedResult = checkNotLinkedSql.executeQuery();
            if (!checkNotLinkedResult.next()) {
                player.sendMessage(ChatColor.RED + "There was an error, please try again later!");
                checkNotLinkedResult.close();
                checkNotLinkedSql.close();
                return;
            }
            String uuidField = checkNotLinkedResult.getString("field_3");
            String usernameField = checkNotLinkedResult.getString("field_4");
            checkNotLinkedResult.close();
            checkNotLinkedSql.close();
            if ((uuidField != null && !uuidField.isEmpty()) || (usernameField != null && !usernameField.isEmpty())) {
                player.sendMessage(ChatColor.RED + "This forum account is already linked to a different Minecraft account!");
                return;
            }

            String linkingCode = getRandomNumberString();
            Launcher.getDashboard().getMongoHandler().setForumLinkingCode(player.getUniqueId(), member_id, linkingCode);
            //Set Linking Code value for forum account.
            PreparedStatement setLinkCodeSql = connection.prepareStatement("UPDATE core_pfields_content SET field_2=? WHERE member_id=?");
            setLinkCodeSql.setString(1, linkingCode);
            setLinkCodeSql.setInt(2, member_id);
            setLinkCodeSql.execute();
            setLinkCodeSql.close();

            player.sendMessage(ChatColor.GREEN + "Okay, you're almost finished linking! Visit your Forum Profile and look for the six-digit Linking Code. When you've found it, run " +
                    ChatColor.YELLOW + "/link confirm [six-digit code].");
            player.sendMessage(ChatColor.GREEN + "If you need help, check out our guide here: https:");
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "There was an error, please try again later!");
            e.printStackTrace();
        }
    }

    public void unlinkAccount(Player player) {
        MongoHandler mongoHandler = Launcher.getDashboard().getMongoHandler();
        try {
            int member_id = mongoHandler.getForumMemberId(player.getUniqueId());
            if (mongoHandler.getForumLinkingCode(player.getUniqueId()) != null || member_id < 0) {
                player.sendMessage(ChatColor.RED + "Your Minecraft and Forums accounts are not currently linked!");
                return;
            }
            player.sendMessage(ChatColor.GREEN + "Unlinking your Minecraft and Forums accounts...");
            try (Connection connection = connectionPool.getConnection()) {
                //Unset uuid/username on forum account
                PreparedStatement unsetPlayerDataSql = connection.prepareStatement("UPDATE core_pfields_content SET field_2=?,field_3=?,field_4=? WHERE member_id=?");
                unsetPlayerDataSql.setString(1, null);
                unsetPlayerDataSql.setString(2, null);
                unsetPlayerDataSql.setString(3, null);
                unsetPlayerDataSql.setInt(4, member_id);
                unsetPlayerDataSql.execute();
                unsetPlayerDataSql.close();

                if (player.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
                    //Set forum group to member
                    PreparedStatement setForumGroupSql = connection.prepareStatement("UPDATE core_members SET member_group_id=? WHERE member_id=?");
                    setForumGroupSql.setInt(1, 3);
                    setForumGroupSql.setInt(2, member_id);
                    setForumGroupSql.execute();
                    setForumGroupSql.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            mongoHandler.unlinkForumAccount(player.getUniqueId());
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "There was an error, please try again later!");
            e.printStackTrace();
        }
        player.sendMessage(ChatColor.GREEN + "Your Minecraft and Forums accounts are no longer linked.");
    }

    private static String getRandomNumberString() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }

    public void confirm(Player player, String code) {
        String correctCode = Launcher.getDashboard().getMongoHandler().getForumLinkingCode(player.getUniqueId());
        if (correctCode == null) {
            player.sendMessage(ChatColor.RED + "Before you can confirm, first you need to start the linking process: /link [email address]");
            return;
        }
        if (!code.equals(correctCode)) {
            player.sendMessage(ChatColor.RED + "That isn't the right code!");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Code accepted! We're finishing the linking process right now...");
        int member_id = Launcher.getDashboard().getMongoHandler().getForumMemberId(player.getUniqueId());
        if (member_id < 0) {
            player.sendMessage(ChatColor.RED + "Uh oh, there was a problem! (Error code 121)");
            return;
        }
        try (Connection connection = connectionPool.getConnection()) {
            //Unset Linking Code value and setting uuid/username on forum account
            PreparedStatement unsetLinkCodeSql = connection.prepareStatement("UPDATE core_pfields_content SET field_2=?,field_3=?,field_4=? WHERE member_id=?");
            unsetLinkCodeSql.setString(1, null);
            unsetLinkCodeSql.setString(2, player.getUniqueId().toString());
            unsetLinkCodeSql.setString(3, player.getUsername());
            unsetLinkCodeSql.setInt(4, member_id);
            unsetLinkCodeSql.execute();
            unsetLinkCodeSql.close();

            //Set forum account group to match player rank (if Developer or above, this isn't automated!)
            if (player.getRank().getRankId() < Rank.DEVELOPER.getRankId()) {
                int forumGroup = getForumGroup(player.getRank());
                PreparedStatement setForumGroupSql = connection.prepareStatement("UPDATE core_members SET member_group_id=? WHERE member_id=?");
                setForumGroupSql.setInt(1, forumGroup);
                setForumGroupSql.setInt(2, member_id);
                setForumGroupSql.execute();
                setForumGroupSql.close();
            } else {
                player.sendMessage(ChatColor.RED + "Note: Since your rank is " + player.getRank().getFormattedName() + ChatColor.RED + ", your forum group will not be automatically set.");
            }

            //Unset Linking Code value on mongo document
            Launcher.getDashboard().getMongoHandler().unsetForumLinkingCode(player.getUniqueId());
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "There was an error, please try again later!");
            e.printStackTrace();
            return;
        }
        player.sendMessage(ChatColor.GREEN + "All done! Your Minecraft and Forums accounts are now linked.");
    }

    public void updatePlayerName(UUID uuid, int member_id, String newUsername) {
        try (Connection connection = connectionPool.getConnection()) {
            // field_4 is username, field_3 is uuid
            PreparedStatement sql = connection.prepareStatement("UPDATE core_pfields_content SET field_4=? WHERE member_id=? AND field_3=?");
            sql.setString(1, newUsername);
            sql.setInt(2, member_id);
            sql.setString(3, uuid.toString());
            sql.execute();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePlayerRank(UUID uuid, int member_id, Rank rank, Player player) {
        if (rank.getRankId() >= Rank.DEVELOPER.getRankId()) {
            if (player != null)
                player.sendMessage(ChatColor.RED + "Note: Since your rank is " + player.getRank().getFormattedName() +
                        ChatColor.RED + ", your forum group will not be automatically set.");
            return;
        }
        try (Connection connection = connectionPool.getConnection()) {
            int forumGroup = getForumGroup(rank);
            PreparedStatement setForumGroupSql = connection.prepareStatement("UPDATE core_members SET member_group_id=? WHERE member_id=?");
            setForumGroupSql.setInt(1, forumGroup);
            setForumGroupSql.setInt(2, member_id);
            setForumGroupSql.execute();
            setForumGroupSql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getForumGroup(Rank rank) {
        switch (rank) {
            case DIRECTOR:
                return 4;
            case MANAGER:
                return 33;
            case ADMIN:
                return 7;
            case DEVELOPER:
                return 8;
            case COORDINATOR:
                return 9;
            case BUILDER:
                return 19;
            case TECHNICIAN:
                return 35;
            case MEDIA:
                return 37;
            case MOD:
                return 6;
            case TRAINEETECH:
                return 34;
            case TRAINEEBUILD:
            case TRAINEE:
                return 10;
            case SPECIALGUEST:
                return 22;
            case SHAREHOLDER:
                return 38;
            case HONORABLE:
                return 17;
            case MAJESTIC:
                return 16;
            case NOBLE:
                return 15;
            case DWELLER:
                return 14;
        }
        return 13;
    }
}
