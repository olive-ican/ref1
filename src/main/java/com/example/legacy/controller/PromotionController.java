package com.example.legacy.controller;

import com.example.legacy.dto.PromotionResponseDto;
import com.example.legacy.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    /**
     * 현재 진행 중인 모든 프로모션 목록을 조회합니다.
     * 이 API는 날짜 파라미터 없이 현재 시스템 날짜를 기준으로 프로모션을 반환합니다.
     * @return PromotionResponseDto 목록
     */
    @GetMapping
    public ResponseEntity<List<PromotionResponseDto>> getPromotions() {
        List<PromotionResponseDto> result = promotionService.getPromotions();
        return ResponseEntity.ok(result);
    }
}
