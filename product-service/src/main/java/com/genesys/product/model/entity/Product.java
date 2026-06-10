package com.genesys.product.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter @Setter
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Column(name = "NAME", nullable = false, length = 200)
    private String name;

    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Positive(message = "Stock must be positive")
    @Column(name = "STOCK_QUANTITY", nullable = false)
    private Integer stockQuantity;

    @Column(name = "SKU", unique = true, length = 50)
    private String sku;

    @Column(name = "CATEGORY", length = 100)
    private String category;

    @Column(name = "ACTIVE", nullable = false)
    private Boolean active = true;
}