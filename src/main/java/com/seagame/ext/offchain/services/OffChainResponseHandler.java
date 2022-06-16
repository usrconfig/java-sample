package com.seagame.ext.offchain.services;

import net.sf.json.JSONObject;

public interface OffChainResponseHandler {
    JSONObject onOk(JSONObject jsonObject);

    JSONObject onNg(JSONObject jsonObject);
}
