package com.bccs.gatewaymanager.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Chay Flyway migration (xem db/migration/V1__baseline.sql) TRUOC KHI Hibernate
 * validate schema - Spring Boot 4.0.8 KHONG con tu dong cau hinh Flyway nua (da
 * xac nhan that: khong co class/artifact Flyway autoconfigure nao trong classpath,
 * khac han Boot 2/3 - property spring.flyway.* KHONG co tac dung gi neu chi khai
 * flyway-core/flyway-database-oracle vao pom.xml suong). Class nay tu lam lai
 * dung viec autoconfigure cu tung lam: 1 bean chay flyway.migrate() + 1
 * BeanFactoryPostProcessor bat "entityManagerFactory" phai @DependsOn bean do,
 * dam bao thu tu chay TRUOC khi JPA EntityManagerFactory (va ddl-auto=validate)
 * duoc khoi tao - khong co buoc nay, validate co the chay truoc khi schema kip
 * duoc Flyway tao/cap nhat.
 */
@Configuration
public class FlywayConfig {

    /** Doc dung 2 property spring.flyway.baseline-on-migrate/baseline-version van khai trong application.yml. */
    @ConfigurationProperties(prefix = "spring.flyway")
    @Bean
    public FlywaySettings flywaySettings() {
        return new FlywaySettings();
    }

    public static class FlywaySettings {
        private boolean enabled = true;
        private boolean baselineOnMigrate = false;
        private String baselineVersion = "1";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isBaselineOnMigrate() {
            return baselineOnMigrate;
        }

        public void setBaselineOnMigrate(boolean baselineOnMigrate) {
            this.baselineOnMigrate = baselineOnMigrate;
        }

        public String getBaselineVersion() {
            return baselineVersion;
        }

        public void setBaselineVersion(String baselineVersion) {
            this.baselineVersion = baselineVersion;
        }
    }

    /**
     * Chay migrate() ngay luc bean nay duoc khoi tao (constructor) - dam bao xong
     * TRUOC khi bat ky bean nao @DependsOn no duoc tao (xem
     * EntityManagerFactoryFlywayDependsOnPostProcessor duoi day).
     */
    public static class FlywayMigrationRunner {
        public FlywayMigrationRunner(DataSource dataSource, FlywaySettings settings) {
            if (!settings.isEnabled()) {
                return;
            }
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .baselineOnMigrate(settings.isBaselineOnMigrate())
                    .baselineVersion(settings.getBaselineVersion())
                    .load();
            flyway.migrate();
        }
    }

    @Bean
    public FlywayMigrationRunner flywayMigrationRunner(DataSource dataSource, FlywaySettings flywaySettings) {
        return new FlywayMigrationRunner(dataSource, flywaySettings);
    }

    /**
     * Ep bean "entityManagerFactory" (dinh nghia boi
     * HibernateJpaConfiguration cua Spring Boot) phai doi flywayMigrationRunner
     * tao xong truoc - neu khong, thu tu khoi tao bean mac dinh cua Spring
     * (theo dependency injection graph, KHONG theo thu tu khai bao @Bean) co
     * the tao entityManagerFactory (va chay ddl-auto=validate) TRUOC ca khi
     * Flyway kip migrate xong.
     */
    @Bean
    public static BeanFactoryPostProcessor entityManagerFactoryDependsOnFlyway() {
        return (ConfigurableListableBeanFactory beanFactory) -> {
            if (beanFactory instanceof BeanDefinitionRegistry registry
                    && registry.containsBeanDefinition("entityManagerFactory")) {
                BeanDefinition def = registry.getBeanDefinition("entityManagerFactory");
                def.setDependsOn("flywayMigrationRunner");
            }
        };
    }
}
