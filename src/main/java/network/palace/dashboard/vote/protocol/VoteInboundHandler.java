package network.palace.dashboard.vote.protocol;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import network.palace.dashboard.utils.VoteUtil;
import network.palace.dashboard.vote.Vote;
import network.palace.dashboard.vote.VotifierSession;
import org.json.JSONObject;

/**
 * Created by Marc on 1/15/17.
 */
public class VoteInboundHandler extends SimpleChannelInboundHandler<Vote> {
    private final VoteUtil handler;

    public VoteInboundHandler(VoteUtil handler) {
        this.handler = handler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, final Vote vote) throws Exception {
        VotifierSession session = ctx.channel().attr(VotifierSession.KEY).get();

        handler.onVoteReceived(ctx.channel(), vote, session.getVersion());

        if (session.getVersion() == VotifierSession.ProtocolVersion.ONE) {
            ctx.close();
        } else {
            JSONObject object = new JSONObject();
            object.put("status", "ok");
            ctx.writeAndFlush(object.toString() + "\r\n").addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        VotifierSession session = ctx.channel().attr(VotifierSession.KEY).get();

        handler.onError(ctx.channel(), cause);

        if (session.getVersion() == VotifierSession.ProtocolVersion.TWO) {
            JSONObject object = new JSONObject();
            object.put("status", "error");
            object.put("cause", cause.getClass().getSimpleName());
            object.put("error", cause.getMessage());
            ctx.writeAndFlush(object.toString() + "\r\n").addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.close();
        }
    }
}