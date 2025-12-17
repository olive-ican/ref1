package com.example.legacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionResponseDto {
    private String prodCd;
    private String prodNm;
    private Integer promoPrice;
    private LocalDate startDt;
    private LocalDate endDt;
}
