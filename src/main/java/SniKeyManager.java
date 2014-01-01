import javax.net.ssl.*;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public final class SniKeyManager extends X509ExtendedKeyManager {
	private final X509ExtendedKeyManager keyManager;
	private final String defaultAlias = "test1.example.com";

	public SniKeyManager(X509ExtendedKeyManager keyManager) {
		this.keyManager = keyManager;
	}

	@Override
	public String[] getClientAliases(String keyType, Principal[] issuers) {
		throw new UnsupportedOperationException(); // we don't use client mode
	}

	@Override
	public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
		throw new UnsupportedOperationException(); // as above
	}

	@Override
	public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
		throw new UnsupportedOperationException(); // as above
	}

	@Override
	public String[] getServerAliases(String keyType, Principal[] issuers) {
		return keyManager.getServerAliases(keyType, issuers);
	}

	@Override
	public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
		throw new UnsupportedOperationException(); // Netty does not use SSLSocket
	}

	@Override
	public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
		ExtendedSSLSession session = (ExtendedSSLSession) engine.getHandshakeSession();

		// Pick first SNIHostName in the list of SNI names.
		String hostname = null;
		for (SNIServerName name : session.getRequestedServerNames()) {
			if (name.getType() == StandardConstants.SNI_HOST_NAME) {
				hostname = ((SNIHostName) name).getAsciiName();
				break;
			}
		}

		// If we got given a hostname over SNI, check if we have a cert and key for that hostname. If so, we use it.
		// Otherwise, we fall back to the default certificate.
		if (hostname != null && (getCertificateChain(hostname) != null && getPrivateKey(hostname) != null))
			return hostname;
		else
			return defaultAlias;
	}

	@Override
	public X509Certificate[] getCertificateChain(String alias) {
		return keyManager.getCertificateChain(alias);
	}

	@Override
	public PrivateKey getPrivateKey(String alias) {
		return keyManager.getPrivateKey(alias);
	}
}
