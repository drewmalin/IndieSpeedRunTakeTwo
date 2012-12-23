package com.orbit.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.orbit.core.ClickListener;
import com.orbit.ui.Button;
import com.orbit.ui.Canvas;
import com.orbit.ui.MessageBox;
import com.orbit.ui.Window;
import com.orbit.xml.Node;
import com.orbit.xml.XMLParser;

public enum WindowManager {
	
	//public final Game gameHandle;
	MANAGER;
	
	public final HashMap<String, Window> gui;
	public final HashMap<String, Window> windows;
	public ArrayList<Window> windowStack;
	private String consoleMessage;
	
	WindowManager() {
		gui 		= new HashMap<String, Window>();
		windows 	= new HashMap<String, Window>();
		windowStack	= new ArrayList<Window>();
	}

	public Window createWindow(String name, int x, int y) {
		Window w = new Window(this, null);
		w.name = name;
		w.x = x;
		w.y = y;
		w.color[0] = w.color[1] = w.color[2] = w.color[3] = 1;
		
		gui.put(name, w);
		return w;
	}
	
	public void addToGui(String name) {
		gui.put(name, windows.get(name));
	}
	
	public void draw() {
		for (Window w : gui.values()) {
			w.draw();
		}
		for (Window w : windowStack) {
			w.draw();
		}
	}
	
	public void pushMenuStack(String name) {
		windowStack.add(windows.get(name));
	}
	
	public void popMenuStack() {
		if (windowStack.size() > 0)
			windowStack.remove(windowStack.size() - 1);
	}

	public void loadConsole() {
		Window window = new Window(this, null);
		MessageBox mb = new MessageBox(this, window);

		window.name = "console";
		window.x = window.y = 0;
		window.width = GraphicsManager.MANAGER.getWidth();
		window.height = 100;
		window.setColor(new float[] {0, 0, 0, .35f});
		
		mb.width = window.width;
		mb.height = mb.height;
		mb.fontName = "Times New Roman";
		mb.fontSize = 14;
		mb.setFontColor("white");
		mb.x = mb.y = 0;
		mb.setMessage("> ");
		mb.load();

		window.messageBoxes.add(mb);
		windows.put(window.name, window);
		consoleMessage = "";
		
		window.setClickListener(new ClickListener() {
			public void onKeyEvent(int key, char keyChar) {
				switch (key) {
				case Keyboard.KEY_SPACE:
					windows.get("console").messageBoxes.get(0).message += " ";
					consoleMessage += " ";
					break;
				case Keyboard.KEY_RETURN:
					windows.get("console").messageBoxes.get(0).addMessage("");
					processConsoleMessage();
					break;
				case Keyboard.KEY_BACK:
					if (consoleMessage.length() > 0) {
						windows.get("console").messageBoxes.get(0).message = 
							windows.get("console").messageBoxes.get(0).message.substring(0, windows.get("console").messageBoxes.get(0).message.length()-1);
						consoleMessage = consoleMessage.substring(0, consoleMessage.length() - 1);
					}
					break;
				default:
					if (isValidKey(key)) {
						windows.get("console").messageBoxes.get(0).message += keyChar;
						consoleMessage += keyChar;
					}
				}
			}
		});
	}
	
	public void processConsoleMessage() {
		windows.get("console").messageBoxes.get(0).message += "> ";
		
		String[] tokens = consoleMessage.split(" ");
		
		if (tokens[0].equals("load")) {
			if ((new File("res/maps/" + tokens[1])).exists())
				ResourceManager.MANAGER.changeLevel(tokens[1]);
			else {
				windows.get("console").messageBoxes.get(0).message += "No file found at location " + "res/maps/" + tokens[1];
				windows.get("console").messageBoxes.get(0).addMessage("");
				windows.get("console").messageBoxes.get(0).message += "> ";
			}
		}
		else if (tokens[0].equals("quit"))
			System.exit(0);
		
		consoleMessage = "";
	}
	
