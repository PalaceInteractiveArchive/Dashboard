package network.palace.dashboard.vote;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import network.palace.dashboard.utils.VoteUtil;

import java.nio.charset.StandardCharsets;

/**
 * Created by Marc on 1/15/17.
 */
public class VotifierGreetingHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        VotifierSession session = ctx.channel().attr(VotifierSession.KEY).get();
        VoteUtil plugin = ctx.channel().attr(VoteUtil.KEY).get();
        String version = "VOTIFIER v1 " + session.getChallenge() + "\n";
        ByteBuf versionBuf = Unpooled.copiedBuffer(version, StandardCharsets.UTF_8);
        ctx.writeAndFlush(versionBuf);
    }
}