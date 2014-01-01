import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public final class SniServer {
	public static void main(String[] args) throws Exception {
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(new NioEventLoopGroup());
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.childHandler(new SniChannelInitializer());
		bootstrap.bind(new InetSocketAddress(8443));
	}
}
