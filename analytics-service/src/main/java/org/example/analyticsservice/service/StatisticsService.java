package org.example.analyticsservice.service;

import lombok.RequiredArgsConstructor;
import org.example.analyticsservice.model.UserPostStatistics;
import org.example.analyticsservice.repository.UserPostStatisticsRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final UserPostStatisticsRepository repository;

    private final UserPostStatistics overallStats = new UserPostStatistics();

    private static final List<String> FORBIDDEN_WORDS = Arrays.asList(
            "spam", "badword", "forbidden", "banned");

    public void updateStatistics(Long userId, boolean published, String reason, String content) {
        UserPostStatistics stats = repository.findByUserId(userId).orElseGet(() -> {
            UserPostStatistics newStats = new UserPostStatistics();
            newStats.setUserId(userId);
            newStats.setRejectionReasonsCount(new HashMap<>());
            newStats.setForbiddenWordsCount(new HashMap<>());
            return newStats;
        });

        stats.setTotalPosts(stats.getTotalPosts() + 1);

        if (published) {
            stats.setPublishedPosts(stats.getPublishedPosts() + 1);
        } else {
            stats.setRejectedPosts(stats.getRejectedPosts() + 1);
            if (reason != null) {
                stats.getRejectionReasonsCount().merge(reason, 1, Integer::sum);
            }
        }

        if (content != null && "forbidden".equals(reason)) {
            List<String> forbiddenWords = extractForbiddenWords(content);
            for (String word : forbiddenWords) {
                stats.getForbiddenWordsCount().merge(word, 1, Integer::sum);
            }
        }

        repository.save(stats);
    }

    public List<String> extractForbiddenWords(String content) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }

        String[] wordsArray = content.toLowerCase()
                .split("[\\s\\p{Punct}]+");


        return Arrays.stream(wordsArray)
                .filter(FORBIDDEN_WORDS::contains)
                .collect(Collectors.toList());
    }


    public Optional<UserPostStatistics> getUserStatistics(Long userId) {
        return repository.findByUserId(userId);
    }

    public UserPostStatistics getOverallStatistics() {
        return overallStats;
    }
}
