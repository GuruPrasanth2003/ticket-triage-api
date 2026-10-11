package com.guru.ticket_triage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository repo;

    @Mock
    private AiTriageService aiTriageService;

    @InjectMocks
    private TicketService service;

    @BeforeEach
    void setUp() {
        // Fake save(): returns the same ticket it receives
        lenient().when(repo.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Ticket newTicket(String subject, String message) {
        Ticket t = new Ticket();
        t.setSubject(subject);
        t.setMessage(message);
        return t;
    }

    @Test
    void create_whenAiSucceeds_marksTicketTriaged() {
        AiTriageResult ai = new AiTriageResult(
                TicketCategory.BILLING, TicketPriority.HIGH, "Sorry about the double charge.");
        when(aiTriageService.triage("Payment failed", "Charged twice")).thenReturn(Optional.of(ai));

        Ticket result = service.create(newTicket("Payment failed", "Charged twice"));

        assertEquals(TicketStatus.TRIAGED, result.getStatus());
        assertEquals(TicketCategory.BILLING, result.getCategory());
        assertEquals(TicketPriority.HIGH, result.getPriority());
        assertEquals("Sorry about the double charge.", result.getSuggestedReply());
    }

    @Test
    void create_whenAiFails_keepsTicketNewAndUnclassified() {
        when(aiTriageService.triage(any(), any())).thenReturn(Optional.empty());

        Ticket result = service.create(newTicket("Login issue", "Cannot sign in"));

        assertEquals(TicketStatus.NEW, result.getStatus());
        assertEquals(TicketCategory.UNCLASSIFIED, result.getCategory());
        assertNull(result.getPriority());
        assertNull(result.getSuggestedReply());
    }

    @Test
    void create_savesTicketBeforeCallingAi() {
        when(aiTriageService.triage(any(), any())).thenReturn(Optional.empty());

        service.create(newTicket("Subject", "Message"));

        InOrder order = inOrder(repo, aiTriageService);
        order.verify(repo).save(any(Ticket.class));
        order.verify(aiTriageService).triage(any(), any());
        order.verify(repo).save(any(Ticket.class));
    }

    @Test
    void create_ignoresValuesSentByClient() {
        Ticket t = newTicket("Subject", "Message");
        t.setId(99L);
        t.setStatus(TicketStatus.RESOLVED);
        t.setCategory(TicketCategory.TECHNICAL);
        t.setPriority(TicketPriority.LOW);
        t.setSuggestedReply("fake reply");
        when(aiTriageService.triage(any(), any())).thenReturn(Optional.empty());

        Ticket result = service.create(t);

        assertNull(result.getId());
        assertEquals(TicketStatus.NEW, result.getStatus());
        assertEquals(TicketCategory.UNCLASSIFIED, result.getCategory());
        assertNull(result.getPriority());
        assertNull(result.getSuggestedReply());
    }

    @Test
    void findById_whenMissing_throwsNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void updateStatus_changesAndSavesStatus() {
        Ticket t = newTicket("Subject", "Message");
        when(repo.findById(1L)).thenReturn(Optional.of(t));

        Ticket result = service.updateStatus(1L, TicketStatus.RESOLVED);

        assertEquals(TicketStatus.RESOLVED, result.getStatus());
        verify(repo).save(t);
    }

    @Test
    void delete_whenMissing_throwsAndDeletesNothing() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () -> service.delete(99L));
        verify(repo, never()).delete(any(Ticket.class));
    }

    @Test
    void findAll_withStatus_filtersByStatus() {
        Ticket t = newTicket("Subject", "Message");
        when(repo.findByStatus(TicketStatus.NEW)).thenReturn(List.of(t));

        List<Ticket> result = service.findAll(TicketStatus.NEW);

        assertEquals(1, result.size());
        verify(repo, never()).findAll();
    }
}