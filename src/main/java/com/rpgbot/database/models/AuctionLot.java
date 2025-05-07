package com.rpgbot.database.models;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auction_lots")
public class AuctionLot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Character seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private int price;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean sold;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private Character buyer;

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Character getSeller() { return seller; }
    public void setSeller(Character seller) { this.seller = seller; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isSold() { return sold; }
    public void setSold(boolean sold) { this.sold = sold; }

    public Character getBuyer() { return buyer; }
    public void setBuyer(Character buyer) { this.buyer = buyer; }
}