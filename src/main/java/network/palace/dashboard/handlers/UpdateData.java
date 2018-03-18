package network.palace.dashboard.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.BsonArray;

@Getter
@AllArgsConstructor
public class UpdateData {
    private BsonArray pack;
    private int packSize;
    private BsonArray locker;
    private int lockerSize;
    private BsonArray hotbar;
}
