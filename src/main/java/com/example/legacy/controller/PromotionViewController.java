package com.example.legacy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PromotionViewController {

    /**
     * 루트 경로("/")로 접근 시 프로모션 목록 페이지를 반환합니다.
     * "promotions.html" 템플릿을 렌더링합니다.
     * @return 프로모션 뷰 페이지의 논리적 이름
     */
    @GetMapping("/")
    public String getPromotionsPage() {
        return "promotions";
    }
}
