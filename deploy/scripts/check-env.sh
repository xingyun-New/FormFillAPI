#!/bin/bash

set -e

echo "🔍 检查云服务器部署环境..."

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查结果
ERRORS=0
WARNINGS=0

# 检查函数
check_command() {
    if command -v $1 &> /dev/null; then
        echo -e "${GREEN}✅ $1 已安装${NC}"
        return 0
    else
        echo -e "${RED}❌ $1 未安装${NC}"
        return 1
    fi
}

check_java_version() {
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        JAVA_MAJOR=$(echo $JAVA_VERSION | cut -d'.' -f1)
        
        if [ "$JAVA_MAJOR" -ge 17 ]; then
            echo -e "${GREEN}✅ Java版本: $JAVA_VERSION (满足要求)${NC}"
            return 0
        else
            echo -e "${RED}❌ Java版本: $JAVA_VERSION (需要17+)${NC}"
            return 1
        fi
    else
        echo -e "${RED}❌ Java 未安装${NC}"
        return 1
    fi
}

check_port() {
    PORT=$1
    if netstat -tuln | grep ":$PORT " > /dev/null 2>&1; then
        echo -e "${YELLOW}⚠️  端口 $PORT 已被占用${NC}"
        return 1
    else
        echo -e "${GREEN}✅ 端口 $PORT 可用${NC}"
        return 0
    fi
}

check_disk_space() {
    AVAILABLE=$(df /opt 2>/dev/null | awk 'NR==2 {print $4}' || echo "0")
    REQUIRED_KB=$((10 * 1024 * 1024))  # 10GB in KB
    
    if [ "$AVAILABLE" -gt "$REQUIRED_KB" ]; then
        AVAILABLE_GB=$((AVAILABLE / 1024 / 1024))
        echo -e "${GREEN}✅ 磁盘空间: ${AVAILABLE_GB}GB 可用${NC}"
        return 0
    else
        AVAILABLE_GB=$((AVAILABLE / 1024 / 1024))
        echo -e "${RED}❌ 磁盘空间不足: ${AVAILABLE_GB}GB (需要至少10GB)${NC}"
        return 1
    fi
}

check_memory() {
    TOTAL_MEM=$(free -m | awk 'NR==2{printf "%.0f", $2}')
    REQUIRED_MEM=2048
    
    if [ "$TOTAL_MEM" -gt "$REQUIRED_MEM" ]; then
        echo -e "${GREEN}✅ 内存: ${TOTAL_MEM}MB (满足要求)${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  内存: ${TOTAL_MEM}MB (推荐4GB+)${NC}"
        return 1
    fi
}

check_directory_permissions() {
    DIR=$1
    if [ -d "$DIR" ]; then
        if [ -w "$DIR" ]; then
            echo -e "${GREEN}✅ 目录 $DIR 可写${NC}"
            return 0
        else
            echo -e "${RED}❌ 目录 $DIR 不可写${NC}"
            return 1
        fi
    else
        echo -e "${YELLOW}⚠️  目录 $DIR 不存在，将自动创建${NC}"
        return 1
    fi
}

# 主要检查项目
echo ""
echo "📋 系统基本信息:"
echo "   操作系统: $(uname -s)"
echo "   内核版本: $(uname -r)"
echo "   架构: $(uname -m)"
echo ""

echo "🔧 检查必需的软件:"

# 检查Java
if ! check_java_version; then
    ERRORS=$((ERRORS + 1))
    echo "   安装建议: sudo apt install openjdk-17-jdk -y"
fi

# 检查Maven
if ! check_command "mvn"; then
    ERRORS=$((ERRORS + 1))
    echo "   安装建议: sudo apt install maven -y"
fi

# 检查curl
if ! check_command "curl"; then
    ERRORS=$((ERRORS + 1))
    echo "   安装建议: sudo apt install curl -y"
fi

# 检查git (可选)
if ! check_command "git"; then
    WARNINGS=$((WARNINGS + 1))
    echo "   安装建议: sudo apt install git -y"
