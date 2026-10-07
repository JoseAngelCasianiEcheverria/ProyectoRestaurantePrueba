package com.jcaa.restaurante.repository;

import com.jcaa.restaurante.entity.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);
}