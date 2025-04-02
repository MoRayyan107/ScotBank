package uk.co.asepstrath.bank.services.login;

import io.jooby.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.Constants;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static uk.co.asepstrath.bank.Constants.TEMPLATE_MANAGER_LOGIN;

class DisplayManagerLoginServiceTest {

    @Mock
    private Logger logger;

    @Mock
    private Context ctx;

    @Mock
    private Session session;


    @Mock
    private Value sessionValue;

    private DisplayManagerLoginService displayManagerLoginService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        displayManagerLoginService = new DisplayManagerLoginService(logger);

        // Setup context mock
        when(ctx.session()).thenReturn(session);
        when(session.get(anyString())).thenReturn(sessionValue);
        when(sessionValue.isPresent()).thenReturn(false);
    }

    @Test
    void testDisplayLogin() {
        when(ctx.session()).thenReturn(session);

        // Mock ValueNode to return the expected string error message
        ValueNode valueNode = mock(ValueNode.class);
        when(valueNode.valueOrNull()).thenReturn("Invalid login.");
        when(session.get(Constants.SESSION_ERROR_MESSAGE)).thenReturn(valueNode);

        ModelAndView<Map<String, Object>> result = displayManagerLoginService.displayManagerLogin(ctx);

        assertNotNull(result);
        assertEquals(TEMPLATE_MANAGER_LOGIN, result.getView());
        assertNotNull(result.getModel());
    }


    @Test
    void testDisplayLoginWithSessionError() {
        // Setup error message in session
        when(sessionValue.isPresent()).thenReturn(true);
        when(sessionValue.value()).thenReturn("Error message");

        ModelAndView<Map<String, Object>> result = displayManagerLoginService.displayManagerLogin(ctx);

        // Verify error was added to model and removed from session
        assertNotNull(result.getModel().get(Constants.SESSION_ERROR_MESSAGE));
        verify(session).remove(Constants.SESSION_ERROR_MESSAGE);
    }
}