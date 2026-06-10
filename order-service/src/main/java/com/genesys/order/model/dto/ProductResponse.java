package com.genesys.order.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductResponse {

    private Long id;
    private String name;
    private BigDecimal price;

    @JsonProperty("stock_quantity")
    private Integer stockQuantity;

    private Boolean active;
}