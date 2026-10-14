package verkkis.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import verkkis.demo.model.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer> {
}