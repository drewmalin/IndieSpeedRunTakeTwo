package com.orbit.managers;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.orbit.core.InputListener;
import com.orbit.core.KeyTrigger;

public enum InputManager {
	
	MANAGER;
	
	private InputListener mouseListener;
	private InputListener keyboardListener;
	private ArrayList<KeyTrigger> keyTriggers;
	
	InputManager() {
		mouseListener 		= new InputListener();
		keyboardListener 	= new InputListener();
		keyTriggers			= new ArrayList<KeyTrigger>();
	}

	public void setMouseListener(InputListener il) {
		mouseListener = il;
	}
	
	public void setKeyboardListener(InputListener il) {
		keyboardListener = il;
	}
	
	public void pollKeyboard() {
		if (WindowManager.MANAGER.windowStack.size() > 0) {
			WindowManager.MANAGER.pollKeyboard();
		}
		else {
			keyboardListener.onEvent();
		}
	}
	
	public void pollMouse() {
		
		// Poll GUI, steal context if menu stack size > 0
		WindowManager.MANAGER.pollMouse();
		
		// Regular game mouse polling
		while (Mouse.next()) {
			if (Mouse.getEventButtonState()) {
				mouseListener.onEvent(Mouse.getEventButton(), Mouse.getX(), Mouse.getY());
			}
			// Make sure the wheel delta doesn't go crazy
			Mouse.getDWheel();
		}
		
		
	}

	public void addKeyTrigger(int key, KeyTrigger keyTrigger) {
		keyTrigger.key = key;
		keyTriggers.add(keyTrigger);
	}
}
