package audittrail

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestActorFactories(t *testing.T) {
	t.Run("NewUserActor", func(t *testing.T) {
		actor := NewUserActor("user-123", "John Doe")
		assert.Equal(t, "user-123", actor.ID)
		assert.Equal(t, ActorTypeUser, actor.Type)
		assert.Equal(t, "John Doe", actor.Name)
	})

	t.Run("NewSystemActor", func(t *testing.T) {
		actor := NewSystemActor("sys-001")
		assert.Equal(t, "sys-001", actor.ID)
		assert.Equal(t, ActorTypeSystem, actor.Type)
	})

	t.Run("NewServiceActor", func(t *testing.T) {
		actor := NewServiceActor("svc-001", "Payment Service")
		assert.Equal(t, "svc-001", actor.ID)
		assert.Equal(t, ActorTypeService, actor.Type)
		assert.Equal(t, "Payment Service", actor.Name)
	})
}

func TestActorBuilder(t *testing.T) {
	actor := NewActorBuilder().
		ID("user-456").
		Type(ActorTypeUser).
		Name("Jane Doe").
		IP("192.168.1.1").
		UserAgent("Mozilla/5.0").
		Attributes(map[string]string{"role": "admin"}).
		Build()

	assert.Equal(t, "user-456", actor.ID)
	assert.Equal(t, ActorTypeUser, actor.Type)
	assert.Equal(t, "Jane Doe", actor.Name)
	assert.Equal(t, "192.168.1.1", actor.IP)
	assert.Equal(t, "Mozilla/5.0", actor.UserAgent)
	assert.Equal(t, "admin", actor.Attributes["role"])
}

func TestActionFactories(t *testing.T) {
	tests := []struct {
		name     string
		action   Action
		expected string
	}{
		{"Create", CreateAction("Created doc"), "CREATE"},
		{"Read", ReadAction("Read doc"), "READ"},
		{"Update", UpdateAction("Updated doc"), "UPDATE"},
		{"Delete", DeleteAction("Deleted doc"), "DELETE"},
		{"Login", LoginAction(), "LOGIN"},
		{"Logout", LogoutAction(), "LOGOUT"},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			assert.Equal(t, tt.expected, tt.action.Type)
		})
	}
}

func TestActionBuilder(t *testing.T) {
	action := NewActionBuilder().
		Type("APPROVE").
		Description("Approved request").
		Category("WORKFLOW").
		Build()

	assert.Equal(t, "APPROVE", action.Type)
	assert.Equal(t, "Approved request", action.Description)
	assert.Equal(t, "WORKFLOW", action.Category)
}

func TestNewAction(t *testing.T) {
	action := NewAction("CUSTOM", "Custom action", "CATEGORY")
	assert.Equal(t, "CUSTOM", action.Type)
	assert.Equal(t, "Custom action", action.Description)
	assert.Equal(t, "CATEGORY", action.Category)
}

func TestResourceFactories(t *testing.T) {
	t.Run("DocumentResource", func(t *testing.T) {
		resource := DocumentResource("doc-123", "Report")
		assert.Equal(t, "doc-123", resource.ID)
		assert.Equal(t, "DOCUMENT", resource.Type)
		assert.Equal(t, "Report", resource.Name)
	})

	t.Run("UserResource", func(t *testing.T) {
		resource := UserResource("user-123", "John")
		assert.Equal(t, "USER", resource.Type)
	})

	t.Run("TransactionResource", func(t *testing.T) {
		resource := TransactionResource("txn-123", "Payment")
		assert.Equal(t, "TRANSACTION", resource.Type)
	})

	t.Run("NewResource", func(t *testing.T) {
		resource := NewResource("config-1", "CONFIG", "Settings")
		assert.Equal(t, "config-1", resource.ID)
		assert.Equal(t, "CONFIG", resource.Type)
		assert.Equal(t, "Settings", resource.Name)
	})
}

func TestResourceBuilder(t *testing.T) {
	before := map[string]interface{}{"status": "draft"}
	after := map[string]interface{}{"status": "published"}

	resource := NewResourceBuilder().
		ID("doc-789").
		Type("DOCUMENT").
		Name("Annual Report").
		Before(before).
		After(after).
		Build()

	assert.Equal(t, "doc-789", resource.ID)
	assert.Equal(t, "DOCUMENT", resource.Type)
	assert.Equal(t, "draft", resource.Before["status"])
	assert.Equal(t, "published", resource.After["status"])
}

func TestNewEventMetadata(t *testing.T) {
	metadata := NewEventMetadata("web-app", "tenant-001")
	assert.Equal(t, "web-app", metadata.Source)
	assert.Equal(t, "tenant-001", metadata.TenantID)
}

func TestMetadataBuilder(t *testing.T) {
	metadata := NewMetadataBuilder().
		Source("web-app").
		TenantID("tenant-001").
		CorrelationID("corr-123").
		SessionID("sess-456").
		Tags(map[string]string{"env": "prod"}).
		Extra(map[string]interface{}{"custom": "data"}).
		Build()

	assert.Equal(t, "web-app", metadata.Source)
	assert.Equal(t, "tenant-001", metadata.TenantID)
	assert.Equal(t, "corr-123", metadata.CorrelationID)
	assert.Equal(t, "sess-456", metadata.SessionID)
	assert.Equal(t, "prod", metadata.Tags["env"])
	assert.Equal(t, "data", metadata.Extra["custom"])
}

func TestEventBuilder(t *testing.T) {
	event := NewEventBuilder().
		Actor(NewUserActor("user-1", "John")).
		Action(CreateAction("Created")).
		Resource(DocumentResource("doc-1", "Report")).
		Metadata(NewEventMetadata("app", "tenant-1")).
		Build()

	assert.Equal(t, "user-1", event.Actor.ID)
	assert.Equal(t, "CREATE", event.Action.Type)
	assert.Equal(t, "DOCUMENT", event.Resource.Type)
	assert.Equal(t, "tenant-1", event.Metadata.TenantID)
}

func TestNewEvent(t *testing.T) {
	event := NewEvent(
		NewUserActor("u1", "User"),
		CreateAction("Test"),
		DocumentResource("d1", "Doc"),
		NewEventMetadata("src", "t1"),
	)

	assert.Equal(t, "u1", event.Actor.ID)
	assert.Equal(t, "CREATE", event.Action.Type)
	assert.Equal(t, "DOCUMENT", event.Resource.Type)
	assert.Equal(t, "t1", event.Metadata.TenantID)
}

func TestActorTypes(t *testing.T) {
	assert.Equal(t, ActorType("USER"), ActorTypeUser)
	assert.Equal(t, ActorType("SYSTEM"), ActorTypeSystem)
	assert.Equal(t, ActorType("SERVICE"), ActorTypeService)
}
