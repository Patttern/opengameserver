package net.patttern.opengameserver.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * client application runner
 *
 * @author Egor Babenko (patttern@gmail.com)
 */
@SpringBootApplication
public class ClientRunner {

    public static void main(String[] args) {
        SpringApplication.run(ClientRunner.class, args);
    }
}
