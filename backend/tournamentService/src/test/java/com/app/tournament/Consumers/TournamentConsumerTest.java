// package com.app.tournament.Consumers;

// import com.csd.tournament.AMQP.RabbitMQConfig;
// import com.csd.tournament.Consumers.TournamentConsumer;
// import com.csd.tournament.service.TournamentService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import static org.mockito.Mockito.*;

// class TournamentConsumerTest {

//     @Mock
//     private TournamentService tournamentService;

//     @InjectMocks
//     private TournamentConsumer tournamentConsumer;

//     @BeforeEach
//     void setUp() {
//         // Initialize mocks
//         MockitoAnnotations.openMocks(this);
//     }

//     @Test
//     void testHandleDeleteTournament_success() throws Exception {
//         // Mock message
//         String message = "admin123,tournament456";

//         // Mock TournamentService behavior
//         doNothing().when(tournamentService).deleteTournament("tournament456");

//         // Call the consumer method
//         tournamentConsumer.handleDeleteTournament(message);

//         // Verify the service method is called with the correct argument
//         verify(tournamentService, times(1)).deleteTournament("tournament456");
//     }

//     @Test
//     void testHandleDeleteTournament_failure() throws Exception {
//         // Mock message
//         String message = "admin123,tournament456";

//         // Mock TournamentService to throw an exception
//         doThrow(new RuntimeException("Tournament not found")).when(tournamentService).deleteTournament("tournament456");

//         // Call the consumer method
//         tournamentConsumer.handleDeleteTournament(message);

//         // Verify the service method is called with the correct argument
//         verify(tournamentService, times(1)).deleteTournament("tournament456");

//         // No assertion needed here as we are only verifying behavior
//     }
// }