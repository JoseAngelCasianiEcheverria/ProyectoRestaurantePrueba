package com.jcaa.restaurante.service;

import com.jcaa.restaurante.dto.ProductoRequest;
import com.jcaa.restaurante.dto.ProductoResponse;
import java.util.List;

public interface ProductoService {

    List<ProductoResponse> listar(String busqueda);

    ProductoResponse buscarPorId(Long id);

    ProductoResponse crear(ProductoRequest request);

    ProductoResponse actualizar(Long id, ProductoRequest request);

    void eliminar(Long id);
}