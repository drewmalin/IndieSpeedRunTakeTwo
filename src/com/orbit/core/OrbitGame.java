package com.orbit.core;


import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import com.orbit.entities.GameEntity;
import com.orbit.managers.GraphicsManager;
import com.orbit.managers.InputManager;
import com.orbit.managers.ResourceManager;
import com.orbit.managers.ScriptManager;
import com.orbit.managers.ShaderManager;
import com.orbit.managers.TextureManager;
import com.orbit.managers.WindowManager;
import com.orbit.ui.MessageBox;
import com.orbit.ui.Window;

public class OrbitGame extends Game {
		
	boolean temp = false;
	
	public static void main(String[] args) {
		OrbitGame game = new OrbitGame();
		game.start();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.orbit.core.Game#initGame()
	 * Initialization of the game. Setup here includes new keyboard and mouse listeners,
	 * key triggers, the player entity, the camera, the first level, and graphics.
	 */
	@Override
	public void initGame() {
				
		// Load input listeners
		InputManager.MANAGER.setKeyboardListener(keyboardListener);
		InputManager.MANAGER.setMouseListener(mouseListener);

		// Load GraphicsManager
		GraphicsManager.MANAGER.setResolution(800, 600);
		GraphicsManager.MANAGER.create("2D");

		// Load game menus
		WindowManager.MANAGER.loadMenu("res/menu/pause.xml");
        WindowManager.MANAGER.loadMenu("res/menu/dialogue.xml");
        WindowManager.MANAGER.loadMenu("res/menu/loading.xml");
        WindowManager.MANAGER.loadConsole();
		WindowManager.MANAGER.createClickListeners();

		// Load Shaders (must be done after loading of graphicsmanager)
		ShaderManager.MANAGER.init("res/shaders/orbit.frag", "res/shaders/orbit.vert");
		
		// Load Main character
		GameEntity player = ResourceManager.MANAGER.loadEntity("res/entities/Player.xml");
		ResourceManager.MANAGER.setFocusEntity(player);
		
		try {
			TextureManager.MANAGER.loadCycle(ResourceManager.MANAGER.playerFocusEntity, ResourceManager.MANAGER.playerFocusEntity.getAnimationFile());
			player.setTexture(TextureManager.MANAGER.setFrame(ResourceManager.MANAGER.playerFocusEntity.id, "idle_south"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Load first map
		ResourceManager.MANAGER.loadMap("res/maps/scene1/Library.xml");
		Camera.CAMERA.findPlayer();
		
		ScriptManager.MANAGER.warmUp();
	}
	
	private InputListener keyboardListener = new InputListener() {
		public void onEvent() {
						
			GameEntity playerFocusEntity = ResourceManager.MANAGER.playerFocusEntity;
			
			float mult = 1;
			float playerDeltaX = 0, camDeltaX = 0;
			float playerDeltaY = 0, camDeltaY = 0;
			boolean lockNS = ResourceManager.MANAGER.playerFocusEntity.cameraLockNS(GameMap.MAP, GraphicsManager.MANAGER);
			boolean lockEW = ResourceManager.MANAGER.playerFocusEntity.cameraLockEW(GameMap.MAP, GraphicsManager.MANAGER);
			
			while (Keyboard.next()) {
				if (Keyboard.getEventKeyState()) {
					if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
						WindowManager.MANAGER.pushMenuStack("pause");
						paused = true;
					}
					else if (Keyboard.getEventKey() == Keyboard.KEY_SLASH)
						WindowManager.MANAGER.pushMenuStack("console");
					else if (Keyboard.getEventKey() == Keyboard.KEY_SPACE)
						ScriptManager.MANAGER.onInteract(PhysicsUtilities.entityProximity(playerFocusEntity));
                    else if (Keyboard.getEventKey() == Keyboard.KEY_0)
                        ResourceManager.MANAGER.changeLevel("scene1/Library.xml");
                        //GraphicsManager.MANAGER.jitterForDays(50); // JITTER FO DAYS MOFOZ

				}
			}
			
			if (PhysicsUtilities.tileTest(playerFocusEntity, -2, GameMap.MAP)) {
				if (ResourceManager.MANAGER.playerFocusEntity.lastMovement.x != 0) {
					playerDeltaX = 2 * playerFocusEntity.lastMovement.x;
					if (lockEW) camDeltaX = playerDeltaX;
					mult = playerFocusEntity.translateX(playerDeltaX);
					Camera.CAMERA.translateX(camDeltaX * mult);
					return;
				}
				else if (playerFocusEntity.lastMovement.y != 0) {
				
					playerDeltaY = 2 * playerFocusEntity.lastMovement.y;
					if (lockNS) camDeltaY = playerDeltaY;
					mult = playerFocusEntity.translateY(playerDeltaY);
					Camera.CAMERA.translateY(camDeltaY * mult);
					return;
				}
			}
			
			if (Keyboard.isKeyDown(Keyboard.KEY_W) && Keyboard.isKeyDown(Keyboard.KEY_A)) {
				playerDeltaY = -1 * diagonal;
				playerDeltaX = -1 * diagonal;
				if (lockNS) camDeltaY = playerDeltaY;
				if (lockEW) camDeltaX = playerDeltaX;
			}
			else if (Keyboard.isKeyDown(Keyboard.KEY_W) && Keyboard.isKeyDown(Keyboard.KEY_D)) {
				playerDeltaY = -1 * diagonal;
				playerDeltaX =  1 * diagonal;
				if (lockNS) camDeltaY = playerDeltaY;
				if (lockEW) camDeltaX = playerDeltaX;
			}
			else if (Keyboard.isKeyDown(Keyboard.KEY_S) && Keyboard.isKeyDown(Keyboard.KEY_A)) {
				playerDeltaY =  1 * diagonal;
				playerDeltaX = -1 * diagonal;
				if (lockNS) camDeltaY = playerDeltaY;
				if (lockEW) camDeltaX = playerDeltaX;
			}
			else if (Keyboard.isKeyDown(Keyboard.KEY_S) && Keyboard.isKeyDown(Keyboard.KEY_D)) {
				playerDeltaY = 1 * diagonal;
				playerDeltaX = 1 * diagonal;
				if (lockNS) camDeltaY = playerDeltaY;
				if (lockEW) camDeltaX = playerDeltaX;
			}
			else if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
				playerDeltaY = -1;
				if (lockNS) camDeltaY = playerDeltaY;
				playerFocusEntity.setTexture(TextureManager.MANAGER.nextFrame(playerFocusEntity.id, "walk_north"));
			}
			else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
				playerDeltaX = -1;
				if (lockEW) camDeltaX = playerDeltaX;
				playerFocusEntity.setTexture(TextureManager.MANAGER.nextFrame(playerFocusEntity.id, "walk_west"));
			}
			else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
				playerDeltaY = 1;
				if (lockNS) camDeltaY = playerDeltaY;
				playerFocusEntity.setTexture(TextureManager.MANAGER.nextFrame(playerFocusEntity.id, "walk_south"));
			}
			else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
				playerDeltaX = 1;
				if (lockEW) camDeltaX = playerDeltaX;
				playerFocusEntity.setTexture(TextureManager.MANAGER.nextFrame(playerFocusEntity.id, "walk_east"));
			}
			else {
				playerFocusEntity.idle();
			}
			
			if (playerDeltaX != 0) mult = playerFocusEntity.translateX(playerDeltaX);
			if (playerDeltaY != 0) mult = playerFocusEntity.translateY(playerDeltaY);
			if (camDeltaX != 0) Camera.CAMERA.translateX(camDeltaX * mult);
			if (camDeltaY != 0) Camera.CAMERA.translateY(camDeltaY * mult);

		}
	};
	
	private InputListener mouseListener = new InputListener() {
		public void onEvent(int key, int x, int y) {
			if (key == 0) {
				System.out.println("Left click");
			}
			else if (key == 1) {
				System.out.println("Right click");
			}
		}
	};
}