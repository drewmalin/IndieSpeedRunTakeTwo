package com.orbit.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.newdawn.slick.opengl.Texture;

public class TextureCycle {
	public HashMap<String, ArrayList<Texture>> cycle;
	
	public int lastFrame;
	public long lastCheckNano;
	public long speedNano;
	public String lastAnimation = "";
	
	public TextureCycle() {
		lastCheckNano = speedNano = lastFrame = 0;
		cycle = new HashMap<String, ArrayList<Texture>>();
	}
	
	private void advanceFrame(String anim) {
		
		if (lastFrame + 1 == cycle.get(anim).size())
			lastFrame = 0;
		else
			lastFrame++;
		
		lastCheckNano = System.nanoTime();

	}
	
	public Texture getNextFrame(String anim) {
	
		if (!lastAnimation.equals(anim)) {
			lastFrame = 0;
			lastAnimation = anim;
		}
		
		if (System.nanoTime() - lastCheckNano >= speedNano)
			advanceFrame(anim);
		
		return cycle.get(anim).get(lastFrame);

	}
}
