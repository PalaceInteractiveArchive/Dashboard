package network.palace.dashboard.vote;

import io.netty.util.AttributeKey;

/**
 * Created by Marc on 1/15/17.
 */
public class VotifierSession {
    public static final AttributeKey<VotifierSession> KEY = AttributeKey.valueOf("votifier_session");
    private ProtocolVersion version = ProtocolVersion.UNKNOWN;
    private final String challenge;

    public VotifierSession() {
        challenge = TokenUtil.newToken();
    }

    public void setVersion(ProtocolVersion version) {
        if (this.version != ProtocolVersion.UNKNOWN)
            throw new IllegalStateException("Protocol version already switched");

        this.version = version;
    }

    public ProtocolVersion getVersion() {
        return version;
    }

    public String getChallenge() {
        return challenge;
    }

    public enum ProtocolVersion {
        UNKNOWN,
        ONE,
        TWO
    }
}
