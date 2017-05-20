package network.palace.dashboard.packets;

import com.google.gson.JsonObject;

/**
 * Created by Marc on 6/15/15
 */
public abstract class BasePacket {
    protected int id = 0;

    public int getID() {
        return id;
    }

    public abstract BasePacket fromJSON(JsonObject obj);
    public abstract JsonObject getJSON();
}