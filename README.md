# MES (Manufacturing Execution System) PoC

1. 데이터 수집 : 센서 기반 자동 수집 (OPC-UA, MQTT), 실시간 수집 로그 시각화
2. 불량 예측 AI : 공정데이터 기반 이상 감지/불량 예측 (AutoML + Scikit-learn / LSTM)
3. RPA 연계 : 생산일지, 품질보고서 자동화 (Slack WebHook 대체)
4. 시각화 : Grafana
5. ERP 연계 : SAP Json 연동, 샘플 ERP 연동 시나리오
6. 모듈화 아키텍처 : Spring Boot, Spring Cloud 기반 MSA 구조로 모듈 분리

센서 시뮬레이터
PLC -> MQTT/OPC
Data Ingestor 서비스

[Frontend]
- React
[Backend]
- Spring Boot + JPA + PostgreSQL

1) 생산 작업지시서 등록 API
2) 실적 등록 API (수량, 작업자, 설비 등) -> BarCode Printer 에뮬레이터 or QR Code Generator
3) 불량 유형 등록 및 이력 관리 API

