package network.palace.dashboard.packets.arcade;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Innectic
 * @since 5/17/2017
 */
@AllArgsConstructor
public enum GameState {
    WAITING(1), RUNNING(2), ENDING(3);

    @Getter private int id;

    /**
     * Get the state of a game from a number representation.
     *
     * @param state the int state to convert
     * @return the state of the game
     */
    public static GameState stateFromInt(int state) {
        if (state == 1) return WAITING;
        if (state == 2) return RUNNING;
        return ENDING;
    }
}
