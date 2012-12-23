package com.orbit.core;

import org.lwjgl.util.vector.Vector3f;

public interface Moveable {

	public void setPosition(float x, float y, float z);
	public void setRotation(float x, float y, float z);
	
	/**
	 * translateX
	 * @param delta
	 * @return Returns the amount translated.
	 */
	public float translateX(float delta);
	
	/**
	 * translateY
	 * @param delta
	 * @return Returns the amount translated.
	 */
	public float translateY(float delta);
	
	public Vector3f getPosition();
	public Vector3f getRotation();

}
