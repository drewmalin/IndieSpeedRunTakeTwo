package com.orbit.managers;
import java.util.ArrayList;
import java.util.HashMap;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import com.orbit.core.TextureCycle;
import com.orbit.entities.GameEntity;
import com.orbit.xml.Node;
import com.orbit.xml.XMLParser;

public enum TextureManager {

	MANAGER;
	
	//private Game gameHandle;
	private HashMap<Integer, TextureCycle> textureStore;

	/* Constructor:
	 * Creates a 'texture store' to persist each unique texture in an animation.
	 * Establishes a handle back to the main game object.
	 */
	TextureManager() {
		textureStore = new HashMap<Integer, TextureCycle>();
	}

	/* loadCycle:
	 * Parses an animation file. Animation files consist of a series of frames in an
	 * animation, and an animation speed (in nanoseconds). The following function persists
	 * that speed, and loads a texture for each unique frame in the cylce. To access animations,
	 * provide an id (unique to each entity) and the name of the animation in question (the 
	 * element name used in the animation xml file).
	 */
	public void loadCycle(GameEntity ge, String filename) throws Exception {

		XMLParser textureFile = new XMLParser(filename);
		TextureCycle t = new TextureCycle();

		for (Node animation : textureFile.root.children) {

			ArrayList<Texture> textureList = new ArrayList<Texture>();

			for (Node text : animation.children) {
				if (text.name.equals("speed")) {
					t.speedNano = text.readInt();
				}
				else if (text.name.equals("frame")) {
					String file = text.readString();
					String type = file.substring(file.indexOf(".") + 1).toUpperCase();
					Texture texture = TextureLoader.getTexture(type, ResourceLoader.getResourceAsStream(file));
					textureList.add(texture);
				}
			}
			
			t.cycle.put(animation.name, textureList);
		}
		
		/* Generate an id for each entity. By default, each entity is given a -1 id.
		 * When an entity is loaded with animations, an id value is generated for it.
		 */
		//if (ge.id == -1) ge.id = textureStore.size();
		textureStore.put(ge.id, t);
	}

	/* nextFrame:
	 * Get the next frame in the requested animation for a given entity. Based on the
	 * animation speed provided in the animation xml file, the function determines if
	 * a new frame be drawn for the animation cycle, or if no new frame is needed at this
	 * time.
	 */
	public Texture nextFrame(int id, String animation) {
		TextureCycle tc = textureStore.get(id);
		return tc.getNextFrame(animation);
	}
	
	public Texture setFrame(int id, String frame) {
		return textureStore.get(id).cycle.get(frame).get(0);
	}
	
	public String lastAnimation(int id) {
		return textureStore.get(id).lastAnimation;
	}
}
