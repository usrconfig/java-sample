package com.seagame.ext.dao;

/**
 * @author LamHM
 */
public interface SequenceRepository {
    long getNextSequenceId(String key);

    long getSequenceId(String key);

    void createSequenceDocument(String documentName, long defaultValue);
}
