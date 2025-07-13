package org.example.postsservice.service;

import lombok.RequiredArgsConstructor;
import org.example.forbiddenwordsservice.model.service.ForbiddenWordsService;
import org.example.postsservice.model.Post;
import org.example.postsservice.repository.PostRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostsService {

    @Value("${post.max.length}")
    private int maxPostLength;

    final private PostRepository postRepository;

    private final RestTemplate restTemplate; // для вызова аналитики

    public Post createPost(Long userId, String content) {
        if (content.length() > maxPostLength) {
            savePost(userId, content, false, "length");
            sendToAnalytics(userId, false, "length");
            throw new RuntimeException("Post length exceeds limit");
        }

        List<String> forbiddenWords = getAllForbiddenWords();

        for (String forbidden : forbiddenWords) {
            if (content.contains(forbidden)) {
                savePost(userId, content, false, "forbidden");
                sendToAnalytics(userId, false, "forbidden");
                throw new RuntimeException("Contains forbidden words");
            }
        }

        Post post = savePost(userId, content, true, null);
        sendToAnalytics(userId, true, null);
        return post;
    }

    public List<String> getAllForbiddenWords() {
        String url = "http://localhost:8085/api/forbidden-words"; // адрес вашего сервиса forbiddenwordsservice
        String[] words = restTemplate.getForObject(url, String[].class);
        return Arrays.asList(words);
    }

    private Post savePost(Long userId, String content, boolean published, String reason) {
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content);
        post.setPublished(published);
        post.setRejectionReason(reason);
        post.setCreatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    private void sendToAnalytics(Long userId, boolean published, String reason) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("published", published);
        payload.put("reason", reason);

        restTemplate.postForObject("http://localhost:8082/analytics/post", payload, Void.class);

    }

    public List<Post> getPostsByUser(Long userId) {
        return postRepository.findByUserId(userId);
    }
}