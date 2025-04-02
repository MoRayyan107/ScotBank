package uk.co.asepstrath.bank.services.error;

import io.jooby.ModelAndView;
import io.jooby.StatusCode;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.services.BaseService;

import java.util.Map;

import static uk.co.asepstrath.bank.Constants.*;

public class ErrorService extends BaseService {

    public ErrorService(Logger logger) {
        super(logger);
        this.logger.info("ErrorService created");
    }

    // 403
    public ModelAndView<Map<String, Object>> renderForbiddenPage() {
        Map<String, Object> model = createModel();
        model.put(SESSION_SERVER_ERROR_TITLE, "Access Denied");
        model.put(SESSION_SERVER_ERROR_CODE, StatusCode.FORBIDDEN_CODE);
        model.put(SESSION_SERVER_ERROR_MESSAGE, "You don't have permission to access this resource.");
        model.put(SESSION_SERVER_ERROR_SUGGESTION, "Please login with appropriate credentials.");
        return render(TEMPLATE_ERROR, model);
    }

    // 404
    public ModelAndView<Map<String, Object>> renderNotFoundPage() {
        logger.debug("Rendering 404 not found page");
        Map<String, Object> model = createModel();
        model.put(SESSION_SERVER_ERROR_TITLE, "Page Not Found");
        model.put(SESSION_SERVER_ERROR_CODE, StatusCode.NOT_FOUND_CODE);
        model.put(SESSION_SERVER_ERROR_MESSAGE, "The page you're looking for doesn't exist or has been moved.");
        model.put(SESSION_SERVER_ERROR_SUGGESTION, "Please check the URL or return to the homepage.");
        return render(TEMPLATE_ERROR, model);
    }

    // 405
    public ModelAndView<Map<String, Object>> renderMethodNotAllowedPage() {
        logger.debug("Rendering 405 method not allowed page");
        Map<String, Object> model = createModel();
        model.put(SESSION_SERVER_ERROR_TITLE, "Method Not Allowed");
        model.put(SESSION_SERVER_ERROR_CODE, StatusCode.METHOD_NOT_ALLOWED_CODE);
        model.put(SESSION_SERVER_ERROR_MESSAGE, "The requested method is not supported for this resource.");
        model.put(SESSION_SERVER_ERROR_SUGGESTION,
                "Please use a different method or contact support if you believe this is an error.");
        return render(TEMPLATE_ERROR, model);
    }

    // 400
    public ModelAndView<Map<String, Object>> renderGenericErrorPage() {
        logger.debug("Rendering generic error page");
        Map<String, Object> model = createModel();
        model.put(SESSION_SERVER_ERROR_TITLE, "Something Went Wrong");
        model.put(SESSION_SERVER_ERROR_CODE, StatusCode.BAD_REQUEST_CODE);
        model.put(SESSION_SERVER_ERROR_MESSAGE, "We encountered an unexpected issue while processing your request.");
        model.put(SESSION_SERVER_ERROR_SUGGESTION,
                "Please try again later or contact customer support if the problem persists.");
        return render(TEMPLATE_ERROR, model);
    }

    // 505
    public ModelAndView<Map<String, Object>> renderInternalServerErrorPage() {
        logger.debug("Rendering internal error page");
        Map<String, Object> model = createModel();
        model.put(SESSION_SERVER_ERROR_TITLE, "Internal Server Error");
        model.put(SESSION_SERVER_ERROR_CODE, StatusCode.SERVER_ERROR_CODE);
        model.put(SESSION_SERVER_ERROR_MESSAGE, "An internal database error occurred.");
        model.put(SESSION_SERVER_ERROR_SUGGESTION,
                "Please try again later or contact support if the problem persists.");
        return render(TEMPLATE_ERROR, model);
    }
}