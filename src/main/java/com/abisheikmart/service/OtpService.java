package com.abisheikmart.service;

import com.abisheikmart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Random;

public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    public String generateAndSendOtp(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().length() < 10) {
            throw new IllegalArgumentException("Valid 10-digit mobile number required");
        }

        String phone = phoneNumber.replaceAll("[^0-9]", "");
        String otpCode = String.format("%06d", new Random().nextInt(900000) + 100000);
        Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + (10 * 60 * 1000)); // 10 minutes

        String sql = "MERGE INTO user_otps (phone_number, otp_code, expires_at) KEY(phone_number) VALUES (?, ?, ?);";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setString(2, otpCode);
            ps.setTimestamp(3, expiresAt);
            ps.executeUpdate();
            logger.info("Generated OTP {} for mobile number: {}", otpCode, phone);
        } catch (Exception e) {
            logger.error("Error generating OTP for phone: {}", phone, e);
            throw new RuntimeException("Could not generate OTP", e);
        }
        return otpCode;
    }

    public boolean verifyOtp(String phoneNumber, String inputOtp) {
        if (phoneNumber == null || inputOtp == null) return false;
        String phone = phoneNumber.replaceAll("[^0-9]", "");
        String sql = "SELECT otp_code, expires_at FROM user_otps WHERE phone_number = ?;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedOtp = rs.getString("otp_code");
                    Timestamp expiresAt = rs.getTimestamp("expires_at");
                    if (storedOtp.equals(inputOtp.trim()) && expiresAt.after(new Timestamp(System.currentTimeMillis()))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error verifying OTP for phone: {}", phone, e);
        }
        return false;
    }
}
