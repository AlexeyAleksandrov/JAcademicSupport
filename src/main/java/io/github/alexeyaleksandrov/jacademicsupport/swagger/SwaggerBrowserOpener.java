package io.github.alexeyaleksandrov.jacademicsupport.swagger;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.URI;

@Component
public class SwaggerBrowserOpener implements ApplicationListener<ApplicationStartedEvent> {
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
//        try {
//            String url = "http://localhost:8080/swagger-ui.html";
//            if (System.getProperty("os.name").toLowerCase().contains("win")) {
//                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
//            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
//                Runtime.getRuntime().exec("open " + url);
//            } else if (System.getProperty("os.name").toLowerCase().contains("nux")) {
//                Runtime.getRuntime().exec("xdg-open " + url);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}