package com.jcaa.restaurante.controller;

import com.jcaa.restaurante.dto.ProductoRequest;
import com.jcaa.restaurante.dto.ProductoResponse;
import com.jcaa.restaurante.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(@RequestParam(name = "q", required = false) String busqueda, Model model) {
        model.addAttribute("productos", service.listar(busqueda));
        model.addAttribute("busqueda", busqueda == null ? "" : busqueda);
        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("formulario", new ProductoRequest());
        model.addAttribute("accion", "/productos");
        model.addAttribute("titulo", "Nuevo producto");
        return "productos/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        ProductoResponse producto = service.buscarPorId(id);
        ProductoRequest formulario = new ProductoRequest();
        formulario.setNombre(producto.getNombre());
        formulario.setDescripcion(producto.getDescripcion());
        formulario.setPrecio(producto.getPrecio());
        formulario.setDisponible(producto.isDisponible());
        model.addAttribute("formulario", formulario);
        model.addAttribute("idProducto", id);
        model.addAttribute("accion", "/productos/" + id + "/editar");
        model.addAttribute("titulo", "Editar producto");
        return "productos/formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("formulario") ProductoRequest formulario,
                        RedirectAttributes redirect) {
        ProductoResponse creado = service.crear(formulario);
        redirect.addFlashAttribute("mensaje", "Producto creado: " + creado.getNombre());
        return "redirect:/productos";
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("formulario") ProductoRequest formulario,
                             RedirectAttributes redirect) {
        ProductoResponse actualizado = service.actualizar(id, formulario);
        redirect.addFlashAttribute("mensaje", "Producto actualizado: " + actualizado.getNombre());
        return "redirect:/productos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        service.eliminar(id);
        redirect.addFlashAttribute("mensaje", "Producto eliminado");
        return "redirect:/productos";
    }
}