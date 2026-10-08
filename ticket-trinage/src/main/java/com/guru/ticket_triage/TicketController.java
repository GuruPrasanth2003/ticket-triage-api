package com.guru.ticket_triage;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Ticket> create(@Valid @RequestBody Ticket ticket) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(ticket));
    }

    @GetMapping
    public List<Ticket> list(@RequestParam(required = false) TicketStatus status) {
        return service.findAll(status);
    }

    @GetMapping("/{id}")
    public Ticket getOne(@PathVariable Long id) {
        return service.findById(id);
    }

    @PatchMapping("/{id}/status")
    public Ticket updateStatus(@PathVariable Long id,
                               @Valid @RequestBody StatusUpdateRequest request) {
        return service.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}