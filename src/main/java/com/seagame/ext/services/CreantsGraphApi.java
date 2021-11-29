package com.seagame.ext.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author LamHM
 */
@Service
public class CreantsGraphApi {
    @Value("${payment.api}")
    private String paymentUrl;
}
