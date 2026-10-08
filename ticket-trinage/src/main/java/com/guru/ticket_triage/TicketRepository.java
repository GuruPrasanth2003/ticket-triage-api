package com.guru.ticket_triage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
	List<Ticket> findByStatus(TicketStatus status);
}
