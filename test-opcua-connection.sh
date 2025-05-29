#!/bin/bash

echo "🔍 OPC-UA 연결 테스트 시작..."

# 1. Docker OPC-UA 서버 상태 확인
echo "1. Docker OPC-UA 서버 상태 확인..."
if docker ps | grep -q "mes-opcua-server"; then
    echo "✅ OPC-UA 서버가 실행 중입니다."
else
    echo "❌ OPC-UA 서버가 실행되지 않았습니다."
    echo "   다음 명령으로 서버를 시작하세요: cd docker && docker-compose up opcua-server -d"
    exit 1
fi

# 2. 포트 연결 테스트
echo "2. 포트 4840 연결 테스트..."
if nc -z localhost 4840 2>/dev/null; then
    echo "✅ 포트 4840에 연결 가능합니다."
else
    echo "❌ 포트 4840에 연결할 수 없습니다."
    echo "   Docker 포트 매핑을 확인하세요: docker-compose.yml의 ports 설정"
fi

# 3. OPC-UA 엔드포인트 디스커버리 테스트 (만약 opc-ua 클라이언트 도구가 있다면)
echo "3. 환경변수 설정 가이드..."
echo "   로컬 개발환경에서 실행하려면:"
echo "   export OPCUA_ENDPOINT=opc.tcp://localhost:4840"
echo "   ./gradlew bootRun"

echo ""
echo "4. Docker 전체 환경에서 실행하려면:"
echo "   cd docker && docker-compose up --build"

echo ""
echo "🏁 테스트 완료!" 