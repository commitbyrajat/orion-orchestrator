# MSME Overdraft Eligibility Orloj Example

This example deploys the `msme-overdraft-eligibility` Spring Boot API, exposes it through Kubernetes ingress host `overdraft.local`, converts its OpenAPI document to MCP tools, and creates the `orion` AgentSystem.

## Image

The image used by the Kubernetes manifest is:

```text
docker.io/rajat965ng/msme-overdraft-eligibility-example3:0.0.1
```

Build and push:

```bash
cd example3/msme-overdraft-eligibility
mvn test
mvn package -DskipTests
docker build -t docker.io/rajat965ng/msme-overdraft-eligibility-example3:0.0.1 .
docker push docker.io/rajat965ng/msme-overdraft-eligibility-example3:0.0.1
```

## Deploy

```bash
kubectl apply -f example3/01-msme-foundation.yaml
kubectl -n orloj rollout status deploy/msme-overdraft-eligibility-api --timeout=240s
kubectl -n orloj rollout status deploy/msme-openapi-to-mcp --timeout=240s

kubectl apply -f example3/02-orion-agent-system.yaml
kubectl get mcpservers.orloj.dev,memories.orloj.dev,agents.orloj.dev,agentsystems.orloj.dev -n orloj
```

Expected synced resources:

```text
mcpserver.orloj.dev/msme-mcp
memory.orloj.dev/orion-memory
agentsystem.orloj.dev/orion
agent.orloj.dev/kyc-agent
agent.orloj.dev/gst-agent
agent.orloj.dev/account-aggregator-agent
agent.orloj.dev/overdraft-evaluation-agent
```

## Direct API Curls

The ingress resource is configured for `overdraft.local`. If your kind ingress controller is not mapped to host port 80, use a port-forward:

```bash
kubectl -n orloj port-forward svc/msme-overdraft-eligibility-api 18083:8080
export MSME_URL="http://127.0.0.1:18083"
```

If ingress is reachable on your machine:

```bash
export MSME_URL="http://overdraft.local"
```

OpenAPI and Swagger:

```bash
curl -sS "$MSME_URL/v3/api-docs" | jq '.info.title, (.paths | keys)'
curl -sS "$MSME_URL/swagger-ui.html"
```

Fetch KYC, GST, and accounts for an eligible PAN:

```bash
curl -sS "$MSME_URL/api/kyc/pan/ABCDE1234F" | jq
curl -sS "$MSME_URL/api/gst/pan/ABCDE1234F" | jq
curl -sS "$MSME_URL/api/accounts/pan/ABCDE1234F" | jq
```

Evaluate an eligible PAN:

```bash
curl -sS "$MSME_URL/api/overdraft/evaluations?kycId=1&gstNumber=27ABCDE1234F1Z5&accountIds=1&accountIds=2" | jq
```

Evaluate a not-eligible PAN:

```bash
curl -sS "$MSME_URL/api/kyc/pan/LOWTR1234A" | jq
curl -sS "$MSME_URL/api/gst/pan/LOWTR1234A" | jq
curl -sS "$MSME_URL/api/accounts/pan/LOWTR1234A" | jq
curl -sS "$MSME_URL/api/overdraft/evaluations?kycId=3&gstNumber=07LOWTR1234A1Z7&accountIds=5" | jq
```

Evaluate a PAN with missing GST:

```bash
curl -sS "$MSME_URL/api/kyc/pan/NOGST1234E" | jq
curl -sS "$MSME_URL/api/overdraft/evaluations?kycId=6&gstNumber=29NOGST1234E1Z5&accountIds=8" | jq
```

## MCP And Orion

The `msme-openapi-to-mcp` adapter registers these tools:

```text
msme-mcp--msme-api-kyc-pan-pan
msme-mcp--msme-api-gst-pan-pan
msme-mcp--msme-api-accounts-pan-pan
msme-mcp--msme-api-overdraft-evaluations
msme-mcp--msme-api-overdraft-evaluate
```

Start an Orloj API port-forward:

```bash
kubectl -n orloj port-forward svc/orloj-server 18080:8080
export ORLOJ_URL="http://127.0.0.1:18080"
export ORLOJ_API_TOKEN="orloj-api-token-change-me"
```

Confirm Orion skills:

```bash
curl -sS \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  "$ORLOJ_URL/v1/agent-systems/orion/.well-known/agent-card.json?namespace=orloj" | jq
```

## Orion A2A Tasks

Eligible PAN task:

```bash
curl -sS -X POST "$ORLOJ_URL/v1/agent-systems/orion/a2a?namespace=orloj" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc":"2.0",
    "id":"1",
    "method":"tasks/send",
    "params":{
      "id":"orion-eligible-pan",
      "message":{
        "role":"user",
        "parts":[
          {
            "type":"text",
            "text":"Use Orion agents and MSME tools to evaluate overdraft eligibility for PAN ABCDE1234F. Fetch KYC, GST, accounts, then evaluate overdraft. Return the final eligibility decision, score, maximum eligible amount, and reasons."
          }
        ]
      }
    }
  }' | jq
```

Not-eligible PAN task:

```bash
curl -sS -X POST "$ORLOJ_URL/v1/agent-systems/orion/a2a?namespace=orloj" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc":"2.0",
    "id":"2",
    "method":"tasks/send",
    "params":{
      "id":"orion-not-eligible-pan",
      "message":{
        "role":"user",
        "parts":[
          {
            "type":"text",
            "text":"Use Orion agents and MSME tools to evaluate overdraft eligibility for PAN LOWTR1234A. Fetch KYC, GST, accounts, then evaluate overdraft. Return the final eligibility decision, score, maximum eligible amount, and reasons."
          }
        ]
      }
    }
  }' | jq
```

Memory task:

```bash
curl -sS -X POST "$ORLOJ_URL/v1/agent-systems/orion/a2a?namespace=orloj" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc":"2.0",
    "id":"3",
    "method":"tasks/send",
    "params":{
      "id":"orion-memory-pan",
      "message":{
        "role":"user",
        "parts":[
          {
            "type":"text",
            "text":"Use memory for this request. Remember that applicant nickname strong-msme maps to PAN PQRST6789L, then use Orion agents and MSME tools to evaluate overdraft eligibility for strong-msme. Return whether memory was used plus the eligibility decision, score, maximum eligible amount, and reasons."
          }
        ]
      }
    }
  }' | jq
```

Fetch task results:

```bash
curl -sS -X POST "$ORLOJ_URL/v1/agent-systems/orion/a2a?namespace=orloj" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"get-1","method":"tasks/get","params":{"id":"orion-eligible-pan"}}' | jq

curl -sS -X POST "$ORLOJ_URL/v1/agent-systems/orion/a2a?namespace=orloj" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"get-2","method":"tasks/get","params":{"id":"orion-not-eligible-pan"}}' | jq

curl -sS -X POST "$ORLOJ_URL/v1/agent-systems/orion/a2a?namespace=orloj" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"get-3","method":"tasks/get","params":{"id":"orion-memory-pan"}}' | jq
```

The A2A task submit curls were accepted by Orloj. In this cluster run, model execution did not complete because the worker could not connect to OpenAI:

```text
Post "https://api.openai.com/v1/chat/completions": dial tcp ...:443: connect: connection refused
```

Enable cluster egress to OpenAI or configure a reachable model endpoint, then rerun the same three task curls.
