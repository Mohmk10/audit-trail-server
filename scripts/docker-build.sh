#!/bin/bash
set -euo pipefail

# ============================================
# Docker Build Script for Audit Trail Server
# ============================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Default values
IMAGE_NAME="devmohmk/audit-trail-server"
VERSION="latest"
PUSH=false
PLATFORMS="linux/amd64"
DEV_MODE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --version)
            VERSION="$2"
            shift 2
            ;;
        --push)
            PUSH=true
            shift
            ;;
        --multi-platform)
            PLATFORMS="linux/amd64,linux/arm64"
            shift
            ;;
        --dev)
            DEV_MODE=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --version <ver>     Tag version (default: latest)"
            echo "  --push              Push to registry"
            echo "  --multi-platform    Build for amd64 and arm64"
            echo "  --dev               Build development image"
            echo "  -h, --help          Show this help"
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

cd "$PROJECT_ROOT"

# Determine Dockerfile
if [ "$DEV_MODE" = true ]; then
    DOCKERFILE="docker/Dockerfile.dev"
    IMAGE_NAME="${IMAGE_NAME}-dev"
    log_info "Building development image..."
else
    DOCKERFILE="docker/Dockerfile"
    log_info "Building production image..."
fi

# Build command
DOCKER_CMD="docker buildx build"
DOCKER_CMD="$DOCKER_CMD --file $DOCKERFILE"
DOCKER_CMD="$DOCKER_CMD --tag $IMAGE_NAME:$VERSION"

if [ "$VERSION" != "latest" ]; then
    DOCKER_CMD="$DOCKER_CMD --tag $IMAGE_NAME:latest"
fi

DOCKER_CMD="$DOCKER_CMD --platform $PLATFORMS"

if [ "$PUSH" = true ]; then
    DOCKER_CMD="$DOCKER_CMD --push"
    log_info "Image will be pushed to registry"
else
    DOCKER_CMD="$DOCKER_CMD --load"
fi

DOCKER_CMD="$DOCKER_CMD ."

log_info "Running: $DOCKER_CMD"

# Ensure buildx is available
if ! docker buildx version > /dev/null 2>&1; then
    log_warn "Docker buildx not available, using regular build"
    docker build --file "$DOCKERFILE" --tag "$IMAGE_NAME:$VERSION" .
else
    # Create builder if needed for multi-platform
    if [ "$PLATFORMS" = "linux/amd64,linux/arm64" ]; then
        if ! docker buildx inspect multiplatform > /dev/null 2>&1; then
            log_info "Creating multi-platform builder..."
            docker buildx create --name multiplatform --use
        else
            docker buildx use multiplatform
        fi
    fi
    eval "$DOCKER_CMD"
fi

log_info "Build completed successfully!"
log_info "Image: $IMAGE_NAME:$VERSION"

if [ "$PUSH" = false ]; then
    log_info ""
    log_info "To run the container:"
    log_info "  docker run -p 8080:8080 $IMAGE_NAME:$VERSION"
fi
