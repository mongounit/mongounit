package org.mongounit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link SeedWithDatasets} annotation enables the {@link SeedWithDataset} annotation to be
 * repeatable.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SeedWithDatasets {

  /**
   * @return Array of instances of the {@link SeedWithDataset} in order of their declaration.
   */
  SeedWithDataset[] value();
}
