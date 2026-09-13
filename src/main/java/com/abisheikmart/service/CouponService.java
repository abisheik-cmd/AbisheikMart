package com.abisheikmart.service;

import com.abisheikmart.dao.CouponDao;
import com.abisheikmart.model.Coupon;

import java.util.Optional;

public class CouponService {

    private final CouponDao couponDao;

    public CouponService() {
        this.couponDao = new CouponDao();
    }

    public CouponService(CouponDao couponDao) {
        this.couponDao = couponDao;
    }

    public Optional<Coupon> validateCoupon(String code, double cartSubtotal) {
        if (code == null || code.isBlank()) return Optional.empty();
        Optional<Coupon> opt = couponDao.findByCode(code.trim());
        if (opt.isPresent()) {
            Coupon coupon = opt.get();
            if (cartSubtotal >= coupon.getMinOrderAmount()) {
                return Optional.of(coupon);
            }
        }
        return Optional.empty();
    }
}
