package uk.co.asepstrath.bank.services.login;

import io.jooby.Context;
import io.jooby.ModelAndView;
import org.slf4j.Logger;
import uk.co.asepstrath.bank.services.BaseService;

import java.util.Map;

import static uk.co.asepstrath.bank.Constants.SESSION_ERROR_MESSAGE;
import static uk.co.asepstrath.bank.Constants.TEMPLATE_MANAGER_LOGIN;

/**
 * The manager display login service
 */
public class DisplayManagerLoginService extends BaseService {

    public DisplayManagerLoginService(Logger logger) {
        super(logger);
    }

    /**
     * Renders the "/manager/login" endpoint
     *
     * @return the "/manager/login" endpoint
     */
    public ModelAndView<Map<String, Object>> displayManagerLogin(Context ctx) {
        Map<String, Object> model = createModel();
        transferSessionAttributeToModel(ctx, SESSION_ERROR_MESSAGE, model);
        return render(TEMPLATE_MANAGER_LOGIN, model);
    }
}
