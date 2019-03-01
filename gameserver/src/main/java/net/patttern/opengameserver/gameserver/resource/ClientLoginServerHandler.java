package net.patttern.opengameserver.gameserver.resource;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import net.patttern.opengameserver.gameserver.config.GameServerConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Client login server handler resource
 *
 * @author Egor Babenko (patttern@gmail.com)
 */
@Slf4j
public class ClientLoginServerHandler extends ChannelInboundHandlerAdapter {

    private static final long CLIENT_GAME_SERVER_RECONNECT = 1L;

    private final ByteBuf firstMessage;
    private final GameServerConfig gameServerConfig;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public ClientLoginServerHandler(GameServerConfig gsc) {
        this.gameServerConfig = gsc;
        firstMessage = Unpooled.buffer(256);
        for (int i = 0; i < firstMessage.capacity(); i ++) {
            firstMessage.writeByte((byte) i);
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
//        log.info("channelRegistered: ctx {}", ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
//        log.info("channelUnregistered: ctx {}", ctx);
        executorService.schedule(gameServerConfig::clientConnect, CLIENT_GAME_SERVER_RECONNECT, TimeUnit.SECONDS);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        log.info("channelActive: ctx {}", ctx);
        ctx.writeAndFlush(firstMessage);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        log.info("channelInactive: ctx {}", ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        log.info("channelRead: ctx {}, msg {}", ctx, msg);
        ctx.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        log.info("channelReadComplete: ctx {}", ctx);
        ctx.flush();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.info("userEventTriggered: ctx {}, evt {}", ctx, evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        log.info("channelWritabilityChanged: ctx {}", ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
