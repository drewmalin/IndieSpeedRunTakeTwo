package com.orbit.core;


public class MapCanvas {
	public int[][][] mapData;

	public int mapWidth;
	public int mapHeight;
	
	/**
	 * @param lines
	 * MapCanvas objects each represent single elevation levels of the
	 * GameMap.
	 */
	public MapCanvas(int lines) {
		mapData = new int[lines][][];
	}
	
	/**
	 * Sets the width of the MapCanvas (this is the maximum number of columns
	 * in the 'canvas' tag of the resource file).
	 */
	public void setWidth() {
		int max = 0;
		for (int i = 0; i < mapData.length; i++) {
			if (mapData[i].length > max) max = mapData[i].length;
		}
		mapWidth = max;
	}
	
	/**
	 * Sets the height of the MapCanvas (this is simply the number of
	 * rows in the 'canvas' tag of the resource file).
	 */
	public void setHeight() {
		mapHeight = mapData.length;
	}
}
