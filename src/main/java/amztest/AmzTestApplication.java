package amztest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AmzTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmzTestApplication.class, args);
    }

}
