package com.csm.service;

import com.csm.model.Notification;
import com.csm.model.User;
import com.csm.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;

    @Transactional
    public void send(User user, String message, String type, String link) {
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message);
        n.setType(type);
        n.setLink(link);
        n.setRead(false);
        repo.save(n);
    }

    @Transactional(readOnly = true)
    public List<Notification> getAllForUser(User user) {
        return repo.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadForUser(User user) {
        return repo.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public long countUnread(User user) {
        return repo.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public void markAllRead(User user) {
        repo.markAllReadForUser(user);
    }

    @Transactional
    public void markRead(Long id) {
        repo.findById(id).ifPresent(n -> {
            n.setRead(true);
            repo.save(n);
        });
    }
}
