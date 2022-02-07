package com.seagame.ext.offchain.services;

import com.creants.eventhandling.dto.OffchainMessageResponse;
import com.creants.eventhandling.dto.UpdateAssetRequest;
import com.creants.eventhandling.dto.UpdateBalanceRequest;
import com.creants.eventhandling.service.IOffchainEventHandling;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author LamHM
 */
@RestController
public class OffChainApiController implements IOffchainEventHandling {


    @Override
    public ResponseEntity<OffchainMessageResponse> updateBalance(UpdateBalanceRequest updateBalanceReq) {
        return null;
    }

    @Override
    public ResponseEntity<OffchainMessageResponse> updateAsset(UpdateAssetRequest updateAssetRequest) {
        return null;
    }

    @Override
    public ResponseEntity<OffchainMessageResponse> getAsset(List<Integer> assetIds) {
        return null;
    }
}
