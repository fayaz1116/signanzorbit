package nu.shacknet.poseidon.pn.signanzorbit.scene;

import static android.opengl.GLES10.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.FloatBuffer;

import nu.shacknet.poseidon.pn.signanzorbit.config.Config;
import nu.shacknet.poseidon.pn.signanzorbit.math.AxisAlignedBox3;
import nu.shacknet.poseidon.pn.signanzorbit.math.Sphere;
import nu.shacknet.poseidon.pn.signanzorbit.util.Persistable;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.OGLManager;

import android.opengl.GLES11;

/**
 * The Class Geometry.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class Geometry implements Persistable
{
	/** The Constant VERTEX_LENGTH. */
	public static final int VERTEX_LENGTH = 3;
	
	/** The Constant TEXCOORD_LENGTH. */
	public static final int TEXCOORD_LENGTH = 2;
	
	/** The ogl manager. */
	private final OGLManager oglManager = OGLManager.instance;
	
	/** The Buffers. */
	private final FloatBuffer vertices, normals, texcoords;
	
	/** The bounding box. */
	private final AxisAlignedBox3 boundingBox;
	
	/** The bounding sphere. */
	private final Sphere boundingSphere;
	
	/** The number of vertices. */
	private final int numVertices;
	
	/** The nr of vertex bytes. */
	private final int nrOfVertexBytes;
	
	/** The nr of normal bytes. */
	private final int nrOfNormalBytes;
	
	/** The nr of texcoord bytes. */
	private final int nrOfTexcoordBytes;
	
	/** The vertex offset. */
	private int vertexOffset;
	
	/** The normal offset. */
	private int normalOffset;
	
	/** The texcoord offset. */
	private int texcoordOffset;

	/** if this is initialized. */
	private boolean initialized;
	
	/**
	 * Instantiates a new geometry.
	 *
	 * @param vertices the vertices
	 * @param normals the normals
	 * @param texcoords the texcoords
	 * @param boundingBox the bounding box
	 * @param boundingSphere the bounding sphere
	 * @param numVertices the num vertices
	 */
	public Geometry(FloatBuffer vertices, FloatBuffer normals, FloatBuffer texcoords, AxisAlignedBox3 boundingBox, Sphere boundingSphere, int numVertices)
	{
		this.vertices = vertices;
		this.normals = normals;
		this.texcoords = texcoords;
		this.boundingBox = boundingBox;
		this.boundingSphere = boundingSphere;
		this.numVertices = numVertices;
		initialized = false;
		
		if(vertices != null)
			nrOfVertexBytes = numVertices*VERTEX_LENGTH*4;
		else
			nrOfVertexBytes = 0;
		if(normals != null)
			nrOfNormalBytes = numVertices*VERTEX_LENGTH*4;
		else
			nrOfNormalBytes = 0;
		if(texcoords != null)
			nrOfTexcoordBytes = numVertices*TEXCOORD_LENGTH*4;
		else
			nrOfTexcoordBytes = 0;
	}
	
	/**
	 * Inits the Geometry
	 */
	void init()
	{
		if(!initialized)
		{
			if(Config.GLES11)
			{
				if(vertices != null)
					vertexOffset = oglManager.addBufferToVBO(vertices, nrOfVertexBytes);
				if(normals != null)
					normalOffset = oglManager.addBufferToVBO(normals, nrOfNormalBytes);
				if(texcoords != null)
					texcoordOffset = oglManager.addBufferToVBO(texcoords, nrOfTexcoordBytes);
			}
			
			initialized = true;
		}
	}
	
	/**
	 * De-inits the Geometry
	 */
	void deInit()
	{
		initialized = false;
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.util.Persistable#persist(java.io.DataOutputStream)
	 */
	public void persist(DataOutputStream dos)
	{
	}
	
	/* (non-Javadoc)
	 * @see nu.shacknet.poseidon.pn.signanzorbit.util.Persistable#restore(java.io.DataInputStream)
	 */
	public void restore(DataInputStream dis)
	{
	}
	
	/**
	 * Render.
	 *
	 * @param rendermode the rendermode
	 */
	public void render(int rendermode)
	{
		oglManager.clientState(vertices != null, normals != null, texcoords != null);
		
		switch(rendermode)
		{
		case Scene.RENDERMODE_VBO:
			if(vertices != null)
				GLES11.glVertexPointer(VERTEX_LENGTH, GL_FLOAT, 0, vertexOffset);
			if(normals != null)
				GLES11.glNormalPointer(GL_FLOAT, 0, normalOffset);
			if(texcoords != null)
				GLES11.glTexCoordPointer(TEXCOORD_LENGTH, GL_FLOAT, 0, texcoordOffset);
			break;
		case Scene.RENDERMODE_VERTEXARRAY:
			if(vertices != null)
				glVertexPointer(VERTEX_LENGTH, GL_FLOAT, 0, vertices);
			if(normals != null)
				glNormalPointer(GL_FLOAT, 0, normals);
			if(texcoords != null)
				glTexCoordPointer(TEXCOORD_LENGTH, GL_FLOAT, 0, texcoords);
			break;
		}
		
		glDrawArrays(GL_TRIANGLES, 0, numVertices);
	}
	
	/**
	 * Update.
	 */
	public void update()
	{
	}
	
	/**
	 * Gets the bounding box.
	 *
	 * @return the bounding box
	 */
	public AxisAlignedBox3 getBoundingBox()
	{
		return boundingBox;
	}
	
	/**
	 * Gets the bounding sphere.
	 *
	 * @return the bounding sphere
	 */
	public Sphere getBoundingSphere()
	{
		return boundingSphere;
	}
}
