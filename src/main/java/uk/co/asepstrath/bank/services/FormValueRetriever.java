package uk.co.asepstrath.bank.services;

import io.jooby.Context;

public interface FormValueRetriever {
    String getFormValue(Context ctx, String name);
}
