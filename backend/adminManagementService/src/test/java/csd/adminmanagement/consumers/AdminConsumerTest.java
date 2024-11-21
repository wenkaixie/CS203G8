// package csd.adminmanagement.consumers;

// import csd.adminmanagement.AMQP.RabbitMQConfig;
// import csd.adminmanagement.Consumers.AdminConsumer;
// import csd.adminmanagement.Service.AdminService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import static org.mockito.Mockito.*;

// class AdminConsumerTest {

//     @Mock
//     private AdminService adminService;

//     @InjectMocks
//     private AdminConsumer adminConsumer;

//     @BeforeEach
//     void setUp() {
//         // Initialize mocks
//         MockitoAnnotations.openMocks(this);
//     }

//     @Test
//     void testHandleDeleteTournament_success() throws Exception {
//         // Mock message
//         String message = "admin123,tournament456";

//         // Mock AdminService behavior
//         doNothing().when(adminService).removeTournamentFromAdmin("admin123", "tournament456");

//         // Call the consumer method
//         adminConsumer.handleDeleteTournament(message);

//         // Verify the service method is called with the correct arguments
//         verify(adminService, times(1)).removeTournamentFromAdmin("admin123", "tournament456");
//     }

//     @Test
//     void testHandleDeleteTournament_failure() throws Exception {
//         // Mock message
//         String message = "admin123,tournament456";

//         // Mock AdminService to throw an exception
//         doThrow(new RuntimeException("Admin not found")).when(adminService).removeTournamentFromAdmin("admin123", "tournament456");

//         // Call the consumer method
//         adminConsumer.handleDeleteTournament(message);

//         // Verify the service method is called with the correct arguments
//         verify(adminService, times(1)).removeTournamentFromAdmin("admin123", "tournament456");

//         // No additional assertion needed, just verify it handled the exception gracefully
//     }
// }