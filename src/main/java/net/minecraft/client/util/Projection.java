package net.minecraft.client.util;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_INVALID_OPERATION;
import static org.lwjgl.opengl.GL11.GL_OUT_OF_MEMORY;
import static org.lwjgl.opengl.GL11.GL_STACK_OVERFLOW;
import static org.lwjgl.opengl.GL11.GL_STACK_UNDERFLOW;
import static org.lwjgl.opengl.GL11.glMultMatrixf;

public final class Projection {

    private Projection() {}
	
	public static String getErrorString(int code) {
		return switch (code) {
			case GL_NO_ERROR -> "No error";
			case GL_INVALID_ENUM -> "Invalid enum";
			case GL_INVALID_VALUE -> "Invalid value";
			case GL_INVALID_OPERATION -> "Invalid operation";
			case GL_STACK_OVERFLOW -> "Stack overflow";
			case GL_STACK_UNDERFLOW -> "Stack underflow";
			case GL_OUT_OF_MEMORY -> "Out of memory";
			default -> "Unknown error";
		};
	}
	
	public static void perspective(float fovY, float aspect, float zNear, float zFar) {
		Matrix4f m = new Matrix4f().setPerspective((float) Math.toRadians(fovY), aspect, zNear, zFar, true);
		FloatBuffer buf = BufferUtils.createFloatBuffer(16);
		m.get(buf);
		glMultMatrixf(buf);
	}
	
	public static void lookAt(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
		Matrix4f m = new Matrix4f()
				.setLookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
		FloatBuffer buf = BufferUtils.createFloatBuffer(16);
		m.get(buf);
		glMultMatrixf(buf);
	}
	
	public static boolean project(float objX, float objY, float objZ, FloatBuffer model, FloatBuffer proj, IntBuffer view, FloatBuffer objPos) {
		float[] m = new float[16];
		float[] p = new float[16];
		model.get(m);
		proj.get(p);
		int[] v = new int[4];
		view.get(v);
		
		Matrix4f modelView = new Matrix4f().set(m);
		Matrix4f projection = new Matrix4f().set(p);
		Matrix4f combined = new Matrix4f(projection).mul(modelView);
		
		Vector4f in = new Vector4f(objX, objY, objZ, 1);
		Vector4f out = combined.transform(in);
		
		if (out.w == 0) return false;
		
		out.x /= out.w;
		out.y /= out.w;
		out.z /= out.w;
		
		objPos.put(0, v[0] + (v[2] * (out.x + 1) / 2));
		objPos.put(1, v[1] + (v[3] * (out.y + 1) / 2));
		objPos.put(2, (out.z + 1) / 2);
		
		return true;
	}
	
	public static boolean unProject(float winX, float winY, float winZ, FloatBuffer model, FloatBuffer proj, IntBuffer view, FloatBuffer objPos) {
		float[] m = new float[16];
		float[] p = new float[16];
		model.get(m);
		proj.get(p);
		int[] v = new int[4];
		view.get(v);
		
		Matrix4f modelView = new Matrix4f().set(m);
		Matrix4f projection = new Matrix4f().set(p);
		Matrix4f combined = new Matrix4f(projection).mul(modelView);
		combined.invert();
		
		Vector4f in = new Vector4f(
				(winX - v[0]) / v[2] * 2 - 1,
				(winY - v[1]) / v[3] * 2 - 1,
				winZ * 2 - 1,
				1
		);
		Vector4f out = combined.transform(in);
		
		if (out.w == 0) return false;
		
		objPos.put(0, out.x / out.w);
		objPos.put(1, out.y / out.w);
		objPos.put(2, out.z / out.w);
		
		return true;
	}

}
