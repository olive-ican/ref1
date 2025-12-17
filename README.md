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
    *   `START_DT` (VARCHAR(8)): 프로모션 시작일
    *   `END_DT` (VARCHAR(8)): 프로모션 종료일

## 튜닝 고려사항
### [AS-IS 쿼리 및 문제점]

```sql
SELECT ... FROM TB_PRODUCT A, TB_PROMOTION B ...
WHERE TO_CHAR(SYSDATE, 'YYYYMMDD') BETWEEN START_DT AND END_DT
```

*   **문제 1**: Inline View 사용으로 인한 비효율 (옵티마이저가 뷰 병합을 못할 경우 전체 데이터 조인 후 필터링)
*   **문제 2**: `TO_CHAR` 변환 등 DB 함수 의존 및 암시적 조인 사용으로 가독성 저하

### [TO-BE 튜닝 포인트]

1.  **바인딩 변수(:today) 사용**:
    *   레거시 DB의 날짜 컬럼(`START_DT`, `END_DT`)이 `VARCHAR('YYYYMMDD')` 형식이므로, DB 함수 `TO_CHAR`를 사용하여 날짜를 변환하면 해당 컬럼에 생성된 인덱스를 활용할 수 없게 됩니다(Full Table Scan 발생). 이를 방지하기 위해 JPQL의 `CURRENT_DATE`(Date 타입) 대신 Java 애플리케이션 단에서 `LocalDate.now().format("yyyyMMdd")`와 같이 동일한 포맷의 `String` 파라미터(`:today`)를 생성하여 넘겨줍니다. 이로써 DB에서는 `String` vs `String` 비교가 이루어져 '타입 불일치로 인한 인덱스 미사용'을 방지하고 인덱스 레인지 스캔을 통해 성능을 향상시킵니다.
2.  **명시적 JOIN (Standard Join)**:
    *   JPA 엔티티 연관관계를 활용한 `JOIN pm.product p` 구문으로 변경하여 가독성을 높이고, 불필요한 카테시안 곱 발생 가능성을 차단하며 JPA 표준에 부합합니다.
3.  **쿼리 구조 개선 (Flat Query)**:
    *   서브쿼리를 제거하고 `WHERE` 절에서 날짜 범위를 직접 필터링하여, DB 옵티마이저가 날짜 인덱스를 통해 대상 범위를 먼저 좁히고(Driving) 조인을 수행하도록 유도합니다.

### 오라클에서 변경 시, TO-BE 쿼리 예시
```sql
SELECT A.PROD_CD,
       A.PROD_NM,
       B.PROMO_PRICE,
       B.START_DT,
       B.END_DT
FROM TB_PRODUCT A
INNER JOIN TB_PROMOTION B
    ON A.PROD_CD = B.PROD_CD
WHERE A.USE_YN = 'Y'
  AND TO_CHAR(SYSDATE, 'YYYYMMDD') BETWEEN B.START_DT AND B.END_DT;
```

### SQL과 Java 결합을 통한 예시
```java
@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /*
     * [AS-IS 쿼리 및 문제점]
     * SELECT ... FROM TB_PRODUCT A, TB_PROMOTION B ...
     * WHERE TO_CHAR(SYSDATE, 'YYYYMMDD') BETWEEN START_DT AND END_DT
     * -> 문제 1: Inline View 사용으로 인한 비효율 (옵티마이저가 뷰 병합을 못할 경우 전체 데이터 조인 후 필터링)
     * -> 문제 2: TO_CHAR 변환 등 DB 함수 의존 및 암시적 조인 사용으로 가독성 저하
     *
     * [TO-BE 튜닝 포인트]
     * 1. 바인딩 변수(:today) 사용:
     *    - 레거시 DB의 날짜 컬럼(START_DT, END_DT)이 VARCHAR('YYYYMMDD') 형식이므로,
     *      JPQL의 CURRENT_DATE(Date 타입) 대신 Java에서 동일한 포맷의 String 파라미터(:today)를 넘겨
     *      '타입 불일치로 인한 인덱스 미사용'을 방지하고 String vs String 비교를 통해 인덱스 작동을 유도합니다.
     * 2. 명시적 JOIN (Standard Join):
     *    - JPA 엔티티 연관관계를 활용한 `JOIN pm.product p` 구문으로 변경하여 가독성을 높이고,
     *      불필요한 카테시안 곱 발생 가능성을 차단하며 JPA 표준에 부합합니다.
     * 3. 쿼리 구조 개선 (Flat Query):
     *    - 서브쿼리를 제거하고 WHERE 절에서 날짜 범위를 직접 필터링하여,
     *      DB 옵티마이저가 날짜 인덱스를 통해 대상 범위를 먼저 좁히고(Driving) 조인을 수행하도록 유도합니다.
     *
     */
    @Query("SELECT new com.example.legacy.dto.PromotionResponseDto(" +
           "  p.prodCd, p.prodNm, pm.promoPrice, pm.startDt, pm.endDt) " +
           "FROM Promotion pm " +
           "JOIN pm.product p " +
           "WHERE p.useYn = 'Y' " +
           "AND pm.startDt <= :today " +
           "AND pm.endDt >= :today")
    List<PromotionResponseDto> findCurrentPromotions(@Param("today") String today);
}
```

**최적의 튜닝을 위해서는 실제 데이터베이스 환경에서의 테이블 레이아웃(컬럼 타입, 인덱스 유무)과 데이터 분포를 기반으로 한 실행 계획 분석이 필수적입니다.**