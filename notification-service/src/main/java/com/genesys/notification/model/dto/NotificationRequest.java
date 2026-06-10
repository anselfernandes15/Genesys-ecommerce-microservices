package com.genesys.notification.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class NotificationRequest {

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("customer_email")
    private String customerEmail;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("product_name")
    private String productName;

    private Integer quantity;

    @JsonProperty("total_price")
    private BigDecimal totalPrice;
}