package com.ascendant.initiative.engine.ai;

import com.ascendant.initiative.model.AiCostLog;
import com.ascendant.initiative.model.User;
import com.ascendant.initiative.repository.AiCostLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class CostController {

    private final AiCostLogRepository aiCostLogRepository;

    @Value("${app.ai.daily-limit-per-user:10}")
    private int dailyLimit;

    @Value("${app.ai.claude.model:claude-sonnet-4-20250514}")
    private String model;

    public boolean hasRemainingQuota(User user) {
        long todayCount = aiCostLogRepository.countByUserIdAndCallDate(
            user.getId(), LocalDate.now()
        );
        return todayCount < dailyLimit;
    }

    public void logUsage(User user, int tokensUsed) {
        AiCostLog log = AiCostLog.builder()
            .user(user)
            .tokensUsed(tokensUsed)
            .model(model)
            .callDate(LocalDate.now())
            .build();
        aiCostLogRepository.save(log);
    }

    public long getRemainingCalls(User user) {
        long used = aiCostLogRepository.countByUserIdAndCallDate(
            user.getId(), LocalDate.now()
        );
        return Math.max(0, dailyLimit - used);
    }
}
