package com.gateway.application.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchOutcome {
    
    List<Map<String, Object>> outcome;

    public SearchOutcome() {
        this.outcome = new ArrayList<>();
    }

    public void addResult(String uuid, Float similarityScore) {
        Map<String, Object> singleResult = new HashMap<>();
        singleResult.put("uuid", uuid);
        singleResult.put("difference", similarityScore);
        this.outcome.add(singleResult);
    }

    public List<Map<String, Object>> getOutcome() {
        return this.outcome;
    }
}
