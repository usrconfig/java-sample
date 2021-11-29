package com.seagame.ext.config.game;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.seagame.ext.entities.asset.Asset;
import com.seagame.ext.util.SourceFileHelper;

import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author LamHM
 *
 */
public class AssetManagerConfig {
	public static final String ASSET_MANAGER_CONFIG = "assets.xml";
	private static AssetManagerConfig instance;
	private Map<String, Asset> assets;


	public static AssetManagerConfig getInstance() {
		if (instance == null) {
			instance = new AssetManagerConfig();
		}

		return instance;
	}


	private AssetManagerConfig() {
		loadStage();
	}


	public String reload() throws IOException {
		loadStage();
		return writeToJsonFile();
	}


	public void loadStage() {
		try {
			Map<String, Asset> assets = new HashMap<>();
			XMLStreamReader sr = SourceFileHelper.getStreamReader(ASSET_MANAGER_CONFIG);
			XmlMapper mapper = new XmlMapper();
			sr.next(); // to point to <Stages>
			sr.next(); // to point to <Stages>
			Asset asset = null;
			while (sr.hasNext()) {
				try {
					asset = mapper.readValue(sr, Asset.class);
					assets.put(asset.getIndex(), asset);
				} catch (Exception e) {
				}
			}

			sr.close();
			this.assets = assets;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public List<Asset> getStages() {
		return new ArrayList<>(assets.values());
	}


	public String writeToJsonFile() throws IOException {
		return SourceFileHelper.exportJsonFile(
				assets.values(),
				"assets.json");
	}


	public static void main(String[] args) throws IOException {
		AssetManagerConfig.getInstance().writeToJsonFile();
	}

}