	public boolean isValidKey(int key) {
		if (key == 15 || 	//TAB
			key == 29 || 	//CTRL
			key == 42 || 	//LSHIFT
			key == 54 || 	//RSHIFT
			key == 56 || 	//LALT
			key == 58 || 	//CAPS
			key == 184 ||	//RALT
		    key == 200 ||	//UP
		    key == 203 ||	//LEFT
		    key == 205 ||	//RIGHT
		    key == 208 ||	//DOWN
		    key == 219 ||	//LCOMMAND
		    key == 220)		//RCOMMAND
			return false;
		else
			return true;
	}
	/** Load a menu xml file and store the resulting window, along with all of its message
	 * boxes, buttons, and attributes.
	 * 
	 * @param filename
	 */
	public void loadMenu(String filename) {
		XMLParser itemLib = new XMLParser(filename);

		for (Node windowEl : itemLib.root.children) {
			Window window = new Window(this, null);
			window.name = filename.substring(filename.lastIndexOf("/")+1, filename.indexOf("."));
				
			for (Node infoEl : windowEl.children) {
				if (infoEl.name.equals("x"))
					window.x = infoEl.readInt();
				else if (infoEl.name.equals("y"))
					window.y = infoEl.readInt();
				else if (infoEl.name.equals("width")) {
					if (infoEl.readString().equals("FULL"))
						window.width = GraphicsManager.MANAGER.getWidth();
					else
						window.width = infoEl.readInt();
				}
				else if (infoEl.name.equals("height")) {
					if (infoEl.readString().equals("FULL"))
						window.height = GraphicsManager.MANAGER.getHeight();
					else
						window.height = infoEl.readInt();
				}
				else if (infoEl.name.equals("backgroundImage"))
					window.setBackgroundImage(infoEl.readString());
				else if (infoEl.name.equals("backgroundColor"))
					window.setColor(infoEl.readFloatArray());
				else if (infoEl.name.equals("messageBox"))
					loadMessageBox(infoEl, window);
				else if (infoEl.name.equals("button")) {
					Button button = new Button(this, window);
				
					for (Node buttonEl : infoEl.children) {
						if (buttonEl.name.equals("name"))
							button.name = buttonEl.readString();
						else if (buttonEl.name.equals("x"))
							button.x = window.x + buttonEl.readInt();
						else if (buttonEl.name.equals("y"))
							button.y = window.y + buttonEl.readInt();
						else if (buttonEl.name.equals("width"))
							button.width = buttonEl.readInt();
						else if (buttonEl.name.equals("height"))
							button.height = buttonEl.readInt();
						else if (buttonEl.name.equals("messageBox"))
							loadMessageBox(buttonEl, button);
						else if (buttonEl.name.equals("onHover"))
							loadHoverBehavior(buttonEl, button);
						else if (buttonEl.name.equals("backgroundImage"))
							button.setBackgroundImage(buttonEl.readString());
						else if (buttonEl.name.equals("backgroundColor"))
							button.setColor(buttonEl.readFloatArray());
					}
					window.buttons.put(button.name, button);
				}
			}
			windows.put(window.name, window);
		}
	}
	
	private void loadMessageBox(Node xmlContext, Canvas canvas) {
		MessageBox mb = new MessageBox(this, canvas);
		for (Node mbEl : xmlContext.children) {
			if (mbEl.name.equals("x"))
				mb.x = canvas.x + mbEl.readInt();
			else if (mbEl.name.equals("y"))
				mb.y = canvas.y + mbEl.readInt();
			else if (mbEl.name.equals("width"))
				mb.width = mbEl.readInt();
			else if (mbEl.name.equals("height"))
				mb.height = mbEl.readInt();
			else if (mbEl.name.equals("fontName"))
				mb.fontName = mbEl.readString();
			else if (mbEl.name.equals("fontSize"))
				mb.fontSize = mbEl.readInt();
			else if (mbEl.name.equals("fontColor"))
				mb.setFontColor(mbEl.readString());
			else if (mbEl.name.equals("message"))
				mb.message = mbEl.readString();
			else if (mbEl.name.equals("backgroundImage"))
				mb.setBackgroundImage(mbEl.readString());
			else if (mbEl.name.equals("backgroundColor"))
				mb.setColor(mbEl.readFloatArray());
		}
		mb.load();
		canvas.messageBoxes.add(mb);
	}
	
