# Dependency Verification Runbook

This document records the manual verification steps for the external runtime
dependencies used by Orloj:

- PostgreSQL, deployed from the Bitnami Helm chart.
- NATS, deployed from the official NATS Kubernetes Helm chart.
- Orloj, deployed from `orloj_beta/charts/orloj`.

The examples below assume a Kubernetes cluster is already available and that
`kubectl`, `helm`, and `git` are configured on the local machine.

## PostgreSQL

### 1. Clone Only The PostgreSQL Chart

Use sparse checkout so only the Bitnami PostgreSQL chart is downloaded.

```bash
git clone --filter=blob:none --no-checkout https://github.com/bitnami/charts.git postgresql-chart
cd postgresql-chart

git sparse-checkout init --cone
git sparse-checkout set bitnami/postgresql
git checkout main
```

### 2. Build Chart Dependencies

```bash
cd bitnami/postgresql
helm dependency build
```

### 3. Install Or Upgrade PostgreSQL

```bash
helm upgrade -i orloj-postgres . \
  -n orloj-data \
  --create-namespace
```

### 4. Verify Kubernetes Resources

```bash
helm list -n orloj-data
kubectl get pods -n orloj-data
kubectl get statefulsets -n orloj-data
kubectl get svc -n orloj-data
kubectl get pvc -n orloj-data
```

Expected resources include:

- `StatefulSet/orloj-postgres-postgresql`
- `Pod/orloj-postgres-postgresql-0`
- PostgreSQL service in namespace `orloj-data`
- PersistentVolumeClaim for PostgreSQL data

### 5. Internal DSN For Orloj

```text
postgres://orloj:orloj123@orloj-postgres-postgresql.orloj-data.svc.cluster.local:5432/orloj?sslmode=disable
```

### 6. Verify Database Connectivity

Open a shell in the PostgreSQL pod:

```bash
kubectl exec -it -n orloj-data orloj-postgres-postgresql-0 -- bash
```

Connect with `psql`:

```bash
psql -U orloj -d orloj
```

When prompted, enter the configured password:

```text
orloj123
```

Verify that the expected database exists:

```sql
\l
SELECT current_database();
```

Expected result:

```text
current_database
----------------
orloj
```

### 7. Optional Cleanup

```bash
helm del orloj-postgres -n orloj-data
```

## NATS

### 1. Clone Only The NATS Helm Chart

Use sparse checkout so only the NATS Helm chart is downloaded.

```bash
git clone --filter=blob:none --no-checkout https://github.com/nats-io/k8s.git
cd k8s

git sparse-checkout init --cone
git sparse-checkout set helm/charts/nats
git checkout main
```

### 2. Install Or Upgrade NATS

```bash
cd helm/charts/nats

helm upgrade -i orloj-nats . \
  -n orloj-nats \
  --create-namespace
```

### 3. Verify Kubernetes Resources

```bash
helm list -n orloj-nats
kubectl get pods -n orloj-nats
kubectl get statefulsets -n orloj-nats
kubectl get svc -n orloj-nats
kubectl get pvc -n orloj-nats
```

Expected resources include:

- `StatefulSet/orloj-nats`
- `Pod/orloj-nats-0`
- NATS client and headless services in namespace `orloj-nats`

### 4. Verify NATS Readiness

```bash
kubectl logs orloj-nats-0 -n orloj-nats
```

Look for:

```text
Server is ready
```

### 5. Inspect JetStream Status

Forward the monitoring port:

```bash
kubectl port-forward svc/orloj-nats-headless 8222:8222 -n orloj-nats
```

Open:

```text
http://localhost:8222/jsz
```

### 6. Publish A Test Message

Run a temporary NATS CLI pod and publish a message to the `demo` subject:

```bash
kubectl run nats-box-pub \
  --rm -it \
  --restart=Never \
  --image=natsio/nats-box \
  -n orloj-nats \
  -- nats --server nats://orloj-nats.orloj-nats.svc.cluster.local:4222 pub demo "Hello Orloj"
```

