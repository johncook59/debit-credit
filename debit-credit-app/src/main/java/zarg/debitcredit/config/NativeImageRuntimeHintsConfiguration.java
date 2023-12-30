package zarg.debitcredit.config;

import org.hibernate.dialect.PostgreSQLDialect;
import org.postgresql.util.PGInterval;
import org.postgresql.util.PGobject;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(NativeImageRuntimeHintsConfiguration.HibernateRegistrar.class)
public class NativeImageRuntimeHintsConfiguration {

    static class HibernateRegistrar implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
            hints.reflection()
                    .registerType(PostgreSQLDialect.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                    .registerType(PGobject.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                    .registerType(PGInterval.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                            MemberCategory.INVOKE_DECLARED_METHODS);
        }
    }

}