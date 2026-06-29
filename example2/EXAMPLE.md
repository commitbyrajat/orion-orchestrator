# Wealth Management Spring Boot Orloj Example

This example creates a Maven/Spring Boot mutual fund API backed by in-memory HSQLDB, publishes Swagger/OpenAPI with springdoc, converts that OpenAPI document to MCP tools, and exposes those tools through the `wealth-advisor-system` AgentSystem.

## Build And Image

The project was generated with Maven archetype:

```bash
cd example2
mvn -B archetype:generate \
  -DgroupId=com.example.wealth \
  -DartifactId=wealth-management \
  -Dversion=0.0.1-SNAPSHOT \
  -Dpackage=com.example.wealth \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DarchetypeVersion=1.5
```

Build and push the application image:

```bash
cd example2/wealth-management
mvn test
mvn package
docker build -t docker.io/rajat965ng/wealth-management-example2:0.0.2 .
docker push docker.io/rajat965ng/wealth-management-example2:0.0.2
```

## Deploy

```bash
kubectl apply -f example2/01-wealth-foundation.yaml
kubectl -n orloj rollout status deploy/wealth-management-api --timeout=180s
kubectl -n orloj rollout status deploy/wealth-openapi-to-mcp --timeout=180s

kubectl apply -f example2/02-wealth-agent-system.yaml
kubectl get mcpservers.orloj.dev,agents.orloj.dev,agentsystems.orloj.dev,memories.orloj.dev -n orloj
```

## Direct API Curl Commands

Port-forward the Spring Boot service:

```bash
kubectl -n orloj port-forward svc/wealth-management-api 18081:8080
export WEALTH_URL="http://127.0.0.1:18081"
```

Check health and OpenAPI:

```bash
curl -s "$WEALTH_URL/actuator/health" | jq
curl -s "$WEALTH_URL/v3/api-docs" | jq '.info'
curl -s "$WEALTH_URL/swagger-ui.html"
```

List all funds:

```bash
curl -s "$WEALTH_URL/api/funds" | jq
```

List moderate-risk funds sorted by three-year return:

```bash
curl -s "$WEALTH_URL/api/funds?riskLevel=Moderate&sortBy=return" | jq
```

List debt funds with minimum investment up to 1000:

```bash
curl -s "$WEALTH_URL/api/funds?category=Debt&maxMinimumInvestment=1000" | jq
```

Get a fund by ID:

```bash
curl -s "$WEALTH_URL/api/funds/1" | jq
```

Search funds:

```bash
curl -s "$WEALTH_URL/api/funds/search?query=index" | jq
```

Get holdings and a portfolio summary:

```bash
curl -s "$WEALTH_URL/api/investors/INV-1001/holdings" | jq
curl -s "$WEALTH_URL/api/investors/INV-1001/summary" | jq
```

Simulate an investment:

```bash
curl -s -X POST "$WEALTH_URL/api/investments/simulate" \
  -H "Content-Type: application/json" \
  -d '{"investorId":"INV-9001","fundId":1,"amount":25000}' | jq
```

Simulate an investment through the query-parameter endpoint used by the MCP tool adapter:

```bash
curl -s "$WEALTH_URL/api/investments/simulations?investorId=INV-9001&fundId=1&amount=25000" | jq
```

## Orloj AgentSystem Curl Commands

```bash
export ORLOJ_URL="http://orloj.local"
export ORLOJ_API_TOKEN="orloj-api-token-change-me"
```

Get the generated Agent Card and confirm wealth skills are visible:

```bash
curl -s \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  "$ORLOJ_URL/v1/agent-systems/wealth-advisor-system/.well-known/agent-card.json?namespace=orloj" | jq
```

Ask the agent to list moderate-risk mutual funds:

```bash
curl -s -X POST \
  "$ORLOJ_URL/v1/agent-systems/wealth-advisor-system/a2a?namespace=orloj" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc":"2.0",
    "id":"1",
    "method":"tasks/send",
    "params":{
      "id":"wealth-task-1",
      "message":{
        "role":"user",
        "parts":[
          {
            "type":"text",
            "text":"Use wealth tools to list moderate risk mutual funds sorted by return."
          }
        ]
      }
    }
  }' | jq
```

Fetch the task:

```bash
curl -s -X POST \
  "$ORLOJ_URL/v1/agent-systems/wealth-advisor-system/a2a?namespace=orloj" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc":"2.0",
    "id":"2",
    "method":"tasks/get",
    "params":{
      "id":"wealth-task-1"
    }
  }' | jq
```

Ask for an investor summary and investment simulation:

```bash
curl -s -X POST \
  "$ORLOJ_URL/v1/agent-systems/wealth-advisor-system/a2a?namespace=orloj" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc":"2.0",
    "id":"3",
    "method":"tasks/send",
    "params":{
      "id":"wealth-task-2",
      "message":{
        "role":"user",
        "parts":[
          {
            "type":"text",
            "text":"Use wealth tools to summarize investor INV-1001 holdings and simulate investing 25000 in fund 1."
          }
        ]
      }
    }
  }' | jq
```

Fetch the investor summary and simulation task:

```bash
curl -s -X POST \
  "$ORLOJ_URL/v1/agent-systems/wealth-advisor-system/a2a?namespace=orloj" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc":"2.0",
    "id":"4",
    "method":"tasks/get",
    "params":{
      "id":"wealth-task-2"
    }
  }' | jq
```

## Multi-Tool Wealth Planning Scenario

This scenario asks the agent to combine portfolio analysis, fund discovery, fund details, and investment simulation. It should require multiple MCP tools, for example:

- `wealth-mcp--wealth-api-investors-investorid-summary`
- `wealth-mcp--wealth-api-investors-investorid-holdings`
- `wealth-mcp--wealth-api-funds-search`
- `wealth-mcp--wealth-api-funds-id`
- `wealth-mcp--wealth-api-investments-simulations`

Send the multi-tool task:

```bash
curl -s -X POST \
  "$ORLOJ_URL/v1/agent-systems/wealth-advisor-system/a2a?namespace=orloj" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc":"2.0",
    "id":"5",
    "method":"tasks/send",
    "params":{
      "id":"wealth-task-multitool-1",
      "message":{
        "role":"user",
        "parts":[
          {
            "type":"text",
            "text":"Use wealth tools to create a compact review for investor INV-1001. First get the investor portfolio summary, then get the holdings, then search for index funds, then fetch details for fund 4, then simulate investing 15000 in fund 4 for INV-1001. Compare the current portfolio with the simulated index fund investment and mention that the data is dummy demo data."
          }
        ]
      }
    }
  }' | jq
```

Fetch the multi-tool task:

```bash
curl -s -X POST \
  "$ORLOJ_URL/v1/agent-systems/wealth-advisor-system/a2a?namespace=orloj" \
  -H "Authorization: Bearer $ORLOJ_API_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc":"2.0",
    "id":"6",
    "method":"tasks/get",
    "params":{
      "id":"wealth-task-multitool-1"
    }
  }' | jq
```

A successful run should complete with more than one tool call in the output metadata, such as `last_tool_calls` greater than `1`.
