package com.abisheikmart.service;

import com.abisheikmart.dao.NotificationDao;
import com.abisheikmart.model.Notification;

import java.sql.SQLException;
import java.util.List;

public class NotificationService {

    private final NotificationDao notificationDao;

    public NotificationService() {
        this.notificationDao = new NotificationDao();
    }

    public NotificationService(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    public Notification notifyUser(Long userId, String title, String message, String link) throws SQLException {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        n.setLink(link);
        n.setIsRead(false);
        return notificationDao.save(n);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationDao.findByUserId(userId);
    }

    public boolean markRead(Long userId) {
        return notificationDao.markAllAsRead(userId);
    }
}
