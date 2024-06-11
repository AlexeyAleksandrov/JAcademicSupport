package io.github.alexeyaleksandrov.jacademicsupport.controllers.advice;

import lombok.AllArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@AllArgsConstructor
public class UserControllerAdvice
{
    @ModelAttribute
    public void addAttributes(Model model)
    {
        boolean isAuth = true;
        String userName = "неавторизованный пользователь";
        model.addAttribute("user_name", userName);
        model.addAttribute("is_auth", isAuth);
    }
}