# Hongik Taste Navigator - Backend

홍대 맛집 네비게이터의 핵심 비즈니스 로직과 데이터 파이프라인을 담당하는 백엔드 저장소입니다. **Java 21**과 **Spring Boot 3.4**를 활용하여 외부 API 데이터를 정제하고, 위치 기반 알고리즘을 통해 최적화된 식당 추천 환경을 제공합니다.

## System Architecture

<img width="5942" height="2472" alt="Mermaid Chart - Create complex, visual diagrams with text -2025-12-21-072738" src="https://github.com/user-attachments/assets/e82bcd8a-e7dc-46ae-9957-2f53bc7a20b1" />

## 핵심 기술 및 특징 | Key Technical Features

### 1. 유클리드 거리 기반 자동 데이터 분류
네이버 API로부터 수집된 식당의 좌표값($mapx$, $mapy$)을 분석하여 홍대입구역과 상수역 중 가장 인접한 역 정보를 자동 매핑합니다.
* **알고리즘**: $\sqrt{(x_1-x_2)^2 + (y_1-y_2)^2}$ 공식을 활용한 거리 계산 로직 구현
* **Java 21 활용**: 최신 Java 환경에서 대량의 좌표 데이터를 효율적으로 연산하고 분류

### 2. 데이터 정제 및 중복 제거 (Deduplication)
* **비정형 데이터 정제**: 정규표현식(`replaceAll("<[^>]*>", "")`)을 사용하여 API 응답에 포함된 HTML 태그를 일괄 제거하여 데이터 가시성 확보
* **중복 저장 방지**: `LinkedHashMap`과 데이터베이스 사전 조회를 통해 동일 식당 정보의 중복 저장을 원천 차단

### 3. 실전 운영 안정성 및 외부 API 정책 준수
* **Rate Limiting 전략**: 외부 API(Naver)의 호출 제한 정책을 준수하기 위해 `Thread.sleep(2000)`을 활용한 지연 호출 로직 적용
* **CI/CD 파이프라인**: **GitHub Actions**를 통한 자동 배포 환경을 구축하여 1,000명 이상의 실사용자 환경에서 발생한 피드백을 실시간으로 반영

## Tech Stack | 기술 스택
* **Language**: Java 21
* **Framework**: Spring Boot 3.4.3
* **ORM**: Spring Data JPA
* **Database**: MySQL 8.0, H2 (Test)
* **Infra**: AWS EC2, GitHub Actions
* **API**: Naver Local API, Springdoc-OpenAPI (Swagger)

## Project Structure
```text
src/main/java/hicc/club_fair_2025/
├── config/      # Spring Security 및 전역 설정
├── controller/  # REST API 엔드포인트
├── dto/         # 데이터 전송 객체
├── entity/      # JPA 엔티티 (Restaurant, Station, Category 등)
├── repository/  # 데이터 액세스 계층
└── service/     # 핵심 비즈니스 로직 및 외부 API 연동
