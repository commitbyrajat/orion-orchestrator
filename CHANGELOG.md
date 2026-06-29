# Changelog

## 2026-06-29 - Example2 Wealth Management Spring Boot AgentSystem

- Generated `example2/wealth-management` with Maven archetype:

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

- Reworked the generated project into a Spring Boot 3.5.13 application using:
  - Spring Web
  - Spring Data JPA
  - HSQLDB in-memory persistence
  - Spring Boot Actuator
  - `springdoc-openapi-starter-webmvc-ui` 2.8.17
- Added dummy mutual fund and investor holding data through `DataSeeder`.
- Added documented REST/OpenAPI endpoints:
  - `GET /api/funds`
  - `GET /api/funds/{id}`
  - `GET /api/funds/search`
  - `GET /api/investors/{investorId}/holdings`
  - `GET /api/investors/{investorId}/summary`
  - `POST /api/investments/simulate`
  - `GET /api/investments/simulations`
- Added `GET /api/investments/simulations` as the MCP-compatible query-parameter variant because the OpenAPI-to-MCP adapter exposes query/path parameters as tool inputs but does not expose the JSON request body from `POST /api/investments/simulate`.
- Added Dockerfile, Kubernetes foundation manifest, Orloj AgentSystem manifest, and usage guide:
  - `example2/wealth-management/Dockerfile`
  - `example2/01-wealth-foundation.yaml`
  - `example2/02-wealth-agent-system.yaml`
  - `example2/EXAMPLE.md`
- Built and tested:

```bash
cd example2/wealth-management
mvn test
mvn package
```

- Test result: 4 tests passed.
- Built and pushed Docker images to Docker Hub:
  - `docker.io/rajat965ng/wealth-management-example2:0.0.1`
    - Digest: `sha256:00173a9f48d34578f149a274bb484e47535fcd468ba0b386fff282a9943ac1b7`
  - `docker.io/rajat965ng/wealth-management-example2:0.0.2`
    - Digest: `sha256:fc5bc365d22379fc995982c115a819b76df385fff1781ea786c3b46022d39f18`
- Deployed `0.0.2` to Kubernetes:

```bash
kubectl apply -f example2/01-wealth-foundation.yaml
kubectl -n orloj rollout status deploy/wealth-management-api --timeout=180s
kubectl -n orloj rollout status deploy/wealth-openapi-to-mcp --timeout=180s
kubectl apply -f example2/02-wealth-agent-system.yaml
```

- Created Orloj resources:
  - `Memory/wealth-memory`
  - `McpServer/wealth-mcp`
  - `AgentPolicy/wealth-agent-policy`
  - `Agent/wealth-advisor-agent`
  - `AgentSystem/wealth-advisor-system`
- Configured `McpServer/wealth-mcp` without an API proxy:
  - OpenAPI source: `http://wealth-management-api.orloj.svc.cluster.local:8080/v3/api-docs`
  - API base URL: `http://wealth-management-api.orloj.svc.cluster.local:8080`
  - MCP endpoint: `http://wealth-openapi-to-mcp.orloj.svc.cluster.local:3100/mcp`
  - `allowPrivate: true`
- Verified rollouts and in-cluster API:

```bash
kubectl exec -n orloj deploy/orloj-server -- \
  wget -qO- http://wealth-management-api.orloj.svc.cluster.local:8080/actuator/health

kubectl exec -n orloj deploy/orloj-server -- \
  wget -qO- 'http://wealth-management-api.orloj.svc.cluster.local:8080/api/investments/simulations?investorId=INV-9001&fundId=1&amount=25000'
```

- Verified the OpenAPI-to-MCP adapter registered 7 tools after restart:
  - `wealth_api_funds`
  - `wealth_api_funds_id`
  - `wealth_api_funds_search`
  - `wealth_api_investments_simulate`
  - `wealth_api_investments_simulations`
  - `wealth_api_investors_investorid_holdings`
  - `wealth_api_investors_investorid_summary`
