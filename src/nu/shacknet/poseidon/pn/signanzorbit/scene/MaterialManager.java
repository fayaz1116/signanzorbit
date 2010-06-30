package nu.shacknet.poseidon.pn.signanzorbit.scene;

import static android.opengl.GLES10.*;
import static android.opengl.GLUtils.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import nu.shacknet.poseidon.pn.signanzorbit.LevelActivity;
import nu.shacknet.poseidon.pn.signanzorbit.config.Config;
import nu.shacknet.poseidon.pn.signanzorbit.math.Color4;
import nu.shacknet.poseidon.pn.signanzorbit.util.Persistable;
import nu.shacknet.poseidon.pn.signanzorbit.util.managers.LogManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11;

/**
 * The Class MaterialManager.
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class MaterialManager
{
	/** The Constant instance. */
	public static final MaterialManager instance = new MaterialManager();
	
	/** The materials. */
	private final HashMap<String, Material> materials;
	
	/** The currently bound material name. */
	private String boundMaterialName;
	
	/** if textures are currently enabled. */
	private boolean textureEnabled;
	
	/**
	 * Instantiates a new material manager.
	 */
	private MaterialManager()
	{
		materials = new HashMap<String, Material>();
		boundMaterialName = "";
		textureEnabled = true;
	}
	
	/**
	 * Reset.
	 */
	public void reset()
	{
		materials.clear();
		boundMaterialName = "";
		textureEnabled = true;
	}
	
	/**
	 * Checks if a material with a given name is present
	 *
	 * @param name the name
	 * @return true, if there is a material with this name
	 */
	public boolean hasMaterial(String name)
	{
		return materials.containsKey(name);
	}
	
	/**
	 * Gets the material.
	 *
	 * @param name the name
	 * @return the material for the given name or null if it doesn't exist
	 */
	public Material getMaterial(String name)
	{
		return materials.get(name);	
	}
	
	/**
	 * Bind material.
	 *
	 * @param material the material
	 */
	public void bindMaterial(Material material)
	{
		if(!material.name.equals(boundMaterialName))
		{
			glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, material.ambient.asArray, 0);
	        glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, material.diffuse.asArray, 0);
	        glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, material.specular.asArray, 0);
	        glMaterialfv(GL_FRONT_AND_BACK, GL_EMISSION, material.emissive.asArray, 0);
	        glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, material.ks);
	        if(textureEnabled && material.texId == -1)
	        {
	        	glDisable(GL_TEXTURE_2D);
	        	textureEnabled = false;
	        }
	        if(material.texId != -1)
	        {
	        	if(!textureEnabled)
	        	{
	        		glEnable(GL_TEXTURE_2D);
	        		textureEnabled = true;
	        	}
	        	glBindTexture(GL_TEXTURE_2D, material.texId);
	        }
	        boundMaterialName = material.name;
		}
	}
	
	/**
	 * Bind material.
	 *
	 * @param name the name
	 */
	public void bindMaterial(String name)
	{
		if(materials.containsKey(name))
			bindMaterial(materials.get(name));
	}
	
	/**
	 * Adds the material.
	 *
	 * @param name the name
	 * @param ambient the ambient
	 * @param diffuse the diffuse
	 * @param specular the specular
	 * @param emissive the emissive
	 * @param ks the ks
	 * @param textureFilename the texture filename
	 * @return the material
	 */
	public Material addMaterial(String name, Color4 ambient, Color4 diffuse, Color4 specular, Color4 emissive, float ks, String textureFilename)
	{
		if(materials.containsKey(name))
			LogManager.w("Material '" + name + "' already exists, replacing...");
		Material m = new Material(name, ambient, diffuse, specular, emissive, ks, textureFilename);
		materials.put(name, m);
		return m;
	}
	
	/**
	 * creates the material if it doesn't exist yet and returns it
	 *
	 * @param name the name
	 * @param ambient the ambient
	 * @param diffuse the diffuse
	 * @param specular the specular
	 * @param emissive the emissive
	 * @param ks the ks
	 * @param textureFilename the texture filename
	 * @return the material
	 */
	public Material getMaterial(String name, Color4 ambient, Color4 diffuse, Color4 specular, Color4 emissive, float ks, String textureFilename)
	{
		if(materials.containsKey(name))
			return materials.get(name);
		else
		{
			Material m = new Material(name, ambient, diffuse, specular, emissive, ks, textureFilename);
			materials.put(name, m);
			return m;
		}
	}
	
	/**
	 * The Class Material.
	 *
	 * @author Alex Druml
	 * @author Lukas Roessler
	 */
	public class Material implements Persistable
	{
		
		/** The name. */
		private final String name;
		
		/** The ambient color. */
		private final Color4 ambient;
		
		/** The diffuse color. */
		private final Color4 diffuse;
		
		/** The specular color. */
		private final Color4 specular;
		
		/** The emissive color. */
		private final Color4 emissive;
		
		/** The ks. */
		private final float ks;
		
		/** The tex. */
		private final Bitmap tex;
		
		/** The tex id. */
		private int texId;

		/** if this is initialized. */
		private boolean initialized;
		
		/**
		 * Instantiates a new material.
		 *
		 * @param name the name
		 * @param ambient the ambient
		 * @param diffuse the diffuse
		 * @param specular the specular
		 * @param emissive the emissive
		 * @param ks the ks
		 * @param textureFilename the texture filename
		 */
		private Material(String name, Color4 ambient, Color4 diffuse, Color4 specular, Color4 emissive, float ks, String textureFilename)
		{
			this.name = name;
			this.ambient = ambient;
			this.diffuse = diffuse;
			this.specular = specular;
			this.emissive = emissive;
			this.ks = ks;
			
			if(textureFilename != null && !textureFilename.equals(""))
				tex = loadTexture(textureFilename);
			else
				tex = null;
		}
		
		/**
		 * Inits the Material
		 */
		void init()
		{
			if(!initialized)
			{
				if(tex != null)
				{
					int[] textures = new int[1];
					glGenTextures(1, textures, 0);
					texId = textures[0];
					glBindTexture(GL_TEXTURE_2D, texId);
					glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
					glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);

					if(Config.GLES11)
					{
						glTexParameterf(GL_TEXTURE_2D, GLES11.GL_GENERATE_MIPMAP, GL_TRUE);
						texImage2D(GL_TEXTURE_2D, 0, tex, 0);
					}
					else
					{
						buildMipmap(tex);
					}
				}
				else
					texId = -1;
				
				initialized = true;
			}
		}
		
		/**
		 * De-inits the Material
		 */
		void deInit()
		{
			initialized = false;
			glDeleteTextures(1, new int[]{texId}, 0);
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
		 * Loads texture into a Bitmap
		 *
		 * @param filename the filename
		 * @return the bitmap
		 */
		private Bitmap loadTexture(String filename)
		{
			Context context = LevelActivity.getInstance();
			InputStream is = context.getResources().openRawResource(context.getResources().getIdentifier(filename, "drawable", "at.ac.tuwien.cg.cgmd.bifth2010"));
			Bitmap bitmap = null;
			try
			{
				bitmap = BitmapFactory.decodeStream(is);
				
				// hack for getting a config... for transparent pngs
				if(bitmap.getConfig() == null)
					bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
				
				LogManager.i("Loaded Texture '" + filename + "': " + bitmap.getWidth() + "x" + bitmap.getHeight() + ", density=" + bitmap.getDensity() + ", config=" + bitmap.getConfig());
			}
			catch(Throwable t)
			{
				LogManager.e("Could not load Texture: " + filename, t);
				bitmap = null;
			}
			finally
			{
				//Always clear and close
				try
				{
					is.close();
					is = null;
				}
				catch (IOException e)
				{
				}
			}
			return bitmap;
		}
		
		/**
		 * Our own MipMap generation implementation.
		 * Scale the original bitmap down, always by factor two,
		 * and set it as new mipmap level.
		 * 
		 * Thanks to Mike Miller (with minor changes by l17)!
		 *
		 * @param bitmap the bitmap
		 */
		private void buildMipmap(Bitmap bitmap)
		{
			int level = 0;
			int height = bitmap.getHeight();
			int width = bitmap.getWidth();
			
			Bitmap bitmap2 = bitmap.copy(bitmap.getConfig(), false);

			while(height >= 1 || width >= 1)
			{
				//First of all, generate the texture from our bitmap and set it to the according level
				texImage2D(GL_TEXTURE_2D, level, bitmap2, 0);
				
				if(height == 1 || width == 1)
					break;

				//Increase the mipmap level
				level++;

				//Downscale
				height /= 2;
				width /= 2;
				bitmap2.recycle();
				bitmap2 = Bitmap.createScaledBitmap(bitmap, width, height, true);
			}
			bitmap2.recycle();
		}
	}
}
