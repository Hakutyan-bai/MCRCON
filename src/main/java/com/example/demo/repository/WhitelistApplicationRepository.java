package com.example.demo.repository;

import com.example.demo.entity.WhitelistApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WhitelistApplicationRepository extends JpaRepository<WhitelistApplication, Long> {
    // 可以根据需要添加自定义查询方法
    boolean existsByPlayerName(String playerName);
    boolean existsByEmail(String email);

    // 根据玩家名查找申请
    WhitelistApplication findByPlayerName(String playerName);
}
