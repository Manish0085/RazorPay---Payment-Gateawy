package com.razorpay.payment_gateway.merchant.entity;

import com.razorpay.payment_gateway.common.enums.BusinessType;
import com.razorpay.payment_gateway.common.enums.MerchantStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * A business that accepts payments through the gateway.
 *
 * <p>A merchant is created in {@link MerchantStatus#PENDING_KYC} and can only
 * be moved to ACTIVE once PAN and settlement bank details are present
 * (enforced by the {@code chk_merchant_active_requires_kyc} DB constraint).
 *
 * <p><b>Sensitive fields:</b> {@code panId}, {@code gstId} and
 * {@code settlementBankAccount} are PII/financial data. Never log them, mask
 * them in API responses, and encrypt at rest (see notes below).
 */
@Entity
@Table(
        name = "merchant"
//        indexes = {
//                @Index(name = "idx_merchant_status", columnList = "merchant_status")
//        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Merchant {

    /** Internal immutable identifier (UUID). Exposed to clients via a prefixed public id if needed. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    /** Legal / owner name of the person registering the merchant account. */
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    String name;

    /** Login and communication email. Stored lowercase, unique across merchants. */
    @NotBlank
    @Email
    @Size(max = 254)
    @Column(nullable = false, unique = true, length = 254)
    String email;

    /** Contact phone number, digits with optional leading '+' (7-15 digits). */
    @Pattern(regexp = "^\\+?[0-9]{7,15}$")
    @Column(name = "contact_number", length = 16)
    String contactNumber;

    /** Legal structure of the business (proprietorship, private limited, etc.). */
    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", length = 30)
    BusinessType businessType;

    /** Registered or trading name of the business. */
    @Size(max = 200)
    @Column(name = "business_name", length = 200)
    String businessName;

    /** Merchant's website; used during KYC review. */
    @Size(max = 2048)
    @Column(name = "website_url", length = 2048)
    String websiteUrl;

    /** Lifecycle state of the account. Defaults to PENDING_KYC. */
    @Enumerated(EnumType.STRING)
    @Column(name = "merchant_status", nullable = false, length = 30)
    @Builder.Default
    MerchantStatus merchantStatus = MerchantStatus.PENDING_KYC;

    /** 15-character GSTIN, e.g. 22AAAAA0000A1Z5. Optional; unique when present. */
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$")
    @Column(name = "gst_id", length = 15)
    String gstId;

    /** 10-character PAN, e.g. ABCDE1234F. Unique when present. */
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$")
    @Column(name = "pan_id", length = 10)
    String panId;

    /** Bank account number (9-18 digits) where settlements are paid out. */
    @Pattern(regexp = "^[0-9]{9,18}$")
    @Column(name = "settlement_bank_account", length = 18)
    String settlementBankAccount;

    /** 11-character IFSC of the settlement bank branch, e.g. HDFC0001234. */
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$")
    @Column(name = "settlement_bank_ifsc", length = 11)
    String settlementBankIfsc;

    /** Name on the settlement bank account (used for penny-drop name matching). */
    @Size(max = 200)
    @Column(name = "settlement_account_holder_name", length = 200)
    String settlementAccountHolderName;


}