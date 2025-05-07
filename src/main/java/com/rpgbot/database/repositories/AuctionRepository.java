package com.rpgbot.database.repositories;

import com.rpgbot.database.models.AuctionLot;
import com.rpgbot.database.models.Character;
import java.util.List;

public interface AuctionRepository {
    void save(AuctionLot lot);
    AuctionLot findById(Long id);
    List<AuctionLot> findBySeller(Character seller);
    List<AuctionLot> findActiveLots();
    List<AuctionLot> findExpiredLots();
    void delete(AuctionLot lot);
}
