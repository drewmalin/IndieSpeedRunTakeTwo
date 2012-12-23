package com.orbit.ui;

import java.util.HashMap;


import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.orbit.managers.GraphicsManager;
import com.orbit.managers.WindowManager;


public class Window extends Canvas {
	public HashMap<String, Button> buttons;
	public Button clickedButton;
	public String name;

	public Window(WindowManager wm, Canvas parent) {
		super(wm, parent);
		buttons = new HashMap<String, Button>();
	}

	public void draw() {
		super.draw();
		drawMessages();
		drawButtons();
	}

	public Window setWidth(int w) {
		width = w;
		return this;
	}
	
	public Window setHeight(int h) {
		height = h;
		return this;
	}
	
	public void drawButtons() {
		for (String b : buttons.keySet()) {
			buttons.get(b).draw();
		}
	}

	public void poll() {
		pollMouse();
		pollKeyboard();
	}
	
	public void pollMouse() {
		
		int mouseWheelDelta = Mouse.getDWheel();
		
		for (String b : buttons.keySet()) {
			buttons.get(b).checkHover(Mouse.getX(), GraphicsManager.MANAGER.getHeight() - Mouse.getY());
		}
		for (MessageBox mb : messageBoxes) {
			mb.checkHover(Mouse.getX(), GraphicsManager.MANAGER.getHeight() - Mouse.getY());
		}
		
		while (Mouse.next()) {
			if (Mouse.getEventButtonState()) {
				switch (Mouse.getEventButton()) {
					case 0:	//Left click
						for (Button b : buttons.values()) {
							if (b.hovering) {
								b.onClick();
							}						
						}
						for (MessageBox mb : messageBoxes) {
							if (mb.hovering)
								mb.onClick();
						}
						break;
					case 1: //Right click
						break;
				}
			}
			if (mouseWheelDelta != 0)
				for (MessageBox mb : messageBoxes) {
					if (mb.hovering) {
						mb.onScroll(mouseWheelDelta);
					}
				}
		}
	}
	
	public void pollKeyboard() {
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				
				switch (Keyboard.getEventKey()) {
					//Quit the game
					case Keyboard.KEY_ESCAPE:
						System.exit(0);
						break;
				}
			}
		}
	}
}