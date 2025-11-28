#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored messages
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if .env file exists
check_env_file() {
    if [ ! -f .env ]; then
        print_warn ".env file not found. Creating from .env.example..."
        cp .env.example .env
        print_info ".env file created. Please update it with your configuration."
        exit 1
    fi
}

# Function to start all services
start() {
    print_info "Starting all services..."
    check_env_file
    docker-compose up -d
    print_info "Services started. Use 'docker-compose logs -f' to view logs."
}

# Function to stop all services
stop() {
    print_info "Stopping all services..."
    docker-compose stop
    print_info "Services stopped."
}

# Function to restart all services
restart() {
    print_info "Restarting all services..."
    docker-compose restart
    print_info "Services restarted."
}

# Function to build and start
build() {
    print_info "Building and starting services..."
    check_env_file
    docker-compose up -d --build
    print_info "Services built and started."
}

# Function to view logs
logs() {
    if [ -z "$1" ]; then
        docker-compose logs -f
    else
        docker-compose logs -f "$1"
    fi
}

# Function to check status
status() {
    print_info "Checking services status..."
    docker-compose ps
}

# Function to clean up
clean() {
    print_warn "This will remove all containers and volumes. Are you sure? (y/N)"
    read -r response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        print_info "Cleaning up..."
        docker-compose down -v
        print_info "Cleanup complete."
    else
        print_info "Cleanup cancelled."
    fi
}

# Function to show health
health() {
    print_info "Checking health status..."
    echo ""
    echo "MySQL:"
    docker-compose exec db mysqladmin ping -h localhost -u root -p123456 2>/dev/null && echo "✓ Healthy" || echo "✗ Unhealthy"
    echo ""
    echo "Redis:"
    docker-compose exec redis redis-cli ping 2>/dev/null && echo "✓ Healthy" || echo "✗ Unhealthy"
    echo ""
    echo "Application:"
    curl -s http://localhost:8080/api/actuator/health > /dev/null && echo "✓ Healthy" || echo "✗ Unhealthy"
}

# Function to backup database
backup() {
    print_info "Creating database backup..."
    timestamp=$(date +%Y%m%d_%H%M%S)
    backup_file="backup_${timestamp}.sql"
    docker-compose exec -T db mysqldump -u root -p123456 oauth2 > "$backup_file"
    print_info "Backup created: $backup_file"
}

# Function to restore database
restore() {
    if [ -z "$1" ]; then
        print_error "Please provide backup file path"
        exit 1
    fi
    print_warn "This will restore the database from $1. Are you sure? (y/N)"
    read -r response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        print_info "Restoring database..."
        docker-compose exec -T db mysql -u root -p123456 oauth2 < "$1"
        print_info "Database restored."
    else
        print_info "Restore cancelled."
    fi
}

# Function to show help
show_help() {
    cat << EOF
Docker Management Script for OAuth2 JWT Application

Usage: ./docker.sh [command] [options]

Commands:
    start       Start all services
    stop        Stop all services
    restart     Restart all services
    build       Build and start services
    logs        View logs (optional: specify service name)
    status      Check services status
    health      Check health of all services
    clean       Remove all containers and volumes
    backup      Backup MySQL database
    restore     Restore MySQL database (requires backup file path)
    help        Show this help message

Examples:
    ./docker.sh start
    ./docker.sh logs app
    ./docker.sh backup
    ./docker.sh restore backup_20231127_120000.sql

EOF
}

# Main script logic
case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    build)
        build
        ;;
    logs)
        logs "$2"
        ;;
    status)
        status
        ;;
    health)
        health
        ;;
    clean)
        clean
        ;;
    backup)
        backup
        ;;
    restore)
        restore "$2"
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
