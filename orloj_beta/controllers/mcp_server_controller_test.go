package controllers

import (
	"context"
	"testing"

	"github.com/OrlojHQ/orloj/resources"
	agentruntime "github.com/OrlojHQ/orloj/runtime"
	"github.com/OrlojHQ/orloj/store"
)

func TestMcpServerSyncToolsSkipsUnchangedGeneratedTools(t *testing.T) {
	ctx := context.Background()
	toolStore := store.NewToolStore()
	controller := NewMcpServerController(store.NewMcpServerStore(), toolStore, nil, 0)
	server := resources.McpServer{
		Metadata: resources.ObjectMeta{Name: "petstore-mcp", Namespace: "orloj"},
	}
	tools := []agentruntime.McpToolDefinition{
		{
			Name:        "petstore_pet_findbystatus",
			Description: "Finds pets by status",
			InputSchema: map[string]any{
				"type": "object",
			},
		},
	}

	generated, err := controller.syncTools(ctx, server, tools)
	if err != nil {
		t.Fatalf("first sync failed: %v", err)
	}
	if len(generated) != 1 {
		t.Fatalf("expected one generated tool, got %d", len(generated))
	}
	key := store.ScopedName("orloj", generated[0])
	first, ok, err := toolStore.Get(ctx, key)
	if err != nil {
		t.Fatalf("get generated tool failed: %v", err)
	}
	if !ok {
		t.Fatalf("expected generated tool %s to exist", key)
	}

	if _, err := controller.syncTools(ctx, server, tools); err != nil {
		t.Fatalf("second sync failed: %v", err)
	}
	second, ok, err := toolStore.Get(ctx, key)
	if err != nil {
		t.Fatalf("get generated tool after second sync failed: %v", err)
	}
	if !ok {
		t.Fatalf("expected generated tool %s to still exist", key)
	}
	if second.Metadata.ResourceVersion != first.Metadata.ResourceVersion {
		t.Fatalf("expected unchanged generated tool resourceVersion %q, got %q", first.Metadata.ResourceVersion, second.Metadata.ResourceVersion)
	}
}