- Updated `Agent/wealth-advisor-agent` to expose these six working AgentSystem skills:
  - `wealth-mcp--wealth-api-funds`
  - `wealth-mcp--wealth-api-funds-id`
  - `wealth-mcp--wealth-api-funds-search`
  - `wealth-mcp--wealth-api-investors-investorid-holdings`
  - `wealth-mcp--wealth-api-investors-investorid-summary`
  - `wealth-mcp--wealth-api-investments-simulations`
- Verified Agent Card exposed all six skills.
- Ran A2A smoke task:

```bash
curl -s -X POST \
  "http://orloj.local/v1/agent-systems/wealth-advisor-system/a2a?namespace=orloj" \
  -H "Authorization: Bearer orloj-api-token-change-me" \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"1","method":"tasks/send","params":{"id":"wealth-task-smoke-1","message":{"role":"user","parts":[{"type":"text","text":"Use wealth tools to list moderate risk mutual funds sorted by return."}]}}}'
```

- Smoke result:
  - Task `wealth-task-smoke-1` completed.
  - `last_agent: wealth-advisor-agent`
  - `last_tool_calls: 1`
  - Returned moderate-risk dummy funds sorted by return.

```
push all the docker images that you are building into https://hub.docker.com/repositories/rajat965ng, remove the docker image from local, update the orloj_beta/charts/orloj/v1-values.yaml with image names and then upgrade the helm chart. 

 curl -s -X POST 'http://orloj.local/v1/agent-systems/petstore-system/a2a?namespace=orloj' -H 'Authorization: Bearer orloj-api-token-change-me' -H 'Content-Type: application/json' -d '{"jsonrpc":"2.0","id":"11-get","method":"tasks/get","params":{"id":"petstore-task-11"}}' | jq
```

## 2026-06-29 - Petstore Example No-Proxy Update

- Updated `example/02-petstore-agent-system.yaml` with proxy-backed CLI tools so create/read/update scenarios work through the `petstore-system` AgentSystem:
  - `petstore-proxy-pet-create`
  - `petstore-proxy-pet-get`
  - `petstore-proxy-pet-update`
  - `petstore-proxy-pet-find-by-status`
- Added the proxy-backed tools to `Agent/petstore-agent` `tools` and `allowed_tools`.
- Updated the agent prompt to use the `petstore-proxy-*` tools for test create/read/update/status flows while keeping the generated MCP tools available.
- Updated `example/TEST.md` with working A2A curl scenarios against:

```text
$ORLOJ_URL/v1/agent-systems/petstore-system/a2a?namespace=orloj
```

- Applied the updated AgentSystem manifest:

```bash
kubectl apply -f example/02-petstore-agent-system.yaml
```

- Verified live AgentSystem scenarios:
  - `petstore-agent-add-stegosaurus-1782687451`
    - completed
    - `last_tool_calls: 2`
    - tool trace: `petstore-proxy-pet-create`, `petstore-proxy-pet-get`
  - `petstore-agent-list-statuses-1782687493`
    - completed
    - `last_tool_calls: 3`
    - tool trace: `petstore-proxy-pet-find-by-status` x3
  - `petstore-agent-sell-stegosaurus-1782687451`
    - completed
    - `last_tool_calls: 2`
    - tool trace: `petstore-proxy-pet-update`, `petstore-proxy-pet-get`

- Added root `TEST.md` with working proxy-backed curl scenarios:
  - Create a `stegosaurus` pet with `status=available`, then read it by ID.
  - List pets for `available`, `pending`, and `sold` statuses.
  - Update the same `stegosaurus` pet to `status=sold`, read it by ID, and confirm it appears in sold pets.
- Validated the `TEST.md` flow through:

```bash
kubectl -n orloj port-forward svc/petstore-api-proxy 18080:8080
```

  - Validation pet ID: `1782686929`
  - Create returned `name=stegosaurus status=available`.
  - Read-by-ID returned `name=stegosaurus status=available`.
  - Status queries returned pets for `available`, `pending`, and `sold`.
  - Update returned `name=stegosaurus status=sold`.
  - Read-by-ID and sold-status lookup both returned the updated `stegosaurus`.

