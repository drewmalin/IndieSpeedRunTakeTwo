package com.orbit.core;

import java.io.IOException;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

public class MapTile {
	public int id;
	public String file;
	public int width;
	public int height;
	public boolean collidable;
	public Texture texture;
	
	public MapTile() {
		id = 0;
		width = 0;
		height = 0;
		collidable = false;
		file = "";
	}

	/**
	 * @param x
	 * @param y
	 * Draws the MapTile object at the specified x,y location. No modelview
	 * matrix transformations are performed.
	 */
	public void draw(int x, int y) {
		if (texture != null) texture.bind();	

		GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2f(0f, 0f);
			GL11.glVertex2f(x, y);
			
			GL11.glTexCoord2f(0f, 1f);
			GL11.glVertex2f(x, y + height);
			
			GL11.glTexCoord2f(1f, 1f);
			GL11.glVertex2f(x + width, y + height);
			
			GL11.glTexCoord2f(1f, 0f);
			GL11.glVertex2f(x + width, y);
		GL11.glEnd();
	}

	/**
	 * @param file
	 * Loads a texture file for this particular MapTile object. PNG files
	 * seem to work well for this.
	 */
	public void loadTexture(String file) {
		try {
			String type = file.substring(file.indexOf(".") + 1).toUpperCase();
			texture = TextureLoader.getTexture(type, ResourceLoader.getResourceAsStream(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
