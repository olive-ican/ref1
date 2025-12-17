package com.example.legacy.repository;

import com.example.legacy.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.legacy.dto.PromotionResponseDto;

import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /*
     * 기존 쿼리:
     * SELECT * FROM ( SELECT A.PROD_CD, A.PROD_NM, B.PROMO_PRICE, B.START_DT, B.END_DT FROM TB_PRODUCT A, TB_PROMOTION B WHERE A.PROD_CD = B.PROD_CD AND A.USE_YN = 'Y' ) WHERE TO_CHAR(SYSDATE, 'YYYYMMDD') BETWEEN START_DT AND END_DT
     *
     * 튜닝된 JPQL 쿼리 설명:
     * 1. `TO_CHAR(SYSDATE, 'YYYYMMDD') BETWEEN START_DT AND END_DT` 대신 `p.startDt <= CURRENT_DATE AND p.endDt >= CURRENT_DATE`를 사용합니다.
     *    이는 데이터베이스의 날짜 컬럼에 인덱스가 있을 경우, `TO_CHAR` 함수 사용으로 인한 타입 불일치로 인한 인덱스 무효화를 방지하고 인덱스 레인지 스캔을 유도하여 성능을 향상시킵니다.
     * 2. `JOIN p.product pr`은 JPA 엔티티 관계를 활용한 명시적인 조인으로, 가독성과 유지보수성을 높입니다.
     *
     * 추가사항:
     * 정확한 테이블 레이아웃(컬럼 타입, 인덱스 유무)과 데이터 분포를 알 수 없으므로, 이 튜닝은 일반적으로 성능 개선에 도움이 되는 수준입니다.
     * 최적의 튜닝을 위해서는 실제 데이터베이스 환경에서의 실행 계획 분석이 필수적입니다.
     */
    @Query("SELECT new com.example.legacy.dto.PromotionResponseDto(" +
           "  p.product.prodCd, p.product.prodNm, p.promoPrice, p.startDt, p.endDt) " +
           "FROM Promotion p " +
           "JOIN p.product pr " +
           "WHERE pr.useYn = 'Y' " +
           "AND p.startDt <= CURRENT_DATE " +
           "AND p.endDt >= CURRENT_DATE")
    List<PromotionResponseDto> findCurrentPromotions();
}