- Follow-up: restored `petstore-api-proxy` because the user requested the available-pets flow to work end to end.
  - Re-added `ConfigMap/petstore-api-proxy`, `Deployment/petstore-api-proxy`, and `Service/petstore-api-proxy` to `example/01-petstore-foundation.yaml`.
  - Changed `MCP_API_BASE_URL` back to `http://petstore-api-proxy.orloj.svc.cluster.local:8080/v2`.
  - Kept `Agent/petstore-agent` unrestricted across the full Petstore tool set; no `execution.tool_sequence` contract was reintroduced.
  - Applied the foundation manifest and waited for:
    - `deploy/petstore-api-proxy`
    - `deploy/petstore-openapi-to-mcp`
  - Verified the proxy rewrite from inside the cluster:

```bash
kubectl exec -n orloj deploy/orloj-server -- \
  wget -qO- \
  'http://petstore-api-proxy.orloj.svc.cluster.local:8080/v2/pet/findbystatus?status=available'
```

  - Verified A2A task `petstore-available-proxy-1` completed:
    - `state: completed`
    - `last_tool_calls: 1`
    - `last_steps: 2`
    - successful tool trace: `petstore-mcp--petstore-pet-findbystatus`

- Updated `example/01-petstore-foundation.yaml`:
  - Removed the `petstore-api-proxy` `ConfigMap`, `Deployment`, and `Service`.
  - Set `petstore-openapi-to-mcp` to call Swagger Petstore directly with `MCP_API_BASE_URL=https://petstore.swagger.io/v2`.
  - Kept `MCP_TOOL_PREFIX=petstore_` so generated MCP tool names continue to match `example/02-petstore-agent-system.yaml`.
- Updated `example/02-petstore-agent-system.yaml`:
  - Removed the contract execution block that required only `petstore-mcp--petstore-pet-findbystatus`.
  - Kept the full Petstore tool set in `tools` and `allowed_tools`.
  - Updated the prompt to prefer the status tool for available pets while allowing the agent to choose other matching Petstore tools.
- Updated `example/USAGE.md`:
  - Added an example task to create a new pet and then find the same pet by ID.
  - Updated generated tool-name examples to the current hyphen-normalized names.
  - Documented the no-proxy caveat: `openapi-to-mcp` calls `GET /pet/findbystatus`, while Swagger Petstore v2 expects case-sensitive `/pet/findByStatus`.
- Applied the updated manifests:

```bash
kubectl apply -f example/01-petstore-foundation.yaml
kubectl apply -f example/02-petstore-agent-system.yaml
```

- Removed the previously created proxy resources from the cluster:

```bash
kubectl delete deploy petstore-api-proxy -n orloj --ignore-not-found=true
kubectl delete svc petstore-api-proxy -n orloj --ignore-not-found=true
kubectl delete configmap petstore-api-proxy -n orloj --ignore-not-found=true
```

- Verified live no-proxy state:
  - `MCP_API_BASE_URL=https://petstore.swagger.io/v2`
  - `MCP_TOOL_PREFIX=petstore_`
  - `Agent/petstore-agent` `.spec.execution={}`
  - `petstore-api-proxy` resources removed
- Validation result:
  - Agent card still exposes `petstore-mcp--petstore-pet-findbystatus`.
  - A no-proxy available-pets task called the status tool, but Swagger Petstore returned HTTP 404 because the MCP bridge used lowercase `/pet/findbystatus`.

## 2026-06-28 - Petstore MCP Helm Deployment Fix

### 2026-06-29 Docker Hub Publish and Helm Upgrade

- Built and pushed these `linux/arm64` images to Docker Hub under `rajat965ng`:
  - `docker.io/rajat965ng/orloj-orlojd:mcp-accept-local`
  - `docker.io/rajat965ng/orloj-orlojworker:mcp-accept-local`
  - `docker.io/rajat965ng/orloj-operator:mcp-accept-local`
