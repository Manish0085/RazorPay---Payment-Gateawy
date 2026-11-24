package com.razorpay.payment_gateway.merchant.dto.request;

import com.razorpay.payment_gateway.common.enums.BusinessType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(
        name = "MerchantSignupRequest",
        description = "Request to register a new merchant account. The merchant starts in PENDING_KYC status."
)
public record MerchantSignupRequest(

        @Schema(description = "Full name of the account owner", example = "Rahul Sharma",
                minLength = 2, maxLength = 200)
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 200, message = "Name must be 2-200 characters")
        @Pattern(regexp = "^\\p{L}[\\p{L}\\p{M} .'-]*$",
                message = "Name may contain only letters, spaces, dots, apostrophes and hyphens")
        String name,

        @Schema(description = "Login email. Case-insensitive and unique across all accounts",
                example = "rahul@acme.com", maxLength = 254)
        @NotBlank(message = "Email is required")
        @Size(max = 254, message = "Email must be at most 254 characters")
        @Email(message = "Email must be valid")
        @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Email must include a domain, e.g. name@example.com")
        String email,

        @Schema(description = "Account password: 8-72 characters with at least one uppercase letter, "
                + "one lowercase letter, one digit and one special character",
                example = "S3cure@Pass", format = "password", minLength = 8, maxLength = 72,
                accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be 8-72 characters")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "Password must contain an uppercase letter, a lowercase letter, a digit and a special character")
        String password,

        @Schema(description = "Registered or trading name of the business", example = "Acme Traders Pvt Ltd",
                minLength = 2, maxLength = 200)
        @NotBlank(message = "Business name is required")
        @Size(min = 2, max = 200, message = "Business name must be 2-200 characters")
        String businessName,

        @Schema(description = "Legal structure of the business", example = "PRIVATE_LIMITED")
        @NotNull(message = "Business type is required")
        BusinessType businessType
) {

    /** Normalize before validation runs. The password is never trimmed. */
    public MerchantSignupRequest {
        if (name != null) name = name.trim();
        if (email != null) email = email.trim().toLowerCase();
        if (businessName != null) businessName = businessName.trim();
    }

    /** Records generate toString() with every field, so mask the password to keep it out of logs. */
    @Override
    public String toString() {
        return "MerchantSignupRequest[name=" + name + ", email=" + email
                + ", password=****, businessName=" + businessName
                + ", businessType=" + businessType + "]";
    }
}