package com.example.legacy.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "TB_PROMOTION")
@Getter
@Setter
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROMO_ID")
    private Long promoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROD_CD")
    private Product product;

    @Column(name = "PROMO_PRICE")
    private Integer promoPrice;

    @Column(name = "START_DT")
    private LocalDate startDt;

    @Column(name = "END_DT")
    private LocalDate endDt;
}
