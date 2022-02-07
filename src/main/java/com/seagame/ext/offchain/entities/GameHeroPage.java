package com.seagame.ext.offchain.entities;

import java.util.List;

/**
 * @author LamHM
 *
 */
public class GameHeroPage {
	private int page;
	private int maxPage;
	private List<GameHeroExt> gameHeroes;


	public int getPage() {
		return page;
	}


	public void setPage(int page) {
		this.page = page;
	}


	public int getMaxPage() {
		return maxPage;
	}


	public void setMaxPage(int maxPage) {
		this.maxPage = maxPage;
	}


	public List<GameHeroExt> getGameHeroes() {
		return gameHeroes;
	}


	public void setGameHeroes(List<GameHeroExt> gameHeroes) {
		this.gameHeroes = gameHeroes;
	}

}
