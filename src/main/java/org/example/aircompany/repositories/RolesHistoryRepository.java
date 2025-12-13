package org.example.aircompany.repositories;

import org.example.aircompany.model.RolesHistory;
import org.example.aircompany.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolesHistoryRepository extends JpaRepository<RolesHistory, Long> {
    List<RolesHistory> findByTargetUser(User targetUser);
    List<RolesHistory> findByAdminUser(User adminUser);
    void deleteByTargetUser(User targetUser);
    void deleteByAdminUser(User adminUser);
}
