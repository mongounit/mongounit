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
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.util.ObjectUtils;

/**
 * {@link MongoDbGuardConfiguration} configuration is a bean definition registry post processor that
 * substitutes a custom {@link MongoDbFactory} that modifies the database URI.
 */
public class MongoDbGuardConfiguration implements BeanDefinitionRegistryPostProcessor {

  /**
   * Logger for this configuration class.
   */
  private static Logger log = LoggerFactory.getLogger(MongoDbGuardConfiguration.class);

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
   * Substitutes {@link MongoDbFactory} bean int the registry.
   *
   * @param registry Bean registry to substitute the bean in.
   * @param beanFactory Factory to create the bean that will substitute existing one.
   */
  private void substituteBean(
      BeanDefinitionRegistry registry,
      ConfigurableListableBeanFactory beanFactory) {

    // Retrieve existing MongoDbFactory bean
    BeanDefinitionHolder holder = getMongoDbFactoryBeanDefinition(beanFactory);

    if (holder != null) {
      String beanName = holder.getBeanName();
      boolean primary = holder.getBeanDefinition().isPrimary();

      log.info("Replacing '" + beanName + "' MongoDbFactory bean with "
          + (primary ? "primary " : "") + "MongoUnit version");
      registry.removeBeanDefinition(beanName);
      registry.registerBeanDefinition(beanName, createMongoDbFactoryBeanDefinition(primary));
    }
  }

  /**
   * @param beanFactory Bean factory where to retrieve the existing bean definition for the {@link
   * MongoDbFactory}.
   * @return Bean definition holder of the existing {@link MongoDbFactory} in the registry.
   */
  private BeanDefinitionHolder getMongoDbFactoryBeanDefinition(
      ConfigurableListableBeanFactory beanFactory) {

    String[] beanNames = beanFactory.getBeanNamesForType(MongoDbFactory.class);

    if (ObjectUtils.isEmpty(beanNames)) {
      log.info("No MongoDbFactory beans found.");
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

    log.info("No primary MongoDbFactory found.");
    return null;
  }

  /**
   * @param primary Flag to indicate if the bean should be set as primary.
   * @return Bean definition to replace the existing {@link MongoDbFactory} bean with.
   */
  private BeanDefinition createMongoDbFactoryBeanDefinition(boolean primary) {

    BeanDefinition beanDefinition = new RootBeanDefinition(MongoDbFactoryBean.class);
    beanDefinition.setPrimary(primary);
    return beanDefinition;
  }
}
