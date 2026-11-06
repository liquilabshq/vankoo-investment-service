#!/bin/sh
set -eu

# Public server certificate only. Never bake private keys into the image.
if [ -n "${INVESTMENT_DB_TLS_CERT_BASE64:-}" ]; then
  trust_dir="$(mktemp -d)"
  printf '%s' "$INVESTMENT_DB_TLS_CERT_BASE64" | base64 -d > "$trust_dir/server.crt"
  # Preserve public CA roots for future HTTPS/Event Hubs connections.
  keytool -importkeystore -noprompt \
    -srckeystore "$JAVA_HOME/lib/security/cacerts" -srcstorepass changeit \
    -destkeystore "$trust_dir/truststore.p12" -deststoretype PKCS12 \
    -deststorepass changeit >/dev/null 2>&1
  keytool -importcert -noprompt -alias investment-oracle \
    -file "$trust_dir/server.crt" -keystore "$trust_dir/truststore.p12" \
    -storetype PKCS12 -storepass changeit >/dev/null 2>&1
  # This truststore contains only a public certificate; changeit is not a credential.
  exec java -Djavax.net.ssl.trustStore="$trust_dir/truststore.p12" \
    -Djavax.net.ssl.trustStorePassword=changeit -jar /app/app.jar "$@"
fi
exec java -jar /app/app.jar "$@"
