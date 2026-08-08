package com.moustapha.tp.clients_api.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moustapha.tp.clients_api.model.Client;
import com.moustapha.tp.clients_api.service.ClientService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

@PostMapping("")
 public ResponseEntity<Client> createClient(@RequestBody Client c) {
    Client result = this.clientService.createOrUpdate(c);
    return ResponseEntity.status(HttpStatus.CREATED).body(result) ;
 }

@GetMapping("")
 public List<Client> getAllClient() {
    return this.clientService.getAllClients();
 }

@GetMapping("/{id}")
 public  ResponseEntity<Client> getClientById(@PathVariable Long id) {
    Client result = this.clientService.getClientById(id);
   if(null==result) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
   }
   return ResponseEntity.status(HttpStatus.OK).body(result);
 }

  @PutMapping("/{id}")
 public ResponseEntity<Client> updateClient(@PathVariable Long id,@RequestBody Client c) {
   Client result = this.clientService.getClientById(id);
   if(null==result) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
   result.setAdresse(c.getAdresse());
   result.setNom(c.getNom());
   result.setPrenom(c.getPrenom());
   this.clientService.createOrUpdate(result);
   return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
 }
 
 @DeleteMapping("/{id}")
 public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
   Client result = this.clientService.getClientById(id);
   if(null==result) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

   if(this.clientService.deleteClient(id)) 
      return ResponseEntity.status(HttpStatus.OK).body(null);

   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
 }



}
