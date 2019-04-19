package network.palace.dashboard.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * @author Marc
 * @since 9/28/17
 */
@AllArgsConstructor
@Getter
public class BseenData {
    private UUID uuid;
    private Rank rank;
    private SponsorTier sponsorTier;
    private long lastLogin;
    private String ipAddress;
    private Mute mute;
    private String server;
}