	private void loadHoverBehavior(Node xmlContext, Button button) {
		
		for (Node hoverEl : xmlContext.children) {
			if (hoverEl.name.equals("backgroundColor"))
				button.setHovorColor(hoverEl.readFloatArray());
			else if (hoverEl.name.equals("messageBox")) {
				MessageBox mb = new MessageBox(this, button);
				for (Node mbEl : hoverEl.children) {
					if (mbEl.name.equals("width"))
						mb.width = mbEl.readInt();
					else if (mbEl.name.equals("height"))
						mb.height = mbEl.readInt();
					else if (mbEl.name.equals("fontName"))
						mb.fontName = mbEl.readString();
					else if (mbEl.name.equals("fontSize"))
						mb.fontSize = mbEl.readInt();
					else if (mbEl.name.equals("fontColor"))
						mb.setFontColor(mbEl.readString());
					else if (mbEl.name.equals("message"))
						mb.message = mbEl.readString();
					else if (mbEl.name.equals("backgroundImage"))
						mb.setBackgroundImage(mbEl.readString());
					else if (mbEl.name.equals("backgroundColor"))
						mb.setColor(mbEl.readFloatArray());
				}
				mb.load();
				button.hoverMessageBox = mb;
			}
		}
	}

	public void pollKeyboard() {
		if (windowStack.size() > 0)

			while (Keyboard.next()) {
				if (Keyboard.getEventKeyState()) {
					if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE)
						popMenuStack();
					else
						windowStack.get(windowStack.size() - 1).onKeyboardEvent(Keyboard.getEventKey(), Keyboard.getEventCharacter());
				}
			}
	}

	public void pollMouse() {
		
		if (windowStack.size() > 0)
			windowStack.get(windowStack.size() - 1).pollMouse();
		else
			for (Window w : gui.values()) {
				if (hovering(w)) {
					w.pollMouse();
				}
			}
	}
	
	public boolean hovering(Window w) {
		int x = Mouse.getX();
		int y = Mouse.getY();
		
		if ((x >= w.x && x <= w.x + w.width)
			&& (y >= w.y && y <= w.y + w.height))
			return true;
		
		return false;
	}
	
	public void createClickListeners() {
        //--------------------------- Dialogue menu  ---------------------------//
        windows.get("dialogue").buttons.get("ok").setClickListener(new ClickListener() {
            public void onClick() {
                popMenuStack();
            }
        });

        if (gui.get("dialogue") != null)
        gui.get("dialogue").messageBoxes.get(0).setClickListener(new ClickListener() {
            public void onScroll(int delta) {
                if (delta > 0) {
                    gui.get("dialogue").messageBoxes.get(0).moveDown();
                }
                else {
                    gui.get("dialogue").messageBoxes.get(0).moveUp();
                }
            }
        });
		//---------------------------   Pause menu   ---------------------------//
		windows.get("pause").buttons.get("resume").setClickListener(new ClickListener() {
            public void onClick() {
                popMenuStack();
            }
        });
		windows.get("pause").buttons.get("quit").setClickListener(new ClickListener() {
			public void onClick() {
				System.exit(0);
			}
		});
		
		if (gui.get("storyBox") != null)
		gui.get("storyBox").messageBoxes.get(0).setClickListener(new ClickListener() {
			public void onScroll(int delta) {
				if (delta > 0) {
					gui.get("storyBox").messageBoxes.get(0).moveDown();
				}
				else {
					gui.get("storyBox").messageBoxes.get(0).moveUp();
				}
			}
		});
	}
}
