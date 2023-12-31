package com.example.javatravel.repository;

import com.example.javatravel.entity.AirportEntity;
import com.example.javatravel.entity.PriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceRepository extends JpaRepository<PriceEntity, Long> {

  PriceEntity getPriceById(Long priceId);
}
