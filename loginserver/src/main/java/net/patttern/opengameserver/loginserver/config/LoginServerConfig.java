package net.patttern.opengameserver.loginserver.config;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import net.patttern.opengameserver.api.resource.PacketServerHandler;
import net.patttern.opengameserver.loginserver.resource.ClientGameServerHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.net.ConnectException;

/**
 * Login server configuration
 *
 * @author Egor Babenko (patttern@gmail.com)
 */
@Slf4j
@Configuration
public class LoginServerConfig {

    //Common
    @Value("${debug.mode}")
    private boolean debugMode;

    //Server
    @Value("${server.global.port}")
    private int serverGlobalPort;
    @Value("${server.private.port}")
    private int serverPrivatePort;

    //Client
    @Value("${gameserver.private.host}")
    private String gameserverHost;
    @Value("${gameserver.private.port}")
    private int gameserverPort;

    @PostConstruct
    public void init() {
        try {
            ChannelFuture local = serverBootstrap().bind(serverPrivatePort).sync();
            ChannelFuture global = serverBootstrap().bind(serverGlobalPort).sync();

            log.info("Private channel started on port {}", serverPrivatePort);
            log.info("Login server started on port {}", serverGlobalPort);

            clientConnect();
            local.channel().closeFuture().sync();
            global.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("Exception on start login server: {}", e.getMessage());
        }
    }

    // Server configuration

    @Bean(destroyMethod = "shutdownGracefully")
    EventLoopGroup serverBossGroup() {
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup(1);
        } else {
            return new NioEventLoopGroup(1);
        }
    }

    @Bean(destroyMethod = "shutdownGracefully")
    EventLoopGroup serverWorkerGroup() {
        int workerCount = Runtime.getRuntime().availableProcessors() * 2;
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup(workerCount);
        } else {
            return new NioEventLoopGroup(workerCount);
        }
    }

    @Bean
    Class<? extends ServerChannel> serverChannel() {
        if (Epoll.isAvailable()) {
            return EpollServerSocketChannel.class;
        } else {
            return NioServerSocketChannel.class;
        }
    }

    @Bean
    ServerBootstrap serverBootstrap() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(serverBossGroup(), serverWorkerGroup())
                .channel(serverChannel())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        if (debugMode) {
                            p.addLast(new LoggingHandler(LogLevel.INFO));
                        }
                        p.addLast(new PacketServerHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        return serverBootstrap;
    }

    // Client configuration

    @Bean(destroyMethod = "shutdownGracefully")
    EventLoopGroup clientWorkerGroup() {
        return Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    }

    @Bean
    Class<? extends Channel> clientChannel() {
        return Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class;
    }

    @Bean
    Bootstrap clientBootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(clientWorkerGroup())
                .channel(clientChannel())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new ClientGameServerHandler(LoginServerConfig.this));
                    }
                });
        return bootstrap;
    }

    public void clientConnect() {
        try {
            log.info("Try connect to game server {}:{} ...", gameserverHost, gameserverPort);

            ChannelFuture cf = clientBootstrap().connect(gameserverHost, gameserverPort).sync();
            log.info("Client game server was connected to {}:{}", gameserverHost, gameserverPort);

            cf.channel().closeFuture().sync();
        } catch (Exception e) {
            if (e.getCause() instanceof ConnectException) {
                //Nothing to do
            } else {
                log.error("Exception on init client game server: {}", e.getMessage());
            }
        }
    }
}
