package com.cho_co_song_i.yummy.yummy.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class IndexController {
    @GetMapping("/index")
    public String Test(Model model) {
        model.addAttribute("name", "신자");
        return "index";
    }
}
