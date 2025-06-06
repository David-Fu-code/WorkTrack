package com.david.worktrack.email;

import org.springframework.stereotype.Repository;

@Repository
public interface EmailSender {
    void send(String to, String email);
}
