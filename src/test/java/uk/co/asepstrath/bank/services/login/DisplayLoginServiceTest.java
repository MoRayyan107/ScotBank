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
import static uk.co.asepstrath.bank.Constants.TEMPLATE_LOGIN;

class DisplayLoginServiceTest {

    @Mock
    private Logger logger;

    @Mock
    private Context ctx;

    @Mock
    private Session session;

    @Mock
    private Value sessionValue;

    private DisplayLoginService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new DisplayLoginService(logger);

        // Setup context mock
        when(ctx.session()).thenReturn(session);
        when(session.get(anyString())).thenReturn(sessionValue);
        when(sessionValue.isPresent()).thenReturn(false);
    }

    @Test
    void testDisplayLogin() {

        // Mock ValueNode to return the expected string error message
        ValueNode valueNode = mock(ValueNode.class);
        when(valueNode.valueOrNull()).thenReturn("Invalid login.");
        when(session.get(Constants.SESSION_ERROR_MESSAGE)).thenReturn(valueNode);
        ModelAndView<Map<String, Object>> result = service.displayLogin(ctx);

        assertNotNull(result);
        assertEquals(TEMPLATE_LOGIN, result.getView());
        assertNotNull(result.getModel());
    }


    @Test
    void testDisplayLoginWithError() {
        when(sessionValue.isPresent()).thenReturn(true);
        when(sessionValue.value()).thenReturn("Error message");

        ModelAndView<Map<String, Object>> result = service.displayLogin(ctx);

        assertNotNull(result.getModel().get(Constants.SESSION_ERROR_MESSAGE));
        verify(session).remove(Constants.SESSION_ERROR_MESSAGE);
    }
}