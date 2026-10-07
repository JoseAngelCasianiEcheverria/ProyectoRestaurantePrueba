package com.jcaa.restaurante.controller;

import com.jcaa.restaurante.entity.Restaurante;
import com.jcaa.restaurante.repository.RestauranteRepository;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final RestauranteRepository restauranteRepository;

    public HomeController(RestauranteRepository restauranteRepository) {
        this.restauranteRepository = restauranteRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Restaurante> restaurantes = restauranteRepository.findAll();
        model.addAttribute("restaurantes", restaurantes);
        model.addAttribute("totalRestaurantes", restaurantes.size());
        return "index";
    }
}