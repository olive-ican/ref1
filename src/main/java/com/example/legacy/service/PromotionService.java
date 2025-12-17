package com.example.legacy.service;

import com.example.legacy.dto.PromotionResponseDto;
import com.example.legacy.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository promotionRepository;

    @Cacheable("promotions")
    public List<PromotionResponseDto> getPromotions() {
        return promotionRepository.findCurrentPromotions();
    }
}
