#!/bin/bash
set -euo pipefail

# ============================================
# Test Script for Audit Trail Server
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
TEST_TYPE="all"
COVERAGE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --unit)
            TEST_TYPE="unit"
            shift
            ;;
        --integration)
            TEST_TYPE="integration"
            shift
            ;;
        --coverage)
            COVERAGE=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --unit          Run unit tests only"
            echo "  --integration   Run integration tests only"
            echo "  --coverage      Generate coverage report"
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

log_info "Running tests..."

case $TEST_TYPE in
    unit)
        log_info "Running unit tests..."
        MVN_CMD="mvn test -B -Dtest='!*IntegrationTest'"
        ;;
    integration)
        log_info "Running integration tests..."
        MVN_CMD="mvn test -B -Dtest='*IntegrationTest'"
        ;;
    all)
        log_info "Running all tests..."
        MVN_CMD="mvn test -B"
        ;;
esac

if [ "$COVERAGE" = true ]; then
    MVN_CMD="$MVN_CMD jacoco:report"
fi

log_info "Running: $MVN_CMD"
eval "$MVN_CMD"

if [ "$COVERAGE" = true ]; then
    log_info "Coverage report generated at: target/site/jacoco/index.html"
fi

log_info "Tests completed successfully!"
