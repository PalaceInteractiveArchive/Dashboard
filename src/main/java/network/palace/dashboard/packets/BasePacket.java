package network.palace.dashboard.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

/**
 * Created by Marc on 6/15/15
 */
public abstract class BasePacket {
    @Getter protected int id = 0;

    public abstract BasePacket fromJSON(JsonObject obj);
    public abstract JsonObject getJSON();
}