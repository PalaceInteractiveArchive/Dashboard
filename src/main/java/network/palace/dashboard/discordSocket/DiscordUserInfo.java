package network.palace.dashboard.discordSocket;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DiscordUserInfo {

    private String discordID;
    private String minecraftUsername;
    private String minecraftUUID;
    private String minecraftRank;

    @Override
    public String toString() {
        return "DiscordUserInfo [" +
                "discordID=" + discordID +
                ", minecraftUsername=" + minecraftUsername +
                ", minecraftUUID=" + minecraftUUID +
                ", minecraftRank=" + minecraftRank +
                "]";
    }
}
