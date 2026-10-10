package com.guru.ticket_triage;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private final TicketRepository repo;
    private final AiTriageService aiTriageService;

    public TicketService(TicketRepository repo, AiTriageService aiTriageService) {
        this.repo = repo;
        this.aiTriageService = aiTriageService;
    }

    public Ticket create(Ticket ticket) {
        ticket.setId(null);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setCategory(null);
        ticket.setPriority(null);
        ticket.setSuggestedReply(null);

        // Save first, so the ticket is never lost even if the AI call fails
        Ticket saved = repo.save(ticket);

        Optional<AiTriageResult> result =
                aiTriageService.triage(saved.getSubject(), saved.getMessage());

        if (result.isPresent()) {
            saved.setCategory(result.get().category());
            saved.setPriority(result.get().priority());
            saved.setSuggestedReply(result.get().suggestedReply());
            saved.setStatus(TicketStatus.TRIAGED);
        } else {
            saved.setCategory(TicketCategory.UNCLASSIFIED);
        }
        return repo.save(saved);
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