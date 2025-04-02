package uk.co.asepstrath.bank.controllers;

import org.slf4j.Logger;

public abstract class BaseController {

    protected final Logger logger;

    protected BaseController(Logger logger) {
        this.logger = logger;
        this.logger.info("{} created", this.getClass().getName());
    }
}
