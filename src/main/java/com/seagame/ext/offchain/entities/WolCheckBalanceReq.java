package com.seagame.ext.offchain.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.fluent.Form;

@Getter
@Setter
public class WolCheckBalanceReq extends WolReq {
    String address;
}
