package network.palace.dashboard.handlers.arcade;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.palace.dashboard.packets.arcade.GameState;

/**
 * @author Innectic
 * @since 5/17/2017
 *
 * An arcade server instance that can be connected to
 */
@AllArgsConstructor
public class ArcadeServer {
    @Getter private String serverName;
    @Getter private boolean canJoinMidGame;
    @Getter private int currentPlayers;
    @Getter private int maxPlayers;
    @Getter private GameState state;
}
