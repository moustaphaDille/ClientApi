package com.moustapha.tp.clients_api.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.moustapha.tp.clients_api.model.Client;
import com.moustapha.tp.clients_api.repository.ClientRepository;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Cacheable(value = "clients")
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client createOrUpdate(Client client) {
        return clientRepository.save(client);
    }

    public Client getClientById(Long id) {
        return clientRepository.findById(id).orElse(null);
    }

    public boolean deleteClient(Long id) {
        clientRepository.deleteById(id);
        return true;
    }



}
