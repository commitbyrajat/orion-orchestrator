## 1. Apply MCP + Model + Memory first

Create `01-petstore-foundation.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: petstore-openapi-to-mcp
  namespace: orloj
spec:
  replicas: 1
  selector:
    matchLabels:
      app: petstore-openapi-to-mcp
  template:
    metadata:
      labels:
        app: petstore-openapi-to-mcp
    spec:
      containers:
        - name: openapi-to-mcp
          image: evilfreelancer/openapi-to-mcp:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 3100
          env:
            - name: MCP_SERVER_NAME
              value: petstore-mcp
            - name: MCP_PORT
              value: "3100"
            - name: MCP_HOST
              value: "0.0.0.0"
            - name: MCP_OPENAPI_SPEC
              value: "https://petstore.swagger.io/v2/swagger.json"
            - name: MCP_API_BASE_URL
              value: "https://petstore.swagger.io/v2"
            - name: MCP_TOOL_PREFIX
              value: "petstore_"
---
apiVersion: v1
kind: Service
metadata:
  name: petstore-openapi-to-mcp
  namespace: orloj
spec:
  type: ClusterIP
  selector:
    app: petstore-openapi-to-mcp
  ports:
    - name: http
      port: 3100
      targetPort: 3100
---
apiVersion: orloj.dev/v1
kind: Secret
metadata:
  name: openai-api-key
  namespace: orloj
spec:
  stringData:
    value: "YOUR_OPENAI_API_KEY"
---
apiVersion: orloj.dev/v1
kind: ModelEndpoint
metadata:
  name: openai-gpt4o-mini
  namespace: orloj
spec:
  provider: openai
  base_url: "https://api.openai.com/v1"
  model: "gpt-4o-mini"
  api_key:
    secretRef: openai-api-key
  default: true
---
apiVersion: orloj.dev/v1
kind: Memory
metadata:
  name: petstore-memory
  namespace: orloj
spec:
  provider: postgres
  scope: agent-system
  retention:
    ttl: 168h
  config:
    table: petstore_agent_memory
---
apiVersion: orloj.dev/v1
kind: McpServer
metadata:
  name: petstore-mcp
  namespace: orloj
spec:
  transport: http
  endpoint: "http://petstore-openapi-to-mcp.orloj.svc.cluster.local:3100/mcp"
  reconnect:
    max_attempts: 5
    backoff: 2s
```

Apply:

```bash
kubectl apply -f 01-petstore-foundation.yaml
```

Verify MCP discovery:

```bash
kubectl get mcpservers.orloj.dev -n orloj
kubectl get tools.orloj.dev -n orloj
kubectl get memories.orloj.dev -n orloj
```

Get actual generated tool names:

```bash
kubectl get tools.orloj.dev -n orloj | grep petstore-mcp
```

## 2. Create Agent using generated tools

After the previous command shows generated tools, create `02-petstore-agent-system.yaml`.

Replace the tool names below with actual names from your cluster if different:

```yaml
apiVersion: orloj.dev/v1
kind: AgentPolicy
metadata:
  name: petstore-agent-policy
  namespace: orloj
spec:
  allowed_models:
    - openai-gpt4o-mini
  max_steps: 8
  timeout: 90s
---
apiVersion: orloj.dev/v1
kind: Agent
metadata:
  name: petstore-agent
  namespace: orloj
spec:
  model_ref: openai-gpt4o-mini
  memory_ref: petstore-memory
  prompt: |
    You are a Petstore API assistant.

    You can use MCP tools generated from the Swagger Petstore API.
    Use tools for listing pets, finding pets by ID, creating pets, updating pets, and deleting pets.

    For destructive actions like delete, ask for confirmation first.
    Remember useful user preferences in memory when relevant.
  tools:
    - petstore-mcp--petstore_findPetsByStatus
    - petstore-mcp--petstore_getPetById
    - petstore-mcp--petstore_addPet
    - petstore-mcp--petstore_updatePet
    - petstore-mcp--petstore_deletePet
  limits:
    max_steps: 8
    timeout: 90s
---
apiVersion: orloj.dev/v1
kind: AgentSystem
metadata:
  name: petstore-system
  namespace: orloj
spec:
  agents:
    - petstore-agent
  graph:
    petstore-agent:
      edges: []
  memory_ref: petstore-memory
  a2a:
    enabled: true
```

Apply:

```bash
kubectl apply -f 02-petstore-agent-system.yaml
```

## 3. Verify everything

```bash
kubectl get modelendpoints.orloj.dev -n orloj
kubectl get mcpservers.orloj.dev -n orloj
kubectl get tools.orloj.dev -n orloj
kubectl get memories.orloj.dev -n orloj
kubectl get agents.orloj.dev -n orloj
kubectl get agentsystems.orloj.dev -n orloj
```

Check operator logs:

```bash
kubectl logs -n orloj deploy/orloj-operator --tail=100
```

Check server and worker logs:

```bash
kubectl logs -n orloj deploy/orloj-server --tail=100
kubectl logs -n orloj deploy/orloj-worker --tail=100
```

## 4. Test A2A through ingress

```bash
export ORLOJ_URL="http://orloj.local"
export ORLOJ_TOKEN="orloj-api-token-change-me"
```

Agent card:

```bash
curl -s \
  -H "Authorization: Bearer $ORLOJ_TOKEN" \
  "$ORLOJ_URL/v1/agent-systems/petstore-system/.well-known/agent-card.json" | jq
```

Send request:

```bash
curl -s \
  -X POST "$ORLOJ_URL/v1/agent-systems/petstore-system/a2a" \
  -H "Authorization: Bearer $ORLOJ_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "petstore-demo-1",
    "method": "message/send",
    "params": {
      "message": {
        "role": "user",
        "parts": [
          {
            "kind": "text",
            "text": "Use Petstore tools to list available pets."
          }
        ]
      }
    }
  }' | jq
```

If the `Agent`, `Memory`, or `Tool` fields fail schema validation, run this and share the output:

```bash
kubectl explain agent.spec --recursive | sed -n '1,200p'
kubectl explain memory.spec --recursive | sed -n '1,200p'
kubectl explain tool.spec --recursive | sed -n '1,200p'
```

[1]: https://docs.orloj.dev/guides/connect-mcp-server?utm_source=chatgpt.com "Connect an MCP Server - Orloj Docs"
