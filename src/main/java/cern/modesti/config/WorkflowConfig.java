package cern.modesti.config;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;

import cern.modesti.security.CustomGroupEntityManager;
import cern.modesti.security.SpringSecurityLdapUserEntityManager;
import cern.modesti.workflow.RequestStatusManager;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Configuration
@Profile("test")
public class WorkflowConfig {

  @Autowired
  Environment env;

  @Bean
  public RequestStatusManager requestStatusManager() {
    return new RequestStatusManager();
  }

  @Bean
  InitializingBean ldapConfigurationInitializer(final SpringProcessEngineConfiguration configuration) {

    return new InitializingBean() {
      @Override
      public void afterPropertiesSet() {
        //configuration.addConfigurator(ldapConfigurator());

        configuration.getSessionFactories().put(UserIdentityManager.class, new CustomUserEntityManagerFactory());
        //configuration.getSessionFactories().put(GroupIdentityManager.class, new CustomGroupEntityManagerFactory());
      }
    };
  }

  class CustomUserEntityManagerFactory implements SessionFactory {

    @Override
    public Class<?> getSessionType() {
      return UserIdentityManager.class;
    }

    @Override
    public Session openSession() {
      return userEntityManager();
    }
  }

  @Bean
  SpringSecurityLdapUserEntityManager userEntityManager() {
    return new SpringSecurityLdapUserEntityManager(anonymousLdapTemplate());
  }

  @Bean
  public LdapTemplate anonymousLdapTemplate() {
    return new SpringSecurityLdapTemplate(anonymousContextSource());
  }

  @Bean
  public LdapContextSource anonymousContextSource() {
    DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(env.getRequiredProperty("ldap.anon.url"));
    contextSource.setBase(env.getRequiredProperty("ldap.base"));
    contextSource.setAnonymousReadOnly(true);
    return contextSource;
  }

  class CustomGroupEntityManagerFactory implements SessionFactory {

    @Override
    public Class<?> getSessionType() {
      return GroupIdentityManager.class;
    }

    @Override
    public Session openSession() {
      return new CustomGroupEntityManager();
    }
  }
}