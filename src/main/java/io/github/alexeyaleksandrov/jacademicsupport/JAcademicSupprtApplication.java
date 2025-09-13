package io.github.alexeyaleksandrov.jacademicsupport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Включаем поддержку планировщика
public class JAcademicSupprtApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(JAcademicSupprtApplication.class, args);
    }
}
