package com.airlinespoo.customers.persistence;

import com.airlinespoo.core.exception.AirlinesException;

public class PersistenceException extends AirlinesException {

    private static final long serialVersionUID = 1L;

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
