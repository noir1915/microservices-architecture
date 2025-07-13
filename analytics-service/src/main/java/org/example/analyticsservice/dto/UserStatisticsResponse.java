package org.example.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserStatisticsResponse {

    private Long userId;
    private int totalPosts;
    private int publishedPosts;
    private int rejectedPosts;
    private Map<String, Integer> rejectionReasonsCount;
    private List<String> topForbiddenWords;
}
