package org.mongounit;

/**
 * {@link MongoUnitException} class represents the root exception of any exceptions in the MongoUnit
 * framework.
 */
public class MongoUnitException extends RuntimeException {

  public MongoUnitException() {
  }

  public MongoUnitException(String message) {
    super(message);
  }

  public MongoUnitException(String message, Throwable cause) {
    super(message, cause);
  }

  public MongoUnitException(Throwable cause) {
    super(cause);
  }

  public MongoUnitException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
