#!/bin/bash
set -euo pipefail
bucket="$1"
revision="$2"
[[ "$bucket" =~ ^[a-z0-9][a-z0-9.-]+$ ]] || exit 2
[[ "$revision" =~ ^[a-f0-9]{7,40}$ ]] || exit 2
install -d -o libros -g libros -m 750 /opt/libros/releases
aws s3 cp "s3://$bucket/releases/$revision.jar" "/opt/libros/releases/$revision.jar" --only-show-errors
chown libros:libros "/opt/libros/releases/$revision.jar"
chmod 640 "/opt/libros/releases/$revision.jar"
previous=$(readlink /opt/libros/app.jar || true)
ln -sfn "/opt/libros/releases/$revision.jar" /opt/libros/app.jar
systemctl restart libros
for attempt in $(seq 1 45); do
  if curl --fail --silent http://127.0.0.1:8080/libros -o /tmp/libros-health.json; then
    python3 -c 'import json; assert isinstance(json.load(open("/tmp/libros-health.json")), list)'
    echo "API y Oracle disponibles; version $revision"
    exit 0
  fi
  sleep 4
done
echo 'La nueva version no paso la comprobacion de salud.' >&2
if [ -n "$previous" ]; then
  ln -sfn "$previous" /opt/libros/app.jar
  systemctl restart libros
  echo 'Se restauro la version anterior.' >&2
fi
exit 1
