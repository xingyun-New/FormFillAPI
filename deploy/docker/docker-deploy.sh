#!/bin/bash

set -e

echo "🐳 开始Docker部署FormFillAPI..."

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 检查Docker是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}❌ Docker未安装${NC}"
        echo "请先安装Docker: https://docs.docker.com/get-docker/"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        echo -e "${RED}❌ Docker Compose未安装${NC}"
        echo "请先安装Docker Compose: https://docs.docker.com/compose/install/"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Docker和Docker Compose已安装${NC}"
}

# 构建应用
build_app() {
    echo "📦 构建应用..."
    
    cd ../..
    
    # 使用Maven构建
    echo "编译和打包应用..."
    mvn clean package -DskipTests
    
    if [ ! -f "target/form-fill-api-1.0.0.jar" ]; then
        echo -e "${RED}❌ 构建失败，找不到jar包${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ 应用构建成功${NC}"
    cd deploy/docker
}

# 准备数据目录
prepare_data_dirs() {
    echo "📁 准备数据目录..."
    
    mkdir -p data/templates data/output data/logs
    mkdir -p nginx/conf.d ssl
    
    # 复制模板文件
    if [ -d "../../templates" ]; then
        cp -r ../../templates/* data/templates/
        echo -e "${GREEN}✅ 模板文件已复制${NC}"
    fi
    
    echo -e "${GREEN}✅ 数据目录准备完成${NC}"
}

# 创建Nginx配置
create_nginx_config() {
    echo "🌐 创建Nginx配置..."
    
    cat > nginx/conf.d/formfillapi.conf << 'EOF'
server {
    listen 80;
    server_name localhost;
    
    # 上传大小限制
    client_max_body_size 100M;
    
    # 反向代理到FormFillAPI
    location / {
        proxy_pass http://formfillapi:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # 健康检查
    location /health {
        proxy_pass http://formfillapi:8080/api/health;
        access_log off;
    }
}
EOF
    
    echo -e "${GREEN}✅ Nginx配置创建完成${NC}"
}

# 部署服务
deploy_services() {
    echo "🚀 部署服务..."
    
    # 停止已存在的服务
    echo "停止现有服务..."
    docker-compose down 2>/dev/null || true
    
    # 构建和启动服务
    echo "构建Docker镜像..."
    docker-compose build
    
    echo "启动服务..."
    docker-compose up -d
    
    echo -e "${GREEN}✅ 服务启动成功${NC}"
}

# 等待服务启动并进行健康检查
wait_for_service() {
    echo "⏳ 等待服务启动..."
    
    # 等待最多5分钟
    for i in {1..60}; do
        if curl -f -s http://localhost:8080/api/health > /dev/null 2>&1; then
            echo -e "${GREEN}✅ 服务启动成功！${NC}"
            return 0
        fi
        echo "等待服务启动... ($i/60)"
        sleep 5
    done
    
    echo -e "${RED}❌ 服务启动超时${NC}"
    echo "查看日志："
    docker-compose logs formfillapi
    exit 1
}

# 显示部署信息
show_deploy_info() {
    echo ""
    echo "🎉 部署完成！"
    echo "=================================="
    echo -e "${GREEN}应用访问地址:${NC}"
    echo "  HTTP: http://localhost"
    echo "  API: http://localhost/api/health"
    echo ""
    echo -e "${GREEN}Docker管理命令:${NC}"
    echo "  查看状态: docker-compose ps"
    echo "  查看日志: docker-compose logs -f formfillapi"
    echo "  重启服务: docker-compose restart"
    echo "  停止服务: docker-compose down"
    echo "  更新服务: ./docker-deploy.sh"
    echo ""
    echo -e "${GREEN}数据目录:${NC}"
    echo "  模板文件: $(pwd)/data/templates"
    echo "  输出文件: $(pwd)/data/output"
    echo "  日志文件: $(pwd)/data/logs"
    echo ""
}

# 主执行流程
main() {
    echo "开始Docker部署流程..."
    
    check_docker
    build_app
    prepare_data_dirs
    create_nginx_config
    deploy_services
    wait_for_service
    show_deploy_info
    
    echo -e "${GREEN}🎉 FormFillAPI Docker部署完成！${NC}"
}

# 脚本选项
case "${1:-}" in
    "build")
        build_app
        ;;
    "deploy")
        deploy_services
        wait_for_service
        ;;
    "logs")
        docker-compose logs -f formfillapi
        ;;
    "status")
        docker-compose ps
        ;;
    "stop")
        docker-compose down
        ;;
    "restart")
        docker-compose restart
        ;;
    *)
        main
        ;;
esac 