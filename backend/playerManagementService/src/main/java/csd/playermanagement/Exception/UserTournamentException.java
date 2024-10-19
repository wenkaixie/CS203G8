package csd.playermanagement.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserTournamentException extends RuntimeException {
    public UserTournamentException(String message) {
        super(message);
    }
}