package com.example.legacy.service;

import com.example.legacy.dto.PromotionResponseDto;
import com.example.legacy.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository promotionRepository;

    @Cacheable("promotions")
    public List<PromotionResponseDto> getPromotions() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return promotionRepository.findCurrentPromotions(today);
    }
}
