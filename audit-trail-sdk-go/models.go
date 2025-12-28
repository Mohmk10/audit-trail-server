package audittrail

import (
	"time"
)

// ActorType represents the type of actor
type ActorType string

const (
	ActorTypeUser    ActorType = "USER"
	ActorTypeSystem  ActorType = "SYSTEM"
	ActorTypeService ActorType = "SERVICE"
)

// Actor represents who performed the action
type Actor struct {
	ID         string            `json:"id"`
	Type       ActorType         `json:"type"`
	Name       string            `json:"name,omitempty"`
	IP         string            `json:"ip,omitempty"`
	UserAgent  string            `json:"userAgent,omitempty"`
	Attributes map[string]string `json:"attributes,omitempty"`
}

// NewUserActor creates a new user actor
func NewUserActor(id, name string) Actor {
	return Actor{ID: id, Type: ActorTypeUser, Name: name}
}

// NewSystemActor creates a new system actor
func NewSystemActor(id string) Actor {
	return Actor{ID: id, Type: ActorTypeSystem}
}

// NewServiceActor creates a new service actor
func NewServiceActor(id, name string) Actor {
	return Actor{ID: id, Type: ActorTypeService, Name: name}
}

// ActorBuilder provides a fluent interface for building Actor
type ActorBuilder struct {
	actor Actor
}

// NewActorBuilder creates a new ActorBuilder
func NewActorBuilder() *ActorBuilder {
	return &ActorBuilder{}
}

func (b *ActorBuilder) ID(id string) *ActorBuilder {
	b.actor.ID = id
	return b
}

func (b *ActorBuilder) Type(t ActorType) *ActorBuilder {
	b.actor.Type = t
	return b
}

func (b *ActorBuilder) Name(name string) *ActorBuilder {
	b.actor.Name = name
	return b
}

func (b *ActorBuilder) IP(ip string) *ActorBuilder {
	b.actor.IP = ip
	return b
}

func (b *ActorBuilder) UserAgent(ua string) *ActorBuilder {
	b.actor.UserAgent = ua
	return b
}

func (b *ActorBuilder) Attributes(attrs map[string]string) *ActorBuilder {
	b.actor.Attributes = attrs
	return b
}

func (b *ActorBuilder) Build() Actor {
	return b.actor
}

// Action represents what action was performed
type Action struct {
	Type        string `json:"type"`
	Description string `json:"description,omitempty"`
	Category    string `json:"category,omitempty"`
}

// Action factory functions
func CreateAction(description string) Action {
	return Action{Type: "CREATE", Description: description}
}

func ReadAction(description string) Action {
	return Action{Type: "READ", Description: description}
}

func UpdateAction(description string) Action {
	return Action{Type: "UPDATE", Description: description}
}

func DeleteAction(description string) Action {
	return Action{Type: "DELETE", Description: description}
}

func LoginAction() Action {
	return Action{Type: "LOGIN", Description: "User login"}
}

func LogoutAction() Action {
	return Action{Type: "LOGOUT", Description: "User logout"}
}

func NewAction(actionType, description, category string) Action {
	return Action{Type: actionType, Description: description, Category: category}
}

// ActionBuilder provides a fluent interface for building Action
type ActionBuilder struct {
	action Action
}

func NewActionBuilder() *ActionBuilder {
	return &ActionBuilder{}
}

func (b *ActionBuilder) Type(t string) *ActionBuilder {
	b.action.Type = t
	return b
}

func (b *ActionBuilder) Description(d string) *ActionBuilder {
	b.action.Description = d
	return b
}

func (b *ActionBuilder) Category(c string) *ActionBuilder {
	b.action.Category = c
	return b
}

func (b *ActionBuilder) Build() Action {
	return b.action
}

// Resource represents what resource was affected
type Resource struct {
	ID     string                 `json:"id"`
	Type   string                 `json:"type"`
	Name   string                 `json:"name,omitempty"`
	Before map[string]interface{} `json:"before,omitempty"`
	After  map[string]interface{} `json:"after,omitempty"`
}

// Resource factory functions
func NewResource(id, resourceType, name string) Resource {
	return Resource{ID: id, Type: resourceType, Name: name}
}

func DocumentResource(id, name string) Resource {
	return Resource{ID: id, Type: "DOCUMENT", Name: name}
}

func UserResource(id, name string) Resource {
	return Resource{ID: id, Type: "USER", Name: name}
}

func TransactionResource(id, name string) Resource {
	return Resource{ID: id, Type: "TRANSACTION", Name: name}
}

// ResourceBuilder provides a fluent interface for building Resource
type ResourceBuilder struct {
	resource Resource
}

func NewResourceBuilder() *ResourceBuilder {
	return &ResourceBuilder{}
}

func (b *ResourceBuilder) ID(id string) *ResourceBuilder {
	b.resource.ID = id
	return b
}

func (b *ResourceBuilder) Type(t string) *ResourceBuilder {
	b.resource.Type = t
	return b
}

