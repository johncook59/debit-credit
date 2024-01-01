package zarg.bank.loader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import zarg.bank.loader.generator.Loader;

@SpringBootApplication()
@Slf4j
class LoaderApplication implements CommandLineRunner {

    private final ApplicationContext applicationContext;

    public LoaderApplication(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(LoaderApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(String... args) {
        applicationContext
                .getBean(Loader.class)
                .loadData();
    }
}
