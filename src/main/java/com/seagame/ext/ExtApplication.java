package com.seagame.ext;

import com.creants.creants_2x.QAntServer;
import com.creants.creants_2x.core.util.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * http://docs.spring.io/spring-data/data-mongo/docs/current/reference/html/
 *
 * @author LamHM
 */
@SpringBootApplication(scanBasePackages = {"com.seagame.ext", "com.creants.eventhandling"})
@EnableScheduling
public class ExtApplication {
    private static ConfigurableApplicationContext context;


    public static <T> T getBean(Class<T> aClass) {
        return context.getBean(aClass);
    }


    public static void main(String[] args) throws InterruptedException {
        System.setProperty("log4j.configurationFile", "resources/log4j2.xml");
        AppConfig.init("config/application.properties");

        context = SpringApplication.run(ExtApplication.class, args);
        QAntServer.getInstance().start();
    }
}
