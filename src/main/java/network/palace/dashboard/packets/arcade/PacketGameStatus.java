package network.palace.dashboard.packets.arcade;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

/**
 * @author Innectic
 * @since 5/17/2017
 * <p>
 * The current status of the game.
 */
public class PacketGameStatus extends BasePacket {

    public PacketGameStatus() {
        this(GameState.LOBBY, 0, "");
    }

    public PacketGameStatus(GameState state, int playerAmount, String serverName) {
        this.id = PacketID.Arcade.GAMESTATUS.getID();
        this.state = state;
        this.playerAmount = playerAmount;
        this.serverName = serverName;
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
     * The full-name of the server: mini-oneshot1
     */
    @Getter private String serverName;

    @Override
    public PacketGameStatus fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        this.playerAmount = obj.get("playerAmount").getAsInt();
        this.serverName = obj.get("serverName").getAsString();
        this.state = GameState.stateFromInt(obj.get("state").getAsInt());
        return this;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("playerAmount", this.playerAmount);
            obj.addProperty("serverName", this.serverName);
            obj.addProperty("state", this.state.getId());
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}
