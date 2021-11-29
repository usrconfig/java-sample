package com.seagame.ext.dao;

import com.seagame.ext.entities.campaign.HeroCampaign;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author LamHM
 */
public interface HeroCampaignRepository extends MongoRepository<HeroCampaign, String> {

}
