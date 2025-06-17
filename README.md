# MES (Manufacturing Execution System) PoC


### ISA-95, MESA

## MES Core - OPC-UA Integration Module
- Spring Boot 기반의 MES(Manufacturing Execution System) Core 모듈로, OPC-UA 프로토콜을 통한 실시간 공장 데이터 수집 및 관리 시스템입니다.

### 🚀 주요 기능

#### 핵심 기능
- OPC-UA 클라이언트: Eclipse Milo를 사용한 OPC-UA 서버 연결 및 데이터 통신
- 실시간 데이터 수집: 구독/발행 방식의 실시간 데이터 모니터링
- 장비 관리: 공장 설비 정보 및 상태 관리
- 생산 데이터 관리: 생산량, 품질 데이터 수집 및 추적
- 알람 시스템: 실시간 알람 감지, 분류 및 관리
R- EST API: 웹 기반 데이터 조회 및 제어 인터페이스

### [기술 스택]
- Backend: Spring Boot 3.2.0, Java 21
- OPC-UA: Eclipse Milo 0.6.8
- Database: H2 (개발용), JPA/Hibernate
- Build Tool: Gradle
- 기타: Lombok, Jackson, Spring Data JPA

### 📦 확장 요소
1. 데이터 수집 : OPC-UA 프로토콜 사용, 실시간 수집 (구독/발행 방식) MQTT 연동 확장
2. 다중 OPC-UA 서버 연결 지원
3. 불량 예측 AI : 공정데이터 기반 이상 감지/불량 예측 (AutoML + Scikit-learn / LSTM)
4. RPA 연계 : 생산일지, 품질보고서 자동화 (Slack WebHook 대체)
5. 시각화 : Grafana
6. ERP 연계 : SAP Json 연동, 샘플 ERP 연동 시나리오
7. 모듈화 아키텍처 : Spring Boot, Spring Cloud 기반 MSA 구조로 모듈 분리

### 추가 예정
1) 생산 작업지시서 등록 API
2) 실적 등록 API (수량, 작업자, 설비 등) -> BarCode Printer 에뮬레이터 or QR Code Generator
3) 불량 유형 등록 및 이력 관리 API

### 센서 시뮬레이터
- PLC -> OPC/MQTT
- Data Ingestor 서비스

### 🛠️ 설치 및 실행
전제 조건
- Java 21 이상
- Gradle 8.5 이상 (또는 Gradle Wrapper 사용)
- OPC-UA 서버 (Docker/테스트 시뮬레이터 또는 실제 시스템)


#### 1. 프로젝트 클론 및 빌드
- git clone <repository-url>
- cd my-mes
- ./gradlew clean build

#### 2. 설정 파일 수정
   - src/main/resources/application.yml 파일에서 OPC-UA 서버 정보 수정:
```yaml
   opcua:
     endpoint: "opc.tcp://your-opcua-server:4840"
       nodes:
         - nodeId: "ns=2;i=1001"
           name: "Temperature"
           dataType: "DOUBLE"
```


#### 3. 애플리케이션 실행
   - 1) ./gradlew bootRun
   - 2) JAR 파일로 실행:
     - java -jar build/libs/poc-0.0.1-SNAPSHOT.jar

#### 4. 접속 확인

- 애플리케이션: http://localhost:8080
- H2 콘솔: http://localhost:8080/h2-console
- 헬스체크: http://localhost:8080/api/health
