package uk.co.asepstrath.bank.controllers;

import io.jooby.ModelAndView;
import io.jooby.annotation.GET;
import io.jooby.annotation.Path;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.services.error.ErrorService;

import java.util.Map;

import static uk.co.asepstrath.bank.Constants.*;

@Path(ROUTE_ERROR)
public class ErrorController extends BaseController {

    private final ErrorService errorService;

    public ErrorController(Logger logger) {
        super(logger);
        this.errorService = new ErrorService(logger);
    }

    @GET
    @Path(ROUTE_403_FORBIDDEN)
    public ModelAndView<Map<String, Object>> forbidden() {
        logger.info("Displaying 403 error page");
        return errorService.renderForbiddenPage();
    }

    @GET
    @Path(ROUTE_404_NOT_FOUND)
    public ModelAndView<Map<String, Object>> notFound() {
        logger.info("Displaying 404 error page");
        return errorService.renderNotFoundPage();
    }

    @GET
    @Path(ROUTE_405_METHOD_NOT_ALLOWED)
    public ModelAndView<Map<String, Object>> methodNotAllowed() {
        logger.info("Displaying 405 error page");
        return errorService.renderMethodNotAllowedPage();
    }

    @GET
    @Path(ROUTE_505_SERVER_ERROR)
    public ModelAndView<Map<String, Object>> serverError() {
        logger.info("Displaying 505 error page");
        return errorService.renderInternalServerErrorPage();
    }


    @GET
    @Path(ROUTE_GENERIC_ERROR)
    public ModelAndView<Map<String, Object>> somethingWentWrong() {
        logger.info("Displaying generic error page");
        return errorService.renderGenericErrorPage();
    }

}