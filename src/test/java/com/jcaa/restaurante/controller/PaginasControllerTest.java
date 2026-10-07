package com.jcaa.restaurante.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.jcaa.restaurante.entity.Producto;
import com.jcaa.restaurante.entity.Restaurante;
import com.jcaa.restaurante.repository.ProductoRepository;
import com.jcaa.restaurante.repository.RestauranteRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prueba de integracion del camino completo: HTTP -> controlador -> servicio -> JPA.
 * Usa H2 en memoria (perfil test), por eso no necesita un PostgreSQL levantado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Paginas web")
class PaginasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Test
    @DisplayName("GET / devuelve la vista de inicio con los restaurantes")
    void inicioMuestraRestaurantes() throws Exception {
        restauranteRepository.save(new Restaurante("El Rincon", "901234567", "Calle 1", "3000000000"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("El Rincon")));
    }

    @Test
    @DisplayName("GET /productos lista lo guardado en la base de datos")
    void listaProductos() throws Exception {
        productoRepository.save(new Producto("Arepa de queso", "con queso", new BigDecimal("3000.00"), true));

        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(view().name("productos/lista"))
                .andExpect(content().string(containsString("Arepa de queso")));
    }

    @Test
    @DisplayName("GET /productos?q= filtra por nombre")
    void listaProductosFiltra() throws Exception {
        productoRepository.save(new Producto("Arepa de queso", "con queso", new BigDecimal("3000.00"), true));
        productoRepository.save(new Producto("Limonada", "con hielo", new BigDecimal("2500.00"), true));

        mockMvc.perform(get("/productos").param("q", "limonada"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Limonada")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Arepa de queso"))));
    }

    @Test
    @DisplayName("POST /productos persiste el producto y redirige al listado")
    void crearProducto() throws Exception {
        mockMvc.perform(post("/productos")
                        .param("nombre", "Bandeja paisa")
                        .param("descripcion", "Completa")
                        .param("precio", "25000.00")
                        .param("disponible", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/productos"));

        assertThat(productoRepository.findAll())
                .extracting(Producto::getNombre)
                .contains("Bandeja paisa");
    }

    @Test
    @DisplayName("POST /productos rechaza un precio de cero")
    void crearProductoConPrecioInvalido() throws Exception {
        mockMvc.perform(post("/productos")
                        .param("nombre", "Producto sin sentido")
                        .param("precio", "0"))
                .andExpect(status().is4xxClientError());

        assertThat(productoRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("POST /productos rechaza un precio que desborda NUMERIC(10, 2)")
    void crearProductoConPrecioDesbordado() throws Exception {
        mockMvc.perform(post("/productos")
                        .param("nombre", "Producto desbordado")
                        .param("precio", "123456789.00"))
                .andExpect(status().is4xxClientError());

        assertThat(productoRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("POST /productos rechaza un precio con exceso de decimales")
    void crearProductoConPrecioDemasiadosDecimales() throws Exception {
        mockMvc.perform(post("/productos")
                        .param("nombre", "Producto con decimales de mas")
                        .param("precio", "10.005"))
                .andExpect(status().is4xxClientError());

        assertThat(productoRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("GET /productos/99/editar devuelve 404 si el producto no existe")
    void editarProductoInexistente() throws Exception {
        mockMvc.perform(get("/productos/99/editar"))
                .andExpect(status().isNotFound());
    }
}