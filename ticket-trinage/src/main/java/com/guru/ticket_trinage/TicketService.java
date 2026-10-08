package com.guru.ticket_trinage;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository repo;

    public TicketService(TicketRepository repo) {
        this.repo = repo;
    }

    public Ticket create(Ticket ticket) {
        ticket.setId(null);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setCreatedAt(LocalDateTime.now());
        return repo.save(ticket);
    }

    public List<Ticket> findAll(TicketStatus status) {
        if (status == null) {
            return repo.findAll();
        }
        return repo.findByStatus(status);
    }

    public Ticket findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
    }

    public Ticket updateStatus(Long id, TicketStatus status) {
        Ticket ticket = findById(id);
        ticket.setStatus(status);
        return repo.save(ticket);
    }

    public void delete(Long id) {
        Ticket ticket = findById(id);
        repo.delete(ticket);
    }
}