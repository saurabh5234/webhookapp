package com.example.demo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class StartupRunner implements CommandLineRunner {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void run(String... args) {

        try {

            
            String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("name", "Saurabh Mali");
            body.put("regNo", "250850120155");
            body.put("email", "saurabhmali@gmail.com");

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    generateUrl,
                    request,
                    String.class
            );

            System.out.println("Generate API Response: " + response.getBody());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.getBody());

            String webhookUrl = jsonNode.get("webhook").asText();
            String accessToken = jsonNode.get("accessToken").asText();

            
            String finalSqlQuery =
            	    "SELECT d.department_name AS DEPARTMENT_NAME, " +
            	    "       SUM(p.amount) AS SALARY, " +
            	    "       e.first_name || ' ' || e.last_name AS EMPLOYEE_NAME, " +
            	    "       EXTRACT(YEAR FROM AGE(CURRENT_DATE, e.dob)) AS AGE " +
            	    "FROM department d " +
            	    "JOIN employee e ON d.department_id = e.department " +
            	    "JOIN payments p ON e.emp_id = p.emp_id " +
            	    "WHERE EXTRACT(DAY FROM p.payment_time) <> 1 " +
            	    "GROUP BY d.department_name, e.emp_id, e.first_name, e.last_name, e.dob " +
            	    "HAVING SUM(p.amount) = ( " +
            	    "   SELECT MAX(total_salary) FROM ( " +
            	    "       SELECT SUM(p2.amount) AS total_salary " +
            	    "       FROM employee e2 " +
            	    "       JOIN payments p2 ON e2.emp_id = p2.emp_id " +
            	    "       WHERE e2.department = d.department_id " +
            	    "         AND EXTRACT(DAY FROM p2.payment_time) <> 1 " +
            	    "       GROUP BY e2.emp_id " +
            	    "   ) t " +
            	    ")";




            HttpHeaders submitHeaders = new HttpHeaders();
            submitHeaders.set("Content-Type", "application/json");
            submitHeaders.set("Authorization", "Bearer " + accessToken);

            Map<String, String> payload = new HashMap<>();
            payload.put("query", finalSqlQuery.trim());

            HttpEntity<Map<String, String>> submitRequest =
                    new HttpEntity<>(payload, submitHeaders);

            ResponseEntity<String> submitResponse = restTemplate.postForEntity(
                    webhookUrl,
                    submitRequest,
                    String.class
            );

            System.out.println("Final Submission Response: " + submitResponse.getBody());



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
