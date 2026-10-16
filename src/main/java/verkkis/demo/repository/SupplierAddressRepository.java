package verkkis.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import verkkis.demo.model.SupplierAddress;

@Repository
public interface SupplierAddressRepository extends JpaRepository<SupplierAddress, Integer> {

    List<SupplierAddress> findBySupplier_IdOrderById(Integer supplierId);

    void deleteBySupplier_Id(Integer supplierId);
}
