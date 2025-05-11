package orf.demo.service;

import orf.demo.checker.SpellChecker;
import orf.demo.dto.BulkSpellCheckRequest;
import orf.demo.dto.SpellCheckResponse;
import orf.demo.model.SpellCheckCategory;
import orf.demo.repository.SpellCheckCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpellCheckServiceImplTest {

    @Mock
    private SpellChecker simpleSpellChecker;

    @Mock
    private SpellChecker apiSpellChecker;

    @Mock
    private SpellCheckCategoryRepository spellCheckCategoryRepository;

    @InjectMocks
    private SpellCheckServiceImpl spellCheckService;

    @Test
    void testCheckSpelling_ValidText_ReturnsCorrectResponse() {
        String text = "hello";
        when(simpleSpellChecker.checkSpelling(text)).thenReturn("Correct");
        String result = spellCheckService.checkSpelling(text);
        assertEquals("Correct", result);
        verify(simpleSpellChecker).checkSpelling(text);
    }

    @Test
    void testCheckSpelling_InvalidText_ReturnsIncorrectResponse() {
        String text = "hi";
        when(simpleSpellChecker.checkSpelling(text)).thenReturn("Incorrect");
        String result = spellCheckService.checkSpelling(text);
        assertEquals("Incorrect", result);
        verify(simpleSpellChecker).checkSpelling(text);
    }

    @Test
    void testCheckSpellingBulk_MixedTexts_ReturnsCorrectResults() {
        List<String> texts = Arrays.asList("hello", "hi", "world");
        when(simpleSpellChecker.checkSpelling("hello")).thenReturn("Correct");
        when(simpleSpellChecker.checkSpelling("hi")).thenReturn("Incorrect");
        when(simpleSpellChecker.checkSpelling("world")).thenReturn("Correct");

        List<SpellCheckResponse> results = spellCheckService.checkSpellingBulk(texts);
        assertEquals(3, results.size());
        assertEquals("hello", results.get(0).getText());
        assertTrue(results.get(0).isCorrect());
        assertEquals("hi", results.get(1).getText());
        assertFalse(results.get(1).isCorrect());
        assertEquals("world", results.get(2).getText());
        assertTrue(results.get(2).isCorrect());
        verify(simpleSpellChecker, times(3)).checkSpelling(anyString());
    }

    @Test
    void testCheckSpellingBulk_EmptyList_ReturnsEmptyList() {
        List<String> texts = Arrays.asList();
        List<SpellCheckResponse> results = spellCheckService.checkSpellingBulk(texts);
        assertTrue(results.isEmpty());
        verify(simpleSpellChecker, never()).checkSpelling(anyString());
    }

    @Test
    void testCheckSpellingBulk_NullList_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            spellCheckService.checkSpellingBulk(null);
        });
        verify(simpleSpellChecker, never()).checkSpelling(anyString());
    }

    @Test
    void testCheckSpellingBulkWithParams_SimpleChecker_MixedTexts_ReturnsCorrectResults() {
        BulkSpellCheckRequest request = new BulkSpellCheckRequest();
        request.setTexts(Arrays.asList("hello", null, "world"));
        request.setCheckerType("simple");
        request.setCategoryName(null);

        when(simpleSpellChecker.checkSpelling("hello")).thenReturn("Correct");
        when(simpleSpellChecker.checkSpelling("world")).thenReturn("Correct");

        List<SpellCheckResponse> results = spellCheckService.checkSpellingBulkWithParams(request);
        assertEquals(2, results.size());
        assertEquals("hello", results.get(0).getText());
        assertTrue(results.get(0).isCorrect());
        assertEquals("world", results.get(1).getText());
        assertTrue(results.get(1).isCorrect());
        verify(simpleSpellChecker, times(2)).checkSpelling(anyString());
        verify(apiSpellChecker, never()).checkSpelling(anyString());
        verify(spellCheckCategoryRepository, never()).save(any());
    }

    @Test
    void testCheckSpellingBulkWithParams_ApiChecker_MixedTexts_ReturnsCorrectResults() {
        BulkSpellCheckRequest request = new BulkSpellCheckRequest();
        request.setTexts(Arrays.asList("hello", "hi"));
        request.setCheckerType("api");
        request.setCategoryName(null);

        when(apiSpellChecker.checkSpelling("hello")).thenReturn("Correct");
        when(apiSpellChecker.checkSpelling("hi")).thenReturn("Incorrect");

        List<SpellCheckResponse> results = spellCheckService.checkSpellingBulkWithParams(request);
        assertEquals(2, results.size());
        assertEquals("hello", results.get(0).getText());
        assertTrue(results.get(0).isCorrect());
        assertEquals("hi", results.get(1).getText());
        assertFalse(results.get(1).isCorrect());
        verify(apiSpellChecker, times(2)).checkSpelling(anyString());
        verify(simpleSpellChecker, never()).checkSpelling(anyString());
        verify(spellCheckCategoryRepository, never()).save(any());
    }

    @Test
    void testCheckSpellingBulkWithParams_WithCategory_SavesToRepository() {
        BulkSpellCheckRequest request = new BulkSpellCheckRequest();
        request.setTexts(Arrays.asList("hello", "hi"));
        request.setCheckerType("simple");
        request.setCategoryName("testCategory");

        when(simpleSpellChecker.checkSpelling("hello")).thenReturn("Correct");
        when(simpleSpellChecker.checkSpelling("hi")).thenReturn("Incorrect");
        when(spellCheckCategoryRepository.save(any(SpellCheckCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<SpellCheckResponse> results = spellCheckService.checkSpellingBulkWithParams(request);
        assertEquals(2, results.size());
        assertEquals("hello", results.get(0).getText());
        assertTrue(results.get(0).isCorrect());
        assertEquals("hi", results.get(1).getText());
        assertFalse(results.get(1).isCorrect());
        verify(simpleSpellChecker, times(2)).checkSpelling(anyString());
        verify(spellCheckCategoryRepository, times(2)).save(any(SpellCheckCategory.class));
    }

    @Test
    void testCheckSpellingBulkWithParams_EmptyList_ThrowsException() {
        BulkSpellCheckRequest request = new BulkSpellCheckRequest();
        request.setTexts(Arrays.asList());
        assertThrows(IllegalArgumentException.class, () -> {
            spellCheckService.checkSpellingBulkWithParams(request);
        });
        verify(simpleSpellChecker, never()).checkSpelling(anyString());
        verify(apiSpellChecker, never()).checkSpelling(anyString());
    }

    @Test
    void testCheckSpellingBulkWithParams_NullRequest_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            spellCheckService.checkSpellingBulkWithParams(null);
        });
        verify(simpleSpellChecker, never()).checkSpelling(anyString());
        verify(apiSpellChecker, never()).checkSpelling(anyString());
    }

    @Test
    void testCheckSpellingBulkWithParams_NullTextList_ThrowsException() {
        BulkSpellCheckRequest request = new BulkSpellCheckRequest();
        request.setTexts(null);
        assertThrows(IllegalArgumentException.class, () -> {
            spellCheckService.checkSpellingBulkWithParams(request);
        });
        verify(simpleSpellChecker, never()).checkSpelling(anyString());
        verify(apiSpellChecker, never()).checkSpelling(anyString());
    }
}