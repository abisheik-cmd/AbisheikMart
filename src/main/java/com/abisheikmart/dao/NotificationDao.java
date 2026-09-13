package com.abisheikmart.dao;

import com.abisheikmart.model.Notification;
import com.abisheikmart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDao.class);

    public Notification save(Notification notification) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, title, message, link, is_read) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, notification.getUserId());
            ps.setString(2, notification.getTitle());
            ps.setString(3, notification.getMessage());
            ps.setString(4, notification.getLink() != null ? notification.getLink() : "#");
            ps.setBoolean(5, notification.getIsRead() != null ? notification.getIsRead() : false);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    notification.setId(keys.getLong(1));
                }
            }
            return notification;
        }
    }

    public List<Notification> findByUserId(Long userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT id, user_id, title, message, COALESCE(link, '#') AS link, is_read, created_at FROM notifications WHERE user_id = ? ORDER BY id DESC LIMIT 20;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getLong("id"));
                    n.setUserId(rs.getLong("user_id"));
                    n.setTitle(rs.getString("title"));
                    n.setMessage(rs.getString("message"));
                    n.setLink(rs.getString("link"));
                    n.setIsRead(rs.getBoolean("is_read"));
                    n.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(n);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding notifications for userId: {}", userId, e);
        }
        return list;
    }

    public boolean markAllAsRead(Long userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE;";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error marking notifications read for userId: {}", userId, e);
            return false;
        }
    }
}
