#!/bin/sh
# Run inside the Temurin runtime image, with docker/ mounted at /checks read-only.
set -eu
test_dir="$(mktemp -d)"
keytool -genkeypair -alias fixture -dname CN=oracle.example.test -keyalg RSA \
  -keystore "$test_dir/fixture.p12" -storepass changeit -keypass changeit \
  -validity 1 >/dev/null 2>&1
keytool -exportcert -rfc -alias fixture -keystore "$test_dir/fixture.p12" \
  -storepass changeit -file "$test_dir/fixture.crt" >/dev/null 2>&1
INVESTMENT_DB_TLS_CERT_BASE64="$(base64 < "$test_dir/fixture.crt" | tr -d '\n')"
export INVESTMENT_DB_TLS_CERT_BASE64
mkdir "$test_dir/bin"
printf '%s\n' '#!/bin/sh' \
  'case "$1" in -Djavax.net.ssl.trustStore=*) truststore=${1#*=} ;; *) exit 1 ;; esac' \
  'keytool -list -alias investment-oracle -keystore "$truststore" -storepass changeit >/dev/null 2>&1' \
  'keytool -list -keystore "$truststore" -storepass changeit 2>/dev/null | grep -q "trustedCertEntry"' \
  'echo ENTRYPOINT_TLS_TRUST_OK' > "$test_dir/bin/java"
chmod +x "$test_dir/bin/java"
PATH="$test_dir/bin:$PATH" sh /checks/entrypoint.sh
