package verkkis.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import verkkis.demo.model.CustomerAddress;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Integer> {

    List<CustomerAddress> findByCustomer_IdOrderById(Integer customerId);

    Optional<CustomerAddress> findByIdAndCustomer_Id(Integer id, Integer customerId);

    void deleteByCustomer_Id(Integer customerId);
}
