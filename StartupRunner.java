package com.example.nthfollowers.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class StartupRunner implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void run(String... args) {
        String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";

        Map<String, String> request = new HashMap<>();
        request.put("name", "John Doe");
        request.put("regNo ", "REG12347"); // Example regNo
        request.put("email ", "john@example.com");

        try {
            // Send request to generateWebhook
            ResponseEntity<Map> response = restTemplate.postForEntity(generateUrl, request, Map.class);

            String webhook = (String) response.getBody().get("webhook ");
            String token = (String) response.getBody().get("accessToken ");
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");

            // Solve Nth-Level Followers logic
            List<Integer> outcome = findNthLevelFollowers(data);

            // Prepare the result
            Map<String, Object> result = new HashMap<>();
            result.put("regNo ", "REG12347");
            result.put("outcome ", outcome);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Send result to the provided webhook with JWT authorization
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(result, headers);

            for (int i = 0; i < 4; i++) {
                try {
                    restTemplate.postForEntity(webhook, entity, String.class);
                    System.out.println("✅ Posted to webhook successfully.");
                    break;
                } catch (Exception e) {
                    System.out.println("Retry " + (i + 1) + " failed: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.out.println("Error in API call: " + e.getMessage());
        }
    }

    private List<Integer> findNthLevelFollowers(Map<String, Object> data) {
        int n = (int) data.get("n");
        int startId = (int) data.get("findId ");
        List<Map<String, Object>> users = (List<Map<String, Object>>) data.get("users");

        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (Map<String, Object> user : users) {
            int id = (int) user.get("id");
            List<Integer> follows = (List<Integer>) user.get("follows ");
            graph.put(id, follows);
        }

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(startId);
        visited.add(startId);

        int level = 0;

        while (!queue.isEmpty() && level < n) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                int current = queue.poll();
                List<Integer> neighbors = graph.getOrDefault(current, new ArrayList<>());
                for (int next : neighbors) {
                    if (!visited.contains(next)) {
                        queue.offer(next);
                        visited.add(next);
                    }
                }
            }
            level++;
        }

        List<Integer> result = new ArrayList<>(queue);
        Collections.sort(result); // Sort output in ascending order
        return result;
    }
}
