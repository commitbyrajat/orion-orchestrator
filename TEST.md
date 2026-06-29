# Petstore Curl Test Scenarios

These scenarios exercise the proxy-backed Swagger Petstore API used by the Petstore MCP bridge.

The proxy is required because `openapi-to-mcp` generates the status path as `/pet/findbystatus`, while Swagger Petstore expects the case-sensitive path `/pet/findByStatus`.

## Setup

Start a port-forward in a separate terminal:

```bash
kubectl -n orloj port-forward svc/petstore-api-proxy 18080:8080
```

In the test terminal:

```bash
export PETSTORE_URL="http://127.0.0.1:18080/v2"
```

Verify the proxy:

```bash
curl -s "$PETSTORE_URL/pet/findbystatus?status=available" | jq '.[0:3]'
```

## 1. Add `stegosaurus`, Make It `available`, Then Query It By ID

Create a unique pet ID:

```bash
export PET_ID="$(date +%s)"
```

Add the pet:

```bash
curl -s -X POST "$PETSTORE_URL/pet" \
  -H "Content-Type: application/json" \
  -d "$(jq -n \
    --argjson id "$PET_ID" \
    '{
      id: $id,
      name: "stegosaurus",
      photoUrls: ["https://example.com/stegosaurus.jpg"],
      tags: [
        {
          id: 1,
          name: "orloj-test"
        }
      ],
      status: "available"
    }')" | jq
```

Query the same pet by ID:

```bash
curl -s "$PETSTORE_URL/pet/$PET_ID" | jq
```

Expected fields:

```text
id: same value as $PET_ID
name: stegosaurus
status: available
```

## 2. List Pets That Are `available`, `pending`, and `sold`

List a few pets for each status:

```bash
for STATUS in available pending sold; do
  echo "status=$STATUS"
  curl -s "$PETSTORE_URL/pet/findbystatus?status=$STATUS" \
    | jq '[.[] | {id, name, status}][0:5]'
done
```

List all three statuses in one Petstore request:

```bash
curl -s "$PETSTORE_URL/pet/findbystatus?status=available,pending,sold" \
  | jq '[.[] | {id, name, status}][0:15]'
```

Expected: returned pets include `status` values from `available`, `pending`, and `sold`.

## 3. Update `stegosaurus` And Mark It As `sold`

Use the same `$PET_ID` from scenario 1.

Update the pet:

```bash
curl -s -X PUT "$PETSTORE_URL/pet" \
  -H "Content-Type: application/json" \
  -d "$(jq -n \
    --argjson id "$PET_ID" \
    '{
      id: $id,
      name: "stegosaurus",
      photoUrls: ["https://example.com/stegosaurus.jpg"],
      tags: [
        {
          id: 1,
          name: "orloj-test"
        }
      ],
      status: "sold"
    }')" | jq
```

Query it again:

```bash
curl -s "$PETSTORE_URL/pet/$PET_ID" | jq
```

Expected fields:

```text
id: same value as $PET_ID
name: stegosaurus
status: sold
```

Confirm it appears in sold pets:

```bash
curl -s "$PETSTORE_URL/pet/findbystatus?status=sold" \
  | jq --argjson id "$PET_ID" '.[] | select(.id == $id) | {id, name, status}'
```
