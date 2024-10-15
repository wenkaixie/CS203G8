package csd.playermanagement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import csd.playermanagement.DTO.UserDTO;
import csd.playermanagement.Model.Tournament;
import csd.playermanagement.Model.User;
import csd.playermanagement.Service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.ExecutionException;

// for Date of birht and timestamp
import com.google.cloud.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class UserServiceRegisterTest {

    @Mock
    private Firestore firestore;

    @InjectMocks
    private UserService userService;

    @Mock
    private CollectionReference tournamentsCollection;

    @Mock
    private DocumentReference tournamentRef;

    @Mock
    private DocumentSnapshot tournamentSnapshot;

    @Mock
    private CollectionReference usersCollection;

    @Mock
    private Query usersQuery;

    @Mock
    private ApiFuture<QuerySnapshot> userQueryFuture;

    @Mock
    private QuerySnapshot userQuerySnapshot;

    @Mock
    private QueryDocumentSnapshot userSnapshot;

    @Mock
    private DocumentReference userDocRef;

    @Test
    void registerUserForTournament_UserSuccessfullyRegistered() throws InterruptedException, ExecutionException {
        // arrange ***
        String tournamentId = "tournament123";
        UserDTO userDto = new UserDTO();
        userDto.setAuthId("user123");

        Tournament tournament = new Tournament();
        tournament.setStartDatetime(Timestamp.of(new Date(System.currentTimeMillis() + 3600000))); // Start in future
        tournament.setUsers(new ArrayList<>());
        tournament.setCapacity(5);
        tournament.setEloRequirement(1000);

        User user = new User();
        user.setAuthId("user123");
        user.setUid("userUid123");

        // Calculate the user's date of birth (25 years ago)
        LocalDate dobLocalDate = LocalDate.now().minusYears(25);
        Date dobDate = Date.from(dobLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        user.setDateOfBirth(Timestamp.of(dobDate));
        user.setElo(1200);
        user.setRegistrationHistory(new ArrayList<>());

        // mock ***
        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.document(tournamentId)).thenReturn(tournamentRef);
        when(tournamentRef.get()).thenReturn(ApiFutures.immediateFuture(tournamentSnapshot));
        when(tournamentSnapshot.exists()).thenReturn(true);
        when(tournamentSnapshot.toObject(Tournament.class)).thenReturn(tournament);

        when(tournamentSnapshot.getLong("capacity")).thenReturn(5L);
        when(tournamentSnapshot.getLong("eloRequirement")).thenReturn(1000L);

        when(firestore.collection("Users")).thenReturn(usersCollection);
        when(usersCollection.whereEqualTo("authId", userDto.getAuthId())).thenReturn(usersQuery);
        when(usersQuery.get()).thenReturn(userQueryFuture);
        when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
        when(userQuerySnapshot.isEmpty()).thenReturn(false);
        when(userQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(userSnapshot));
        when(userSnapshot.toObject(User.class)).thenReturn(user);

        when(usersCollection.document(user.getUid())).thenReturn(userDocRef);
        when(userDocRef.update(anyString(), any())).thenReturn(ApiFutures.immediateFuture(null));

        when(tournamentRef.update(anyString(), any())).thenReturn(ApiFutures.immediateFuture(null));

        // act
        String result = userService.registerUserForTournament(tournamentId, userDto);

        // assert
        assertEquals("User successfully registered for the tournament.", result);
        verify(tournamentRef).update(eq("users"), anyList());
        verify(userDocRef).update(eq("registrationHistory"), anyList());
    }

    @Test
    void registerUserForTournament_TournamentNotFound_ReturnsError() throws Exception {
        // arrange ***
        String tournamentId = "invalidTournamentId";
        UserDTO userDto = new UserDTO();
        userDto.setAuthId("user123");

        // mock ***
        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.document(tournamentId)).thenReturn(tournamentRef);
        when(tournamentRef.get()).thenReturn(ApiFutures.immediateFuture(tournamentSnapshot));
        when(tournamentSnapshot.exists()).thenReturn(false);

        // act
        // String result = userService.registerUserForTournament(tournamentId, userDto);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUserForTournament(tournamentId, userDto);
        });

        // assert
        assertEquals("No tournament found with the provided ID.", exception.getMessage());
        verify(tournamentRef).get();
    }

    @Test
    void registerUserForTournament_TournamentAlreadyStarted_ReturnsError() throws InterruptedException, ExecutionException {
        // arrange ***
        String tournamentId = "tournamentPast";
        UserDTO userDto = new UserDTO();
        userDto.setAuthId("user123");

        Tournament tournament = new Tournament();
        tournament.setStartDatetime(Timestamp.of(new Date(System.currentTimeMillis() - 3600000))); // Started in past

        // mock ***
        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.document(tournamentId)).thenReturn(tournamentRef);
        when(tournamentRef.get()).thenReturn(ApiFutures.immediateFuture(tournamentSnapshot));
        when(tournamentSnapshot.exists()).thenReturn(true);
        when(tournamentSnapshot.toObject(Tournament.class)).thenReturn(tournament);

        // act
        String result = userService.registerUserForTournament(tournamentId, userDto);

        // assert
        assertEquals("Cannot register: The tournament has already started.", result);
        verify(tournamentRef).get();
    }

    @Test
    void registerUserForTournament_UserNotFound_ReturnsError() throws InterruptedException, ExecutionException {
        // arrange ***
        String tournamentId = "tournament123";
        UserDTO userDto = new UserDTO();
        userDto.setAuthId("userNotFound");

        Tournament tournament = new Tournament();
        tournament.setStartDatetime(Timestamp.of(new Date(System.currentTimeMillis() + 3600000))); // Start in future
        tournament.setUsers(new ArrayList<>());
        tournament.setCapacity(5);
        tournament.setEloRequirement(1000);

        // mock ***
        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.document(tournamentId)).thenReturn(tournamentRef);
        when(tournamentRef.get()).thenReturn(ApiFutures.immediateFuture(tournamentSnapshot));
        when(tournamentSnapshot.exists()).thenReturn(true);
        when(tournamentSnapshot.toObject(Tournament.class)).thenReturn(tournament);

        when(tournamentSnapshot.getLong("capacity")).thenReturn(5L);
        when(tournamentSnapshot.getLong("eloRequirement")).thenReturn(1000L);

        when(firestore.collection("Users")).thenReturn(usersCollection);
        when(usersCollection.whereEqualTo("authId", userDto.getAuthId())).thenReturn(usersQuery);
        when(usersQuery.get()).thenReturn(userQueryFuture);
        when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
        when(userQuerySnapshot.isEmpty()).thenReturn(true);

        // act
        String result = userService.registerUserForTournament(tournamentId, userDto);

        // assert
        assertEquals("User not found.", result);
        verify(usersCollection).whereEqualTo("authId", userDto.getAuthId());
        verify(usersQuery).get();
    }

    @Test
    void registerUserForTournament_UserAlreadyRegistered_ReturnsError() throws InterruptedException, ExecutionException {
        // arrange ***
        String tournamentId = "tournament123";
        UserDTO userDto = new UserDTO();
        userDto.setAuthId("user123");

        Tournament tournament = new Tournament();
        tournament.setStartDatetime(Timestamp.of(new Date(System.currentTimeMillis() + 3600000))); // Start in future
        tournament.setUsers(new ArrayList<>(Arrays.asList("user123")));
        tournament.setCapacity(5);
        tournament.setEloRequirement(1000);

        User user = new User();
        user.setAuthId("user123");
        user.setUid("userUid123");
        user.setElo(1200);
        user.setRegistrationHistory(new ArrayList<>());

        // mock ***
        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.document(tournamentId)).thenReturn(tournamentRef);
        when(tournamentRef.get()).thenReturn(ApiFutures.immediateFuture(tournamentSnapshot));
        when(tournamentSnapshot.exists()).thenReturn(true);
        when(tournamentSnapshot.toObject(Tournament.class)).thenReturn(tournament);

        when(tournamentSnapshot.getLong("capacity")).thenReturn(5L);
        when(tournamentSnapshot.getLong("eloRequirement")).thenReturn(1000L);

        when(firestore.collection("Users")).thenReturn(usersCollection);
        when(usersCollection.whereEqualTo("authId", userDto.getAuthId())).thenReturn(usersQuery);
        when(usersQuery.get()).thenReturn(userQueryFuture);
        when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
        when(userQuerySnapshot.isEmpty()).thenReturn(false);
        when(userQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(userSnapshot));
        when(userSnapshot.toObject(User.class)).thenReturn(user);

        // act
        String result = userService.registerUserForTournament(tournamentId, userDto);

        // assert
        assertEquals("User already registered for this tournament.", result);
        verify(tournamentRef).get();
    }

    @Test
    void registerUserForTournament_UserDoesNotMeetEloRequirement_ReturnsError() throws InterruptedException, ExecutionException {
        // arrange ***
        String tournamentId = "tournament123";
        UserDTO userDto = new UserDTO();
        userDto.setAuthId("user123");

        Tournament tournament = new Tournament();
        tournament.setStartDatetime(Timestamp.of(new Date(System.currentTimeMillis() + 3600000))); // Start in future
        tournament.setUsers(new ArrayList<>());
        tournament.setCapacity(5);
        tournament.setEloRequirement(1300); // Elo requirement higher than user's elo

        User user = new User();
        user.setAuthId("user123");
        user.setUid("userUid123");
        user.setElo(1200); // User's elo is below requirement
        user.setRegistrationHistory(new ArrayList<>());

        // mock ***
        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.document(tournamentId)).thenReturn(tournamentRef);
        when(tournamentRef.get()).thenReturn(ApiFutures.immediateFuture(tournamentSnapshot));
        when(tournamentSnapshot.exists()).thenReturn(true);
        when(tournamentSnapshot.toObject(Tournament.class)).thenReturn(tournament);

        when(tournamentSnapshot.getLong("capacity")).thenReturn(5L);
        when(tournamentSnapshot.getLong("eloRequirement")).thenReturn(1300L);

        when(firestore.collection("Users")).thenReturn(usersCollection);
        when(usersCollection.whereEqualTo("authId", userDto.getAuthId())).thenReturn(usersQuery);
        when(usersQuery.get()).thenReturn(userQueryFuture);
        when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
        when(userQuerySnapshot.isEmpty()).thenReturn(false);
        when(userQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(userSnapshot));
        when(userSnapshot.toObject(User.class)).thenReturn(user);

        // act
        String result = userService.registerUserForTournament(tournamentId, userDto);

        // assert
        assertEquals("User does not meet the Elo requirement for this tournament.", result);
        verify(tournamentRef).get();
    }

    @Test
    void registerUserForTournament_TournamentAtFullCapacity_ReturnsError() throws InterruptedException, ExecutionException {
        // arrange ***
        String tournamentId = "tournament123";
        UserDTO userDto = new UserDTO();
        userDto.setAuthId("user123");

        // Tournament is full (capacity reached)
        Tournament tournament = new Tournament();
        tournament.setStartDatetime(Timestamp.of(new Date(System.currentTimeMillis() + 3600000))); // Start in future
        tournament.setUsers(Arrays.asList("user1", "user2", "user3", "user4", "user5"));
        tournament.setCapacity(5);
        tournament.setEloRequirement(1000);

        User user = new User();
        user.setAuthId("user123");
        user.setUid("userUid123");
        user.setElo(1200);
        user.setRegistrationHistory(new ArrayList<>());

        // mock ***
        when(firestore.collection("Tournaments")).thenReturn(tournamentsCollection);
        when(tournamentsCollection.document(tournamentId)).thenReturn(tournamentRef);
        when(tournamentRef.get()).thenReturn(ApiFutures.immediateFuture(tournamentSnapshot));
        when(tournamentSnapshot.exists()).thenReturn(true);
        when(tournamentSnapshot.toObject(Tournament.class)).thenReturn(tournament);

        when(tournamentSnapshot.getLong("capacity")).thenReturn(5L);
        when(tournamentSnapshot.getLong("eloRequirement")).thenReturn(1000L);

        when(firestore.collection("Users")).thenReturn(usersCollection);
        when(usersCollection.whereEqualTo("authId", userDto.getAuthId())).thenReturn(usersQuery);
        when(usersQuery.get()).thenReturn(userQueryFuture);
        when(userQueryFuture.get()).thenReturn(userQuerySnapshot);
        when(userQuerySnapshot.isEmpty()).thenReturn(false);
        when(userQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(userSnapshot));
        when(userSnapshot.toObject(User.class)).thenReturn(user);

        // act
        String result = userService.registerUserForTournament(tournamentId, userDto);

        // assert
        assertEquals("Tournament is at full capacity.", result);
        verify(tournamentRef).get();
    }
}