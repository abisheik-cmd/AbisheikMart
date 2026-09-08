package com.abisheikmart.dto;

public class CartRequest {
    private Long productId;
    private Integer quantity;

    public CartRequest() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
