package com.ascendant.initiative.service;

import com.ascendant.initiative.dto.mission.MissionDetailDto;
import com.ascendant.initiative.dto.mission.MissionSummaryDto;
import com.ascendant.initiative.exception.AppException;
import com.ascendant.initiative.model.Mission;
import com.ascendant.initiative.repository.EvaluationRepository;
import com.ascendant.initiative.repository.MissionRepository;
import com.ascendant.initiative.repository.ResponseRepository;
import com.ascendant.initiative.util.MissionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MissionService {

    private final MissionRepository missionRepository;
    private final ResponseRepository responseRepository;
    private final EvaluationRepository evaluationRepository;
    private final MissionMapper missionMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.cache.mission-ttl-seconds:3600}")
    private long cacheTtl;

    private static final String CACHE_PREFIX = "mission:";

    public List<MissionSummaryDto> getAllForUser(UUID userId) {
        List<Mission> missions = missionRepository.findByIsActiveTrueOrderByDifficultyLevelAsc();
        List<UUID> completedIds = getCompletedMissionIds(userId);

        return missions.stream().map(m -> {
            boolean completed = completedIds.contains(m.getId());
            double bestScore = getBestScore(userId, m.getId());
            boolean locked = false; // MVP: no level lock, add in v2
            return missionMapper.toSummary(m, locked, completed, completed ? bestScore : null);
        }).toList();
    }

    public MissionDetailDto getById(UUID missionId) {
        String cacheKey = CACHE_PREFIX + missionId;

        // Try Redis cache first
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Cache HIT for mission {}", missionId);
            return objectMapper.convertValue(cached, MissionDetailDto.class);
        }

        // Cache miss — fetch from DB
        log.debug("Cache MISS for mission {}", missionId);
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> AppException.notFound("Mission not found: " + missionId));

        if (!mission.getIsActive()) {
            throw AppException.notFound("Mission not found: " + missionId);
        }

        MissionDetailDto dto = missionMapper.toDetail(mission);

        // Populate Redis cache
        redisTemplate.opsForValue().set(cacheKey, dto, cacheTtl, TimeUnit.SECONDS);
        return dto;
    }

    public Optional<MissionDetailDto> getNextForUser(UUID userId) {
        return missionRepository.findNextForUser(userId).stream()
                .findFirst()
                .map(missionMapper::toDetail);
    }

    public void invalidateCache(UUID missionId) {
        redisTemplate.delete(CACHE_PREFIX + missionId);
        log.info("Cache invalidated for mission {}", missionId);
    }

    private List<UUID> getCompletedMissionIds(UUID userId) {
        return responseRepository.findByUserIdOrderByCreatedAtDesc(userId,
                org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(r -> evaluationRepository.findByResponseId(r.getId())
                        .map(e -> e.getEvalStatus() == com.ascendant.initiative.model.Evaluation.EvalStatus.DONE)
                        .orElse(false))
                .map(r -> r.getMission().getId())
                .distinct()
                .toList();
    }

    private double getBestScore(UUID userId, UUID missionId) {
        return responseRepository.findByUserIdAndMissionId(userId, missionId).stream()
                .map(r -> evaluationRepository.findByResponseId(r.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(e -> e.getFinalScore() != null)
                .mapToDouble(e -> e.getFinalScore())
                .max()
                .orElse(0.0);
    }
}
