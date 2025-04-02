package uk.co.asepstrath.bank.services;

import io.jooby.ModelAndView;

import java.util.Map;

/**
 * Interface for rendering views with a model
 */
public interface ViewRenderer {
    /**
     * Renders an endpoint
     *
     * @param viewName The handlebars file to be rendered
     * @param model    The map of which is modelled onto the view
     * @return an endpoint
     */
    ModelAndView<Map<String, Object>> render(String viewName, Map<String, Object> model);
}