Expected output:

```text
Published 11 bytes to "demo"
pod "nats-box-pub" deleted
```

## Orloj

These steps are based on `orloj_beta/charts/orloj/SETUP.md` and assume the
external PostgreSQL and NATS dependencies above are already installed.

### 1. Install Or Upgrade Orloj

From the chart directory:

```bash
cd orloj_beta/charts/orloj
helm dependency update .

helm upgrade -i orloj . \
  -n orloj \
  --create-namespace \
  -f v1-values.yaml
```

Alternative from a cloned upstream Orloj repository:

```bash
git clone https://github.com/OrlojHQ/orloj.git
cd orloj

helm dependency update charts/orloj/

helm upgrade --install orloj ./charts/orloj \
  --namespace orloj \
  --create-namespace \
  -f v1-values.yaml
```

### 2. Configure Local Ingress Host

For local ingress access, add `orloj.local` to `/etc/hosts`:

```bash
echo "127.0.0.1 orloj.local" | sudo tee -a /etc/hosts
```

### 3. Verify Helm Release

```bash
helm list -n orloj
```

Expected release state:

```text
orloj   orloj   deployed
```

### 4. Verify Pods

```bash
kubectl get pods -n orloj
```

Expected workload state:

```text
orloj-...server...      Running
orloj-...worker...      Running
orloj-...worker...      Running
orloj-...operator...    Running
```

### 5. Verify Services And Ingress

```bash
kubectl get svc -n orloj
kubectl get ingress -n orloj
```

Expected ingress host:

```text
orloj.local
```

Test the ingress route:

```bash
curl -i http://orloj.local
```

### 6. Verify External Dependency Values

```bash
helm get values orloj -n orloj
```

Confirm the chart is using external PostgreSQL and NATS:

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

### 7. Verify Orloj Logs

Server logs:

```bash
kubectl logs -n orloj deploy/orloj-orloj-server
```

Worker logs:

```bash
kubectl logs -n orloj -l app.kubernetes.io/component=worker --tail=100
```

Operator logs:

```bash
kubectl logs -n orloj -l app.kubernetes.io/component=operator --tail=100
```

Check that logs do not contain dependency failures such as:

```text
connection refused
database does not exist
authentication failed
nats: no servers available
```

### 8. Verify Orloj CRDs

```bash
kubectl get crds | grep -i orloj
```

### 9. Recheck External Dependencies From The Cluster

PostgreSQL:

```bash
kubectl get pods -n orloj-data
kubectl exec -it -n orloj-data orloj-postgres-postgresql-0 -- \
  psql -U orloj -d orloj -c "SELECT current_database();"
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

Expected NATS publish result:

```text
Published 11 bytes to "demo"
```

### 10. Enable Debug Logs During Upgrade

Use this only when troubleshooting.

```bash
helm upgrade orloj ./charts/orloj \
  --namespace orloj \
  --reuse-values \
  --set server.logLevel=debug \
  --set worker.logLevel=debug
```

### 11. Optional Cleanup

```bash
helm uninstall orloj --namespace orloj
```

This uninstalls Orloj only. The external PostgreSQL and NATS releases remain
untouched.

## Verification Summary

PostgreSQL is considered healthy when:

- The PostgreSQL pod is running.
- The service resolves inside the cluster.
- `psql -U orloj -d orloj` connects successfully.
- `SELECT current_database();` returns `orloj`.

NATS is considered healthy when:

- The NATS pod is running.
- Logs include `Server is ready`.
- The monitoring endpoint `/jsz` is reachable through port-forwarding.
- A message can be published to the `demo` subject from an in-cluster NATS CLI pod.

Orloj is considered healthy when:

- The Helm release is `deployed` in namespace `orloj`.
- Server, worker, and operator pods are running.
- The ingress exposes `orloj.local`.
- Helm values point to external PostgreSQL and NATS.
- Logs do not show PostgreSQL or NATS connection failures.
- Orloj CRDs are installed.
