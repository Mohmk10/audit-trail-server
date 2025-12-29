#!/bin/bash
set -euo pipefail

# ============================================
# Build Script for Audit Trail Server
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

# Parse arguments
SKIP_TESTS=false
PROFILE=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        --profile)
            PROFILE="$2"
            shift 2
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --skip-tests    Skip running tests"
            echo "  --profile       Maven profile to use"
            echo "  -h, --help      Show this help"
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

cd "$PROJECT_ROOT"

log_info "Building Audit Trail Server..."

# Build command
MVN_CMD="mvn clean package -B"

if [ "$SKIP_TESTS" = true ]; then
    MVN_CMD="$MVN_CMD -DskipTests"
fi

if [ -n "$PROFILE" ]; then
    MVN_CMD="$MVN_CMD -P $PROFILE"
fi

log_info "Running: $MVN_CMD"
eval "$MVN_CMD"

log_info "Build completed successfully!"
log_info "JAR location: audit-trail-app/target/*.jar"
