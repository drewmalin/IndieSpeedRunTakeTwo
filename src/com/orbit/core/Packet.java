package com.orbit.core;

import java.io.Serializable;

public class Packet implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public int code;	// Packet code - designates the action to take with the info below
						// 0 - Create a new entity
						// 1 - Update existing entity
						// 2 - Remove an existing entity
	
	public int id;		// ID that identifies the entity
	
	public float x;		// x position
	public float y;		// y position
	public float z;  	// z position
	
	public float rx;	// x rotation
	public float ry;	// y rotation
	public float rz;	// z rotation

	public int width;
	public int height;

	public String file;
	public String type; //'MAP', 'ENTITY'
	
	public Packet() {}
}
