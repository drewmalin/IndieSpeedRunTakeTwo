package com.orbit.managers;

import java.util.ArrayList;

import com.orbit.core.Camera;
import com.orbit.core.GameMap;
import com.orbit.core.MapTile;
import com.orbit.entities.GameEntity;
import com.orbit.xml.Node;
import com.orbit.xml.XMLParser;


public enum ResourceManager {
	
	MANAGER;
	
	public ArrayList<GameEntity> gameEntities;
	public GameEntity playerFocusEntity;
	
	ResourceManager() {
		gameEntities = new ArrayList<GameEntity>();
		playerFocusEntity = new GameEntity();
	}
	
	/**
	 * Loads from an external resource file a new GameEntity object for use
	 * within the game. This method will parse the given XML file and instantiate
	 * a new GameEntity object, returning it to the calling context.
	 * @param file
	 * @return The newly created GameEntity
	 */
	public GameEntity loadEntity(String file) {

		XMLParser entityFile = new XMLParser(file);
		
		GameEntity entity = new GameEntity();
		entity.id = MANAGER.gameEntities.size() + 1;
		MANAGER.gameEntities.add(entity);
		
		entity.setFile(file);
		
		for (Node entityEl : entityFile.root.children) {
			for (Node el : entityEl.children) {
				if (el.name.equals("position"))
					entity.setPosition(el.readFloatArray());
                else if (el.name.equals("direction"))
                    entity.setDirection(el.readFloatArray());
				else if (el.name.equals("width"))
					entity.setWidth(el.readInt());
				else if (el.name.equals("height"))
					entity.setHeight(el.readInt());
				else if (el.name.equals("mass"))
					entity.setMass(el.readFloat());
				else if (el.name.equals("animationFile")) {
					entity.setAnimationFile(el.readString());
					try {
						TextureManager.MANAGER.loadCycle(entity, entity.getAnimationFile());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else if (el.name.equals("scriptFile")) {
					entity.setScriptFile(el.readString());
				}
				else if (el.name.equals("lightRadius"))
					entity.setLightRadius(el.readFloat());
			}
		}

		return entity;
	}

	/**
	 * Loads from an external file a new GameMap object for use within the
	 * game. A GameMap is the primary object which house each MapCanvas object
	 * (that is, each elevation level in the map) and in turn, each MapTile object
	 * (that is, each unique texture block).
	 * @param file
	 * @return The newly created GameMap
	 */
	public void loadMap(String file) {

		XMLParser mapFile = new XMLParser(file);
		
		for (Node mapEl : mapFile.root.children) {
			for (Node el : mapEl.children) {
				if (el.name.equals("tileDimensions"))
					GameMap.MAP.setTileDimension(el.readInt());
				else if (el.name.equals("lightLevel"))
					GameMap.MAP.setLightLevel(el.readFloat());
				else if (el.name.equals("tile")) {
					MapTile mt = new MapTile();
					for (Node tileChild : el.children) {
						if (tileChild.name.equals("id"))
							mt.id = tileChild.readInt();
						else if (tileChild.name.equals("file"))
							mt.loadTexture(tileChild.readString());
						else if (tileChild.name.equals("collidable"))
							mt.collidable = tileChild.readBoolean();
					}
					mt.width = mt.height = GameMap.MAP.tileDimensions;
					GameMap.MAP.tiles.put(mt.id, mt);
				}
				else if (el.name.equals("canvas"))
					GameMap.MAP.parseCanvas(el.data);
				else if (el.name.equals("entity")) {

					GameEntity entity = null;

					for (Node entityEl : el.children) {
						if (entityEl.name.equals("file")) {
							entity = loadEntity(entityEl.readString());
						}
						else if (entityEl.name.equals("position"))
							entity.setPosition(entityEl.readFloatArray());
					}
					//addEntity(entity);
				}
				else if (el.name.equals("player")) {
					for (Node playerNode : el.children) {
						if (playerNode.name.equals("position"))
							MANAGER.playerFocusEntity.setPosition(playerNode.readFloatArray());
					}
				}
			}
		}
	}
	/*
	public void addEntity(GameEntity ge) {
		ge.id = MANAGER.gameEntities.size() + 1;
		MANAGER.gameEntities.add(ge);
	}*/
	
	public void changeLevel(String lvlFile) {
		
		GraphicsManager.MANAGER.fadeToBlack();

		GameEntity temp = MANAGER.playerFocusEntity;
		MANAGER.gameEntities.clear();
		MANAGER.gameEntities.add(temp);

		GameMap.MAP.unload();
		loadMap("res/maps/"+lvlFile);
		Camera.CAMERA.findPlayer();
	}
	
	public void setFocusEntity(GameEntity ge) {
		MANAGER.playerFocusEntity = ge;
	}
}
