package org.example.analyticsservice.controller;

import org.example.analyticsservice.dto.PostData;
import org.example.analyticsservice.dto.UserStatisticsResponse;
import org.example.analyticsservice.model.UserPostStatistics;
import org.example.analyticsservice.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserStatisticsResponse> getUserStatistics(@PathVariable("userId") Long userId) {
        Optional<UserPostStatistics> stats = statisticsService.getUserStatistics(userId);
        return stats.map(userPostStatistics -> ResponseEntity.ok(new UserStatisticsResponse(
                userPostStatistics.getId(),
                userPostStatistics.getTotalPosts(),
                userPostStatistics.getPublishedPosts(),
                userPostStatistics.getRejectedPosts(),
                userPostStatistics.getRejectionReasonsCount(),
                (List<String>) userPostStatistics.getForbiddenWordsCount()
        ))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/overall")
    public ResponseEntity<UserPostStatistics> getOverallStatistics() {
        return ResponseEntity.ok(statisticsService.getOverallStatistics());
    }

    @PostMapping("/")
    public ResponseEntity<Void> receivePost(@RequestBody PostData postData) {
        boolean published = postData.isPublished();
        String reason = postData.getRejectionReason();
        Long userId = postData.getUserId();
        String content = postData.getContent();

        statisticsService.updateStatistics(userId, published, reason, content);

        return ResponseEntity.ok().build();
    }
}