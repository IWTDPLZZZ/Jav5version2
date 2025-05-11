package orf.demo.service;

import orf.demo.dto.BulkSpellCheckRequest;
import orf.demo.dto.SpellCheckResponse;

import java.util.List;

public interface SpellCheckService {
    String checkSpelling(String word);
    List<SpellCheckResponse> checkSpellingBulk(List<String> texts);
    List<SpellCheckResponse> checkSpellingBulkWithParams(BulkSpellCheckRequest request);
}