fi

echo ""
echo "🌐 检查网络和端口:"

# 检查端口
if ! check_port 8080; then
    WARNINGS=$((WARNINGS + 1))
    echo "   解决方案: 修改application.yml中的server.port"
fi

# 检查Internet连接
if curl -f -s http://www.google.com > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Internet连接正常${NC}"
else
    echo -e "${YELLOW}⚠️  Internet连接可能有问题${NC}"
    WARNINGS=$((WARNINGS + 1))
fi

echo ""
echo "💾 检查系统资源:"

# 检查磁盘空间
if ! check_disk_space; then
    ERRORS=$((ERRORS + 1))
fi

# 检查内存
if ! check_memory; then
    WARNINGS=$((WARNINGS + 1))
fi

echo ""
echo "📁 检查目录权限:"

# 检查/opt目录
if ! check_directory_permissions "/opt"; then
    WARNINGS=$((WARNINGS + 1))
fi

# 检查用户权限
if [ "$EUID" -eq 0 ]; then
    echo -e "${YELLOW}⚠️  当前以root用户运行，建议创建专用用户${NC}"
    WARNINGS=$((WARNINGS + 1))
else
    echo -e "${GREEN}✅ 当前用户: $(whoami)${NC}"
fi

echo ""
echo "🔒 检查安全配置:"

# 检查防火墙
if command -v ufw &> /dev/null; then
    UFW_STATUS=$(ufw status | head -n 1)
    if echo "$UFW_STATUS" | grep -q "Status: active"; then
        echo -e "${GREEN}✅ UFW防火墙已启用${NC}"
    else
        echo -e "${YELLOW}⚠️  UFW防火墙未启用${NC}"
        WARNINGS=$((WARNINGS + 1))
    fi
elif command -v firewall-cmd &> /dev/null; then
    if systemctl is-active --quiet firewalld; then
        echo -e "${GREEN}✅ Firewalld防火墙已启用${NC}"
    else
        echo -e "${YELLOW}⚠️  Firewalld防火墙未启用${NC}"
        WARNINGS=$((WARNINGS + 1))
    fi
else
    echo -e "${YELLOW}⚠️  未检测到防火墙配置${NC}"
    WARNINGS=$((WARNINGS + 1))
fi

echo ""
echo "🎯 检查可选组件:"

# 检查Nginx
if check_command "nginx"; then
    if systemctl is-active --quiet nginx; then
        echo -e "${GREEN}✅ Nginx正在运行${NC}"
    else
        echo -e "${YELLOW}⚠️  Nginx已安装但未运行${NC}"
        WARNINGS=$((WARNINGS + 1))
    fi
else
    echo -e "${YELLOW}⚠️  Nginx未安装 (可选，用于反向代理)${NC}"
    echo "   安装建议: sudo apt install nginx -y"
    WARNINGS=$((WARNINGS + 1))
fi

# 检查系统服务管理器
if command -v systemctl &> /dev/null; then
    echo -e "${GREEN}✅ Systemd可用${NC}"
else
    echo -e "${RED}❌ Systemd不可用${NC}"
    ERRORS=$((ERRORS + 1))
fi

echo ""
echo "📊 环境检查总结:"
echo "=================================="

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}🎉 环境检查完全通过，可以开始部署！${NC}"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo -e "${YELLOW}⚠️  环境基本满足要求，但有 $WARNINGS 个警告${NC}"
    echo -e "${YELLOW}建议修复警告后再部署${NC}"
    exit 0
else
    echo -e "${RED}❌ 发现 $ERRORS 个错误和 $WARNINGS 个警告${NC}"
    echo -e "${RED}请修复所有错误后再部署${NC}"
    echo ""
    echo "📋 快速修复命令 (Ubuntu/Debian):"
    echo "  sudo apt update"
    echo "  sudo apt install openjdk-17-jdk maven curl git nginx -y"
    echo ""
    exit 1
fi 