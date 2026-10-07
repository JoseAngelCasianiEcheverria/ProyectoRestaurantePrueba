package com.jcaa.restaurante.service.impl;

import com.jcaa.restaurante.dto.ProductoRequest;
import com.jcaa.restaurante.dto.ProductoResponse;
import com.jcaa.restaurante.entity.Producto;
import com.jcaa.restaurante.exception.ResourceNotFoundException;
import com.jcaa.restaurante.repository.ProductoRepository;
import com.jcaa.restaurante.service.ProductoService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository repository;

    public ProductoServiceImpl(ProductoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(String busqueda) {
        List<Producto> productos = StringUtils.hasText(busqueda)
                ? repository.findByNombreContainingIgnoreCaseOrderByNombreAsc(busqueda.trim())
                : repository.findAll();
        return productos.stream().map(this::mapear).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse buscarPorId(Long id) {
        return mapear(buscarEntidad(id));
    }

    @Override
    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = new Producto();
        aplicar(producto, request);
        return mapear(repository.save(producto));
    }

    @Override
    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = buscarEntidad(id);
        aplicar(producto, request);
        return mapear(repository.save(producto));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscarEntidad(id);
        repository.delete(producto);
    }

    private Producto buscarEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un producto con el id " + id));
    }

    private void aplicar(Producto producto, ProductoRequest request) {
        producto.setNombre(request.getNombre().trim());
        producto.setDescripcion(StringUtils.hasText(request.getDescripcion())
                ? request.getDescripcion().trim() : null);
        producto.setPrecio(request.getPrecio());
        producto.setDisponible(request.isDisponible());
    }

    private ProductoResponse mapear(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.isDisponible(),
                producto.getCreadoEn());
    }
}