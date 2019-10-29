package org.mongounit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mongounit.config.MongoUnitConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

/**
 * {@link MongoUnitTest} is an annotation meant to be placed on JUnit classes to autoconfigure the
 * Spring-based MongoDbFactory to use a test database for the integration tests.
 * <p>
 * Placing this annotation on a test class automatically triggers the MongoUnit framework to look
 * for the 'mongounit.properties' file at the root of the classpath. If such a file is not found,
 * the following defaults are used:
 * </p>
 * <p>mongounit.base-uri = mongodb://localhost:27017/mongounit-testdb </p>
 * <p>mongounit.base-uri.keep-as-is = false </p>
 * <p>mongounit.drop-database = true </p>
 * <p>mongounit.indicator-field-name = $$mongounit$$ </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(MongoUnitExtension.class)
@ImportAutoConfiguration(classes = MongoUnitConfiguration.class)
public @interface MongoUnitTest {

}
