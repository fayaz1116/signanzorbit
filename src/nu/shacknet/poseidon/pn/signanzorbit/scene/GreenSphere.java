package nu.shacknet.poseidon.pn.signanzorbit.scene;

import java.nio.FloatBuffer;

import nu.shacknet.poseidon.pn.signanzorbit.config.Config;
import nu.shacknet.poseidon.pn.signanzorbit.math.AxisAlignedBox3;
import nu.shacknet.poseidon.pn.signanzorbit.math.Color4;
import nu.shacknet.poseidon.pn.signanzorbit.math.Sphere;
import nu.shacknet.poseidon.pn.signanzorbit.math.Vector3;
import nu.shacknet.poseidon.pn.signanzorbit.scene.MaterialManager.Material;


/**
 * The Class GreenSphere, represents a green sphere (for bounding sphere visualization).
 *
 * @author Alex Druml
 * @author Lukas Roessler
 */
public class GreenSphere extends Model
{
	
	/**
	 * Instantiates a new green sphere.
	 */
	public GreenSphere()
	{
		super();
		int spacing = Config.BOUNDING_SPHERE_SPACING;
		int thetaDegree = 360; // horizontal
		int phiDegree = 180; // vertical
		float[] temp = new float[12];						// 4 vertices with 3 floats each
		// (thetaDegree/spacing)							.. points per horizontal ring
		// (phiDegree/spacing)								.. points per vertical half-ring
		// (thetaDegree/spacing)*(phiDegree/spacing)		.. sum of points = sum of quads
		// ((thetaDegree/spacing)*(phiDegree/spacing))*6	.. sum of vertices (6 per quad)
		int vertexCount = ((thetaDegree/spacing)*(phiDegree/spacing))*6;
		float[] vertnormals = new float[vertexCount*3];
		int vertnormalsIndex = 0;
		
		// create sphere
		for(int theta = -(thetaDegree/2); theta < (thetaDegree/2); theta += spacing)
		{
			for(int phi = -(phiDegree/2); phi < (phiDegree/2); phi += spacing)
			{
				float phiR = (float)Math.toRadians(phi);
				float phi2R = (float)Math.toRadians(phi+spacing);
				float thetaR = (float)Math.toRadians(theta);
				float theta2R = (float)Math.toRadians(theta+spacing);
				
				// upper left vertex
				temp[ 0] = (float)(Math.cos(thetaR)*Math.cos(phiR)); 
				temp[ 1] = (float)(Math.sin(thetaR)*Math.cos(phiR));
				temp[ 2] = (float)(Math.sin(phiR));
				// lower left vertex
				temp[ 3] = (float)(Math.cos(thetaR)*Math.cos(phi2R));
				temp[ 4] = (float)(Math.sin(thetaR)*Math.cos(phi2R));
				temp[ 5] = (float)(Math.sin(phi2R));
				// lower right vertex
				temp[ 6] = (float)(Math.cos(theta2R)*Math.cos(phi2R));
				temp[ 7] = (float)(Math.sin(theta2R)*Math.cos(phi2R));
				temp[ 8] = (float)(Math.sin(phi2R));
				// upper right vertex
				temp[ 9] = (float)(Math.cos(theta2R)*Math.cos(phiR));
				temp[10] = (float)(Math.sin(theta2R)*Math.cos(phiR));
				temp[11] = (float)(Math.sin(phiR));
				
				// upper left vertex
				vertnormals[vertnormalsIndex++] = temp[0];
				vertnormals[vertnormalsIndex++] = temp[1];
				vertnormals[vertnormalsIndex++] = temp[2];
				
				// lower right vertex
				vertnormals[vertnormalsIndex++] = temp[6];
				vertnormals[vertnormalsIndex++] = temp[7];
				vertnormals[vertnormalsIndex++] = temp[8];
				
				// lower left vertex
				vertnormals[vertnormalsIndex++] = temp[3];
				vertnormals[vertnormalsIndex++] = temp[4];
				vertnormals[vertnormalsIndex++] = temp[5];
				
				// upper left vertex
				vertnormals[vertnormalsIndex++] = temp[0];
				vertnormals[vertnormalsIndex++] = temp[1];
				vertnormals[vertnormalsIndex++] = temp[2];
				
				// upper right vertex
				vertnormals[vertnormalsIndex++] = temp[9];
				vertnormals[vertnormalsIndex++] = temp[10];
				vertnormals[vertnormalsIndex++] = temp[11];
				
				// lower right vertex
				vertnormals[vertnormalsIndex++] = temp[6];
				vertnormals[vertnormalsIndex++] = temp[7];
				vertnormals[vertnormalsIndex++] = temp[8];
			}
		}
		
		FloatBuffer vertNormalBuffer = AOMLoader.instance.arrayToBuffer(vertnormals);
		Geometry sphereGeom = new Geometry(vertNormalBuffer, 
				vertNormalBuffer, 
				null, 
				new AxisAlignedBox3(new Vector3(-0.5f,-0.5f,-0.5f), new Vector3(0.5f,0.5f,0.5f)),
				new Sphere(new Vector3(0,0,0), 1),
				vertexCount);
		Material sphereMat = MaterialManager.instance.getMaterial("BoundingSphereMaterial",
				new Color4(0.0f, 0.5f, 0.0f, 0.1f),
				new Color4(0.0f, 0.5f, 0.0f, 0.1f),
				Color4.BLACK,
				Color4.BLACK,
				0,
				null);
		add(sphereGeom, sphereMat);
	}
}
