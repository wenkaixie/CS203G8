package csd.playermanagement.helper;

import java.util.List;

import com.google.cloud.firestore.DocumentSnapshot;

import csd.shared_library.model.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserMapper {

    public static User mapDocumentToUser(DocumentSnapshot document) {
        try {
            if (document == null || !document.exists()) {
                log.error("Document is null or does not exist.");
                return null;
            }

            User user = new User();
            user.setAuthId(document.getString("authId"));
            user.setDateOfBirth(
                    document.contains("dateOfBirth") ? document.getTimestamp("dateOfBirth").toDate().toInstant()
                            : null);
            user.setElo(document.contains("elo") ? document.getLong("elo").intValue() : 0);
            user.setEmail(document.getString("email"));
            user.setName(document.getString("name"));
            user.setNationality(document.getString("nationality"));
            user.setPhoneNumber(document.contains("phoneNumber") ? document.getLong("phoneNumber").intValue() : null);
            user.setRegistrationHistory((List<String>) document.get("registrationHistory"));
            user.setUsername(document.getString("username"));
            user.setChessUsername(document.getString("chessUsername"));

            log.info("Mapped Firestore document to User object: {}", user);
            return user;
        } catch (Exception e) {
            log.error("Error mapping Firestore document to User object: {}", e.getMessage(), e);
            throw e; // Propagate or handle the exception as needed
        }
    }
}
