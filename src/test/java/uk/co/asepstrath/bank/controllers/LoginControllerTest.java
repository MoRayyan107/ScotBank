package uk.co.asepstrath.bank.controllers;

import io.jooby.Context;
import io.jooby.ModelAndView;
import io.jooby.Session;
import io.jooby.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.services.login.DisplayLoginService;
import uk.co.asepstrath.bank.services.login.ProcessLoginService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LoginControllerTest {

    @Mock
    private Context mockContext = mock(Context.class);
    @Mock
    private DataSource mockDataSource = mock(DataSource.class);
    @Mock
    private Connection mockConnection = mock(Connection.class);
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet = mock(ResultSet.class);
    @Mock
    private Logger mockLogger = mock(Logger.class);
    @Mock
    private Value mockValue = mock(Value.class);
    @Mock
    private Session mockSession = mock(Session.class);
    @Mock
    private DisplayLoginService mockDisplayLoginService = mock(DisplayLoginService.class);
    @Mock
    private ProcessLoginService mockProcessLoginService = mock(ProcessLoginService.class);
    LoginController loginController = new LoginController(mockDisplayLoginService, mockProcessLoginService, mockLogger);
    @Mock
    private ModelAndView<Map<String, Object>> mockModelAndView;

    @BeforeEach
    void setUp() throws SQLException {

        MockitoAnnotations.openMocks(this);

        // Set up mock chain for database operations
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Set up context and session
        when(mockContext.session()).thenReturn(mockSession);

        // Initialize controller with mocked dependencies
        loginController = new LoginController(mockDisplayLoginService, mockProcessLoginService, mockLogger);
    }


    @Test
    void testDisplayLogin() {
        // Mock DisplayLogin
        when(mockDisplayLoginService.displayLogin(mockContext)).thenReturn(mockModelAndView);
        // Act
        ModelAndView<Map<String, Object>> result = loginController.displayLogin(mockContext);

        assertNotNull(result);
        assertEquals(mockModelAndView, result);
        verify(mockDisplayLoginService).displayLogin(mockContext);
    }

    // Test that the LoginController was successful in calling loginProcess
    @Test
    void testLoginProcessCall() {
        // Mock ProcessLogin

        // Act
        loginController.processLogin(mockContext);
        // Assert
        verify(mockProcessLoginService).processLogin(mockContext);
        // Verify no other interactions 
        verifyNoMoreInteractions(mockProcessLoginService);
        // Verify no unexpected interactions with the session
        verifyNoMoreInteractions(mockSession);
    }

}
