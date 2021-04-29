package amztest;

import amztest.rest.AmzTestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AmzTestApplication {

    public AmzTestController amzTestController;

    @Autowired
    public AmzTestApplication(AmzTestController amzTestController) {
        this.amzTestController = amzTestController;
    }

    public static void main(String[] args) {
        SpringApplication.run(AmzTestApplication.class, args);
    }

}
