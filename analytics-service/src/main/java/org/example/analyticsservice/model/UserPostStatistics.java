package org.example.analyticsservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@Table(name = "user_post_statistics")
@AllArgsConstructor
@NoArgsConstructor
public class UserPostStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long userId;

    private int totalPosts;
    private int publishedPosts;
    private int rejectedPosts;

    @Convert(converter = MapToJsonConverter.class)
    private Map<String, Integer> rejectionReasonsCount = new HashMap<>();

    @Convert(converter = MapToJsonConverter.class)
    private Map<String, Integer> forbiddenWordsCount = new HashMap<>();

}