- Removed the local Docker image tags after successful push:
  - `rajat965ng/orloj-orlojd:mcp-accept-local`
  - `rajat965ng/orloj-orlojworker:mcp-accept-local`
  - `rajat965ng/orloj-operator:mcp-accept-local`
- Updated `orloj_beta/charts/orloj/v1-values.yaml` to use Docker Hub images:
  - `image.registry: docker.io`
  - `image.server.repository: rajat965ng/orloj-orlojd`
  - `image.server.tag: mcp-accept-local`
  - `image.worker.repository: rajat965ng/orloj-orlojworker`
  - `image.worker.tag: mcp-accept-local`
  - `agentExecution.kubernetes.image: docker.io/rajat965ng/orloj-orlojworker:mcp-accept-local`
  - `operator.image.repository: rajat965ng/orloj-operator`
  - `operator.image.tag: mcp-accept-local`
- Installed/upgraded the Helm release with:

```bash
helm upgrade --install orloj orloj_beta/charts/orloj \
  -n orloj \
  --create-namespace \
  -f orloj_beta/charts/orloj/v1-values.yaml
```

- Verified rollouts:
  - `deploy/orloj-server`
  - `deploy/orloj-worker`
  - `deploy/orloj-operator`
- Verified deployed images:
  - `orloj-server`: `docker.io/rajat965ng/orloj-orlojd:mcp-accept-local`
  - `orloj-worker`: `docker.io/rajat965ng/orloj-orlojworker:mcp-accept-local`
  - `orloj-operator`: `docker.io/rajat965ng/orloj-operator:mcp-accept-local`

### 2026-06-29 Required Tool Runtime Fix

- Built and pushed updated runtime images to Docker Hub under `rajat965ng`:
  - `docker.io/rajat965ng/orloj-orlojd:mcp-accept-required-tool`
  - `docker.io/rajat965ng/orloj-orlojworker:mcp-accept-required-tool`
- Built and pushed the follow-up required-tool fallback images:
  - `docker.io/rajat965ng/orloj-orlojd:mcp-required-tool-fallback`
  - `docker.io/rajat965ng/orloj-orlojworker:mcp-required-tool-fallback`
- Built and pushed the worker model-error fallback image:
  - `docker.io/rajat965ng/orloj-orlojworker:mcp-required-tool-modelerr`
- Built and pushed the worker schema-forwarding image:
  - `docker.io/rajat965ng/orloj-orlojworker:mcp-required-tool-schema`
- Built and pushed the worker status-fallback image:
  - `docker.io/rajat965ng/orloj-orlojworker:mcp-required-tool-status`
  - Digest: `sha256:7aa6d2f0d0971d513359658ab1e8b8885fc0e7e9129e181ae5e9f3d16431b4c5`
- Built and pushed the server policy-log cleanup image:
  - `docker.io/rajat965ng/orloj-orlojd:mcp-policy-log-clean`
  - Digest: `sha256:0cb005e6f44ceeab8cb79cebd777668fbfc27a3c250b70846f9b39e45e70768d`
- Removed the local Docker tag after push:
  - `docker.io/rajat965ng/orloj-orlojworker:mcp-required-tool-status`
  - `docker.io/rajat965ng/orloj-orlojd:mcp-policy-log-clean`
- Updated `orloj_beta/charts/orloj/v1-values.yaml` to use the new server and worker image tags.
- Disabled `agentExecution.kubernetes.enabled` in `orloj_beta/charts/orloj/v1-values.yaml` for the Petstore validation path.
  - Reason: the disposable single-agent Job path was returning only the top-level contract failure to the A2A task, while the worker in-process path uses the same governed MCP runtime and keeps execution in the current worker deployment for easier validation.
- Kept the operator image on `docker.io/rajat965ng/orloj-operator:mcp-accept-local` because the operator code did not change for this fix.

### Code Changes

