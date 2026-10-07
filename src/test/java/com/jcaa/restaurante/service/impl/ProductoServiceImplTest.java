package com.jcaa.restaurante.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcaa.restaurante.dto.ProductoRequest;
import com.jcaa.restaurante.dto.ProductoResponse;
import com.jcaa.restaurante.entity.Producto;
import com.jcaa.restaurante.exception.ResourceNotFoundException;
import com.jcaa.restaurante.repository.ProductoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Servicio de productos")
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository repository;

    @InjectMocks
    private ProductoServiceImpl service;

    private ProductoRequest requestValido() {
        ProductoRequest request = new ProductoRequest();
        request.setNombre("  Bandeja paisa  ");
        request.setDescripcion("  con frijoles  ");
        request.setPrecio(new BigDecimal("25000.00"));
        request.setDisponible(true);
        return request;
    }

    private Producto entidad(Long id, String nombre) {
        Producto producto = new Producto(nombre, "descripcion", new BigDecimal("1000.00"), true);
        producto.setId(id);
        return producto;
    }

    @Test
    @DisplayName("crear guarda el producto con el nombre normalizado")
    void crearNormalizaCampos() {
        when(repository.save(any(Producto.class))).thenAnswer(inv -> {
            Producto guardado = inv.getArgument(0);
            guardado.setId(1L);
            return guardado;
        });

        ProductoResponse respuesta = service.crear(requestValido());

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Bandeja paisa");
        assertThat(captor.getValue().getDescripcion()).isEqualTo("con frijoles");
        assertThat(respuesta.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("crear deja la descripcion en null si viene vacia")
    void crearNormalizaDescripcionVacia() {
        ProductoRequest request = requestValido();
        request.setDescripcion("   ");
        when(repository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        service.crear(request);

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDescripcion()).isNull();
    }

    @Test
    @DisplayName("buscarPorId lanza ResourceNotFoundException si no existe")
    void buscarPorIdNoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("actualizar no crea un producto nuevo cuando el id existe")
    void actualizarReutilizaLaEntidad() {
        Producto existente = entidad(7L, "Sancocho");
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoRequest request = requestValido();
        request.setNombre("Sancocho especial");

        ProductoResponse respuesta = service.actualizar(7L, request);

        assertThat(respuesta.getId()).isEqualTo(7L);
        assertThat(respuesta.getNombre()).isEqualTo("Sancocho especial");
        verify(repository, never()).save(argThat(producto -> producto.getId() == null));
    }

    @Test
    @DisplayName("listar sin busqueda devuelve todos los productos")
    void listarSinBusqueda() {
        when(repository.findAll()).thenReturn(List.of(entidad(1L, "Arepa"), entidad(2L, "Empanada")));

        List<ProductoResponse> resultado = service.listar("  ");

        assertThat(resultado).hasSize(2);
        verify(repository, never()).findByNombreContainingIgnoreCaseOrderByNombreAsc(any());
    }

    @Test
    @DisplayName("listar con busqueda delega en la consulta filtrada")
    void listarConBusqueda() {
        when(repository.findByNombreContainingIgnoreCaseOrderByNombreAsc("arepa"))
                .thenReturn(List.of(entidad(1L, "Arepa")));

        List<ProductoResponse> resultado = service.listar("  arepa  ");

        assertThat(resultado).hasSize(1);
        verify(repository).findByNombreContainingIgnoreCaseOrderByNombreAsc("arepa");
    }

    @Test
    @DisplayName("eliminar borra la entidad cuando existe")
    void eliminarExistente() {
        Producto existente = entidad(3L, "Limonada");
        when(repository.findById(3L)).thenReturn(Optional.of(existente));

        service.eliminar(3L);

        verify(repository).delete(existente);
    }
}