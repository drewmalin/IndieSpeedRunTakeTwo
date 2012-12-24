package com.orbit.managers;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;

import com.orbit.core.Camera;
import com.orbit.core.GameMap;
import com.orbit.entities.GameEntity;

public enum GraphicsManager {
	
	MANAGER;

	private int windowWidth			= 100;
	private int windowHeight		= 100;
	private float frust				= 60;
	private float zNear				= 0;
	private float zFar				= 100;
	private boolean fullScreen		= false;
	private boolean vSync			= false;
	private int MSAA				= 0;
	private String title			= "Lezend";
	
	public final int border = 30;
	
	private long lastCheck = 0;
	private float fadeDuration = 1500;
	
	GraphicsManager() {}
	
	private void setGraphicsMode(String string) {
		if (string.equals("2D")) {
			GL11.glDepthMask(false);
			GL11.glDisable(GL11.GL_DEPTH_TEST);								
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glEnable(GL11.GL_TEXTURE_2D);	
			
			GL11.glLoadIdentity();
			GL11.glOrtho(0, windowWidth, windowHeight, 0, -1, 1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glShadeModel(GL11.GL_SMOOTH);
		}
		
		else if (string.equals("3D")) {
			GL11.glDepthMask(true);
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			
			GLU.gluPerspective(frust, (float)windowWidth/(float)windowHeight, zNear, zFar);
			GLU.gluLookAt(Camera.CAMERA.position.x, 
					Camera.CAMERA.position.y, 
					Camera.CAMERA.position.z, 
					Camera.CAMERA.target.x, 
					Camera.CAMERA.target.y, 
					Camera.CAMERA.target.z,
						  0, 1, 0);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			
		}
		else
			try {
				throw new Exception("Invalid graphics mode. Mode must be 2D or 3D.");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
	}

	public void setResolution(int requestW, int requestH) {
		windowWidth	 = requestW;
		windowHeight = requestH;
	}

	public void create(String gmode) {		
		try {
			Display.setDisplayMode(new DisplayMode( windowWidth, windowHeight ));
			if (fullScreen) {
				for (DisplayMode dm : Display.getAvailableDisplayModes()) {
					if (dm.toString().contains(windowWidth + " x " + windowHeight + " x " + 32)) {
						Display.setDisplayMode(dm);
						Display.setFullscreen(true);
						break;
					}
				}
			}
			
			Display.setTitle(title);
			Display.setVSyncEnabled(vSync);
			
			if (MSAA > 0) {
				PixelFormat format = new PixelFormat(32, 0, 24, 8, MSAA);
				Pbuffer pb = new Pbuffer(windowWidth, windowHeight, format, null);
				pb.makeCurrent();
				Display.create(format);
			}
			else {
				Display.create();
			}
			
			setGraphicsMode(gmode);

		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(1);
		}		
	}

	/**
	 * Draws the current scene. First sets up the texture parameters to ensure proper rendering/
	 * blending of adjacent textures. Next, the scene is translated to the middle of the map, then
	 * to the position of the camera. Finally, each elevation level in the current GameMap is rendered.
	 */
	public void drawGame() {
		
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GL11.glTranslatef(windowWidth/2, windowHeight/2, 0);
		GL11.glTranslatef(-Camera.CAMERA.position.x, 
						  -Camera.CAMERA.position.y, 
						  -Camera.CAMERA.position.z);
		
		if (ResourceManager.MANAGER.playerFocusEntity.mapLevel > 0)
			drawBackground();
		
		drawCurrentLevel();
		
		if (ResourceManager.MANAGER.playerFocusEntity.mapLevel + 1 < GameMap.MAP.mapCanvas.size())
			drawForeground();

		GL11.glPopMatrix();
	}
	
	public void jitterForDays(int duration) {
		
		int jitterX, jitterY;
		Random generator = new Random(System.nanoTime());
		
		for (int i = 0; i < duration; i++) {
			
			jitterX = generator.nextInt() % 4;
			jitterY = generator.nextInt() % 4;
			
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			
			GL11.glPushMatrix();
			GL11.glLoadIdentity();
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			GL11.glTranslatef(windowWidth/2, windowHeight/2, 0);
			GL11.glTranslatef(-Camera.CAMERA.position.x + jitterX, 
							  -Camera.CAMERA.position.y + jitterY, 
							  -Camera.CAMERA.position.z);
			if (ResourceManager.MANAGER.playerFocusEntity.mapLevel > 0)
				drawBackground();
			
			drawCurrentLevel();
			
			if (ResourceManager.MANAGER.playerFocusEntity.mapLevel + 1 < GameMap.MAP.mapCanvas.size())
				drawForeground();
	
			GL11.glPopMatrix();
			
			Display.update();
			Display.sync(60);
		}
	}
	/**
	 * Draws the current elevation level of the map (that is, the level that the playerFocusEntity
	 * currently sits at). All entities also at this level will be drawn.
	 */
	public void drawCurrentLevel() {
		int playerLevel = ResourceManager.MANAGER.playerFocusEntity.mapLevel;
		ShaderManager.MANAGER.bind();

		for (GameEntity ge : ResourceManager.MANAGER.gameEntities) {
			if (ge.mapLevel == playerLevel && ge.lightRadius > 0) {
				ShaderManager.MANAGER.parmData.put((windowWidth/2) + ge.position.x - Camera.CAMERA.position.x + ge.width/2);
				ShaderManager.MANAGER.parmData.put((windowHeight/2) + ge.position.y - Camera.CAMERA.position.y + ge.height/2);
				ShaderManager.MANAGER.parmData.put(ge.position.z);
				ShaderManager.MANAGER.parmData.put(ge.lightRadius);
			}
		}

		ShaderManager.MANAGER.persistParmData(playerLevel);
				
		GameMap.MAP.drawLevel(playerLevel);
		
		ShaderManager.MANAGER.parmData.clear();
		ShaderManager.MANAGER.unbind();
		
		for (GameEntity ge : ResourceManager.MANAGER.gameEntities) {
			if (ge.mapLevel == playerLevel) ge.draw();
		}
		
	}

	/**
	 * Draws all elevation levels that are *above* the level that the playerFocusEntity occupies. This
	 * level will be slightly translated by a factor of its relative distance from the playerFocusEntity's
	 * level. All entities also at this level will be drawn.
	 */
	public void drawForeground() {

		for (int i = ResourceManager.MANAGER.playerFocusEntity.mapLevel + 1; i < GameMap.MAP.mapCanvas.size(); i++) {
			GL11.glPushMatrix();
			GL11.glTranslatef(-Camera.CAMERA.position.x * i * .1f, 
					  	 	  -Camera.CAMERA.position.y * i * .1f, 
					  	 	  -Camera.CAMERA.position.z * i * .1f);
			
			ShaderManager.MANAGER.bind();
			ShaderManager.MANAGER.persistParmData(i);
			GameMap.MAP.drawLevel(i);
			ShaderManager.MANAGER.parmData.clear();
			ShaderManager.MANAGER.unbind();
			
			for (GameEntity ge : ResourceManager.MANAGER.gameEntities) {
				if (ge.mapLevel == i) ge.draw();
			}
			
			GL11.glPopMatrix();
		}
	}
	
	/**
	 * Draws all elevation levels that are *below* the level that the playerFocusEntity occupies. This
	 * level will be slightly translated (inversely relative to the foreground) by a factor of its relative
	 * distance from the playerFocusEntity's level. All entities also at this level will be drawn.
	 */
	public void drawBackground() {
		int playerLevel = ResourceManager.MANAGER.playerFocusEntity.mapLevel;

		for (int i = 0; i < playerLevel; i++) {
			GL11.glPushMatrix();
			GL11.glTranslatef(Camera.CAMERA.position.x * (playerLevel - i) * .1f, 
					Camera.CAMERA.position.y * (playerLevel - i) * .1f, 
					Camera.CAMERA.position.z * (playerLevel - i) * .1f);
			
			ShaderManager.MANAGER.bind();
			ShaderManager.MANAGER.persistParmData(i);
			GameMap.MAP.drawLevel(i);
			ShaderManager.MANAGER.parmData.clear();
			ShaderManager.MANAGER.unbind();
			
			for (GameEntity ge : ResourceManager.MANAGER.gameEntities) {
				if (ge.mapLevel == i) ge.draw();
			}
			
			GL11.glPopMatrix();
		}
	}
	
	public void fadeToBlack() {
		long currentTime = System.currentTimeMillis();

		do {

			lastCheck = System.currentTimeMillis();

			GL11.glColor4f(0f, 0f, 0f, (float)((lastCheck - currentTime) / fadeDuration));
			GL11.glBegin(GL11.GL_QUADS);
				GL11.glVertex2f(0, 0);
				GL11.glVertex2f(GraphicsManager.MANAGER.getWidth(), 0);
				GL11.glVertex2f(GraphicsManager.MANAGER.getWidth(), GraphicsManager.MANAGER.getHeight());
				GL11.glVertex2f(0, GraphicsManager.MANAGER.getHeight());
			GL11.glEnd();

            Display.update();
			Display.sync(60);

		} while (lastCheck - currentTime < fadeDuration);
	}

	//------------------ Getters/Setters ------------------//
	
	public void setFullscreen(boolean b) { fullScreen = b; }
	public void setVSync(boolean b) 	 { vSync = b; }
	public void setMSAA(int i) 			 { MSAA = i; }
	public void setTitle(String t)     	 { title = t;}
	public void setZNear(float z) 		 { zNear = z; }
	public void setZFar(float z) 		 { zFar = z; }
	public void setFrust(float f) 	  	 { frust = f; }
	
	public int getWidth() 				 { return windowWidth; }
	public int getHeight()  			 { return windowHeight; }
}
