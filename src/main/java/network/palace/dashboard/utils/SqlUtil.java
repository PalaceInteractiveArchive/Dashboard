package network.palace.dashboard.utils;

/**
 * Created by Marc on 7/14/16
 */
public class SqlUtil {
//    private BoneCP connectionPool;
//
//    public SqlUtil() throws SQLException, IOException {
//        BoneCPConfig config = new BoneCPConfig();
//        String address = "";
//        String database = "";
//        String username = "";
//        String password = "";
//        try (BufferedReader br = new BufferedReader(new FileReader("sql.txt"))) {
//            String line = br.readLine();
//            while (line != null) {
//                if (line.startsWith("address:")) {
//                    address = line.split("address:")[1];
//                }
//                if (line.startsWith("username:")) {
//                    username = line.split("username:")[1];
//                }
//                if (line.startsWith("password:")) {
//                    password = line.split("password:")[1];
//                }
//                if (line.startsWith("database:")) {
//                    database = line.split("database:")[1];
//                }
//                line = br.readLine();
//            }
//        }
//        config.setJdbcUrl("jdbc:mysql://" + address + ":3306/" + database);
//        config.setUsername(username);
//        config.setPassword(password);
//        config.setMinConnectionsPerPartition(5);
//        config.setMaxConnectionsPerPartition(25);
//        config.setIdleMaxAge(100, TimeUnit.SECONDS);
//        config.setMaxConnectionAge(300, TimeUnit.SECONDS);
//        config.setPartitionCount(2);
//        config.setIdleConnectionTestPeriod(300, TimeUnit.SECONDS);
//        connectionPool = new BoneCP(config);
//    }
//
//    public Optional<Connection> getConnection() {
//        try {
//            return Optional.of(connectionPool.getConnection());
//        } catch (SQLException e) {
//            return Optional.empty();
//        }
//    }
//
//
//    public void stop() {
//        connectionPool.shutdown();
//        Dashboard dashboard = Launcher.getDashboard();
////        dashboard.getActivityUtil().stop();
//    }
}