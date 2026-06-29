
# Orloj Petstore MCP Agent Demo

This example deploys a Swagger Petstore MCP server using `openapi-to-mcp`, registers it with Orloj using CRDs, and exposes an Agent System through the Orloj A2A API.

## Files

```text
01-petstore-foundation.yaml
02-petstore-agent-system.yaml
```

## Prerequisites

- Orloj installed
- External PostgreSQL configured
- External NATS configured
- Ingress available at `http://orloj.local`
- Native authentication enabled
- OpenAI API key available

Verify:

```bash
kubectl get pods -n orloj
kubectl get crds | grep -i orloj
```

---

# 1. Configure OpenAI

Replace the placeholder OpenAI API key in `01-petstore-foundation.yaml`.

The ModelEndpoint should use:

```yaml
provider: openai
base_url: https://api.openai.com/v1
default_model: gpt-4o-mini
```

---

# 2. Deploy foundation

```bash
kubectl apply -f 01-petstore-foundation.yaml
```

This creates:

- Petstore API proxy Deployment
- Petstore API proxy Service
- Petstore MCP Deployment
- Petstore MCP Service
- OpenAI Secret
- ModelEndpoint
- Memory
- McpServer

The MCP bridge calls Swagger Petstore directly:

```yaml
MCP_OPENAPI_SPEC: https://petstore.swagger.io/v2/swagger.json
MCP_API_BASE_URL: http://petstore-api-proxy.orloj.svc.cluster.local:8080/v2
MCP_TOOL_PREFIX: petstore_
```

The proxy forwards requests to `https://petstore.swagger.io` and rewrites the generated lowercase status path `/pet/findbystatus` to Swagger Petstore's case-sensitive `/pet/findByStatus`.

Verify:

```bash
kubectl get deploy -n orloj | grep petstore
kubectl get svc -n orloj | grep petstore
kubectl get modelendpoints.orloj.dev -n orloj
kubectl get memories.orloj.dev -n orloj
kubectl get mcpservers.orloj.dev -n orloj
```

Check MCP logs:

```bash
kubectl logs -n orloj deploy/petstore-openapi-to-mcp --tail=100
```

Expected:

```text
Registered 20 tool(s)
MCP server listening on http://0.0.0.0:3100
```

---

# 3. Deploy Agent

```bash
kubectl apply -f 02-petstore-agent-system.yaml
```

Verify:

```bash
kubectl get agents.orloj.dev -n orloj
kubectl get agentsystems.orloj.dev -n orloj
kubectl get agentpolicies.orloj.dev -n orloj
```

---

# 4. Allow private Kubernetes services

```bash
kubectl patch mcpserver petstore-mcp -n orloj --type merge -p '{
  "spec":{
    "allowPrivate": true
  }
}'
```

Verify:

```bash
kubectl get mcpserver petstore-mcp -n orloj -o yaml | grep allowPrivate
```

---

# 5. Restart Orloj

```bash
kubectl rollout restart deployment/orloj-server -n orloj
kubectl rollout restart deployment/orloj-worker -n orloj

kubectl rollout status deployment/orloj-server -n orloj
```

---

# 6. Verify MCP discovery

```bash
kubectl logs -n orloj deploy/orloj-server --tail=100 | grep -i -E "mcp|tool|petstore|error"
```

```bash
kubectl get tools.orloj.dev -n orloj
```

If tools are generated they should resemble:

```text
petstore-mcp--petstore-pet-findbystatus
petstore-mcp--petstore-pet-petid-get
petstore-mcp--petstore-pet-post
```

---

# 7. Environment variables

```bash
export ORLOJ_URL="http://orloj.local"
export ORLOJ_API_TOKEN="orloj-api-token-change-me"
export SETUP_TOKEN="orloj-setup-token-change-me"
```

---

# 8. Bootstrap admin (first run only)

```bash
curl -X POST   "$ORLOJ_URL/v1/auth/setup"   -H "Content-Type: application/json"   -d '{
    "username":"admin",
    "password":"MyStrongPass@2026",
    "setup_token":"'"$SETUP_TOKEN"'"
  }'
```

Login:

