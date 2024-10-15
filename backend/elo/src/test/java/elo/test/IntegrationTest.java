package elo.test;

import elo.model.EloUpdateRequest;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.boot.test.web.client.TestRestTemplate;


import java.net.URI;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

    @LocalServerPort
    private int port;

    private final String baseUrl = "http://localhost:";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Firestore firestore;

    @Test
    public void invalidUser() throws Exception {
        String userId1 = "hi";  
        String userId2 = "6XMAy9fDuKwkTgzkgDmF";  
        EloUpdateRequest request = new EloUpdateRequest(1, 0);  

        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + port + "/api/elo/update/" + userId1 + "/" + userId2)
                                      .build()
                                      .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        HttpEntity<EloUpdateRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.PUT, entity, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("User 1 does not exist in Firebase."));
    }

    @Test
    public void invalidAS() throws Exception {
        String userId1 = "4IALdQoh5GprTvoiQXZf";  
        String userId2 = "6XMAy9fDuKwkTgzkgDmF";  
        EloUpdateRequest request = new EloUpdateRequest(-1, 0);  //AS1 and AS2

        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + port + "/api/elo/update/" + userId1 + "/" + userId2)
                                      .build()
                                      .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        HttpEntity<EloUpdateRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.PUT, entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("AS1 and AS2 must be 0, 0.5, or 1."));
    }

    @Test
    public void invalidEqualAS() throws Exception {
        String userId1 = "4IALdQoh5GprTvoiQXZf";  
        String userId2 = "6XMAy9fDuKwkTgzkgDmF";  
        EloUpdateRequest request = new EloUpdateRequest(0, 0);  //AS1 and AS2

        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + port + "/api/elo/update/" + userId1 + "/" + userId2)
                                      .build()
                                      .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        HttpEntity<EloUpdateRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.PUT, entity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("AS1 and AS2 must be different or 0.5 each."));
    }
    
    @Test
    public void updateElo_Success() throws Exception {
        // Define the user IDs and EloUpdateRequest
        String userId1 = "4IALdQoh5GprTvoiQXZf";  // Use existing user
        String userId2 = "6XMAy9fDuKwkTgzkgDmF";  // Use existing user
        EloUpdateRequest request = new EloUpdateRequest(1, 0);  

        // Get the initial Elo ratings before the update
        DocumentSnapshot user1BeforeSnapshot = firestore.collection("Users").document(userId1).get().get();
        DocumentSnapshot user2BeforeSnapshot = firestore.collection("Users").document(userId2).get().get();

        Double initialEloUser1 = user1BeforeSnapshot.getDouble("elo");
        Double initialEloUser2 = user2BeforeSnapshot.getDouble("elo");

        assertNotNull(initialEloUser1);
        assertNotNull(initialEloUser2);

        // Create the URI for the PUT request
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + port + "/api/elo/update/" + userId1 + "/" + userId2)
                                      .build()
                                      .toUri();

        // Set the headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        // Create the HttpEntity with the request body and headers
        HttpEntity<EloUpdateRequest> entity = new HttpEntity<>(request, headers);

        // Send the PUT request and get the response
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.PUT, entity, String.class);

        // Assert that the status code is 200 OK
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Optionally assert that the response body contains a success message
        assertTrue(response.getBody().contains("Elo ratings successfully updated"));

        Thread.sleep(2000);

        // Fetch Elo ratings after the update
        DocumentSnapshot user1AfterSnapshot = firestore.collection("Users").document(userId1).get().get();
        DocumentSnapshot user2AfterSnapshot = firestore.collection("Users").document(userId2).get().get();

        Double updatedEloUser1 = user1AfterSnapshot.getDouble("elo");
        Double updatedEloUser2 = user2AfterSnapshot.getDouble("elo");

        assertNotNull(updatedEloUser1);
        assertNotNull(updatedEloUser2);

        
        System.out.println("Initial Elo for user 1: " + initialEloUser1);
        System.out.println("Initial Elo for user 2: " + initialEloUser2);

        System.out.println("Updated Elo for user 1: " + updatedEloUser1);
        System.out.println("Updated Elo for user 2: " + updatedEloUser2);

        // Assert that Elo ratings have changed
        assertNotEquals(initialEloUser1, updatedEloUser1, "User 1 Elo should have changed");
        assertNotEquals(initialEloUser2, updatedEloUser2, "User 2 Elo should have changed");
    }
}
