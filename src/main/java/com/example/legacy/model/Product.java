package com.example.legacy.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TB_PRODUCT")
@Getter
@Setter
public class Product {

    @Id
    @Column(name = "PROD_CD")
    private String prodCd;

    @Column(name = "PROD_NM")
    private String prodNm;

    @Column(name = "USE_YN")
    private char useYn;
}

