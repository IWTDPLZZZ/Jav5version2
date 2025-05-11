package orf.demo.service;

import orf.demo.checker.SpellChecker;
import orf.demo.dto.BulkSpellCheckRequest;
import orf.demo.dto.SpellCheckResponse;
import orf.demo.model.SpellCheckCategory;
import orf.demo.repository.SpellCheckCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SpellCheckServiceImpl implements SpellCheckService {

    private final SpellChecker simpleSpellChecker;
    private final SpellChecker apiSpellChecker;
    private final SpellCheckCategoryRepository spellCheckCategoryRepository;

    @Autowired
    public SpellCheckServiceImpl(
            @Qualifier("simpleSpellChecker") SpellChecker simpleSpellChecker,
            @Qualifier("apiSpellChecker") SpellChecker apiSpellChecker,
            SpellCheckCategoryRepository spellCheckCategoryRepository) {
        this.simpleSpellChecker = simpleSpellChecker;
        this.apiSpellChecker = apiSpellChecker;
        this.spellCheckCategoryRepository = spellCheckCategoryRepository;
    }

    @Override
    public String checkSpelling(String word) {
        return simpleSpellChecker.checkSpelling(word);
    }

    @Override
    public List<SpellCheckResponse> checkSpellingBulk(List<String> texts) {
        if (texts == null) {
            throw new IllegalArgumentException("Список текстов не может быть null");
        }
        return texts.stream()
                .map(text -> {
                    String result = simpleSpellChecker.checkSpelling(text);
                    return new SpellCheckResponse(text, "Correct".equals(result));
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SpellCheckResponse> checkSpellingBulkWithParams(BulkSpellCheckRequest request) {
        if (request == null || request.getTexts() == null || request.getTexts().isEmpty()) {
            throw new IllegalArgumentException("Запрос или список текстов не могут быть null или пустыми");
        }

        SpellChecker selectedChecker = "api".equalsIgnoreCase(request.getCheckerType()) ?
                apiSpellChecker : simpleSpellChecker;

        List<SpellCheckResponse> responses = request.getTexts().stream()
                .filter(Objects::nonNull)
                .map(text -> {
                    String result = selectedChecker.checkSpelling(text);
                    boolean isCorrect = "Correct".equals(result);

                    // Если указана категория, сохраняем результат проверки в базу данных
                    if (request.getCategoryName() != null && !request.getCategoryName().isEmpty()) {
                        SpellCheckCategory spellCheck = new SpellCheckCategory();
                        spellCheck.setName(text);
                        spellCheck.setStatus(isCorrect ? "Correct" : "Incorrect");
                        spellCheck.setError(isCorrect ? null : "Ошибка орфографии");
                        spellCheckCategoryRepository.save(spellCheck);
                    }

                    return new SpellCheckResponse(text, isCorrect);
                })
                .collect(Collectors.toList());

        return responses;
    }
}