```bash
curl -c orloj-cookies.txt   -X POST   "$ORLOJ_URL/v1/auth/login"   -H "Content-Type: application/json"   -d '{
    "username":"admin",
    "password":"MyStrongPass@2026"
  }'
```

---

# 9. Verify Agent Card

```bash
curl -s   -H "Authorization: Bearer $ORLOJ_API_TOKEN"   "$ORLOJ_URL/v1/agent-systems/petstore-system/.well-known/agent-card.json?namespace=orloj" | jq
```

---

# 10. Find available pets

```bash
curl -s   -X POST   "$ORLOJ_URL/v1/agent-systems/petstore-system/a2a?namespace=orloj"   -H "Authorization: Bearer $ORLOJ_API_TOKEN"   -H "Content-Type: application/json"   -d '{
    "jsonrpc":"2.0",
    "id":"1",
    "method":"tasks/send",
    "params":{
      "id":"petstore-task-1",
      "message":{
        "role":"user",
        "parts":[
          {
            "type":"text",
            "text":"Use Petstore tools to find pets having status available."
          }
        ]
      }
    }
  }'
```

Expected:

```json
{
  "result": {
    "status": {
      "state": "submitted"
    }
  }
}
```

---

# 11. Poll available pets task

```bash
curl -s   -X POST   "$ORLOJ_URL/v1/agent-systems/petstore-system/a2a?namespace=orloj"   -H "Authorization: Bearer $ORLOJ_API_TOKEN"   -H "Content-Type: application/json"   -d '{
    "jsonrpc":"2.0",
    "id":"2",
    "method":"tasks/get",
    "params":{
      "id":"petstore-task-1"
    }
  }' | jq
```

Expected states:

```text
submitted
working
completed
```

# 12. Add a new pet, then find it by ID

This sends one natural-language task to the agent. The agent can use the full Petstore tool set, so it should create the pet with `petstore-mcp--petstore-pet-post` and then fetch the same pet with `petstore-mcp--petstore-pet-petid-get`.

Choose an ID that is unlikely to collide:

```bash
export PET_ID="$(date +%s)"
```

Submit the task:

```bash
curl -s   -X POST   "$ORLOJ_URL/v1/agent-systems/petstore-system/a2a?namespace=orloj"   -H "Authorization: Bearer $ORLOJ_API_TOKEN"   -H "Content-Type: application/json"   -d '{
    "jsonrpc":"2.0",
    "id":"3",
    "method":"tasks/send",
    "params":{
      "id":"petstore-add-find-'"$PET_ID"'",
      "message":{
        "role":"user",
        "parts":[
          {
            "type":"text",
            "text":"Use Petstore tools to add a new pet with id '"$PET_ID"', name Orloj Demo Pet, status available, and photoUrls [\"https://example.com/orloj-demo-pet.jpg\"]. After creating it, find the same pet by id '"$PET_ID"' and report the returned id, name, and status."
          }
        ]
      }
    }
  }'
```

Poll the task:

```bash
curl -s   -X POST   "$ORLOJ_URL/v1/agent-systems/petstore-system/a2a?namespace=orloj"   -H "Authorization: Bearer $ORLOJ_API_TOKEN"   -H "Content-Type: application/json"   -d '{
    "jsonrpc":"2.0",
    "id":"4",
    "method":"tasks/get",
    "params":{
      "id":"petstore-add-find-'"$PET_ID"'"
    }
  }' | jq
```

Expected result:

```text
completed
last_tool_calls is at least 2
```

---

# Troubleshooting

## No Tool CRDs

```bash
kubectl get tools.orloj.dev -n orloj
```

If empty, inspect:

```bash
kubectl logs -n orloj deploy/orloj-server --tail=200 | grep -i -E "mcp|tool|petstore|error"
```

## Admin setup required

Run the bootstrap step above.

## Missing credentials

Use:

```bash
-H "Authorization: Bearer $ORLOJ_API_TOKEN"
```

## Unknown method: message/send

Use:

```text
tasks/send
```

instead of

```text
message/send
```

## HTTP 406

If you see:

```text
Client must accept both application/json and text/event-stream
```

this indicates an incompatibility between the current Orloj MCP HTTP client and the deployed MCP server. No proxy workaround is included in this example; resolve the MCP protocol compatibility before expecting Tool CRDs to be generated.
