package org.mongounit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link AssertMatchesDatasets} annotation enables the {@link AssertMatchesDataset} annotation to
 * be repeatable.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AssertMatchesDatasets {

  /**
   * @return Array of instances of the {@link AssertMatchesDataset} in order of their declaration.
   */
  AssertMatchesDataset[] value();
}
