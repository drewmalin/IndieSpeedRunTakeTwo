package com.orbit.ui;


import org.lwjgl.input.Mouse;

import com.orbit.managers.GraphicsManager;
import com.orbit.managers.WindowManager;


public class Button extends Canvas {

	public Button(WindowManager wm, Canvas parent) {
		super(wm, parent);
	}

	public String name;
	public boolean selected;
	
	
	public void setHoverMessage(String msg) {
		String[] tok = msg.split("\n");
		hoverMessageBox.message = msg;
		hoverMessageBox.skipProcessing = true;
		hoverMessageBox.lineWidth = hoverMessageBox.maxWidth;
		hoverMessageBox.lineCount = tok.length;
	}
	
	public void openHoverMessageBox() {
		if (!hoverMessageBox.message.isEmpty()) {
			hoverMessageBox.x = Mouse.getX() + 20;
			hoverMessageBox.y = GraphicsManager.MANAGER.getHeight() - Mouse.getY() + 20;
			hoverMessageBox.show = true;
			hoverMessageBox.print();
		}
	}
	
	public void closeHoverMessageBox() {
		hoverMessageBox.show = false;
	}
	
	public void draw() {
		super.draw();
		drawMessages();
	}
}
