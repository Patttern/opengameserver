package net.patttern.opengameserver.gameserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Game server application runner
 *
 * @author Egor Babenko (patttern@gmail.com)
 */
@SpringBootApplication
public class GameRunner {

    public static void main(String[] args) {
        SpringApplication.run(GameRunner.class, args);
    }
}
