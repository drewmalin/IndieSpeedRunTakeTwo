package com.orbit.core;

/**
 * Created with IntelliJ IDEA.
 * User: drewmalin
 * Date: 12/23/12
 * Time: 7:24 PM
 * To change this template use File | Settings | File Templates.
 */
import java.io.FileInputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;


public class Sound {
    // Buffer holding the raw sound data
    private IntBuffer buffer;
    // Source from which sound is emitted
    private IntBuffer source;
    // Position of source
    private FloatBuffer sourcePos;
    // Velocity of the source
    private FloatBuffer sourceVel;
    // Position of listener
    private FloatBuffer listenerPos;
    // Velocity of listener
    private FloatBuffer listenerVel;
    // Orientation of listener
    private FloatBuffer listenerOri;

    private ArrayList<String> files;
    private ArrayList<Boolean> loops;	//really dumb way of storing per-file flags

    public Sound() {
        files = new ArrayList<String>();
        loops = new ArrayList<Boolean>();
    }

    public void load(String mode, String file, Boolean loop) {
        if (mode.equals("WAV") || mode.equals("MP3")) {
            files.add(file);
            loops.add(loop);
        }
    }

    public void create() {
        int numBuffers = files.size();
        FileInputStream fin = null;

        buffer = BufferUtils.createIntBuffer(numBuffers);
        source = BufferUtils.createIntBuffer(numBuffers);
        sourcePos = (FloatBuffer)BufferUtils.createFloatBuffer(3 * numBuffers).put(new float[] {0f, 0f, 0f}).rewind();
        sourceVel = (FloatBuffer)BufferUtils.createFloatBuffer(3 * numBuffers).put(new float[] {0f, 0f, 0f}).rewind();
        listenerPos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] {0f, 0f, 0f}).rewind();
        listenerVel = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] {0f, 0f, 0f}).rewind();
        listenerOri = (FloatBuffer)BufferUtils.createFloatBuffer(6).put(
                new float[] {0f, 0f, -1f, 0f, 1f, 0f}).rewind(); // (x, y, z, xup, yup, zup)

        try {
            AL.create();
            AL10.alGenBuffers(buffer);
            for (int i = 0; i < files.size(); i++) {
                fin = new FileInputStream(files.get(i));
                WaveData waveFile = WaveData.create(fin);
                AL10.alBufferData(buffer.get(i), waveFile.format, waveFile.data, waveFile.samplerate);
            }
            AL10.alGenSources(source);
            for (int i = 0; i < files.size(); i++) {
                AL10.alSourcei(source.get(i), AL10.AL_BUFFER, buffer.get(i));
                AL10.alSourcef(source.get(i), AL10.AL_PITCH, 1f);
                AL10.alSourcef(source.get(i), AL10.AL_GAIN, 1f);
                AL10.alSource(source.get(i), AL10.AL_POSITION, (FloatBuffer)sourcePos.position(i*3));
                AL10.alSource(source.get(i), AL10.AL_VELOCITY, (FloatBuffer)sourceVel.position(i*3));
                if (loops.get(i))
                    AL10.alSourcei(source.get(i), AL10.AL_LOOPING, AL10.AL_TRUE);
                else
                    AL10.alSourcei(source.get(i), AL10.AL_LOOPING, AL10.AL_FALSE);
            }
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setListenerValues();
    }

    public void setListenerValues() {
        AL10.alListener(AL10.AL_POSITION, listenerPos);
        AL10.alListener(AL10.AL_VELOCITY, listenerVel);
        AL10.alListener(AL10.AL_ORIENTATION, listenerOri);
    }

    public void killALData() {
        AL10.alDeleteSources(source);
        AL10.alDeleteBuffers(buffer);
        AL.destroy();
    }

    public void play(int idx) {
        AL10.alSourcePlay(source.get(idx));
    }

    public void play(String name) {
        for (int i = 0; i < files.size(); i++) {
            if (name.equals(files.get(i).substring(files.get(i).lastIndexOf("/") + 1, files.get(i).lastIndexOf("."))))
                AL10.alSourcePlay(source.get(i));
        }
    }

    public void pause(int idx) {
        AL10.alSourcePause(source.get(idx));
    }

    public int soundCount() {
        return files.size();
    }
}
