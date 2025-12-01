package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class StartupRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {

        RestTemplate restTemplate = new RestTemplate();

        // STEP 1 => Generate Webhook
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        String requestBody = """
                {
                    "name": "Bandaru Dhanya Deepika",
                    "regNo": "22BCE3693",
                    "email": "dhanyadeepika2004@gmail.com"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        WebhookResponse response = restTemplate.postForObject(url, entity, WebhookResponse.class);

        assert response != null;

        String webhookURL = response.getWebhook();
        String token = response.getAccessToken();

        System.out.println("Webhook URL: " + webhookURL);
        System.out.println("Token: " + token);

        // STEP 2 => Final SQL Query
        String finalQuery = """
                SELECT 
                    d.DEPARTMENT_NAME,
                    AVG(TIMESTAMPDIFF(YEAR, e.DOB, CURRENT_DATE)) AS AVERAGE_AGE,
                    GROUP_CONCAT(CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) SEPARATOR ', ') AS EMPLOYEE_LIST
                FROM EMPLOYEE e
                JOIN DEPARTMENT d 
                    ON e.DEPARTMENT = d.DEPARTMENT_ID
                JOIN PAYMENTS p 
                    ON e.EMP_ID = p.EMP_ID
                WHERE p.AMOUNT > 70000
                GROUP BY d.DEPARTMENT_ID, d.DEPARTMENT_NAME
                ORDER BY d.DEPARTMENT_ID DESC;
                """;

        // STEP 3 => Submit SQL Answer
        HttpHeaders headers2 = new HttpHeaders();
        headers2.setContentType(MediaType.APPLICATION_JSON);
        headers2.set("Authorization", token);

        String body = "{ \"finalQuery\": \"" + finalQuery.replace("\"", "\\\"") + "\" }";

        HttpEntity<String> entity2 = new HttpEntity<>(body, headers2);

        ResponseEntity<String> result = restTemplate.exchange(
                webhookURL,
                HttpMethod.POST,
                entity2,
                String.class
        );

        System.out.println("Webhook Response: " + result.getBody());
    }
}
