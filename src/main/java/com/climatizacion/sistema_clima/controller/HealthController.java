package com.climatizacion.sistema_clima.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String ping() {
        return "🟢 Servi A/C Pro Backend Activo y en línea 24/7";
    }
}