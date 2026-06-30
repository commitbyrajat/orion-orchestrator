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
export orloj_url="http://orloj.local"
export ORLOJ_API_TOKEN="orloj-api-token-change-me"
export ORLOJ_SETUP_TOKEN="orloj-setup-token-change-me"

curl -i "$orloj_url"
```

#### Configure First Admin Credentials With Curl

Use `/v1/auth/setup` to configure the admin username and password the first
time Orloj starts with native auth. The chart values used above enable native
auth and set `auth.setupToken`; replace the default setup token in
`v1-values.yaml` before using this outside a local test cluster.

First, confirm that native auth is enabled and first-user setup is still
required:

```bash
curl -i "$orloj_url/v1/auth/config"
```

Expected first-time setup response:

```json
{
  "mode": "native",
  "setup_required": true,
  "setup_token_required": true,
  "login_methods": ["password"]
}
```

Create the initial admin user. This exact example configures username `admin`
with password `admin-password-change-me` using the sample setup token from
`v1-values.yaml`. Change these values before using the command outside a local
test cluster. This is accepted only while `setup_required` is `true`; the
password must be at least 12 characters, and `setup_token` must match the
configured `auth.setupToken` value.

```bash
curl -i -c /tmp/orloj-cookie.jar \
  -X POST "$orloj_url/v1/auth/setup" \
  -H 'Content-Type: application/json' \
  -d "{
    \"username\": \"admin\",
    \"password\": \"admin-password-change-me\",
    \"setup_token\": \"$ORLOJ_SETUP_TOKEN\"
  }"
```

Expected status: `201 Created`.

The setup response creates a session and writes it to `/tmp/orloj-cookie.jar`.
To verify the credentials from a fresh session, log in with the admin username
and password:

```bash
rm -f /tmp/orloj-cookie.jar

curl -i -c /tmp/orloj-cookie.jar \
  -X POST "$orloj_url/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "admin",
    "password": "admin-password-change-me"
  }'
```

Expected status: `200 OK`.

If `/v1/auth/config` returns `"setup_required": false`, the first admin user has
already been configured. Use the password reset flow instead of calling
`/v1/auth/setup` again.

Update the admin password:

```bash
curl -i -b /tmp/orloj-cookie.jar -c /tmp/orloj-cookie.jar \
  -X POST "$orloj_url/v1/auth/change-password" \
  -H 'Content-Type: application/json' \
  -d '{
    "current_password": "replace-with-strong-password",
    "new_password": "replace-with-new-strong-password"
  }'
```

The password update clears existing sessions. Log in again with the new
password before making more admin-only API calls.

To change the admin username later, create a replacement admin user, set its
password, and delete the old user. The API does not rename existing users.

```bash
curl -i \
  -X POST "$orloj_url/v1/auth/users" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "new-admin",
    "role": "admin"
  }'
```

The create-user response includes a generated password once. To set a specific
password for the replacement admin:

```bash
curl -i \
  -X POST "$orloj_url/v1/auth/admin/reset-password" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "new-admin",
    "new_password": "replace-with-new-admin-password"
  }'
```

After confirming the replacement admin can log in, delete the old admin user:

```bash
curl -i \
  -X DELETE "$orloj_url/v1/auth/users/admin" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN"
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
