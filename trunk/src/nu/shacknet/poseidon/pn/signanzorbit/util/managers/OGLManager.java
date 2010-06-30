package nu.shacknet.poseidon.pn.signanzorbit.util.managers;

import static android.opengl.GLES10.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.Buffer;
import java.util.ArrayList;

import nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector3;
import nu.shacknet.poseidon.pn.signanzorbit.util.datastructures.Triple;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.opengl.GLES11;
import android.opengl.GLU;

/**
 * The Class OGLManager.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class OGLManager
{
	static
	{
		System.loadLibrary("signanzorbit");
	}
	
	/** The Constant instance. */
	public static final OGLManager instance = new OGLManager();

	/** whether the vertex client state is set. */
	private boolean clientStateVertices;
	
	/** whether the normal client state is set. */
	private boolean clientStateNormals;
	
	/** whether the texcoord client state is set. */
	private boolean clientStateTexcoords;
	
	/** Pointers to the Buffers, along with their offsets (Second part of the Triple) and their sizes in Bytes (Third part of the Triple). */
	private ArrayList<Triple<Integer, Integer, Buffer>> vboBuffers;
	
	/** holds the current vbo offset, needed during assembling of the vbo. */
	private int currentVBOoffset;
	
	/** The VBO's id. */
	private int vbo;

	/** The modelview matrix. */
	private Matrix44 modelview;
	
	/** The projection matrix. */
	private Matrix44 projection;
	
	/** The viewport as returned by glViewport. */
	private int[] viewport;

	//temp vars for unproject
	/** The viewport array4. */
	private int[] viewportArray4;
	
	/** The modelview array16. */
	private float[] modelviewArray16;
	
	/** The projection array16. */
	private float[] projectionArray16;
	
	/** The window. */
	private float[] window;
	
	/** The unprojected pos. */
	private float[] unprojectedPos;
	
	/** The unprojected pos vec. */
	private Vector3 unprojectedPosVec;

	/**
	 * Instantiates a new OGL manager.
	 */
	private OGLManager()
	{
		modelview = new Matrix44();
		projection = new Matrix44();
		viewport = new int[]{0,0,1,1};	// dummy viewport
		
		window = new float[3];
		unprojectedPos = new float[4];
		unprojectedPosVec = new Vector3();
		
		vboBuffers = new ArrayList<Triple<Integer,Integer,Buffer>>();
		
		reset();
	}

	/**
	 * Reset.
	 */
	public void reset()
	{
		clientStateVertices = false;
		clientStateNormals = false;
		clientStateTexcoords = false;
		currentVBOoffset = 0;
		vboBuffers.clear();
	}
	
	/**
	 * adds a buffer to the vbo, the buffer will be compiled
	 * into the vbo as soon as compileVBO is called.
	 *
	 * @param buffer the buffer
	 * @param bytes the bytes
	 * @return the int
	 */
	public int addBufferToVBO(Buffer buffer, int bytes)
	{
		int thisOffset = currentVBOoffset;
		vboBuffers.add(new Triple<Integer, Integer, Buffer>(thisOffset, bytes, buffer));
		currentVBOoffset += bytes;
		return thisOffset;
	}
	
	/**
	 * Compiles all added Buffers into a single VBO.
	 */
	public void init()
	{
		int[] bufferIDs = new int[1];
		GLES11.glGenBuffers(1, bufferIDs, 0);
		vbo = bufferIDs[0];
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, vbo);
		GLES11.glBufferData(GLES11.GL_ARRAY_BUFFER, currentVBOoffset, null, GLES11.GL_STATIC_DRAW);
		
		ArrayList<Triple<Integer, Integer, Buffer>> buffers = vboBuffers;
		int size = buffers.size();
		
		for(int i=0; i<size; i++)
		{
			Triple<Integer, Integer, Buffer> triple = buffers.get(i);
			GLES11.glBufferSubData(GLES11.GL_ARRAY_BUFFER, 
					triple.getFirst(),		// offset
					triple.getSecond(),		// size
					triple.getThird());		// data
		}
	}
	
	/**
	 * De init.
	 */
	public void deInit()
	{
		GLES11.glDeleteBuffers(1, new int[]{vbo}, 0);
		vboBuffers.clear();
	}
	
	/**
	 * sets Client states, minimizes state changes.
	 *
	 * @param vertices whether the vertex client state should be enabled
	 * @param normals whether the normal client state should be enabled
	 * @param texcoords whether the texcoord client state should be enabled
	 */
	public void clientState(boolean vertices, boolean normals, boolean texcoords)
	{
		if(vertices && !clientStateVertices)
			glEnableClientState(GL_VERTEX_ARRAY);
		if(!vertices && clientStateVertices)
			glDisableClientState(GL_VERTEX_ARRAY);

		if(normals && !clientStateNormals)
			glEnableClientState(GL_NORMAL_ARRAY);
		if(!normals && clientStateNormals)
			glDisableClientState(GL_NORMAL_ARRAY);

		if(texcoords && !clientStateTexcoords)
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		if(!texcoords && clientStateTexcoords)
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
	}

	/**
	 * Gets the modelview.
	 *
	 * @return the modelview
	 */
	public synchronized Matrix44 getModelview()
	{
		return modelview;
	}

	/**
	 * Gets the projection.
	 *
	 * @return the projection
	 */
	public synchronized Matrix44 getProjection()
	{
		return projection;
	}

	/**
	 * Gets the viewport.
	 *
	 * @return the viewport
	 */
	public synchronized int[] getViewport()
	{
		return viewport;
	}

	/**
	 * Gl frustum infinite.
	 *
	 * @param left the left
	 * @param right the right
	 * @param bottom the bottom
	 * @param top the top
	 * @param zNear the z near
	 * @param zFar the z far
	 * @param result the result
	 */
	public void glFrustumInfinite(float left, float right, float bottom, float top, float zNear, float zFar, Matrix44 result)
	{
		float x, y, a, b, c, d;
		x = (2.0f * zNear) / (right - left);
		y = (2.0f * zNear) / (top - bottom);
		a = (right + left) / (right - left);
		b = (top + bottom) / (top - bottom);

		if (zFar == Float.MAX_VALUE)
		{
			// no depth clamp extension here - workaround see
			// http://www.gamasutra.com/view/feature/2942/the_mechanics_of_robust_stencil_.php?page=2
			c = -0.999f;
			d = -1.999f * zNear;
		}
		else
		{
			c = -(zFar + zNear) / (zFar - zNear);
			d = -(2.0f * zFar * zNear) / (zFar - zNear);
		}

		result.set(
				x, 0, 0, 0,
				0, y, 0, 0,
				a, b, c, -1,
				0, 0, d, 0);
	}

	/**
	 * Glu look at.
	 *
	 * @param eyePos the eye pos
	 * @param inverseForwardNormalized the inverse forward normalized
	 * @param rightNormalized the right normalized
	 * @param upNormalized the up normalized
	 * @param result the result
	 */
	public void gluLookAt(Vector3 eyePos, Vector3 inverseForwardNormalized, Vector3 rightNormalized, Vector3 upNormalized, Matrix44 result)
	{
		float[] view = result.getArray16();
		view[ 0] = rightNormalized.v[0];
		view[ 1] = upNormalized.v[0];
		view[ 2] = inverseForwardNormalized.v[0];
		view[ 3] = 0.0f;
		view[ 4] = rightNormalized.v[1];
		view[ 5] = upNormalized.v[1];
		view[ 6] = inverseForwardNormalized.v[1];
		view[ 7] = 0.0f;
		view[ 8] = rightNormalized.v[2];
		view[ 9] = upNormalized.v[2];
		view[10] = inverseForwardNormalized.v[2];
		view[11] = 0.0f;
		view[12] = (rightNormalized.v[0]			* -eyePos.v[0]) + (rightNormalized.v[1]				* -eyePos.v[1]) + (rightNormalized.v[2]				* -eyePos.v[2]);
		view[13] = (upNormalized.v[0]				* -eyePos.v[0]) + (upNormalized.v[1]				* -eyePos.v[1]) + (upNormalized.v[2]				* -eyePos.v[2]);
		view[14] = (inverseForwardNormalized.v[0]	* -eyePos.v[0]) + (inverseForwardNormalized.v[1]	* -eyePos.v[1]) + (inverseForwardNormalized.v[2]	* -eyePos.v[2]);
		view[15] = 1.0f;
		
		result.set(view);
	}
	
	/**
	 * Un project.
	 *
	 * @param x the x
	 * @param y the y
	 * @return the vector3
	 */
	public Vector3 unProject(int x, int y)
	{
		viewportArray4 = getViewport();
		modelviewArray16 = getModelview().getArray16();
		projectionArray16 = getProjection().getArray16();
		
		window[0] = (float)x;
		window[1] = (float)(viewport[3] - y);
		window[2] = 1;
			
        GLU.gluUnProject( 	window[0], window[1], window[2], modelviewArray16, 0, 
        					projectionArray16, 0, viewportArray4, 0, unprojectedPos, 0);
		
        unprojectedPosVec.v[0] = unprojectedPos[0];
        unprojectedPosVec.v[1] = unprojectedPos[1];
        unprojectedPosVec.v[2] = unprojectedPos[2];
        
        //normalize with 4th component
        unprojectedPosVec.divide(unprojectedPos[3]);
   
        return unprojectedPosVec;
	}
	
	/**
	 * Save screenshot.
	 *
	 * @param os the fos
	 */
	public void saveScreenshot(OutputStream os)
	{
		saveScreenshot(viewport[0], viewport[1], viewport[2], viewport[3], os);
	}
	
	/**
	 * Save screenshot natively.
	 *
	 * @param x the x
	 * @param y the y
	 * @param w the w
	 * @param h the h
	 * @param os the fos
	 */
	public static void saveScreenshot(final int x, final int y, final int w, final int h, final OutputStream os)
	{
		final int[] pixels = getPixelsNative(x,y,w,h);
		new Thread()
		{
			public void run()
			{
				Bitmap bmp = Bitmap.createBitmap(pixels, w, h, Config.ARGB_8888);
				try
				{
					bmp.compress(CompressFormat.PNG, 100, os);
					os.flush();
				}
				catch(Throwable t)
				{
					LogManager.eTag("ScreenShot","Saving PNG failed", t);
				}
				finally
				{
					try
					{
						os.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			};
		}.start();
	}
	
	/**
	 * Gets the pixels.
	 *
	 * @param x the x
	 * @param y the y
	 * @param w the w
	 * @param h the h
	 * @return the pixels
	 */
	private static native int[] getPixelsNative(int x, int y, int w, int h);
	
	/**
	 * Convert an image from ABGR to ARGB.
	 *
	 * @param w the width of the image
	 * @param h the height of the image
	 * @param bitmap_ogl the bitmap in OpenGL ES format (ABGR)
	 * @param bitmap_android the bitmap in Android format (ARGB) will be written into this
	 */
	public static native void convertPixelFormatABGRtoARGB(int w, int h, int[] bitmap_ogl, int[] bitmap_android);
}
