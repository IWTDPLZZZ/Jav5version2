package orf.demo.checker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class SimpleSpellCheckerTest {

    private SimpleSpellChecker spellChecker;

    @BeforeEach
    void setUp() {
        spellChecker = new SimpleSpellChecker();
    }


    @Test
    void testCheckSpelling_ValidText_ReturnsCorrect() {
        assertEquals("Correct", spellChecker.checkSpelling("hello"));
    }


    @Test
    void testCheckSpelling_ShortText_ReturnsIncorrect() {
        assertEquals("Incorrect", spellChecker.checkSpelling("hi"));
    }


    @Test
    void testCheckSpelling_NullText_ReturnsIncorrect() {
        assertEquals("Incorrect", spellChecker.checkSpelling(null));
    }


    @Test
    void testCheckSpelling_EmptyText_ReturnsIncorrect() {
        assertEquals("Incorrect", spellChecker.checkSpelling(""));
    }
}