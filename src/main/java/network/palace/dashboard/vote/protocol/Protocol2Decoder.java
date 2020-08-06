package network.palace.dashboard.vote.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToMessageDecoder;
import network.palace.dashboard.utils.VoteUtil;
import network.palace.dashboard.vote.Vote;
import network.palace.dashboard.vote.VotifierSession;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 1/15/17.
 */
public class Protocol2Decoder extends MessageToMessageDecoder<String> {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void decode(ChannelHandlerContext ctx, String s, List<Object> list) throws Exception {
        JsonObject voteMessage;
        JsonElement element = new Gson().fromJson(s, JsonElement.class);
        if (element == null || !element.isJsonObject()) {
            voteMessage = new JsonObject();
        } else {
            voteMessage = element.getAsJsonObject();
        }
        VotifierSession session = ctx.channel().attr(VotifierSession.KEY).get();

        // Deserialize the payload.
        element = new Gson().fromJson(voteMessage.get("payload").getAsString(), JsonElement.class);
        JsonObject votePayload;
        if (element == null || !element.isJsonObject()) {
            votePayload = new JsonObject();
        } else {
            votePayload = element.getAsJsonObject();
        }

        // Verify challenge.
        if (!votePayload.get("challenge").getAsString().equals(session.getChallenge())) {
            throw new CorruptedFrameException("Challenge is not valid");
        }

        // Verify that we have keys available.
        VoteUtil plugin = ctx.channel().attr(VoteUtil.KEY).get();
        Key key = plugin.getTokens().get(votePayload.get("serviceName").getAsString());

        if (key == null) {
            key = plugin.getTokens().get("default");
            if (key == null) {
                throw new RuntimeException("Unknown service '" + votePayload.get("serviceName").getAsString() + "'");
            }
        }

        // Verify signature.
        String sigHash = voteMessage.get("signature").getAsString();
        byte[] sigBytes = DatatypeConverter.parseBase64Binary(sigHash);

        if (!hmacEqual(sigBytes, voteMessage.get("payload").getAsString().getBytes(StandardCharsets.UTF_8), key)) {
            throw new CorruptedFrameException("Signature is not valid (invalid token?)");
        }

        // Stopgap: verify the "uuid" field is valid, if provided.
        if (votePayload.has("uuid")) {
            UUID.fromString(votePayload.get("uuid").getAsString());
        }

        if (votePayload.get("username").getAsString().length() > 16) {
            throw new CorruptedFrameException("Username too long");
        }

        // Create the vote.
        Vote vote = new Vote(votePayload);
        list.add(vote);

        ctx.pipeline().remove(this);
    }

    private boolean hmacEqual(byte[] sig, byte[] message, Key key) throws NoSuchAlgorithmException, InvalidKeyException {
        // See https://www.nccgroup.trust/us/about-us/newsroom-and-events/blog/2011/february/double-hmac-verification/
        // This randomizes the byte order to make timing attacks more difficult.
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        byte[] calculatedSig = mac.doFinal(message);

        // Generate a random key for use in comparison
        byte[] randomKey = new byte[32];
        RANDOM.nextBytes(randomKey);

        // Then generate two HMACs for the different signatures found
        Mac mac2 = Mac.getInstance("HmacSHA256");
        mac2.init(new SecretKeySpec(randomKey, "HmacSHA256"));
        byte[] clientSig = mac2.doFinal(sig);
        mac2.reset();
        byte[] realSig = mac2.doFinal(calculatedSig);

        return MessageDigest.isEqual(clientSig, realSig);
    }
}