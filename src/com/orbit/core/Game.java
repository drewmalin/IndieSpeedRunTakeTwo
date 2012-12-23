package com.orbit.core;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

import com.orbit.managers.GraphicsManager;
import com.orbit.managers.InputManager;
import com.orbit.managers.NetworkManager;
import com.orbit.managers.ScriptManager;
import com.orbit.managers.WindowManager;

public class Game {
	
	public boolean paused = false;
	public final float diagonal = .70710678f;

	public Game() {}
	
	public void initGame() {

		InputManager.MANAGER.setKeyboardListener(keyboardListener);
		InputManager.MANAGER.setMouseListener(mouseListener);
		
		GraphicsManager.MANAGER.setResolution(800, 600);
		GraphicsManager.MANAGER.setFrust(60f);
		GraphicsManager.MANAGER.setZNear(0.1f);
		GraphicsManager.MANAGER.setZFar(100f);
		GraphicsManager.MANAGER.create("3D");
		
		Camera.CAMERA.setPosition(0f, 0f, 0f);
		Camera.CAMERA.setTarget(new Vector3f(0f, 0f, 0f));
		
	}

	
	private InputListener keyboardListener = new InputListener() {
		public void onEvent(int key) {
			if (key == Keyboard.KEY_W) {
				System.out.println("Pressed 'W'!");
			}
			else if (key == Keyboard.KEY_A) {
				System.out.println("Pressed 'A'!");
			}
			else if (key == Keyboard.KEY_S) {
				System.out.println("Pressed 'S'!");
			}
			else if (key == Keyboard.KEY_D) {
				System.out.println("Pressed 'D'!");
			}
			else if (key == Keyboard.KEY_ESCAPE) {
				System.exit(0);
			}
		}
	};
	
	private InputListener mouseListener = new InputListener() {
		public void onEvent(int key, int x, int y) {
			if (key == 0) {
				System.out.println("Left click!");
			}
			else if (key == 1) {
				System.out.println("Right click!");
			}
		}
	};
	
	public void start() {
		
		initGame();
		
		while (!Display.isCloseRequested()) {
			
			InputManager.MANAGER.pollKeyboard();
			InputManager.MANAGER.pollMouse();
			
			if (WindowManager.MANAGER.windowStack.size() == 0) 
				ScriptManager.MANAGER.updateEntities();
			
			GraphicsManager.MANAGER.drawGame();			
			WindowManager.MANAGER.draw();
			
			Display.update();
			Display.sync(60);
		}
		
		Display.destroy();
	}
}
