package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "creator_codes")
public class CreatorCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private CreatorProfile creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(name = "store_url", length = 500)
    private String storeUrl;

    @Column(name = "discount_description", length = 255)
    private String discountDescription;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Column(name = "affiliate_disclosure", columnDefinition = "TEXT")
    private String affiliateDisclosure;

    @Column(name = "verified", nullable = false, columnDefinition = "tinyint(1) not null default 0")
    private Boolean verified = false;

    @Column(name = "last_verified_at")
    private LocalDateTime lastVerifiedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public CreatorCode() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CreatorProfile getCreator() { return creator; }
    public void setCreator(CreatorProfile creator) { this.creator = creator; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getStoreUrl() { return storeUrl; }
    public void setStoreUrl(String storeUrl) { this.storeUrl = storeUrl; }
    public String getDiscountDescription() { return discountDescription; }
    public void setDiscountDescription(String discountDescription) { this.discountDescription = discountDescription; }
    public LocalDate getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDate expiresAt) { this.expiresAt = expiresAt; }
    public String getAffiliateDisclosure() { return affiliateDisclosure; }
    public void setAffiliateDisclosure(String affiliateDisclosure) { this.affiliateDisclosure = affiliateDisclosure; }
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    public LocalDateTime getLastVerifiedAt() { return lastVerifiedAt; }
    public void setLastVerifiedAt(LocalDateTime lastVerifiedAt) { this.lastVerifiedAt = lastVerifiedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
