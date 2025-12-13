package org.example.aircompany.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "roles_history")
@Data
public class RolesHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    // Связь Many-to-One: пользователь, чья роль была изменена
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    // Связь Many-to-One: администратор, который внес изменения
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private User adminUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_role", nullable = false)
    private UserRole oldRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_role", nullable = false)
    private UserRole newRole;

    @Column(name = "change_timestamp", nullable = false)
    private LocalDateTime changeTimestamp = LocalDateTime.now();
}
