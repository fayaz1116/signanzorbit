package nu.shacknet.poseidon.pn.signanzorbit.scene;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import nu.shacknet.poseidon.pn.signanzorbit.LevelActivity;
import nu.shacknet.poseidon.pn.signanzorbit.math.AxisAlignedBox3;
import nu.shacknet.poseidon.pn.signanzorbit.math.Color4;
import nu.shacknet.poseidon.pn.signanzorbit.math.Matrix44;
import nu.shacknet.poseidon.pn.signanzorbit.math.Sphere;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector3;
import nu.shacknet.poseidon.pn.signanzorbit.scene.MaterialManager.Material;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.LogManager;

import android.content.Context;

/**
 * # AndroidModel format (*.aom) layout. The whole thing is usually GZIPed.
 * # Entities:
 * #	- Material
 * #		A Material is a set of ambient, diffuse, specular and emissive colors, a ks and an
 * #		optional textureFilename; identified by a name
 * #	- Geometry
 * #		A Geometry is a set of Vertices (forming Triangles), along with their normals
 * #		and (optionally) their texcoords. Each Geometry is identified by a name
 * #	- Model
 * #		A Model is a set of Geometries, identified by a name, each having it's own Material
 * #	- SceneEntity
 * #		A SceneEntity is a set of Models, each having it's own Transformation Matrix. Furthermore
 * #		the SceneEntity as whole has a Transformation Matrix as well, and is identified by a name
 * 
 * #----------
 * # Header
 * #----------
 * 4ndr01dM0d3l				String			Magic Bytes
 * version					int				The current Version of this Format
 * 
 * #----------
 * # Materials
 * #----------
 * numMaterials				int				Number of Materials in this file
 * for(numMaterials)
 * {
 * 	name					String			Name of this Material (must be unique)
 * 	ambient					float[4]		ambient color
 * 	diffuse					float[4]		diffuse color
 * 	specular				float[4]		specular color
 * 	emissive				float[4]		emissive color
 * 	ks						float			specular reflecion coefficient
 * 	textureFilename			String			texture filename, "" if no texture present
 * }
 * 
 * #----------
 * # Geometries
 * #----------
 * numGeometries			int				Number of Geometries in this file
 * for(numGeometries)
 * {
 * 	geometryName			String			Name of this Geometry (must be unique)
 * 	boundingBoxMin			float[3]		Minimum Point of the bounding box
 * 	boundingBoxMax			float[3]		Maximum Point of the bounding box
 * 	boundingSphereCenter	float[3]		Center of the bounding sphere
 * 	boundingSphereRadius	float			Radius of the bounding sphere
 * 	numVertices				int				Number of Vertices of this Geometry
 * 	for(numVertices)	
 * 	{
 * 		vertex				float[3]		A Vertex
 * 	}
 * 	for(numVertices)	
 * 	{
 * 		normal				float[3]		A Normal
 * 	}
 * 	hasTexcoords			boolean			If we have Texcoords
 * 	if(hasTexcoords)
 * 	{
 * 		for(numTexcoords)
 * 		{
 * 			texcoord		float[2]		A Texcoord
 * 		}
 * 	}
 * }
 * 
 * #----------
 * # Models
 * #----------
 * numModels				int				Number of Models in this file
 * for(numModels)
 * {
 * 	modelName				String			Name of this Model (must be unique)
 * 	boundingSphereCenter	float[3]		Center of the bounding sphere
 * 	boundingSphereRadius	float			Radius of the bounding sphere
 * 	numGeoms				int				Number of Geometries in this Model
 * 	for(numGeoms)
 * 	{
 * 		geometry			String			A Geometry, identified by its name
 * 		materialName		String			Material of this Geometry
 * 	}
 * }
 * 
 * #----------
 * # SceneEntities
 * #----------
 * numSceneEntities			int				Number of Scene Entities in this file
 * for(numSceneEntities)
 * {
 * 	SceneEntityName			String			Name of this SceneEntity (must be unique)
 * 	sceneTransformation		float[16]		Transformation matrix of this SceneEntity (regarding world)
 * 	numModls				int				Number of Models in this SceneEntity
 * 	for(numModls)
 * 	{
 * 		modelTransformation	float[16]		Transformation matrix of this Model (regarding the Scene Entity)
 * 		model				String			A Model, identified by its name
 * 	}
 * }
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class AOMLoader
{
	
	/** The Constant CURRENT_VERSION. */
	private static final int CURRENT_VERSION = 6;
	
	/** The Constant instance. */
	public static final AOMLoader instance = new AOMLoader();
	
	/**
	 * Instantiates a new scene loader.
	 */
	private AOMLoader()
	{
		
	}
	
	/**
	 * Read scene.
	 *
	 * @param filename the filename
	 * @return the scene
	 */
	public Scene readScene(String filename)
	{
		long startTime = System.currentTimeMillis();
		DataInputStream dis = null;
		Scene scene = new Scene();
		Context context = LevelActivity.getInstance();
		MaterialManager materialManager = MaterialManager.instance;
		float[] temp3 = new float[3], temp4 = new float[4], temp16 = new float[16];
		
		try
		{
			dis = new DataInputStream(new GZIPInputStream(context.getResources().openRawResource(context.getResources().getIdentifier(filename, "raw", "at.ac.tuwien.cg.cgmd.bifth2010"))));
			HashMap<String, Material> materials = new HashMap<String, Material>();
			HashMap<String, Geometry> geometries = new HashMap<String, Geometry>();
			HashMap<String, Model> models = new HashMap<String, Model>();
			
			/*
			 * Header
			 */
			if(!dis.readUTF().equals("4ndr01dM0d3l"))
				throw new IOException("Invalid AndroidModel File: " + filename);
			
			int version = dis.readInt();
			if(version != CURRENT_VERSION)
				throw new IOException("Invalid AndroidModel Version: need " + CURRENT_VERSION + ", found " + version);
			
			/*
			 * Materials
			 */
			int numMaterials = dis.readInt();
			for(int i=0; i<numMaterials; i++)
			{
				String name = dis.readUTF();
				
				readFloatArray(temp4, dis);
				Color4 ambient = new Color4(temp4);
				
				readFloatArray(temp4, dis);
				Color4 diffuse = new Color4(temp4);
				
				readFloatArray(temp4, dis);
				Color4 specular = new Color4(temp4);
				
				readFloatArray(temp4, dis);
				Color4 emissive = new Color4(temp4);
			
				float ks = dis.readFloat();
				
				String textureFilename = dis.readUTF();
				
				materials.put(name, materialManager.addMaterial(name, ambient, diffuse, specular, emissive, ks, textureFilename));
			}
			
			/*
			 * Geometries
			 */
			int numGeometries = dis.readInt();
			for(int i=0; i<numGeometries; i++)
			{
				String name = dis.readUTF();
				
				readFloatArray(temp3, dis);
				Vector3 boundingBoxMin = new Vector3(temp3);
				readFloatArray(temp3, dis);
				Vector3 boundingBoxMax = new Vector3(temp3);
				AxisAlignedBox3 boundingBox = new AxisAlignedBox3(boundingBoxMin, boundingBoxMax);
				
				readFloatArray(temp3, dis);
				Vector3 boundingSphereCenter = new Vector3(temp3);
				float boundingSphereRadius = dis.readFloat();
				Sphere boundingSphere = new Sphere(boundingSphereCenter, boundingSphereRadius);
				
				int numVertices = dis.readInt();
				
				float[] vertices = new float[numVertices*3];
				readFloatArray(vertices, dis);

				float[] normals = new float[numVertices*3];
				readFloatArray(normals, dis);
				
				boolean hasTexcoords = dis.readBoolean();
				float[] texcoords = null;
				if(hasTexcoords)
				{
					texcoords = new float[numVertices*2];
					readFloatArray(texcoords, dis);
				}
				geometries.put(name, new Geometry(arrayToBuffer(vertices), arrayToBuffer(normals), arrayToBuffer(texcoords), boundingBox, boundingSphere, numVertices));
			}
			
			/*
			 * Models
			 */
			int numModels = dis.readInt();
			for(int i=0; i<numModels; i++)
			{
				Model m = new Model();
				
				m.setName(dis.readUTF());
				
				readFloatArray(temp3, dis);
				Vector3 boundingSphereCenter = new Vector3(temp3);
				float boundingSphereRadius = dis.readFloat();
				Sphere boundingSphere = new Sphere(boundingSphereCenter, boundingSphereRadius);
				m.getBoundingSphere().set(boundingSphere);
				
				int numGeoms = dis.readInt();
				for(int j=0; j<numGeoms; j++)
				{
					String geometryName = dis.readUTF();
					Geometry geometry = geometries.get(geometryName);
					if(geometry == null)
						throw new IOException("Model " + m.getName() + " specifies an invalid Geometry " + geometryName);
					
					String materialName = dis.readUTF();
					Material mat = materials.get(materialName);
					if(mat==null)
						throw new IOException("Model " + m.getName() + " specifies an invalid Material " + materialName);
					
					m.add(geometry, mat);
				}
				
				models.put(m.getName(), m);
			}
			
			/*
			 * SceneEntities
			 */
			int numSceneEntities = dis.readInt();
			for(int i=0; i<numSceneEntities; i++)
			{
				SceneEntity sceneEntity = new SceneEntity();
				
				String name = dis.readUTF();
				
				readFloatArray(temp16, dis);
				Matrix44 sceneTransformation = new Matrix44(temp16);
				
				int numModls = dis.readInt();
				for(int j=0; j<numModls; j++)
				{
					readFloatArray(temp16, dis);
					Matrix44 modelTransformation = new Matrix44(temp16);
					
					String modelName = dis.readUTF();
					if(!models.containsKey(modelName))
						throw new IOException("SceneEntity " + name + " specifies an invalid Model " + modelName);
					
					//Model gets copied because it holds its own transformation, still references the same Geometry
					Model m = new Model(models.get(modelName));
					m.setTransformation(modelTransformation);
					sceneEntity.add(m);
				}
				
				sceneEntity.setTransformation(sceneTransformation);
				sceneEntity.setName(name);
				
				scene.add(sceneEntity);
			}
			
		}
		catch(Throwable t)
		{
			scene = null;
			LogManager.e("Error when reading Scene:", t);
		}
		finally
		{
			if(dis != null)
				try{dis.close();}catch(IOException e){}
		}
		
		if(scene != null)
		{
			long duration = System.currentTimeMillis()-startTime;
			LogManager.i("Successfully read Scene '" + filename + "' in " + duration + " ms");
			scene.update();
		}
		
		return scene;
	}
	
	/**
	 * Read float array.
	 *
	 * @param arr the array to write to
	 * @param dis the DataInputStream to read from
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void readFloatArray(float[] arr, DataInputStream dis) throws IOException
	{
		for(int i=0; i<arr.length; i++)
			arr[i] = dis.readFloat();
	}
	
	/**
	 * Array to buffer.
	 *
	 * @param data the array to read from
	 * @return the float buffer
	 */
	public FloatBuffer arrayToBuffer(float[] data)
	{
		if(data == null)
			return null;
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(data.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(data);
        floatBuffer.position(0);
        return floatBuffer;
	}
}
