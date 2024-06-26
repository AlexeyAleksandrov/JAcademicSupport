package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.ollama;

import io.github.alexeyaleksandrov.jacademicsupport.services.ollama.OllamaService;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ChatRestController {
    private final OllamaService ollamaService;

    @GetMapping("/ask")
    public String answer(@RequestParam(name = "text", required = true, defaultValue = "Hi") String text)
    {
//        String competency = "ОПК-3.2 : Решает задачи профессиональной деятельности с применением современных информационно-коммуникационных технологий с учетом требований информационной безопасности. Знать: - Выбирает информационные технологии для профессиональной деятельности с учетом требований безопасности. Уметь: - Идентифицирует требования к информационной безопасности в профессиональной деятельности. Владеть: - Применяет технологии обеспечения информационной безопасности.";
//        String content = "Выдели основные ключевые слова из описания профессиональной компетенции: " + competency + "Исключи из ответа размытые и обобщённые словосочетания. Ответ должен быть только на русском языке. Все слова в ответе должны находиться в нормальной форме без склонений и спряжений.  В ответе не пиши словоочетание \"ключевые слова\", напиши только сами слова\"";

        return ollamaService.chat(text);
    }

}