func (b *ResourceBuilder) Name(name string) *ResourceBuilder {
	b.resource.Name = name
	return b
}

func (b *ResourceBuilder) Before(before map[string]interface{}) *ResourceBuilder {
	b.resource.Before = before
	return b
}

func (b *ResourceBuilder) After(after map[string]interface{}) *ResourceBuilder {
	b.resource.After = after
	return b
}

func (b *ResourceBuilder) Build() Resource {
	return b.resource
}

// EventMetadata contains additional context about the event
type EventMetadata struct {
	Source        string                 `json:"source"`
	TenantID      string                 `json:"tenantId"`
	CorrelationID string                 `json:"correlationId,omitempty"`
	SessionID     string                 `json:"sessionId,omitempty"`
	Tags          map[string]string      `json:"tags,omitempty"`
	Extra         map[string]interface{} `json:"extra,omitempty"`
}

// NewEventMetadata creates new metadata with required fields
func NewEventMetadata(source, tenantID string) EventMetadata {
	return EventMetadata{Source: source, TenantID: tenantID}
}

// MetadataBuilder provides a fluent interface for building EventMetadata
type MetadataBuilder struct {
	metadata EventMetadata
}

func NewMetadataBuilder() *MetadataBuilder {
	return &MetadataBuilder{}
}

func (b *MetadataBuilder) Source(s string) *MetadataBuilder {
	b.metadata.Source = s
	return b
}

func (b *MetadataBuilder) TenantID(t string) *MetadataBuilder {
	b.metadata.TenantID = t
	return b
}

func (b *MetadataBuilder) CorrelationID(c string) *MetadataBuilder {
	b.metadata.CorrelationID = c
	return b
}

func (b *MetadataBuilder) SessionID(s string) *MetadataBuilder {
	b.metadata.SessionID = s
	return b
}

func (b *MetadataBuilder) Tags(tags map[string]string) *MetadataBuilder {
	b.metadata.Tags = tags
	return b
}

func (b *MetadataBuilder) Extra(extra map[string]interface{}) *MetadataBuilder {
	b.metadata.Extra = extra
	return b
}

func (b *MetadataBuilder) Build() EventMetadata {
	return b.metadata
}

// Event represents an audit event
type Event struct {
	Actor    Actor         `json:"actor"`
	Action   Action        `json:"action"`
	Resource Resource      `json:"resource"`
	Metadata EventMetadata `json:"metadata"`
}

// NewEvent creates a new event
func NewEvent(actor Actor, action Action, resource Resource, metadata EventMetadata) Event {
	return Event{Actor: actor, Action: action, Resource: resource, Metadata: metadata}
}

// EventBuilder provides a fluent interface for building Event
type EventBuilder struct {
	event Event
}

func NewEventBuilder() *EventBuilder {
	return &EventBuilder{}
}

func (b *EventBuilder) Actor(a Actor) *EventBuilder {
	b.event.Actor = a
	return b
}

func (b *EventBuilder) Action(a Action) *EventBuilder {
	b.event.Action = a
	return b
}

func (b *EventBuilder) Resource(r Resource) *EventBuilder {
	b.event.Resource = r
	return b
}

func (b *EventBuilder) Metadata(m EventMetadata) *EventBuilder {
	b.event.Metadata = m
	return b
}

func (b *EventBuilder) Build() Event {
	return b.event
}

// EventResponse represents the response after logging an event
type EventResponse struct {
	ID        string    `json:"id"`
	Timestamp time.Time `json:"timestamp"`
	Hash      string    `json:"hash"`
	Status    string    `json:"status"`
}

// BatchEventResponse represents the response after logging multiple events
type BatchEventResponse struct {
	Total     int             `json:"total"`
	Succeeded int             `json:"succeeded"`
	Failed    int             `json:"failed"`
	Events    []EventResponse `json:"events"`
	Errors    []ErrorDetail   `json:"errors,omitempty"`
}

// ErrorDetail represents an error for a specific event in a batch
type ErrorDetail struct {
	Index      int      `json:"index"`
	Message    string   `json:"message"`
	Violations []string `json:"violations,omitempty"`
}

// SearchCriteria represents search parameters
type SearchCriteria struct {
	TenantID     string `json:"tenantId"`
	ActorID      string `json:"actorId,omitempty"`
	ActorType    string `json:"actorType,omitempty"`
	ActionType   string `json:"actionType,omitempty"`
	ResourceID   string `json:"resourceId,omitempty"`
	ResourceType string `json:"resourceType,omitempty"`
	FromDate     string `json:"fromDate,omitempty"`
	ToDate       string `json:"toDate,omitempty"`
	Query        string `json:"query,omitempty"`
	Page         int    `json:"page,omitempty"`
	Size         int    `json:"size,omitempty"`
}

// SearchResult represents the search response
type SearchResult struct {
	Items      []map[string]interface{} `json:"items"`
	TotalCount int64                    `json:"totalCount"`
	Page       int                      `json:"page"`
	Size       int                      `json:"size"`
	TotalPages int                      `json:"totalPages"`
}
