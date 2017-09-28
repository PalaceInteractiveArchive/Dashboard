package network.palace.dashboard.mongo.structures;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Innectic
 * @since 9/23/2017
 */
@AllArgsConstructor
@Getter
public class MuteStructure {
    private String reason;
    private UUID source;
    private long created;
    private long expires;

    public Map<String, Object> get() {
        Map<String, Object> data = new HashMap<>();
        data.put("reason", reason);
        data.put("created", created);
        data.put("expires", expires);
        data.put("source", source);
        return data;
    }

    public static MuteStructure from(Document document) {
        return new MuteStructure(document.getString("reason"), UUID.fromString(document.getString("source")),
                document.getLong("created"), document.getLong("expires"));
    }
}
