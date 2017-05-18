package network.palace.dashboard.packets.arcade;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import network.palace.dashboard.packets.BasePacket;

/**
 * @author Innectic
 * @since 5/17/2017
 *
 * The current status of the game.
 */
@AllArgsConstructor
public class PacketGameStatus extends BasePacket {

    public PacketGameStatus() {
        this(GameState.WAITING, 0, 0, false, "", "", 0);
    }
    /**
     * The current state of the game.
     */
    @Getter private GameState state;
    /**
     * The amount of players on the server.
     */
    @Getter private int playerAmount;
    /**
     * The max amount of players that are allowed on the server.
     */
    @Getter private int maxPlayers;
    /**
     * Are players allowed to join mid-game?
     */
    @Getter private boolean canJoinMidGame;
    /**
     * The full-name of the server: mini-oneshot1
     */
    @Getter private String serverName;
    /**
     * The type of the server: mini-oneshot1 -> oneshot
     */
    @Getter private String serverType;

    /**
     * The id of the game: mini-oneshot1 -> 1
     */
    @Getter private int gameId;

    @Override
    public PacketGameStatus fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        this.playerAmount = obj.get("playerAmount").getAsInt();
        this.maxPlayers = obj.get("maxPlayers").getAsInt();
        this.canJoinMidGame = obj.get("canJoinMidGame").getAsBoolean();
        this.serverName = obj.get("serverName").getAsString();
        this.serverType = obj.get("serverType").getAsString();
        this.state = GameState.stateFromInt(obj.get("state").getAsInt());
        return this;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("playerAmount", this.playerAmount);
            obj.addProperty("maxPlayers", this.maxPlayers);
            obj.addProperty("canJoinMidGame", this.canJoinMidGame);
            obj.addProperty("serverName", this.serverName);
            obj.addProperty("serverType", this.serverType);
            obj.addProperty("state", this.state.getId());
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}
