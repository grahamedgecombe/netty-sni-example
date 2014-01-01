This is a quick example demonstrating how to use Java 8's server-side SNI
support in Netty.

`SniKeyManager` is a key manager which wraps around the default key manager. It
forwards most methods to the default key manager. However, it implements its
own logic for the `chooseEngineServerAlias()` method - using SNI to pick which
certificate to use.

I've included an example key store with two self-signed certificates for
`test1.example.com` and `test2.example.com`.

You can use `openssl s_client` to check it works:

    $ openssl s_client -connect localhost:8443 -servername test1.example.com 2>&1 | grep "subject="
    subject=/C=Unknown/ST=Unknown/L=Unknown/O=Unknown/OU=Unknown/CN=test1.example.com

    $ openssl s_client -connect localhost:8443 -servername test2.example.com 2>&1 | grep "subject="
    subject=/C=Unknown/ST=Unknown/L=Unknown/O=Unknown/OU=Unknown/CN=test2.example.com

If an unknown hostname is given, it falls back to `test1.example.com`:

    $ openssl s_client -connect localhost:8443 -servername unknown.example.com 2>&1 | grep "subject="
    subject=/C=Unknown/ST=Unknown/L=Unknown/O=Unknown/OU=Unknown/CN=test1.example.com

If the client does not support SNI, it also falls back to `test1.example.com`:

    $ openssl s_client -connect localhost:8443 2>&1 | grep "subject="
    subject=/C=Unknown/ST=Unknown/L=Unknown/O=Unknown/OU=Unknown/CN=test1.example.com
