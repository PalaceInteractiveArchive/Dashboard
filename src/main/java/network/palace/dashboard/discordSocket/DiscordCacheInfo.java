package network.palace.dashboard.discordSocket;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class DiscordCacheInfo {

    @Getter private Minecraft minecraft;
    @Getter private Discord discord;

    @AllArgsConstructor
    public class Minecraft {
        @Getter private String username = "";
        @Getter private String uuid = "";
        @Getter private String rank = "";
    }

    @AllArgsConstructor
    public class Discord {
        @Getter private String username = "";
    }
}
