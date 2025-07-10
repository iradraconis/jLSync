package com.iradraconis.jlsync;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FuzzySearchUtil {
    
    public static class CaseResult {
        private final String id;
        private final String fileNumber;
        private final String name;
        private final double score;
        
        public CaseResult(String id, String fileNumber, String name, double score) {
            this.id = id;
            this.fileNumber = fileNumber;
            this.name = name;
            this.score = score;
        }
        
        public String getId() { return id; }
        public String getFileNumber() { return fileNumber; }
        public String getName() { return name; }
        public double getScore() { return score; }
        
        @Override
        public String toString() {
            return fileNumber + " - " + name;
        }
    }
    
    public static List<CaseResult> fuzzySearch(String query, int maxResults) throws FileNotFoundException {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String directoryPath = System.getProperty("user.home") + "/.jL_Sync_Files_data";
        String filePath = directoryPath + "/jL_Sync_Files_Cases.json";
        
        List<CaseResult> results = new ArrayList<>();
        
        JsonArray jsonArray = JsonParser.parseReader(new FileReader(filePath)).getAsJsonArray();
        
        String queryLower = query.toLowerCase().trim();
        
        for (JsonElement element : jsonArray) {
            JsonObject obj = element.getAsJsonObject();
            String id = obj.get("id").getAsString();
            String fileNumber = obj.get("fileNumber").getAsString();
            String name = obj.get("name").getAsString();
            
            double score = calculateScore(queryLower, fileNumber, name);
            
            if (score > 0) {
                results.add(new CaseResult(id, fileNumber, name, score));
            }
        }
        
        results.sort(Comparator.comparingDouble(CaseResult::getScore).reversed());
        
        if (results.size() > maxResults) {
            results = results.subList(0, maxResults);
        }
        
        return results;
    }
    
    private static double calculateScore(String query, String fileNumber, String name) {
        String fileNumberLower = fileNumber.toLowerCase();
        String nameLower = name.toLowerCase();
        
        double score = 0;
        
        if (fileNumberLower.contains(query)) {
            if (fileNumberLower.equals(query)) {
                score += 100;
            } else if (fileNumberLower.startsWith(query)) {
                score += 80;
            } else {
                score += 60;
            }
        }
        
        if (nameLower.contains(query)) {
            if (nameLower.equals(query)) {
                score += 90;
            } else if (nameLower.startsWith(query)) {
                score += 70;
            } else {
                score += 50;
            }
        }
        
        String[] queryWords = query.split("\\s+");
        for (String word : queryWords) {
            if (word.length() > 2) {
                if (fileNumberLower.contains(word)) {
                    score += 30;
                }
                if (nameLower.contains(word)) {
                    score += 25;
                }
            }
        }
        
        double fileNumberSimilarity = calculateLevenshteinSimilarity(query, fileNumberLower);
        double nameSimilarity = calculateLevenshteinSimilarity(query, nameLower);
        
        if (fileNumberSimilarity > 0.6) {
            score += fileNumberSimilarity * 40;
        }
        if (nameSimilarity > 0.6) {
            score += nameSimilarity * 35;
        }
        
        return score;
    }
    
    private static double calculateLevenshteinSimilarity(String s1, String s2) {
        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 1.0;
        return 1.0 - (double) distance / maxLength;
    }
    
    private static int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]) + 1;
                }
            }
        }
        
        return dp[len1][len2];
    }
}