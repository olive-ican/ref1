package com.example.legacy.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

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
    private String startDt;

    @Column(name = "END_DT")
    private String endDt;
}
