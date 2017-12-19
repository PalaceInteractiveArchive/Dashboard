package network.palace.dashboard.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class IgnoreData {
    private UUID uuid;
    private UUID ignored;
    private long started;
}
