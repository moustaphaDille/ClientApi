package com.moustapha.tp.clients_api.repository;
import org.springframework.data.jpa.repository.JpaRepository;


import com.moustapha.tp.clients_api.model.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {

}
