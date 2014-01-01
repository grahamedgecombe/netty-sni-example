import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.*;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

public final class SniChannelInitializer extends ChannelInitializer<Channel> {
	private final SSLContext context;

	public SniChannelInitializer() throws Exception {
		KeyStore store = KeyStore.getInstance("JKS");
		try (InputStream is = Files.newInputStream(Paths.get("keys.jks"))) {
			store.load(is, "password".toCharArray());
		}

		KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		factory.init(store, "password".toCharArray());

		// Javadoc of SSLContext.init() states the first KeyManager implementing X509ExtendedKeyManager in the array is
		// used. We duplicate this behaviour when picking the KeyManager to wrap around.
		X509ExtendedKeyManager x509KeyManager = null;
		for (KeyManager keyManager : factory.getKeyManagers()) {
			if (keyManager instanceof X509ExtendedKeyManager) {
				x509KeyManager = (X509ExtendedKeyManager) keyManager;
			}
		}

		if (x509KeyManager == null)
			throw new Exception("KeyManagerFactory did not create an X509ExtendedKeyManager");

		SniKeyManager sniKeyManager = new SniKeyManager(x509KeyManager);

		context = SSLContext.getInstance("TLS");
		context.init(new KeyManager[] {
			sniKeyManager
		}, null, null);
	}

	@Override
	protected void initChannel(Channel ch) throws NoSuchAlgorithmException, KeyManagementException {
		SSLEngine engine = context.createSSLEngine();
		engine.setUseClientMode(false);

		ch.pipeline().addLast(
			new SslHandler(engine),
			new SniChannelHandler()
		);
	}
}
