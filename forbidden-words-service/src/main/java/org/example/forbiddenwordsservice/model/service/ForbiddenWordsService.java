package org.example.forbiddenwordsservice.model.service;

import lombok.RequiredArgsConstructor;
import org.example.forbiddenwordsservice.model.model.ForbiddenWord;
import org.example.forbiddenwordsservice.model.repository.ForbiddenWordRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForbiddenWordsService {

    private final ForbiddenWordRepository repository;


    public List<String> getAllWords() {
        return repository.findAll().stream().map(ForbiddenWord::getWord).collect(Collectors.toList());
    }

    public void addWord(String word) {
        if (repository.findByWord(word).isPresent()) {
            throw new IllegalArgumentException("Word already exists");
        }
        ForbiddenWord fw = new ForbiddenWord();
        fw.setWord(word);
        repository.save(fw);
    }

    public void removeWord(String word) {
        repository.findByWord(word).ifPresent(repository::delete);
    }

    public boolean isForbiddenWord(String word) {
        return repository.findByWord(word).isPresent();
    }
}
