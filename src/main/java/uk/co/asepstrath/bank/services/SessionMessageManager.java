package uk.co.asepstrath.bank.services;

import io.jooby.Context;

import java.util.Map;

/**
 * Interface for managing session messages and attributes
 */
public interface SessionMessageManager {
    /**
     * Add a customer message to the session to be displayed after a redirect
     *
     * @param ctx     Session context
     * @param key     the key value of the message
     * @param message Custom message
     */
    void addMessageToSession(Context ctx, String key, String message);

    /**
     * Transfer a session attribute to the model and remove it from the session
     *
     * @param ctx           Session context
     * @param attributeName The name of the attribute to transfer
     * @param model         The model to transfer the attribute to
     */
    void transferSessionAttributeToModel(Context ctx, String attributeName, Map<String, Object> model);
}