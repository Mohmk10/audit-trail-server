#!/bin/bash
set -euo pipefail

# ============================================
# Release Script for Audit Trail Server
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

# Check required arguments
if [ $# -lt 1 ]; then
    echo "Usage: $0 <version> [OPTIONS]"
    echo ""
    echo "Arguments:"
    echo "  version         Version to release (e.g., 1.0.0)"
    echo ""
    echo "Options:"
    echo "  --dry-run       Show what would be done without making changes"
    echo "  --skip-tests    Skip running tests"
    echo "  -h, --help      Show this help"
    exit 1
fi

VERSION="$1"
shift

DRY_RUN=false
SKIP_TESTS=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 <version> [OPTIONS]"
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

cd "$PROJECT_ROOT"

log_info "Preparing release v$VERSION..."

# Validate version format
if ! [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9]+)?$ ]]; then
    log_error "Invalid version format: $VERSION"
    log_error "Expected format: X.Y.Z or X.Y.Z-suffix"
    exit 1
fi

# Check for uncommitted changes
if [ -n "$(git status --porcelain)" ]; then
    log_error "There are uncommitted changes. Please commit or stash them first."
    exit 1
fi

# Check we're on main branch
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" != "main" ]; then
    log_warn "Not on main branch (current: $CURRENT_BRANCH)"
    read -p "Continue anyway? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

if [ "$DRY_RUN" = true ]; then
    log_info "[DRY RUN] Would perform the following actions:"
    log_info "  1. Update version to $VERSION"
    log_info "  2. Run tests"
    log_info "  3. Create git tag v$VERSION"
    log_info "  4. Push tag to origin"
    exit 0
fi

# Update version
log_info "Updating version to $VERSION..."
mvn versions:set -DnewVersion="$VERSION" -DgenerateBackupPoms=false

# Run tests
if [ "$SKIP_TESTS" = false ]; then
    log_info "Running tests..."
    mvn clean verify -B
else
    log_warn "Skipping tests..."
fi

# Commit version change
log_info "Committing version change..."
git add pom.xml */pom.xml
git commit -m "chore: release v$VERSION"

# Create and push tag
log_info "Creating tag v$VERSION..."
git tag -a "v$VERSION" -m "Release v$VERSION"

log_info "Pushing to origin..."
git push origin main
git push origin "v$VERSION"

log_info "Release v$VERSION completed successfully!"
log_info "GitHub Actions will now build and publish the release."
