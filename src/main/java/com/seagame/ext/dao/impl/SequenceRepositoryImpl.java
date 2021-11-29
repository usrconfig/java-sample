package com.seagame.ext.dao.impl;

import com.seagame.ext.dao.SequenceRepository;
import com.seagame.ext.entities.SequenceId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/**
 * @author LamHM
 */
@Repository
public class SequenceRepositoryImpl implements SequenceRepository {
    @Autowired
    private MongoOperations mongoOperations;


    @Override
    public void createSequenceDocument(String documentName, long defaultValue) {
        Query query = new Query(Criteria.where("_id").is(documentName));
        SequenceId sequenceId = mongoOperations.findOne(query, SequenceId.class);
        if (sequenceId == null) {
            SequenceId sequence = new SequenceId();
            sequence.setId(documentName);
            sequence.setSeq(defaultValue);
            mongoOperations.save(sequence);
        }

    }


    @Override
    public long getNextSequenceId(String key) {
        // get sequence id
        Query query = new Query(Criteria.where("_id").is(key));

        // increase sequence id by 1
        Update update = new Update().inc("seq", 1);

        // return new increased id
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);

        // this is the magic happened
        SequenceId sequenceId = mongoOperations.findAndModify(query, update, options, SequenceId.class);
        return sequenceId.getSeq();
    }

    @Override
    public long getSequenceId(String key) {
        // get sequence id
        Query query = new Query(Criteria.where("_id").is(key));

        // this is the magic happened
        SequenceId sequenceId = mongoOperations.findOne(query, SequenceId.class);
        return sequenceId.getSeq();
    }


    public long setDefaultValue(String documentName, long value) {
        // get sequence id
        Query query = new Query(Criteria.where("_id").is(documentName));

        // increase sequence id by 1
        Update update = new Update().set("seq", value);

        // return new increased id
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);

        // this is the magic happened
        SequenceId sequenceId = mongoOperations.findAndModify(query, update, options, SequenceId.class);
        return sequenceId.getSeq();
    }

}
