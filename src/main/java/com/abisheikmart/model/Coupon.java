package com.abisheikmart.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Coupon implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private String discountType; // PERCENT, FLAT
    private Double discountValue;
    private Double minOrderAmount;
    private Timestamp createdAt;

    public Coupon() {}

    public Coupon(Long id, String code, String discountType, Double discountValue, Double minOrderAmount) {
        this.id = id;
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public Double getDiscountValue() { return discountValue; }
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }

    public Double getMinOrderAmount() { return minOrderAmount != null ? minOrderAmount : 0.0; }
    public void setMinOrderAmount(Double minOrderAmount) { this.minOrderAmount = minOrderAmount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public double calculateDiscount(double subtotal) {
        if (subtotal < getMinOrderAmount()) {
            return 0.0;
        }
        if ("PERCENT".equalsIgnoreCase(discountType)) {
            return Math.round((subtotal * (discountValue / 100.0)) * 100.0) / 100.0;
        } else if ("FLAT".equalsIgnoreCase(discountType)) {
            return Math.min(discountValue, subtotal);
        }
        return 0.0;
    }
}
