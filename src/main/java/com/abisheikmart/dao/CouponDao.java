package com.abisheikmart.dao;

import com.abisheikmart.model.Coupon;
import com.abisheikmart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class CouponDao {

    private static final Logger logger = LoggerFactory.getLogger(CouponDao.class);

    public Optional<Coupon> findByCode(String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        String sql = "SELECT id, code, discount_type, discount_value, min_order_amount, created_at FROM coupons WHERE UPPER(code) = UPPER(?);";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Coupon coupon = new Coupon();
                    coupon.setId(rs.getLong("id"));
                    coupon.setCode(rs.getString("code"));
                    coupon.setDiscountType(rs.getString("discount_type"));
                    coupon.setDiscountValue(rs.getDouble("discount_value"));
                    coupon.setMinOrderAmount(rs.getDouble("min_order_amount"));
                    coupon.setCreatedAt(rs.getTimestamp("created_at"));
                    return Optional.of(coupon);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding coupon by code: {}", code, e);
        }
        return Optional.empty();
    }
}
