package com.example.demo.service;

import com.example.demo.entity.WhitelistApplication;
import com.example.demo.repository.WhitelistApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WhitelistApplicationService {

    @Autowired
    private WhitelistApplicationRepository repository;

    public WhitelistApplication saveApplication(WhitelistApplication application) {
        return repository.save(application);
    }

    public boolean isPlayerNameExists(String playerName) {
        return repository.existsByPlayerName(playerName);
    }

    public boolean isEmailExists(String email) {
        return repository.existsByEmail(email);
    }

    // 管理功能
    public List<WhitelistApplication> getAllApplications() {
        return repository.findAll();
    }

    public Optional<WhitelistApplication> getApplicationById(Long id) {
        return repository.findById(id);
    }

    public void deleteApplication(Long id) {
        repository.deleteById(id);
    }

    public WhitelistApplication updateApplication(WhitelistApplication application) {
        return repository.save(application);
    }

    // 根据玩家名查找申请
    public WhitelistApplication getApplicationByPlayerName(String playerName) {
        return repository.findByPlayerName(playerName);
    }
}
