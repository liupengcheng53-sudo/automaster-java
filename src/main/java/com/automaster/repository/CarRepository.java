package com.automaster.repository;

import com.automaster.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, String> {

    List<Car> findByStatus(String status);


    // 按VIN查询车辆（用于校验重复）
    Optional<Car> findByVin(String vin);
}
