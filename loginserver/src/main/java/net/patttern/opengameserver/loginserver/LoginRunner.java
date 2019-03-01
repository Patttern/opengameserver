package net.patttern.opengameserver.loginserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Login server application runner
 *
 * @author Egor Babenko (patttern@gmail.com)
 */
@SpringBootApplication
public class LoginRunner {

    public static void main(String[] args) {
        SpringApplication.run(LoginRunner.class, args);
    }
}
