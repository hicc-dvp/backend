package hicc.club_fair_2025.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

    @GetMapping("test")
    @ResponseBody
    public String home() {
        return "test123324";
    }
}
