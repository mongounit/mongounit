package org.mongounit.model;

import java.util.Objects;

/**
 * {@link AssertionResult} class represents internal assertion result.
 */
public class AssertionResult {

  /**
   * Flag to indicate if the match was found or not.
   */
  private boolean match;

  /**
   * Optional message, which is usually used as an assertion failure message to give more details.
   */
  private String message;

  /**
   * Default constructor.
   */
  public AssertionResult() {
  }

  /**
   * Constructor.
   *
   * @param match Flag to indicate if the match was found or not.
   * @param message Optional message, which is usually used as an assertion failure message to give
   * more details.
   */
  public AssertionResult(boolean match, String message) {
    this.match = match;
    this.message = message;
  }

  /**
   * @return Instance of the builder pattern version of the {@link AssertionResult} class.
   */
  public static AssertionResultBuilder builder() {
    return new AssertionResultBuilder();
  }

  /**
   * @return Flag to indicate if the match was found or not.
   */
  public boolean isMatch() {
    return this.match;
  }

  /**
   * @return Optional message, which is usually used as an assertion failure message to give more
   * details.
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * @param match Flag to indicate if the match was found or not.
   */
  public void setMatch(boolean match) {
    this.match = match;
  }

  /**
   * @param message Optional message, which is usually used as an assertion failure message to give
   * more details.
   */
  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof AssertionResult)) {
      return false;
    }
    final AssertionResult other = (AssertionResult) o;
    if (!other.canEqual(this)) {
      return false;
    }
    if (this.isMatch() != other.isMatch()) {
      return false;
    }
    final Object this$message = this.getMessage();
    final Object other$message = other.getMessage();
    return Objects.equals(this$message, other$message);
  }

  /**
   * @param other Object to test possible equality with.
   * @return true if 'other' can equal this object based on 'other''s type, false otherwise.
   */
  private boolean canEqual(final Object other) {
    return other instanceof AssertionResult;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + (this.isMatch() ? 79 : 97);
    final Object $message = this.getMessage();
    result = result * PRIME + ($message == null ? 43 : $message.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "AssertionResult(match=" + this.isMatch() + ", message=" + this.getMessage() + ")";
  }

  /**
   * {@link AssertionResultBuilder} class is a builder pattern class for the {@link AssertionResult}
   * class.
   */
  public static class AssertionResultBuilder {

    /**
     * Flag to indicate if the match was found or not.
     */
    private boolean match;

    /**
     * Optional message, which is usually used as an assertion failure message to give more
     * details.
     */
    private String message;

    /**
     * Constructor.
     */
    AssertionResultBuilder() {
    }

    /**
     * @param match Flag to indicate if the match was found or not.
     * @return Instance of this builder class.
     */
    public AssertionResult.AssertionResultBuilder match(boolean match) {
      this.match = match;
      return this;
    }

    /**
     * @param message Optional message, which is usually used as an assertion failure message to
     * give more details.
     * @return Instance of this builder class.
     */
    public AssertionResult.AssertionResultBuilder message(String message) {
      this.message = message;
      return this;
    }

    /**
     * @return New instance of the {@link AssertionResult} class with the previously set properties.
     */
    public AssertionResult build() {
      return new AssertionResult(match, message);
    }

    @Override
    public String toString() {
      return "AssertionResult.AssertionResultBuilder(match=" + this.match + ", message="
          + this.message + ")";
    }
  }
}
