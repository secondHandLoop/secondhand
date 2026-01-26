package com.secondhand.user.repository;
import com.secondhand.common.entity.Status;
import com.secondhand.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);

    List<Address> findByUserIdAndStatus(Long userId, Status status);

    boolean existsByUserIdAndStatus(Long userId, Status status);

    List<Address> findByUserIdAndStatusAndIsDefault(Long userId, Status status, Boolean isDefault);
}