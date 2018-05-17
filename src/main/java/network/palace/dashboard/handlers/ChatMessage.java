package network.palace.dashboard.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ChatMessage {
    private UUID uuid;
    private String message;
    private long time;
}
