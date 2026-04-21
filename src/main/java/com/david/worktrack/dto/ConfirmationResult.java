package com.david.worktrack.dto;

import com.david.worktrack.entity.AppUser;

public record ConfirmationResult(
        String message,
        AppUser user,
        boolean firstTime
) {
}
