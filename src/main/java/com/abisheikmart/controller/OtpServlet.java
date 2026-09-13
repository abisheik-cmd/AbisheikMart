package com.abisheikmart.controller;

import com.abisheikmart.dto.ApiResponse;
import com.abisheikmart.service.OtpService;
import com.abisheikmart.util.JsonUtil;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/otp/*")
public class OtpServlet extends HttpServlet {

    private final OtpService otpService = new OtpService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        try {
            JsonObject json = JsonUtil.fromJson(req.getReader(), JsonObject.class);
            String phone = json.has("phone") ? json.get("phone").getAsString() : "";

            if ("/send".equalsIgnoreCase(pathInfo)) {
                String otpCode = otpService.generateAndSendOtp(phone);
                JsonObject data = new JsonObject();
                data.addProperty("phone", phone);
                data.addProperty("otpSimulated", otpCode); // Included for convenient testing
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok("OTP sent successfully to " + phone, data)));
            } else if ("/verify".equalsIgnoreCase(pathInfo)) {
                String otpInput = json.has("otp") ? json.get("otp").getAsString() : "";
                boolean valid = otpService.verifyOtp(phone, otpInput);
                if (valid) {
                    req.getSession().setAttribute("verified_phone", phone);
                    resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok("Mobile number verified successfully!", null)));
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("Invalid or expired OTP code.")));
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("OTP Endpoint Not Found")));
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("OTP Error: " + e.getMessage())));
        }
    }
}