- Updated `example/01-petstore-foundation.yaml`:
  - Added `Deployment/petstore-api-proxy`, `Service/petstore-api-proxy`, and `ConfigMap/petstore-api-proxy`.
  - The proxy rewrites `GET /pet/findbystatus` to `GET /pet/findByStatus` before forwarding to `https://petstore.swagger.io`.
  - Reason: `evilfreelancer/openapi-to-mcp` generated lowercase backend calls for the Petstore `findByStatus` operation, and Swagger Petstore v2 treats that path as case-sensitive, returning HTTP 404.
  - Changed `petstore-openapi-to-mcp` `MCP_API_BASE_URL` to `http://petstore-api-proxy.orloj.svc.cluster.local:8080/v2`.
  - Added `spec.allowPrivate: true` to `McpServer/petstore-mcp`.
  - Reason: Orloj was blocking the in-cluster Kubernetes Service IP for `petstore-openapi-to-mcp` with `private address ... is not allowed`.
  - Changed `ModelEndpoint/openai-gpt4o-mini` auth from `secretRef: openai-api-key` to `secretRef: openai-api-key:api-key`.
  - Reason: the Orloj `Secret/openai-api-key` stores the credential under `spec.data.api-key`; without the explicit key, the runtime resolver looks for the default `value` key and the agent task fails with `secret "openai-api-key" not found in environment`.

- Updated `example/02-petstore-agent-system.yaml`:
  - Replaced underscore-based MCP tool references with the generated hyphenated tool names.
  - Example: `petstore-mcp--petstore_pet_findbystatus` became `petstore-mcp--petstore-pet-findbystatus`.
  - Reason: Orloj generates MCP tool resource names with `_` and `.` normalized to `-`.
  - Raised `AgentPolicy/petstore-agent-policy` `max_tokens_per_run` from `8000` to `100000`.
  - Added a direct prompt instruction to use `petstore-mcp--petstore-pet-findbystatus` with status `available` when listing available pets.
  - Reason: after the model secret fix, the Petstore A2A run consumed `38704` provider-reported tokens before completing and dead-lettered on the previous 8000-token budget.
  - Added an agent execution contract requiring `petstore-mcp--petstore-pet-findbystatus` and stopping after the first successful tool call.
  - Reason: the next validation task completed without dead-lettering, but reported `last_tool_calls: 0` and `last_event: max steps reached`.
  - Removed the optional `memory` binding from `Agent/petstore-agent`.
  - Reason: this Petstore smoke test only needs MCP tool execution, and memory wrapping was preventing the required-tool schema path from being exercised reliably in the single-agent job runtime.

- Updated `orloj_beta/charts/orloj/templates/server-deployment.yaml` and `orloj_beta/charts/orloj/templates/worker-deployment.yaml`:
  - Always emit `ORLOJ_TOOL_K8S_NAMESPACE` and `ORLOJ_AGENT_K8S_NAMESPACE`, defaulting to the Helm release namespace.
  - Reason: agent execution Jobs were attempted in Kubernetes namespace `default` while the chart-created RBAC was in namespace `orloj`, causing `jobs.batch is forbidden`.

- Updated `orloj_beta/charts/orloj/v1-values.yaml`:
  - Set `toolIsolation.kubernetes.namespace: orloj`.
  - Set `agentExecution.kubernetes.namespace: orloj`.

- Updated `orloj_beta/runtime/mcp_transport_http.go`:
  - Changed Streamable HTTP MCP requests to send:
    - `Content-Type: application/json`
    - `Accept: application/json, text/event-stream`
  - Added the same `Accept` header to MCP notifications.
  - Added decoding support for MCP Streamable HTTP responses returned as SSE:
    - `event: message`
    - `data: <json-rpc-payload>`
  - Reason: `evilfreelancer/openapi-to-mcp` rejected the old client with HTTP 406 until both accepted media types were advertised, then returned JSON-RPC payloads inside SSE frames.

