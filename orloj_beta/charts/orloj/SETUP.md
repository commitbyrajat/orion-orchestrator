## Install Orloj

```bash
git clone https://github.com/OrlojHQ/orloj.git
cd orloj
```

```bash
helm dependency update charts/orloj/
```

```bash
helm upgrade --install orloj ./charts/orloj \
  --namespace orloj \
  --create-namespace \
  -f v1-values.yaml
```

### or

```bash
helm upgrade -i orloj . -n orloj --create-namespace -f v1-values.yaml
```

For local ingress:

```bash
echo "127.0.0.1 orloj.local" | sudo tee -a /etc/hosts
```

## Verify Helm release

```bash
helm list -n orloj
```

Expected:

```text
orloj   orloj   deployed
```

## Verify pods

```bash
kubectl get pods -n orloj
```

Expected:

```text
orloj-...server...      Running
orloj-...worker...      Running
orloj-...worker...      Running
orloj-...operator...    Running
```

## Verify services and ingress

```bash
kubectl get svc -n orloj
kubectl get ingress -n orloj
```

Expected ingress host:

```text
orloj.local
```

Test:

```bash
curl -i http://orloj.local
```

## Verify external PostgreSQL/NATS values are applied

```bash
helm get values orloj -n orloj
```

Confirm:

```yaml
postgresql:
  enabled: false

externalPostgres:
  dsn: postgres://orloj:orloj123@orloj-postgres-postgresql.orloj-data.svc.cluster.local:5432/orloj?sslmode=disable

nats:
  enabled: false

externalNats:
  url: nats://orloj-nats.orloj-nats.svc.cluster.local:4222
```

## Verify logs

```bash
kubectl logs -n orloj deploy/orloj-orloj-server
```

Check there are no errors like:

```text
connection refused
database does not exist
authentication failed
nats: no servers available
```

Workers:

```bash
kubectl logs -n orloj -l app.kubernetes.io/component=worker --tail=100
```

Operator:

```bash
kubectl logs -n orloj -l app.kubernetes.io/component=operator --tail=100
```

## Verify CRDs

```bash
kubectl get crds | grep -i orloj
```

## Verify external dependencies still work

Postgres:

```bash
kubectl get pods -n orloj-data
kubectl exec -it -n orloj-data orloj-postgres-postgresql-0 -- psql -U orloj -d orloj -c "SELECT current_database();"
```

NATS:

```bash
kubectl run nats-box-pub \
  --rm -it \
  --restart=Never \
  --image=natsio/nats-box \
  -n orloj-nats \
  -- nats --server nats://orloj-nats.orloj-nats.svc.cluster.local:4222 pub demo "Hello Orloj"
```

Expected:

```text
Published 11 bytes to "demo"
```

## Upgrade with debug logs if needed

```bash
helm upgrade orloj ./charts/orloj \
  --namespace orloj \
  --reuse-values \
  --set server.logLevel=debug \
  --set worker.logLevel=debug
```

## Uninstall

```bash
helm uninstall orloj --namespace orloj
```

This will uninstall Orloj only. Your external PostgreSQL and NATS releases remain untouched.

[1]: https://raw.githubusercontent.com/OrlojHQ/orloj/main/charts/orloj/README.md "raw.githubusercontent.com"
