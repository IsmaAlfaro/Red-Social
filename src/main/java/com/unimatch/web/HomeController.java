package com.unimatch.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /** Sirve la pagina principal publica. */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /** Sirve la pagina de acceso y registro. */
    @GetMapping("/access")
    public String access() {
        return "access";
    }

    /** Sirve la pagina protegida para descubrir perfiles. */
    @GetMapping("/discover")
    public String discover() {
        return "discover";
    }

    /** Sirve la pagina protegida de conversaciones. */
    @GetMapping("/messages")
    public String messages() {
        return "messages";
    }

    /** Sirve la pagina protegida de edicion de perfil. */
    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    /** Sirve la pagina protegida de consulta de perfiles. */
    @GetMapping("/profile-view")
    public String profileView() {
        return "profile-view";
    }
}
