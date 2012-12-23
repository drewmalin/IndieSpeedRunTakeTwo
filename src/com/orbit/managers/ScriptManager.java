package com.orbit.managers;
import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;


import com.orbit.core.EntityScript;
import com.orbit.entities.GameEntity;

public enum ScriptManager {
	
	MANAGER;
	
	private PythonInterpreter interpreter = null;
	private PyObject pyObj, entObj;
	public EntityScript entityScript;
	private String globalImports;
	
	ScriptManager() {
		
		globalImports = "";
		
		PythonInterpreter.initialize(System.getProperties(),
								     System.getProperties(), new String[0]);
		
		interpreter = new PythonInterpreter();
		
		interpreter.exec("import sys\n"+
					     "sys.path.append(\"res/scripts\")\n");
	}
	
	/**
	 * 
	 * @param script
	 * Method performs the 'setup' to treat the Python object of choice as a Java object.
	 * The following code imports the appropriate Python script, and essentially performs
	 * a "cast" of the Python class itself, allowing 'entityScript' (using the EntityScript
	 * interface) to be treated as a Java object later on.
	 */
	public void run(String script) {
		interpreter.exec(globalImports + "\n" + "from " + script + " import Entity");
		pyObj = interpreter.get("Entity");
		entObj = pyObj.__call__();
		
		entityScript = (EntityScript) entObj.__tojava__(EntityScript.class);
	}
	
	
	/**
	 * 
	 * @param ge
	 * When interaction is detected, the 'onInteract' function of the entityScript object (created 
	 * above, see run(String script)) will be run, returning a python dictionary (PyDictionary) of 
	 * persisted values. This dictionary contains python objects which can be cast as instances of
	 * the Object (Java) class. Finally, these objects are cast to Strings, and their value is used
	 * later.
	 */
	public void onInteract(GameEntity ge) {
		
		Object queryScript;
		PyList pyList = new PyList();
		PyDictionary pyDict = new PyDictionary();
		
		if (ge != null && ge.scriptFile != "") {
			
			ScriptManager.MANAGER.run(ge.scriptFile);
			
			/* Call the various method that exist in the Java interface/Python class. If a method
			 * called here has not been implemented in the Python object, the return object will be
			 * null or the setup method will have no effect.
			 */
			MANAGER.entityScript.setPosition(ge.position.x, ge.position.y, ge.position.z);
			pyDict = MANAGER.entityScript.onInteract();
			
			if (pyDict != null) {
								
				queryScript = pyDict.get("newMessage");
				if (queryScript != null) {
					System.out.println(queryScript.toString());
					WindowManager.MANAGER.pushMenuStack("dialogue");
                    WindowManager.MANAGER.windows.get("dialogue").messageBoxes.get(0).replaceMessage(queryScript.toString());
				}
				
				queryScript = pyDict.get("appendMessage");
				if (queryScript != null) {
					WindowManager.MANAGER.gui.get("storyBox").messageBoxes.get(0).addMessage(queryScript.toString());
				}
				
				queryScript = pyDict.get("destroy");
				if (queryScript != null) {
					if (queryScript.toString().toLowerCase().equals("true"))
						ResourceManager.MANAGER.gameEntities.remove(ge);
				}
				
				// Supports multi
				pyList = (PyList) pyDict.get("destroyOther");
				if (pyList != null) {
					for (Object query : pyList) {
						for (GameEntity tempGE : ResourceManager.MANAGER.gameEntities)
							if (tempGE.getFile().equals(query.toString())) {
								ResourceManager.MANAGER.gameEntities.remove(tempGE);
								break;
							}
					}
				}
				
				//Supports multi
				pyList = (PyList) pyDict.get("createOther");
				if (pyList != null) {
					for (Object query : pyList) {
						ResourceManager.MANAGER.loadEntity(query.toString());
					}
				}
				
				queryScript = pyDict.get("level");
				if (queryScript != null) {
					ResourceManager.MANAGER.changeLevel(queryScript.toString());
				}
				
				queryScript = pyDict.get("mass");
				if (queryScript != null) {
					ge.setMass(Float.parseFloat(queryScript.toString()));
				}

                queryScript = pyDict.get("follow");
                if (queryScript != null) {
                    ge.followingPlayer = Integer.parseInt(queryScript.toString());
                }
			}
		}		
	}
	
	public void updateEntities() {
		PyList pyList = new PyList();
		Object queryScript;
		PyDictionary pyDict = new PyDictionary();
		
		for (GameEntity ge : ResourceManager.MANAGER.gameEntities) {
			if (ge.scriptFile != null && ge.scriptFile != "") {
				
				if (System.nanoTime() - ge.haltTime < ge.haltDuration) continue;
				
				ge.haltDuration = 0;
				ScriptManager.MANAGER.run(ge.scriptFile);
				MANAGER.entityScript.setPosition(ge.position.x, ge.position.y, ge.position.z);
				MANAGER.entityScript.setDirection(ge.direction.x, ge.direction.y, ge.direction.z);

                MANAGER.entityScript.setFollow(ge.followingPlayer);

                if (ge.followingPlayer == 1) {
                    MANAGER.entityScript.setTarget(
                            ResourceManager.MANAGER.playerFocusEntity.position.x,
                            ResourceManager.MANAGER.playerFocusEntity.position.y,
                            ResourceManager.MANAGER.playerFocusEntity.position.z);
                }

				pyDict = MANAGER.entityScript.update();
				
				
				if (pyDict != null) {
					queryScript = pyDict.get("halt");
					if (queryScript != null) {
						ge.haltTime = System.nanoTime();
						ge.haltDuration = Long.parseLong(queryScript.toString());
					}
					
					queryScript = pyDict.get("deltaY");
					if (queryScript != null) {
						float delta = Float.parseFloat(queryScript.toString());
						if (delta > 0) {
							ge.setTexture(TextureManager.MANAGER.nextFrame(ge.id, "walk_south"));
							ge.direction.y = 1;
						}
						else if (delta < 0) {
							ge.setTexture(TextureManager.MANAGER.nextFrame(ge.id, "walk_north"));
							ge.direction.y = -1;	
						}
						else {
							ge.direction.y = 0;
						}
						ge.translateY(delta);
					}
					
					queryScript = pyDict.get("deltaX");
					if (queryScript != null) {
						float delta = Float.parseFloat(queryScript.toString());
						if (delta > 0) {
							ge.setTexture(TextureManager.MANAGER.nextFrame(ge.id, "walk_east"));
							ge.direction.x = 1;
						}
						else if (delta < 0) {
							ge.setTexture(TextureManager.MANAGER.nextFrame(ge.id, "walk_west"));
							ge.direction.x = -1;
						}
						else {
							ge.direction.x = 0;
						}
						ge.translateX(delta);
					}

                    queryScript = pyDict.get("follow");
                    if (queryScript != null) {
                        ge.followingPlayer = Integer.parseInt(queryScript.toString());
                    }
				}
			}
		}
	}
	
	//TODO Create further functionality for scripts
	public void onCreate(GameEntity ge) {}
	public void onTouch(GameEntity ge) {}
	public void onDestroy(GameEntity ge) {}

	/**
	 * The interpreter 'wakes up' only when it's needed. Therefore the first call to the
	 * Python interpreter takes roughly 1000ms. The warmUp method is meant to use up this
	 * 1000ms during loadtime rather than playtime.
	 */
	public void warmUp() {
		interpreter.exec("");		
	}
}
