package com.app.tournament.consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;

import csd.tournament.consumer.TournamentConsumer;
import csd.tournament.service.TournamentService;

class TournamentConsumerTest {

    @Mock
    private TournamentService tournamentService;

    @InjectMocks
    private TournamentConsumer tournamentConsumer;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleDeleteTournament_success() throws Exception {
        // Mock message
        String message = "admin123,tournament456";

        // Mock TournamentService behavior
        doNothing().when(tournamentService).deleteTournament("tournament456");

        // Call the consumer method
        tournamentConsumer.handleDeleteTournament(message);

        // Verify the service method is called with the correct argument
        verify(tournamentService, times(1)).deleteTournament("tournament456");
    }

    @Test
    void testHandleDeleteTournament_failure() throws Exception {
        // Mock message
        String message = "admin123,tournament456";

        // Mock TournamentService to throw an exception
        doThrow(new RuntimeException("Tournament not found")).when(tournamentService).deleteTournament("tournament456");

        // Call the consumer method
        tournamentConsumer.handleDeleteTournament(message);

        // Verify the service method is called with the correct argument
        verify(tournamentService, times(1)).deleteTournament("tournament456");

        // No assertion needed here as we are only verifying behavior
    }
}