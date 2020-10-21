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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.util.ObjectUtils;

/**
 * {@link MongoDatabaseGuardConfiguration} configuration is a bean definition registry post processor that
 * substitutes a custom {@link MongoDatabaseFactory} that modifies the database URI.
 */
public class MongoDatabaseGuardConfiguration implements BeanDefinitionRegistryPostProcessor {

  /**
   * Logger for this configuration class.
   */
  private static Logger log = LoggerFactory.getLogger(MongoDatabaseGuardConfiguration.class);

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {

    // Processes the bean registry such that the substitution of mongo db URI occurs
    substituteBean(registry, (ConfigurableListableBeanFactory) registry);
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
  }

  /**
   * Substitutes {@link MongoDatabaseFactory} bean int the registry.
   *
   * @param registry Bean registry to substitute the bean in.
   * @param beanFactory Factory to create the bean that will substitute existing one.
   */
  private void substituteBean(
      BeanDefinitionRegistry registry,
      ConfigurableListableBeanFactory beanFactory) {

    // Retrieve existing MongoDatabaseFactory bean
    BeanDefinitionHolder holder = getMongoDatabaseFactoryBeanDefinition(beanFactory);

    if (holder != null) {
      String beanName = holder.getBeanName();
      boolean primary = holder.getBeanDefinition().isPrimary();

      log.info("Replacing '" + beanName + "' MongoDatabaseFactory bean with "
          + (primary ? "primary " : "") + "MongoUnit version");
      registry.removeBeanDefinition(beanName);
      registry.registerBeanDefinition(beanName, createMongoDatabaseFactoryBeanDefinition(primary));
    }
  }

  /**
   * @param beanFactory Bean factory where to retrieve the existing bean definition for the {@link
   * MongoDatabaseFactory}.
   * @return Bean definition holder of the existing {@link MongoDatabaseFactory} in the registry.
   */
  private BeanDefinitionHolder getMongoDatabaseFactoryBeanDefinition(
      ConfigurableListableBeanFactory beanFactory) {

    String[] beanNames = beanFactory.getBeanNamesForType(MongoDatabaseFactory.class);

    if (ObjectUtils.isEmpty(beanNames)) {
      log.info("No MongoDatabaseFactory beans found.");
      return null;
    }

    if (beanNames.length == 1) {
      String beanName = beanNames[0];
      BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
      return new BeanDefinitionHolder(beanDefinition, beanName);
    }

    for (String beanName : beanNames) {
      BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
      if (beanDefinition.isPrimary()) {
        return new BeanDefinitionHolder(beanDefinition, beanName);
      }
    }

    log.info("No primary MongoDatabaseFactory found.");
    return null;
  }

  /**
   * @param primary Flag to indicate if the bean should be set as primary.
   * @return Bean definition to replace the existing {@link MongoDatabaseFactory} bean with.
   */
  private BeanDefinition createMongoDatabaseFactoryBeanDefinition(boolean primary) {

    BeanDefinition beanDefinition = new RootBeanDefinition(MongoDatabaseFactoryBean.class);
    beanDefinition.setPrimary(primary);
    return beanDefinition;
  }
}
