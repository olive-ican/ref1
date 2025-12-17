package com.example.legacy.repository;

import com.example.legacy.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // @Param 사용을 위해 추가
import org.springframework.stereotype.Repository;
import com.example.legacy.dto.PromotionResponseDto;

import java.util.List;

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
     * 추가사항:
     * 정확한 테이블 레이아웃(컬럼 타입, 인덱스 유무)과 데이터 분포를 알 수 없으므로, 이 튜닝은 일반적으로 성능 개선에 도움이 되는 수준입니다.
     * 최적의 튜닝을 위해서는 실제 데이터베이스 환경에서의 실행 계획 분석이 필수적입니다.
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
