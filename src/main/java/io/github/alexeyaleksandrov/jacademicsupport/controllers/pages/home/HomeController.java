package io.github.alexeyaleksandrov.jacademicsupport.controllers.pages.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController
{
    @GetMapping(value = "/")
    public String getHomePage()
    {
        return "home";
    }
}
