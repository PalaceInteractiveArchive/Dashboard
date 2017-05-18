package network.palace.dashboard.handlers;

import network.palace.dashboard.handlers.arcade.ArcadeServer;
import network.palace.dashboard.packets.arcade.GameState;

import java.util.*;

/**
 * @author Innectic
 * @since 5/17/2017
 *
 * Handle searches for arcade games, and the storage of currently running games.
 */
public class Arcade {
    /**
     * Currently connected Arcade servers.
     *
     * Key: Game type. Example: oneshot
     * Value: Information about the server
     */
    private HashMap<String, List<ArcadeServer>> arcadeServers = new HashMap<>();

    /**
     * Attempt to find a game to put a player in
     *
     * @param gameType the type of game to find
     * @return the game, if one was found.
     */
    public Optional<ArcadeServer> findGame(String gameType) {
        // Make sure we even have a game with that type
        if (!arcadeServers.containsKey(gameType)) return Optional.empty();
        // If we only have one type of the server, just return that one
        if (arcadeServers.get(gameType).size() == 1) return Optional.of(arcadeServers.get(gameType).get(0));
        // Check each instance to find the one that's the most fit for this player
        for (ArcadeServer server : arcadeServers.get(gameType)) {
            if (server == null) break;
            // Check the state of the game
            if (server.getState().equals(GameState.ENDING)) return Optional.empty();
            if (server.getState().equals(GameState.WAITING)) {
                if (checkPlayers(server)) return Optional.of(server);
                // Not waiting, not space for another player.
                return Optional.empty();
            }
            if (server.getState().equals(GameState.RUNNING)) {
                if (server.isCanJoinMidGame() && checkPlayers(server)) return Optional.of(server);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * Add a game server to the running server list
     *
     * @param gameType the type of the game to add
     * @param server the server's information
     */
    public void addServer(String gameType, ArcadeServer... server) {
        this.arcadeServers.get(gameType).addAll(new ArrayList<>(Arrays.asList(server)));
    }

    /**
     * Remove a server from the running list
     *
     * @param gameType the type of game being removed
     * @param server more information about the game
     * @return if it was removed
     */
    public boolean removeServer(String gameType, ArcadeServer server) {
        return arcadeServers.get(gameType).removeIf(s -> s.equals(server));
    }

    /**
     * Check the player count
     *
     * @param server the server to check
     * @return if there's room for another player
     */
    private boolean checkPlayers(ArcadeServer server) {
        return server.getCurrentPlayers() < server.getMaxPlayers();
    }

}
