package com.orbit.core;


import org.lwjgl.util.vector.Vector3f;

import com.orbit.managers.GraphicsManager;
import com.orbit.managers.ResourceManager;

public enum Camera implements Moveable {
	
	CAMERA;
	
	public Vector3f target;
	public final Vector3f position = new Vector3f(0, 0, 0);
	final Vector3f rotation = new Vector3f(0, 0, 0);
	
	Camera() {
		target = new Vector3f(0f, 0f, 0f);
	}

	public int getPosition(int i) {
		switch (i) {
			case 0:
				return (int) position.x;
			case 1:
				return (int) position.y;
			case 2:
				return (int) position.z;
		}
		return 0;
	}
	
	public void setTarget(Vector3f vector3f)   { target.set(vector3f); }

	/*
	 * A super messy way to initiate the camera position without gameplay-input (meant to be run exactly once,
	 * at the beginning of the loading of a level). 
	 */
	public void findPlayer() {
		boolean lockNS = ResourceManager.MANAGER.playerFocusEntity.cameraLockNS(GameMap.MAP, GraphicsManager.MANAGER);
		boolean lockEW = ResourceManager.MANAGER.playerFocusEntity.cameraLockEW(GameMap.MAP, GraphicsManager.MANAGER);
		
		int mapHeight = GameMap.MAP.mapCanvas.get(ResourceManager.MANAGER.playerFocusEntity.mapLevel).mapHeight *  GameMap.MAP.tileDimensions;
		int mapWidth = GameMap.MAP.mapCanvas.get(ResourceManager.MANAGER.playerFocusEntity.mapLevel).mapWidth * GameMap.MAP.tileDimensions;

		int screenHeight = GraphicsManager.MANAGER.getHeight();
		int screenWidth = GraphicsManager.MANAGER.getWidth();

		int top = mapHeight - screenHeight + (GraphicsManager.MANAGER.border*2); //excess height!
		int right = mapWidth - screenWidth + (GraphicsManager.MANAGER.border*2);
		
		setPosition(0, 0, 0);
		
		if (lockNS)
			position.y = ResourceManager.MANAGER.playerFocusEntity.position.y;
		else {
			if (ResourceManager.MANAGER.playerFocusEntity.position.y > ((mapHeight/2) - top/2))
				position.y = mapHeight/2 + top/2;
			else
				position.y = mapHeight/2 - top/2;
		}
		
		if (lockEW)
			position.x = ResourceManager.MANAGER.playerFocusEntity.position.x;
		else {
			if (ResourceManager.MANAGER.playerFocusEntity.position.x > ((mapWidth/2) - right/2))
				position.x = mapWidth/2 + right/2;
			else
				position.x = mapWidth/2 - right/2;
		}
	}
	
	@Override
	public void setPosition(float x, float y, float z) {
		if (position == null) position.set(x, y, z);
		else position.set(x, y, z);
	}
	
	@Override
	public void setRotation(float x, float y, float z) {
		if (rotation == null) rotation.set(x, y, z);
		else rotation.set(x, y, z);
	}
	
	@Override
	public float translateX(float delta) {
		position.x += delta;
		return delta;
	}
	
	@Override
	public float translateY(float delta) {
		position.y += delta;
		return delta;
	}
	
	@Override
	public Vector3f getPosition() { return position; }
	
	@Override
	public Vector3f getRotation() { return rotation; }
}
