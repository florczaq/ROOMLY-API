package org.roomly.notifications.repositories;

import org.roomly.notifications.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    
    @Query("SELECT n FROM Notification n WHERE n.profile.account.id = :accountId AND n.read = false ORDER BY n.timestamp ASC")
    List<Notification> findAllByAccountIdUnreadAndOrderFromOldest (@Param("accountId") String accountId);
}
