package uk.co.asepstrath.bank.services;

import io.jooby.Context;

import java.math.BigDecimal;

public interface FormBigDecimalRetriever {
    BigDecimal getFormBigDecimal(Context ctx, String name);
}
