package network.palace.dashboard.packets.arcade;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Innectic
 * @since 5/17/2017
 */
@AllArgsConstructor
public enum GameState {
    LOBBY(1), INGAME(2), RESTARTING(3);

    @Getter private int id;

    /**
     * Get the state of a game from a number representation.
     *
     * @param state the int state to convert
     * @return the state of the game
     */
    public static GameState stateFromInt(int state) {
        switch (state) {
            case 1:
                return LOBBY;
            case 2:
                return INGAME;
            case 3:
                return RESTARTING;
            default:
                return null;
        }
    }
}
