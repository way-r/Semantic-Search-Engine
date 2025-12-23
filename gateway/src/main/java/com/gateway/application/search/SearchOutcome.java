package com.gateway.application.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gateway.application.entry.Entry;

public class SearchOutcome {
    
    List<Map<String, Object>> outcome;

    public SearchOutcome() {
        this.outcome = new ArrayList<>();
    }

    public void addResult(Entry entry, Float similarityScore) {
        Map<String, Object> singleResult = new HashMap<>();
        singleResult.put("entry", entry);
        singleResult.put("difference", similarityScore);
        this.outcome.add(singleResult);
    }

    public List<Map<String, Object>> getOutcome() {
        return this.outcome;
    }
}
