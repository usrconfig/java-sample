package com.seagame.ext.services;

import com.seagame.ext.entities.Player;

public interface IMailService {

    void sendTestMail(Player player);

    void sendTestInfoMail(Player player);

    void createWelcomeNewPlayerMail(String gameId);
}
