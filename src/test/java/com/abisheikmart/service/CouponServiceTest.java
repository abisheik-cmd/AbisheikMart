package com.abisheikmart.service;

import com.abisheikmart.dao.CouponDao;
import com.abisheikmart.model.Coupon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CouponServiceTest {

    private CouponDao couponDao;
    private CouponService couponService;

    @BeforeEach
    void setUp() {
        couponDao = mock(CouponDao.class);
        couponService = new CouponService(couponDao);
    }

    @Test
    @DisplayName("Valid coupon with subtotal meeting minimum order amount should return Coupon")
    void testValidateCouponSuccess() {
        Coupon c = new Coupon(1L, "WELCOME10", "PERCENT", 10.0, 500.0);
        when(couponDao.findByCode("WELCOME10")).thenReturn(Optional.of(c));

        Optional<Coupon> opt = couponService.validateCoupon("WELCOME10", 1000.0);

        assertTrue(opt.isPresent());
        assertEquals("WELCOME10", opt.get().getCode());
        assertEquals(100.0, opt.get().calculateDiscount(1000.0));
    }

    @Test
    @DisplayName("Coupon with subtotal less than minimum order amount should return empty")
    void testValidateCouponBelowMinOrder() {
        Coupon c = new Coupon(1L, "WELCOME10", "PERCENT", 10.0, 500.0);
        when(couponDao.findByCode("WELCOME10")).thenReturn(Optional.of(c));

        Optional<Coupon> opt = couponService.validateCoupon("WELCOME10", 300.0);

        assertTrue(opt.isEmpty());
    }
}