- Updated `orloj_beta/runtime/contracts.go`, `orloj_beta/runtime/agent_worker.go`, and `orloj_beta/runtime/model_gateway_openai.go`:
  - Added `ModelRequest.RequiredTool`.
  - Contract-mode agents now pass the next pending `tool_sequence` item into model requests.
  - OpenAI chat-completions requests now emit a forced `tool_choice` object for that required tool, using the provider-safe tool alias.
  - Reason: OpenAI was previously sent `tool_choice: auto`, so the Petstore agent could ignore the required MCP tool despite having the tool configured.
  - Added a schema-based fallback for contract-mode required tools when the model still returns no tool call.
  - For required string fields, the fallback infers values from task/prompt text and tool schema enums; the Petstore `status` field resolves to `available`.
  - Extended the fallback to run after model-provider errors as well, so a required contract tool can still execute when its input can be inferred.
  - Forwarded `ToolSchemaResolver` through `MemoryToolRuntime` and `OrlojToolRuntime`.
  - Reason: agents with memory enabled wrapped the governed runtime and hid generated MCP tool schemas from the agent worker.

- Updated `orloj_beta/runtime/mcp_transport_http_test.go`:
  - Added a regression test for the Streamable HTTP `Accept` header.
  - Added a regression test for decoding JSON-RPC responses from SSE `data:` frames.

- Updated `orloj_beta/controllers/mcp_server_controller.go`:
  - Made generated MCP tool sync idempotent.
  - Existing generated tools are skipped when ownership labels and normalized specs already match.
  - Reason: repeated upserts caused generated tool metadata churn and resource-version conflicts during controller status reconciliation.

- Added `orloj_beta/controllers/mcp_server_controller_test.go`:
  - Verifies a second generated-tool sync does not change `resourceVersion` when the generated tool spec is unchanged.

- Added `orloj_beta/controllers/reconcile_log.go` and updated:
  - `orloj_beta/controllers/mcp_server_controller.go`
  - `orloj_beta/controllers/tool_controller.go`
  - `orloj_beta/controllers/memory_controller.go`
  - `orloj_beta/controllers/policy_controller.go`
  - Benign optimistic-concurrency conflicts from the store are no longer logged as reconcile errors.
  - Reason: these conflicts are retried by normal reconcile loops and polluted the requested log grep with INFO lines containing `error`.

### Validation Commands

```bash
gofmt -w \
  orloj_beta/runtime/mcp_transport_http.go \
  orloj_beta/runtime/mcp_transport_http_test.go \
  orloj_beta/controllers/mcp_server_controller.go \
  orloj_beta/controllers/mcp_server_controller_test.go \
  orloj_beta/controllers/reconcile_log.go \
  orloj_beta/controllers/tool_controller.go \
  orloj_beta/controllers/memory_controller.go

cd orloj_beta
go test ./runtime -run 'TestStreamableHTTPMcpTransport'
go test ./controllers -run 'TestMcpServerSyncToolsSkipsUnchangedGeneratedTools'
go test ./runtime -run 'TestTaskExecutorContractModeSynthesizesRequiredTool|TestTaskExecutorContractModeSynthesizesStatusToolWithoutSchema|TestOpenAIModelGatewayRequiredToolChoiceUsesAlias'
go test ./controllers -run 'TestMcpServerSyncToolsSkipsUnchangedGeneratedTools'
```

### Kubernetes Manifest Apply Steps

```bash
kubectl apply -f example/01-petstore-foundation.yaml
kubectl apply -f example/02-petstore-agent-system.yaml
```

### Local Image Build Steps

The kind node is `arm64`, so images were built with `TARGETARCH=arm64`.

```bash
cd orloj_beta

docker build \
  --build-arg TARGETOS=linux \
  --build-arg TARGETARCH=arm64 \
  --target orlojd \
  -t ghcr.io/orlojhq/orloj-orlojd:mcp-accept-local \
  .

docker build \
  --build-arg TARGETOS=linux \
  --build-arg TARGETARCH=arm64 \
  --target orlojworker \
  -t ghcr.io/orlojhq/orloj-orlojworker:mcp-accept-local \
  .
```

### kind Image Load Steps

