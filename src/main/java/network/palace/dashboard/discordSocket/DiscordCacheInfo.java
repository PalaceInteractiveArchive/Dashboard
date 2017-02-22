package network.palace.dashboard.discordSocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class DiscordCacheInfo {

    @Getter private Minecraft minecraft;
    @Getter private Discord discord;

    @AllArgsConstructor
    public static class Minecraft {
        @Getter private String username = "";
        @Getter private String uuid = "";
        @Getter @Setter private String rank = "";
    }

    @AllArgsConstructor
    public static class Discord {
        @Getter private String username = "";
    }
}
