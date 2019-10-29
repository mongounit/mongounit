package org.mongounit.config;

import com.mongodb.MongoClientURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.stereotype.Component;

/**
 * {@link MongoUnitConfiguration} is a configuration class that configures database protection post
 * processor.
 */
@SuppressWarnings("all")
@Configuration
public class MongoUnitConfiguration {

  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(MongoUnitConfiguration.class);

  /**
   * @return {@link org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor}
   * which protects the configured database from being wiped out by the MongoUnit and test method
   * execution.
   */
  @Bean
  public static MongoDbGuardConfiguration createDatabaseGuardConfiguration() {
    return new MongoDbGuardConfiguration();
  }

  /**
   * {@link ApplicationLifecycle} class is a Spring component used to optionally drop the database
   * once all the tests complete execution.
   */
  @Component
  public static class ApplicationLifecycle implements Lifecycle {

    /**
     * Spring application context.
     */
    @Autowired
    ApplicationContext applicationContext;

    @Override
    public void start() {

    }

    @Override
    public void stop() {

      // Load mongo properties
      MongoUnitProperties mongoProperties = MongoUnitConfigurationUtil.loadMongoUnitProperties();

      MongoDbFactory mongoDbFactory = applicationContext.getBean(MongoDbFactory.class);

      // Drop database if not disabled
      if (mongoProperties.isDropDatabase()) {

        log.info("Dropping test database '" + mongoDbFactory.getDb().getName() + "'.");
        mongoDbFactory.getDb().drop();
      } else {

        log.info("Test database '" + mongoDbFactory.getDb().getName() + "' is NOT dropped. Manual"
            + " cleanup is necessary to remove it.");
      }
    }

    @Override
    public boolean isRunning() {
      return true;
    }
  }
}
