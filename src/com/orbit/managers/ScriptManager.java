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
	public boolean onInteract(GameEntity ge) {
		
		Object queryScript;
		PyList pyList = new PyList();
		PyDictionary pyDict = new PyDictionary();
		boolean changeLevel = false;

		if (ge != null && ge.scriptFile != "") {
			
			ScriptManager.MANAGER.run(ge.scriptFile);
			
			/* Call the various method that exist in the Java interface/Python class. If a method
			 * called here has not been implemented in the Python object, the return object will be
			 * null or the setup method will have no effect.
			 */
			MANAGER.entityScript.setPosition(ge.position.x, ge.position.y, ge.position.z);
            MANAGER.entityScript.setStage(ge.questStage, 0);
            pyDict = MANAGER.entityScript.onInteract();
			
			if (pyDict != null) {
                runReleaseTheKraken(pyDict);
                runPlaySounds(pyDict);
                runNewMessage(pyDict);
                runAppendMessage(pyDict);
                runDestroy(pyDict, ge);
                runDestroyOther(pyDict);
                runCreateOther(pyDict);
                runUpdateStageOther(pyDict);
                changeLevel = runUpdates(pyDict, ge);
			}
		}
        return changeLevel;
	}

    /*
     * Every tick, the updateEntities method is called to act as the primary way of managing and monitoring
     * entity behavior.
     */
    public boolean updateEntities() {
        PyList pyList = new PyList();
        Object queryScript;
        PyDictionary pyDict = new PyDictionary();
        boolean changeLevel = false;

        for (GameEntity ge : ResourceManager.MANAGER.gameEntities) {
            if (ge.scriptFile != null && ge.scriptFile != "") {

                if (System.nanoTime() - ge.haltTime < ge.haltDuration) continue;

                ge.haltDuration = 0;
                ScriptManager.MANAGER.run(ge.scriptFile);
                MANAGER.entityScript.setPosition(ge.position.x, ge.position.y, ge.position.z);
                MANAGER.entityScript.setDirection(ge.direction.x, ge.direction.y, ge.direction.z);

                MANAGER.entityScript.setStage(ge.questStage, 0);

                if (ge.followPlayer)
                    MANAGER.entityScript.setTarget(
                            ResourceManager.MANAGER.playerFocusEntity.position.x,
                            ResourceManager.MANAGER.playerFocusEntity.position.y,
                            ResourceManager.MANAGER.playerFocusEntity.position.z);

                pyDict = MANAGER.entityScript.update();


                if (pyDict != null) {
                    runHalt(pyDict, ge);
                    runDeltaXY(pyDict, ge);
                    changeLevel = runUpdates(pyDict, ge);
                }
            }
        }
        return changeLevel;
    }

    /*
     * onTouch is called upon the physical interaction of two entities (primarily to be used at player/entity collision
     * time.
     */
    public boolean onTouch(GameEntity ge) {
        PyList pyList = new PyList();
        Object queryScript;
        PyDictionary pyDict = new PyDictionary();
        boolean changeLevel = false;

        if (ge != null && ge.scriptFile != "") {

            ScriptManager.MANAGER.run(ge.scriptFile);
            pyDict = MANAGER.entityScript.onTouch();

            if (pyDict != null) {
                runReleaseTheKraken(pyDict);
                runPlaySounds(pyDict);
                runDestroy(pyDict, ge);
                runNewMessage(pyDict);
                runUpdateStageOther(pyDict);
                runCreateOther(pyDict);
                changeLevel = runUpdates(pyDict, ge);
            }

        }
        return changeLevel;
    }

    //TODO Create further functionality for scripts
    public void onCreate(GameEntity ge) {}
    public void onDestroy(GameEntity ge) {}

    public void runHalt(PyDictionary pyDict, GameEntity ge) {
        Object queryScript = pyDict.get("halt");
        if (queryScript != null) {
            ge.haltTime = System.nanoTime();
            ge.haltDuration = Long.parseLong(queryScript.toString());
        }
    }

    public void runDeltaXY(PyDictionary pyDict, GameEntity ge) {
        Object queryScript = pyDict.get("deltaY");
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
    }

    public void runNewMessage(PyDictionary pyDict) {
        Object queryScript = pyDict.get("newMessage");
        if (queryScript != null) {
            System.out.println(queryScript.toString());
            WindowManager.MANAGER.pushMenuStack("dialogue");
            WindowManager.MANAGER.windows.get("dialogue").messageBoxes.get(0).replaceMessage(queryScript.toString());
        }
    }

    public void runAppendMessage(PyDictionary pyDict) {
        Object queryScript = pyDict.get("appendMessage");
        if (queryScript != null) {
            WindowManager.MANAGER.gui.get("storyBox").messageBoxes.get(0).addMessage(queryScript.toString());
        }
    }

    public void runDestroy(PyDictionary pyDict, GameEntity ge) {
        Object queryScript = pyDict.get("destroy");
        if (queryScript != null) {
            if (queryScript.toString().toLowerCase().equals("true"))
                ResourceManager.MANAGER.gameEntities.remove(ge);
        }
    }

    public void runDestroyOther(PyDictionary pyDict) {
        // Supports multi
        PyList pyList = (PyList) pyDict.get("destroyOther");
        if (pyList != null) {
            for (Object query : pyList) {
                for (GameEntity tempGE : ResourceManager.MANAGER.gameEntities)
                    if (tempGE.getFile().equals(query.toString())) {
                        ResourceManager.MANAGER.gameEntities.remove(tempGE);
                        break;
                    }
            }
        }
    }

    public void runCreateOther(PyDictionary pyDict) {
        //Supports multi
        PyList pyList = (PyList) pyDict.get("createOther");
        if (pyList != null) {
            for (Object query : pyList) {
                ResourceManager.MANAGER.loadEntity(query.toString());
            }
        }
    }

    public void runUpdateStageOther(PyDictionary pyDict) {
        PyList pyList = (PyList) pyDict.get("updateStageOther");
        if (pyList != null) {

            for (int i = 0; i < pyList.size(); i++) {
                for (GameEntity tempGE : ResourceManager.MANAGER.gameEntities) {
                    if (tempGE.getFile().equals(pyList.get(i).toString())) {
                        tempGE.questStage = Integer.parseInt(pyList.get(++i).toString());
                    }
                }
            }
        }
    }

    public void runReleaseTheKraken(PyDictionary pyDict) {
        Object queryScript = pyDict.get("releaseTheKraken");
        if (queryScript != null)
            if (queryScript.toString().toLowerCase().equals("true"))
                GraphicsManager.MANAGER.jitterForDays(50);
    }

    public void runPlaySounds(PyDictionary pyDict) {
        PyList pyList = (PyList) pyDict.get("playSounds");
        if (pyList != null) {
            for (Object query : pyList) {
                ResourceManager.MANAGER.sound.play(query.toString());
            }
        }
    }

    // General updates for the entity
    public boolean runUpdates(PyDictionary pyDict, GameEntity ge) {
        boolean changeLevel = false;
        Object queryScript = pyDict.get("level");
        if (queryScript != null) {
            //ResourceManager.MANAGER.changeLevel(queryScript.toString());
            ResourceManager.queueNextLevel = queryScript.toString();
            changeLevel = true;
        }

        queryScript = pyDict.get("mass");
        if (queryScript != null) {
            ge.setMass(Float.parseFloat(queryScript.toString()));
        }

        queryScript = pyDict.get("stage");
        if (queryScript != null) {
            ge.questStage = Integer.parseInt(queryScript.toString());
        }

        queryScript = pyDict.get("follow");
        if (queryScript != null) {
            if (queryScript.toString().toLowerCase().equals("true"))
                ge.followPlayer = true;
            else
                ge.followPlayer = false;
        }

        return changeLevel;
    }

	/**
	 * The interpreter 'wakes up' only when it's needed. Therefore the first call to the
	 * Python interpreter takes roughly 1000ms. The warmUp method is meant to use up this
	 * 1000ms during loadtime rather than playtime.
	 */
	public void warmUp() {
		interpreter.exec("");		
	}
}
