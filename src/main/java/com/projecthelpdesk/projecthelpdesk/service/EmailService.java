package com.projecthelpdesk.projecthelpdesk.service;

import java.util.logging.Logger;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.projecthelpdesk.projecthelpdesk.entity.Ticket;

@Service
public class EmailService {

    private static final Logger log = Logger.getLogger(EmailService.class.getName());

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendTicketCreatedEmail(Ticket ticket) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(ticket.getCreator().getEmail());
            message.setSubject("Ticket #" + ticket.getId() + " Created - " + ticket.getTitle());
            message.setText("Your ticket has been created successfully.\n\n"
                    + "Title: " + ticket.getTitle() + "\n"
                    + "Priority: " + ticket.getPriority() + "\n"
                    + "Department: " + ticket.getDepartment().getName() + "\n\n"
                    + "We will get back to you shortly.");
            mailSender.send(message);
        } catch (Exception e) {
            log.warning("Failed to send ticket created email: " + e.getMessage());
        }
    }

    @Async
    public void sendTicketAssignedEmail(Ticket ticket) {
        try {
            if (ticket.getAssignedAgent() != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(ticket.getAssignedAgent().getEmail());
                message.setSubject("Ticket #" + ticket.getId() + " Assigned to You - " + ticket.getTitle());
                message.setText("A ticket has been assigned to you.\n\n"
                        + "Title: " + ticket.getTitle() + "\n"
                        + "Priority: " + ticket.getPriority() + "\n\n"
                        + "Please review and take action.");
                mailSender.send(message);
            }
        } catch (Exception e) {
            log.warning("Failed to send ticket assigned email: " + e.getMessage());
        }
    }

    @Async
    public void sendTicketResolvedEmail(Ticket ticket) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(ticket.getCreator().getEmail());
            message.setSubject("Ticket #" + ticket.getId() + " Resolved - " + ticket.getTitle());
            message.setText("Your ticket has been resolved.\n\n"
                    + "Title: " + ticket.getTitle() + "\n\n"
                    + "Please provide feedback if you have any.");
            mailSender.send(message);
        } catch (Exception e) {
            log.warning("Failed to send ticket resolved email: " + e.getMessage());
        }
    }
}
