package com.seagame.ext.dao;

import com.seagame.ext.entities.SystemSetting;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * id: playerId#chapterId(server_id#user_id#hero_no#chapterId)
 *
 * @author LamHM
 */
@Repository
public interface SystemSettingRepository extends MongoRepository<SystemSetting, String> {

}
