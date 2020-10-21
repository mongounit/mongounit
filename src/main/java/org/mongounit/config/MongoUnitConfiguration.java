/*
 * Copyright 2019 Yaakov Chaikin (yaakov@ClearlyDecoded.com). Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package org.mongounit.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
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
  public static MongoDatabaseGuardConfiguration createDatabaseGuardConfiguration() {
    return new MongoDatabaseGuardConfiguration();
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

      MongoDatabaseFactory mongoDatabaseFactory = applicationContext.getBean(MongoDatabaseFactory.class);

      // Drop database if not disabled
      if (mongoProperties.isDropDatabase()) {

        log.info("Dropping test database '" + mongoDatabaseFactory.getMongoDatabase().getName() + "'.");
        mongoDatabaseFactory.getMongoDatabase().drop();
      } else {

        log.info("Test database '" + mongoDatabaseFactory.getMongoDatabase().getName() + "' is NOT dropped. Manual"
            + " cleanup is necessary to remove it.");
      }
    }

    @Override
    public boolean isRunning() {
      return true;
    }
  }
}
