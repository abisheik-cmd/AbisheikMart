package com.abisheikmart.controller;

import com.abisheikmart.dto.ApiResponse;
import com.abisheikmart.model.Coupon;
import com.abisheikmart.service.CouponService;
import com.abisheikmart.util.JsonUtil;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/api/coupon/apply")
public class CouponServlet extends HttpServlet {

    private final CouponService couponService = new CouponService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            JsonObject json = JsonUtil.fromJson(req.getReader(), JsonObject.class);
            String code = json.has("code") ? json.get("code").getAsString() : "";
            double subtotal = json.has("subtotal") ? json.get("subtotal").getAsDouble() : 0.0;

            Optional<Coupon> couponOpt = couponService.validateCoupon(code, subtotal);
            if (couponOpt.isPresent()) {
                Coupon c = couponOpt.get();
                double discount = c.calculateDiscount(subtotal);
                double finalTotal = Math.max(0.0, subtotal - discount);

                JsonObject data = new JsonObject();
                data.addProperty("code", c.getCode());
                data.addProperty("discountType", c.getDiscountType());
                data.addProperty("discountValue", c.getDiscountValue());
                data.addProperty("discountAmount", discount);
                data.addProperty("finalTotal", finalTotal);

                resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok("Coupon applied successfully!", data)));
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("Invalid coupon code or minimum order amount not met.")));
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("Error applying coupon: " + e.getMessage())));
        }
    }
}
