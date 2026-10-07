package com.jcaa.restaurante.repository;

import com.jcaa.restaurante.entity.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {
}