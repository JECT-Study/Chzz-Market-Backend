package org.chzz.market.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.chzz.market.common.config.AuditingConfig;
import org.chzz.market.common.config.QuerydslConfig;
import org.chzz.market.common.config.QuerydslOrderConfig;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QuerydslConfig.class,  AuditingConfig.class, QuerydslOrderConfig.class, QuartzConfig.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Profile("test")
public @interface DatabaseTest {
}
