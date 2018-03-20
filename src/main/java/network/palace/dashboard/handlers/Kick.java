package network.palace.dashboard.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Created by Marc on 8/25/16
 */
@AllArgsConstructor
@RequiredArgsConstructor
public class Kick {
    @Getter private final UUID uniqueId;
    @Getter private final String reason;
    @Getter private final String source;
    @Getter @Setter private long time = 0;
}