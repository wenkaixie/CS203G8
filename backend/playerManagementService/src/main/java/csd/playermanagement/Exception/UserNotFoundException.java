// UserNotFoundException.java
package csd.playermanagement.Exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}


// TO UPDATE

// In UserService.java

// if (userDocuments.isEmpty()) {
//     throw new UserNotFoundException("User with ID " + userId + " not found.");
// }

// if (!tournamentSnapshot.exists()) {
//     throw new TournamentNotFoundException("Tournament with ID " + tournamentId + " not found.");
// }