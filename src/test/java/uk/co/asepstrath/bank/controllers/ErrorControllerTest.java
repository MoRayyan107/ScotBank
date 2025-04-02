package uk.co.asepstrath.bank.controllers;

import io.jooby.ModelAndView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.services.error.ErrorService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static uk.co.asepstrath.bank.Constants.TEMPLATE_ERROR;

class ErrorControllerTest {

    @Mock
    private Logger mockLogger;

    private ErrorController errorController;
    private ErrorService spyErrorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        errorController = new ErrorController(mockLogger);

        // Get access to the errorService to spy on it
        spyErrorService = spy(new ErrorService(mockLogger));

        // Use reflection to set the spied service
        try {
            java.lang.reflect.Field field = ErrorController.class.getDeclaredField("errorService");
            field.setAccessible(true);
            field.set(errorController, spyErrorService);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to inject spy service", e);
        }
    }

    @Test
    void testForbidden() {
        // Prepare a mock response
        Map<String, Object> model = new HashMap<>();
        ModelAndView<Map<String, Object>> mockResponse = new ModelAndView<>(TEMPLATE_ERROR, model);

        // Configure spy service
        doReturn(mockResponse).when(spyErrorService).renderForbiddenPage();

        // Call the method under test
        ModelAndView<Map<String, Object>> result = errorController.forbidden();

        // Verify logger was called
        verify(mockLogger).info("Displaying 403 error page");

        // Verify the error service was called
        verify(spyErrorService).renderForbiddenPage();

        // Verify the result
        assertNotNull(result);
        assertEquals(mockResponse, result);
    }

    @Test
    void testNotFound() {
        // Prepare a mock response
        Map<String, Object> model = new HashMap<>();
        ModelAndView<Map<String, Object>> mockResponse = new ModelAndView<>(TEMPLATE_ERROR, model);

        // Configure spy service
        doReturn(mockResponse).when(spyErrorService).renderNotFoundPage();

        // Call the method under test
        ModelAndView<Map<String, Object>> result = errorController.notFound();

        // Verify logger was called
        verify(mockLogger).info("Displaying 404 error page");

        // Verify the error service was called
        verify(spyErrorService).renderNotFoundPage();

        // Verify the result
        assertNotNull(result);
        assertEquals(mockResponse, result);
    }

    @Test
    void testMethodNotAllowed() {
        // Prepare a mock response
        Map<String, Object> model = new HashMap<>();
        ModelAndView<Map<String, Object>> mockResponse = new ModelAndView<>(TEMPLATE_ERROR, model);

        // Configure spy service
        doReturn(mockResponse).when(spyErrorService).renderMethodNotAllowedPage();

        // Call the method under test
        ModelAndView<Map<String, Object>> result = errorController.methodNotAllowed();

        // Verify logger was called
        verify(mockLogger).info("Displaying 405 error page");

        // Verify the error service was called
        verify(spyErrorService).renderMethodNotAllowedPage();

        // Verify the result
        assertNotNull(result);
        assertEquals(mockResponse, result);
    }

    @Test
    void testSomethingWentWrong() {
        // Prepare a mock response
        Map<String, Object> model = new HashMap<>();
        ModelAndView<Map<String, Object>> mockResponse = new ModelAndView<>(TEMPLATE_ERROR, model);

        // Configure spy service
        doReturn(mockResponse).when(spyErrorService).renderGenericErrorPage();

        // Call the method under test
        ModelAndView<Map<String, Object>> result = errorController.somethingWentWrong();

        // Verify logger was called
        verify(mockLogger).info("Displaying generic error page");

        // Verify the error service was called
        verify(spyErrorService).renderGenericErrorPage();

        // Verify the result
        assertNotNull(result);
        assertEquals(mockResponse, result);
    }
}