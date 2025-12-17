# Legacy Promotion Service

## 프로젝트 개요

이 프로젝트는 현재 진행 중인 상품 프로모션 정보를 조회하는 간단한 Spring Boot 애플리케이션입니다. 레거시 시스템의 프로모션 조회 로직을 현대적인 Spring Data JPA와 RESTful API 형태로 구현하고, 성능 튜닝을 고려하여 개발되었습니다.

## 기술 스택

*   **언어**: Java 17
*   **프레임워크**: Spring Boot 3.x
*   **데이터베이스**: H2 Database (인메모리)
*   **ORM**: Spring Data JPA
*   **빌드 도구**: Maven
*   **템플릿 엔진**: Thymeleaf
*   **캐싱**: Spring Cache (기본 설정)

## 기능

*   현재 날짜를 기준으로 활성화된 프로모션 목록 조회
*   RESTful API를 통한 프로모션 데이터 제공
*   웹 페이지를 통한 프로모션 목록 시각화

## 설정 및 실행 방법

1.  **프로젝트 클론**: Git 저장소에서 프로젝트를 클론합니다.
    ```bash
    git clone <repository-url>
    cd legacy
    ```

2.  **의존성 설치**: Maven을 사용하여 프로젝트 의존성을 다운로드합니다.
    ```bash
    mvn clean install
    ```

3.  **애플리케이션 실행**: Spring Boot 애플리케이션을 실행합니다.
    ```bash
    mvn spring-boot:run
    ```
    또는 IDE(IntelliJ IDEA, Eclipse 등)에서 `LegacyApplication.java`를 실행합니다.

4.  **접속**: 애플리케이션이 성공적으로 실행되면 다음 URL로 접속할 수 있습니다.
    *   **웹 페이지**: `http://localhost:8080/`
    *   **API 엔드포인트**: `http://localhost:8080/api/v1/promotions`

## API 엔드포인트

### `GET /api/v1/promotions`

현재 날짜를 기준으로 활성화된 모든 프로모션 목록을 조회합니다.

*   **응답**: `List<PromotionResponseDto>`

    ```json
    [
      {
        "prodCd": "P001",
        "prodNm": "상품명1",
        "promoPrice": 10000,
        "startDt": "2025-01-01",
        "endDt": "2025-12-31"
      },
      // ...
    ]
    ```

## 데이터베이스 스키마 (H2)

`src/main/resources/schema.sql` 및 `src/main/resources/data.sql` 파일을 통해 초기 스키마 및 데이터가 설정됩니다.

*   **TB_PRODUCT**: 상품 정보 테이블
    *   `PROD_CD` (VARCHAR): 상품 코드 (PK)
    *   `PROD_NM` (VARCHAR): 상품명
    *   `USE_YN` (CHAR): 사용 여부 (Y/N)

*   **TB_PROMOTION**: 프로모션 정보 테이블
    *   `PROMO_ID` (BIGINT): 프로모션 ID (PK)
    *   `PROD_CD` (VARCHAR): 상품 코드 (FK, TB_PRODUCT 참조)
    *   `PROMO_PRICE` (DECIMAL): 프로모션 가격
    *   `START_DT` (DATE): 프로모션 시작일
    *   `END_DT` (DATE): 프로모션 종료일

## 튜닝 고려사항

`PromotionRepository`의 JPQL 쿼리는 `CURRENT_DATE`를 사용하여 `START_DT`와 `END_DT` 컬럼에 대한 인덱스 활용을 유도하도록 튜닝되었습니다. 이는 `TO_CHAR` 함수 사용으로 인한 인덱스 무효화를 방지하고, 인덱스 레인지 스캔을 통해 조회 성능을 개선하기 위함입니다.

최적의 튜닝을 위해서는 실제 데이터베이스 환경에서의 테이블 레이아웃(컬럼 타입, 인덱스 유무)과 데이터 분포를 기반으로 한 실행 계획 분석이 필수적입니다.

