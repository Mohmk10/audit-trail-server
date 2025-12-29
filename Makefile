.PHONY: help build test clean docker helm sdk all

# ============================================
# Variables
# ============================================
VERSION ?= 1.0.0
DOCKER_REPO ?= devmohmk/audit-trail-server
HELM_RELEASE ?= audit-trail

# ============================================
# Help
# ============================================
help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# ============================================
# Build
# ============================================
build: ## Build all backend modules
	mvn clean package -DskipTests -B

build-full: ## Build with tests
	mvn clean verify -B

compile: ## Compile without packaging
	mvn clean compile -B

# ============================================
# Test
# ============================================
test: ## Run all tests
	mvn test -B

test-unit: ## Run unit tests only
	mvn test -Dtest="!*IntegrationTest" -B

test-integration: ## Run integration tests
	mvn test -Dtest="*IntegrationTest" -B

coverage: ## Generate coverage report
	mvn test jacoco:report -B
	@echo "Coverage report: target/site/jacoco/index.html"

# ============================================
# Docker
# ============================================
docker-build: ## Build Docker image
	docker build -t $(DOCKER_REPO):$(VERSION) -f docker/Dockerfile .
	docker tag $(DOCKER_REPO):$(VERSION) $(DOCKER_REPO):latest

docker-build-dev: ## Build development Docker image
	docker build -t $(DOCKER_REPO)-dev:latest -f docker/Dockerfile.dev .

docker-push: ## Push Docker image
	docker push $(DOCKER_REPO):$(VERSION)
	docker push $(DOCKER_REPO):latest

docker-run: ## Run Docker container
	docker run -p 8080:8080 $(DOCKER_REPO):$(VERSION)

# ============================================
# Docker Compose
# ============================================
up: ## Start all services with docker-compose
	docker-compose -f docker/docker-compose.yml up -d

up-build: ## Build and start all services
	docker-compose -f docker/docker-compose.yml up -d --build

down: ## Stop all services
	docker-compose -f docker/docker-compose.yml down

down-volumes: ## Stop all services and remove volumes
	docker-compose -f docker/docker-compose.yml down -v

logs: ## View server logs
	docker-compose -f docker/docker-compose.yml logs -f audit-trail-server

logs-all: ## View all logs
	docker-compose -f docker/docker-compose.yml logs -f

ps: ## Show running services
	docker-compose -f docker/docker-compose.yml ps

restart: down up ## Restart all services

# ============================================
# Helm
# ============================================
helm-deps: ## Update Helm dependencies
	helm dependency update helm/audit-trail

helm-lint: ## Lint Helm chart
	helm lint helm/audit-trail

helm-template: ## Render Helm templates
	helm template $(HELM_RELEASE) helm/audit-trail

helm-template-prod: ## Render Helm templates with production values
	helm template $(HELM_RELEASE) helm/audit-trail -f helm/audit-trail/values-prod.yaml

helm-install: ## Install Helm chart
	helm install $(HELM_RELEASE) helm/audit-trail

helm-install-prod: ## Install Helm chart with production values
	helm install $(HELM_RELEASE) helm/audit-trail -f helm/audit-trail/values-prod.yaml

helm-upgrade: ## Upgrade Helm release
	helm upgrade $(HELM_RELEASE) helm/audit-trail

helm-uninstall: ## Uninstall Helm release
	helm uninstall $(HELM_RELEASE)

helm-dry-run: ## Dry run Helm install
	helm install $(HELM_RELEASE) helm/audit-trail --dry-run --debug

# ============================================
# SDK
# ============================================
sdk-java: ## Build SDK Java
	cd audit-trail-sdk-java && mvn clean package -B

sdk-java-test: ## Test SDK Java
	cd audit-trail-sdk-java && mvn clean verify -B

sdk-js: ## Build SDK JavaScript
	cd audit-trail-sdk-js && npm ci && npm run build

sdk-js-test: ## Test SDK JavaScript
	cd audit-trail-sdk-js && npm ci && npm test

sdk-python: ## Build SDK Python
	cd audit-trail-sdk-python && pip install build && python -m build

sdk-python-test: ## Test SDK Python
	cd audit-trail-sdk-python && pip install -e ".[dev]" && pytest

sdk-go: ## Build SDK Go
	cd audit-trail-sdk-go && go build ./...

sdk-go-test: ## Test SDK Go
	cd audit-trail-sdk-go && go test -v ./...

sdk-all: sdk-java sdk-js sdk-python sdk-go ## Build all SDKs

sdk-test-all: sdk-java-test sdk-js-test sdk-python-test sdk-go-test ## Test all SDKs

# ============================================
# Development
# ============================================
dev: ## Start development environment
	docker-compose -f docker/docker-compose.yml up -d postgres elasticsearch redis kafka zookeeper
	@echo "Services started. Run 'mvn spring-boot:run -pl audit-trail-app' to start the server"

dev-stop: ## Stop development environment
	docker-compose -f docker/docker-compose.yml stop postgres elasticsearch redis kafka zookeeper

run: ## Run the application locally
	mvn spring-boot:run -pl audit-trail-app

run-debug: ## Run the application with debug
	mvn spring-boot:run -pl audit-trail-app -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

# ============================================
# Clean
# ============================================
clean: ## Clean build artifacts
	mvn clean
	rm -rf audit-trail-sdk-js/dist audit-trail-sdk-js/node_modules
	rm -rf audit-trail-sdk-python/dist audit-trail-sdk-python/*.egg-info

clean-docker: ## Clean Docker resources
	docker-compose -f docker/docker-compose.yml down -v --rmi local

clean-all: clean clean-docker ## Clean everything

# ============================================
# Release
# ============================================
release: ## Create a release (usage: make release VERSION=1.0.0)
	./scripts/release.sh $(VERSION)

release-dry-run: ## Dry run release
	./scripts/release.sh $(VERSION) --dry-run

# ============================================
# Quality
# ============================================
lint: ## Run linters
	mvn checkstyle:check -B || true
	cd audit-trail-sdk-js && npm run lint || true
	cd audit-trail-sdk-python && ruff check src/ || true
	cd audit-trail-sdk-go && golangci-lint run || true

format: ## Format code
	cd audit-trail-sdk-js && npm run format || true
	cd audit-trail-sdk-python && ruff format src/ || true
	cd audit-trail-sdk-go && go fmt ./... || true

security-scan: ## Run security scans
	mvn dependency-check:check -B || true
	cd audit-trail-sdk-js && npm audit || true
	cd audit-trail-sdk-python && pip-audit || true
	cd audit-trail-sdk-go && govulncheck ./... || true

# ============================================
# Documentation
# ============================================
docs: ## Generate documentation
	mvn javadoc:aggregate -B
	@echo "JavaDoc: target/site/apidocs/index.html"

# ============================================
# CI/CD
# ============================================
ci: lint test ## Run CI pipeline locally

ci-full: lint test-unit test-integration coverage docker-build helm-lint ## Full CI pipeline