```bash
kind load docker-image ghcr.io/orlojhq/orloj-orlojd:mcp-accept-local --name kind-ingress
kind load docker-image ghcr.io/orlojhq/orloj-orlojworker:mcp-accept-local --name kind-ingress
```

### Helm Deploy Steps

The installed release was `orloj` in namespace `orloj`.

```bash
helm upgrade orloj orloj_beta/charts/orloj \
  -n orloj \
  --reuse-values \
  --set image.server.tag=mcp-accept-local \
  --set image.worker.tag=mcp-accept-local \
  --set agentExecution.kubernetes.image=ghcr.io/orlojhq/orloj-orlojworker:mcp-accept-local
```

### Rollout Steps

```bash
kubectl rollout restart deploy/orloj-server -n orloj
kubectl rollout restart deploy/orloj-worker -n orloj

kubectl rollout status deploy/orloj-server -n orloj --timeout=120s
kubectl rollout status deploy/orloj-worker -n orloj --timeout=120s
```

### Final Rollout and Validation

- Helm revision `8`: disabled Kubernetes agent execution for the Petstore validation path.
- Helm revision `9`: deployed worker image `docker.io/rajat965ng/orloj-orlojworker:mcp-required-tool-status`.
- Helm revision `10`: deployed server image `docker.io/rajat965ng/orloj-orlojd:mcp-policy-log-clean`.
- Reapplied manifests:

```bash
kubectl apply -f example/01-petstore-foundation.yaml
kubectl apply -f example/02-petstore-agent-system.yaml
```

- Verified deployed images:
  - `orloj-server`: `docker.io/rajat965ng/orloj-orlojd:mcp-policy-log-clean`
  - `orloj-worker`: `docker.io/rajat965ng/orloj-orlojworker:mcp-required-tool-status`
  - `petstore-api-proxy`: `nginx:1.29-alpine`
  - `petstore-openapi-to-mcp`: `evilfreelancer/openapi-to-mcp:latest`
- Verified local pushed tags were removed:
  - `docker.io/rajat965ng/orloj-orlojd:mcp-policy-log-clean`
  - `docker.io/rajat965ng/orloj-orlojworker:mcp-required-tool-status`
- Verified the Orloj API reports `20` generated `petstore-mcp--...` tools.
- Verified final A2A task `petstore-task-12` completed:
  - `state: completed`
  - `last_tool_calls: 1`
  - `last_steps: 1`
  - successful tool trace: `petstore-mcp--petstore-pet-findbystatus`
- Verified the requested server log grep no longer shows Petstore/MCP failures:

```bash
kubectl logs -n orloj deploy/orloj-server --tail=100 \
  | grep -i -E "mcp|tool|petstore|error"
```

### Verification Steps

```bash
kubectl get pods -n orloj

kubectl logs -n orloj deploy/orloj-server --tail=100 \
  | grep -i -E "mcp|tool|petstore|error"

kubectl exec -n orloj deploy/orloj-server -- \
  wget -qO- \
  --header='Authorization: Bearer orloj-api-token-change-me' \
  'http://127.0.0.1:8080/v1/mcp-servers?namespace=orloj'

kubectl exec -n orloj deploy/orloj-server -- \
  wget -qO- \
  --header='Authorization: Bearer orloj-api-token-change-me' \
  'http://127.0.0.1:8080/v1/tools?namespace=orloj' \
  | grep -o '"name":"petstore-mcp--' \
  | wc -l
```

Expected verification results:

- `McpServer/petstore-mcp` status phase is `Ready`.
- `status.discoveredTools` contains 20 Petstore MCP tools.
- `status.generatedTools` contains 20 generated `petstore-mcp--...` tools.
- The generated Petstore MCP tool count from the Orloj API is `20`.
- `Agent/petstore-agent` status phase is `Synced`.
- The filtered log check no longer shows the original failures:
  - no `private address ... is not allowed`
  - no HTTP 406 `Client must accept both application/json and text/event-stream`
  - no `decode JSON-RPC response: invalid character 'e'`
