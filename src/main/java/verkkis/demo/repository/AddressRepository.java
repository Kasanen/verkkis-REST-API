package verkkis.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import verkkis.demo.model.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
}