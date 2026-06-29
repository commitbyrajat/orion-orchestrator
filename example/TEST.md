# Petstore AgentSystem Curl Tests

These scenarios call the `petstore-system` AgentSystem through Orloj's A2A JSON-RPC endpoint.

Prerequisites:

```bash
kubectl apply -f example/01-petstore-foundation.yaml
kubectl apply -f example/02-petstore-agent-system.yaml
```

Environment:

```bash
export ORLOJ_URL="http://orloj.local"
export ORLOJ_API_TOKEN="orloj-api-token-change-me"
export ORLOJ_NAMESPACE="orloj"
export A2A_URL="$ORLOJ_URL/v1/agent-systems/petstore-system/a2a?namespace=$ORLOJ_NAMESPACE"
```

The working create/read/update scenarios use the `petstore-proxy-*` tools exposed by `petstore-system`. Those tools call the in-cluster Petstore proxy and avoid the empty request-body schema exposed by the generated `openapi-to-mcp` `pet-post` and `pet-put` tools.

## 1. Add `stegosaurus`, Make It `available`, Then Query It

Submit:

```bash
export PET_ID="$(date +%s)"
export TASK_ID="petstore-agent-add-stegosaurus-$PET_ID"

curl -s -X POST "$A2A_URL" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$(jq -n \
    --arg id "$TASK_ID" \
    --arg pet_id "$PET_ID" \
    '{
      jsonrpc: "2.0",
      id: "add-stegosaurus",
      method: "tasks/send",
      params: {
        id: $id,
        message: {
          role: "user",
          parts: [
            {
              type: "text",
              text: ("Use petstore-system tools. I confirm you may create this Petstore resource. First call petstore-proxy-pet-create with id " + $pet_id + ", name stegosaurus, status available, photoUrl https://example.com/stegosaurus.jpg. Then call petstore-proxy-pet-get with id " + $pet_id + ". Report the returned id, name, and status.")
            }
          ]
        }
      }
    }')"
```

Poll:

```bash
curl -s -X POST "$A2A_URL" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$(jq -n \
    --arg id "$TASK_ID" \
    '{
      jsonrpc: "2.0",
      id: "get-add-stegosaurus",
      method: "tasks/get",
      params: { id: $id }
    }')" | jq
```

Expected:

```text
state: completed
last_tool_calls: 2
output includes: stegosaurus, available, and the same PET_ID
```

Expected tool trace:

```text
petstore-proxy-pet-create
petstore-proxy-pet-get
```

## 2. List Pets That Are `available`, `pending`, and `sold`

Submit:

```bash
export TASK_ID="petstore-agent-list-statuses-$(date +%s)"

curl -s -X POST "$A2A_URL" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$(jq -n \
    --arg id "$TASK_ID" \
    '{
      jsonrpc: "2.0",
      id: "list-statuses",
      method: "tasks/send",
      params: {
        id: $id,
        message: {
          role: "user",
          parts: [
            {
              type: "text",
              text: "Use petstore-system tools. Call petstore-proxy-pet-find-by-status for available, pending, and sold pets. Summarize a few returned pets for each status."
            }
          ]
        }
      }
    }')"
```

Poll:

```bash
curl -s -X POST "$A2A_URL" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$(jq -n \
    --arg id "$TASK_ID" \
    '{
      jsonrpc: "2.0",
      id: "get-list-statuses",
      method: "tasks/get",
      params: { id: $id }
    }')" | jq
```

Expected:

```text
state: completed
last_tool_calls: 3
output includes sections or summaries for: available, pending, sold
```

Expected tool trace:

```text
petstore-proxy-pet-find-by-status
petstore-proxy-pet-find-by-status
petstore-proxy-pet-find-by-status
```

## 3. Update `stegosaurus` And Mark It As `sold`

Use the same `PET_ID` from scenario 1.

Submit:

```bash
export TASK_ID="petstore-agent-sell-stegosaurus-$PET_ID"

curl -s -X POST "$A2A_URL" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$(jq -n \
    --arg id "$TASK_ID" \
    --arg pet_id "$PET_ID" \
    '{
      jsonrpc: "2.0",
      id: "sell-stegosaurus",
      method: "tasks/send",
      params: {
        id: $id,
        message: {
          role: "user",
          parts: [
            {
              type: "text",
              text: ("Use petstore-system tools. I confirm you may update this Petstore resource. First call petstore-proxy-pet-update with id " + $pet_id + ", name stegosaurus, status sold, photoUrl https://example.com/stegosaurus.jpg. Then call petstore-proxy-pet-get with id " + $pet_id + ". Report the returned id, name, and status.")
            }
          ]
        }
      }
    }')"
```

Poll:

```bash
curl -s -X POST "$A2A_URL" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$(jq -n \
    --arg id "$TASK_ID" \
    '{
      jsonrpc: "2.0",
      id: "get-sell-stegosaurus",
      method: "tasks/get",
      params: { id: $id }
    }')" | jq
```

Expected:

```text
state: completed
last_tool_calls: 2
output includes: stegosaurus, sold, and the same PET_ID
```

Expected tool trace:

```text
petstore-proxy-pet-update
petstore-proxy-pet-get
```

## Inspect Tool Calls

Each `tasks/send` response includes `metadata.orloj.task`. Use that value to inspect the actual tools called:

```bash
export ORLOJ_TASK="<metadata.orloj.task from tasks/send>"

curl -s \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  "$ORLOJ_URL/v1/tasks/$ORLOJ_TASK?namespace=$ORLOJ_NAMESPACE" \
  | jq '[.status.trace[]? | select(.type=="tool_call") | {step, tool, message}]'
```

Validated task examples:

```text
petstore-agent-add-stegosaurus-1782687451       -> 2 tool calls: create, get
petstore-agent-list-statuses-1782687493         -> 3 tool calls: find-by-status x3
petstore-agent-sell-stegosaurus-1782687451      -> 2 tool calls: update, get
```
