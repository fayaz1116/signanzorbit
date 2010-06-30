#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <GLES/gl.h>

#define  LOG_TAG    "Signanzorbit"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define PI 3.1415926535897932384626433832795f
#define PIHALF 1.5707963267948966192313216916398f

static jclass vector3_class;
static jmethodID vector3_constructor_FFF;

void vectorAddVector(int dimension, jfloat* result, jfloat* vectorA, jfloat* vectorB)
{
	int i;
	for(i=0; i<dimension; i++)
		result[i] = vectorA[i] + vectorB[i];
}

void vectorAddSkalar(int dimension, jfloat* result, jfloat* vector, jfloat skalar)
{
	int i;
	for(i=0; i<dimension; i++)
		result[i] = vector[i] + skalar;
}

void vectorSubtractVector(int dimension, jfloat* result, jfloat* vectorA, jfloat* vectorB)
{
	int i;
	for(i=0; i<dimension; i++)
		result[i] = vectorA[i] - vectorB[i];
}

void vectorSubtractSkalar(int dimension, jfloat* result, jfloat* vector, jfloat skalar)
{
	int i;
	for(i=0; i<dimension; i++)
		result[i] = vector[i] - skalar;
}

void vectorMultiplyVector(int dimension, jfloat* result, jfloat* vectorA, jfloat* vectorB)
{
	int i;
	for(i=0; i<dimension; i++)
		result[i] = vectorA[i] * vectorB[i];
}

void vectorMultiplySkalar(int dimension, jfloat* result, jfloat* vector, jfloat scalar)
{
	int i;
	for(i=0; i<dimension; i++)
		result[i] = vector[i] * scalar;
}

void vectorDivideVector(int dimension, jfloat* result, jfloat* vectorA, jfloat* vectorB)
{
	int i;
	for(i=0; i<dimension; i++)
		result[i] = vectorA[i] / vectorB[i];
}

void vectorDivideSkalar(int dimension, jfloat* result, jfloat* vector, jfloat scalar)
{
	int i;
	for(i=0; i<dimension; i++)
		result[i] = vector[i] / scalar;
}

jfloat vectorLength(int dimension, jfloat* vector)
{
	jfloat sum = 0;
	int i;
	for(i=0; i<dimension; i++)
		sum += (vector[i]*vector[i]);
	return sqrtf(sum);
}

void vectorNormalize(int dimension, jfloat* result, jfloat* vector)
{
	jfloat length = vectorLength(dimension, vector);
	if(length != 0 && length != 1)
	{
		int i;
		for(i=0; i<dimension; i++)
			result[i] = vector[i] / length;
	}
}

jfloat dotProduct(int dimension, jfloat* a, jfloat* b)
{
	jfloat sum = 0;
	int i;
	for(i=0; i<dimension; i++)
		sum += (a[i]*b[i]);
	return sum;
}

jfloat vectorAngle(int dimension, jfloat* a, jfloat* b)
{
	return acosf(dotProduct(dimension, a, b) / (vectorLength(dimension, a) * vectorLength(dimension, b)));
}

void vectorInvert(int dimension, jfloat* v)
{
	int i;
	for(i=0; i<dimension; i++)
		v[i] = -v[i];
}

void vector4Homogenize(jfloat* result, jfloat* vector)
{
	if(vector[3] != 1 && vector[3] != 0)
	{
		int i;
		for(i=0; i<4; i++)
			result[i] = vector[i] / vector[3];
	}
}

void vector3CrossProduct(jfloat* result, jfloat* a, jfloat* b)
{
	result[0] = a[1]*b[2] - a[2]*b[1];
	result[1] = a[2]*b[0] - a[0]*b[2];
	result[2] = a[0]*b[1] - a[1]*b[0];
}

void matrixMult(jfloat* left, jfloat* right, jfloat* result)
{
	jfloat temp[16];
	temp[ 0] = left[ 0]*right[ 0] + left[ 4]*right[ 1] + left[ 8]*right[ 2] + left[12]*right[ 3];
	temp[ 1] = left[ 1]*right[ 0] + left[ 5]*right[ 1] + left[ 9]*right[ 2] + left[13]*right[ 3];
	temp[ 2] = left[ 2]*right[ 0] + left[ 6]*right[ 1] + left[10]*right[ 2] + left[14]*right[ 3];
	temp[ 3] = left[ 3]*right[ 0] + left[ 7]*right[ 1] + left[11]*right[ 2] + left[15]*right[ 3];

	temp[ 4] = left[ 0]*right[ 4] + left[ 4]*right[ 5] + left[ 8]*right[ 6] + left[12]*right[ 7];
	temp[ 5] = left[ 1]*right[ 4] + left[ 5]*right[ 5] + left[ 9]*right[ 6] + left[13]*right[ 7];
	temp[ 6] = left[ 2]*right[ 4] + left[ 6]*right[ 5] + left[10]*right[ 6] + left[14]*right[ 7];
	temp[ 7] = left[ 3]*right[ 4] + left[ 7]*right[ 5] + left[11]*right[ 6] + left[15]*right[ 7];

	temp[ 8] = left[ 0]*right[ 8] + left[ 4]*right[ 9] + left[ 8]*right[10] + left[12]*right[11];
	temp[ 9] = left[ 1]*right[ 8] + left[ 5]*right[ 9] + left[ 9]*right[10] + left[13]*right[11];
	temp[10] = left[ 2]*right[ 8] + left[ 6]*right[ 9] + left[10]*right[10] + left[14]*right[11];
	temp[11] = left[ 3]*right[ 8] + left[ 7]*right[ 9] + left[11]*right[10] + left[15]*right[11];

	temp[12] = left[ 0]*right[12] + left[ 4]*right[13] + left[ 8]*right[14] + left[12]*right[15];
	temp[13] = left[ 1]*right[12] + left[ 5]*right[13] + left[ 9]*right[14] + left[13]*right[15];
	temp[14] = left[ 2]*right[12] + left[ 6]*right[13] + left[10]*right[14] + left[14]*right[15];
	temp[15] = left[ 3]*right[12] + left[ 7]*right[13] + left[11]*right[14] + left[15]*right[15];

	memcpy(result, temp, 16*sizeof(jfloat));
}

void transformVector(jfloat* matrix, jfloat* vector, jfloat* result)
{
	jfloat x = matrix[ 0]*vector[0] + matrix[ 4]*vector[1] + matrix[ 8]*vector[2] + matrix[12]*vector[3];
	jfloat y = matrix[ 1]*vector[0] + matrix[ 5]*vector[1] + matrix[ 9]*vector[2] + matrix[13]*vector[3];
	jfloat z = matrix[ 2]*vector[0] + matrix[ 6]*vector[1] + matrix[10]*vector[2] + matrix[14]*vector[3];
	jfloat w = matrix[ 3]*vector[0] + matrix[ 7]*vector[1] + matrix[11]*vector[2] + matrix[15]*vector[3];

	result[0] = x;
	result[1] = y;
	result[2] = z;
	result[3] = w;
}

void calcCentroid(int dimension, jfloat* result, jsize nrOfVertices, jfloat* vertices)
{
	// reset center to zero
	int i;
	for(i=0; i<dimension; i++)
		result[i] = 0;

	// calculate centroid
	for(i=0; i<nrOfVertices; i++)
		vectorAddVector(dimension, result, result, &vertices[dimension*i]);
	vectorDivideSkalar(dimension, result, result, nrOfVertices);
}

void convertPixelFormatABGRtoARGB(jint w, jint h, jint* input, jint* output)
{
	jint i;
	for(i=0; i<h; i++)
	{
		jint j;
		for(j=0; j<w; j++)
		{
			jint color_ABGR = input[i*w+j];

			jint alpha	= (color_ABGR >> 24) & 0xff;
			jint blue	= (color_ABGR >> 16) & 0xff;
			jint green	= (color_ABGR >>  8) & 0xff;
			jint red	= (color_ABGR >>  0) & 0xff;

			jint color_ARGB =	((alpha	& 0xff) << 24) |
					((red	& 0xff) << 16) |
					((green	& 0xff) <<  8) |
					((blue	& 0xff) <<  0);

			// Android starts in the upper left corner, OpenGL starts in the lower left corner..
			output[(h-i-1)*w+j] = color_ARGB;
		}
	}
}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env;
    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
        return -1;

    vector3_class				= (*env)->FindClass(env, "nu/shacknet/poseidon/pn/signanzorbit/math/Vector3");
    vector3_constructor_FFF		= (*env)->GetMethodID(env, vector3_class, "<init>", "(FFF)V");

    return JNI_VERSION_1_4;
}

jobject Java_nu_shacknet_poseidon_pn_signanzorbit_math_AxisAlignedBox3_center(JNIEnv* env, jobject thizz, jfloat minx, jfloat maxx, jfloat miny, jfloat maxy, jfloat minz, jfloat maxz)
{
	return (*env)->NewObject(env, vector3_class, vector3_constructor_FFF, 0.5f*(minx+maxx), 0.5f*(miny+maxy), 0.5f*(minz+maxz));
}

jfloat Java_nu_shacknet_poseidon_pn_signanzorbit_math_Ellipse_calcPerimeter(JNIEnv* env, jobject thiz, jfloatArray a_java, jfloatArray b_java)
{
	jfloat* vectorA	= (*env)->GetFloatArrayElements(env, a_java, NULL);
	jfloat* vectorB	= (*env)->GetFloatArrayElements(env, b_java, NULL);
	jfloat a = vectorLength(3, vectorA);
	jfloat b = vectorLength(3, vectorB);
	(*env)->ReleaseFloatArrayElements(env, a_java, vectorA, JNI_ABORT);
	(*env)->ReleaseFloatArrayElements(env, b_java, vectorB, JNI_ABORT);

	jfloat sum = a+b;
	jfloat lambda = (a-b)/sum;
	jfloat threeLambdaPowTwo = 3*powf(lambda, 2);

	return M_PI * sum * (1+(threeLambdaPowTwo/(10+(sqrtf(4.0f-threeLambdaPowTwo)))));
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Ellipse_getPoint(JNIEnv* env, jobject thiz, jfloatArray center_java, jfloatArray a_java, jfloatArray b_java, jfloatArray result_java, jfloat t)
{
	jfloat* center	= (*env)->GetFloatArrayElements(env, center_java, NULL);
	jfloat* a 		= (*env)->GetFloatArrayElements(env, a_java, NULL);
	jfloat* b		= (*env)->GetFloatArrayElements(env, b_java, NULL);
	jfloat* result	= (*env)->GetFloatArrayElements(env, result_java, NULL);

	jfloat aCosT[3], bSinT[3];
	vectorMultiplySkalar(3, aCosT, a, cosf(t));
	vectorMultiplySkalar(3, bSinT, b, sinf(t));
	vectorAddVector(3, aCosT, aCosT, bSinT);
	vectorAddVector(3, result, center, aCosT);

	(*env)->ReleaseFloatArrayElements(env, result_java, result, 0);
	(*env)->ReleaseFloatArrayElements(env, center_java, center, JNI_ABORT);
	(*env)->ReleaseFloatArrayElements(env, a_java, a, JNI_ABORT);
	(*env)->ReleaseFloatArrayElements(env, b_java, b, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Matrix44_setFromQuaternion(JNIEnv* env, jobject thiz, jfloatArray m16_java, jfloat x, jfloat y, jfloat z, jfloat w)
{
	jfloat* m16 = (*env)->GetFloatArrayElements(env, m16_java, NULL);

	m16[ 0] = 1.0f - 2.0f * ( y * y + z * z );
	m16[ 1] = 2.0f * ( x * y + w * z );
	m16[ 2] = 2.0f * ( x * z - w * y );
	m16[ 3] = 0.0f;

	m16[ 4] = 2.0f * ( x * y - w * z );
	m16[ 5] = 1.0f - 2.0f * ( x * x + z * z );
	m16[ 6] = 2.0f * ( y * z + w * x );
	m16[ 7] = 0.0f;

	m16[ 8] = 2.0f * ( x * z + w * y );
	m16[ 9] = 2.0f * ( y * z - w * x );
	m16[10] = 1.0f - 2.0f * ( x * x + y * y );
	m16[11] = 0.0f;

	m16[12] = 0.0f;
	m16[13] = 0.0f;
	m16[14] = 0.0f;
	m16[15] = 1.0f;

	(*env)->ReleaseFloatArrayElements(env, m16_java, m16, 0);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Matrix44_setRotate(JNIEnv* env, jobject thiz, jfloatArray m16_java, jfloatArray axis_java, jfloat alpha)
{
	jfloat* m16		= (*env)->GetFloatArrayElements(env, m16_java, NULL);
	jfloat* axis	= (*env)->GetFloatArrayElements(env, axis_java, NULL);

	vectorNormalize(3, axis, axis);

	jfloat cosa = cos(alpha);
	jfloat sina = sin(alpha);

	m16[ 0] = axis[0] * axis[0] + (1 - axis[0] * axis[0]) * cosa;
	m16[ 1] = axis[0] * axis[1] * (1 - cosa) + axis[2] * sina;
	m16[ 2] = axis[0] * axis[2] * (1 - cosa) - axis[1] * sina;
	m16[ 3] = 0;

	m16[ 4] = axis[0] * axis[1] * (1 - cosa) - axis[2] * sina;
	m16[ 5] = axis[1] * axis[1] + (1 - axis[1] * axis[1]) * cosa;
	m16[ 6] = axis[1] * axis[2] * (1 - cosa) + axis[0] * sina;
	m16[ 7] = 0;

	m16[ 8] = axis[0] * axis[2] * (1 - cosa) + axis[1] * sina;
	m16[ 9] = axis[1] * axis[2] * (1 - cosa) - axis[0] * sina;
	m16[10] = axis[2] * axis[2] + (1 - axis[2] * axis[2]) * cosa;
	m16[11] = 0;

	m16[12] = 0;
	m16[13] = 0;
	m16[14] = 0;
	m16[15] = 1;

	(*env)->ReleaseFloatArrayElements(env, m16_java, m16, 0);
	(*env)->ReleaseFloatArrayElements(env, axis_java, axis, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Matrix44_mult(JNIEnv* env, jobject thiz, jfloatArray left_java, jfloatArray right_java, jfloatArray result_java)
{
	jfloat* left = (*env)->GetFloatArrayElements(env, left_java, NULL);
	jfloat* right = (*env)->GetFloatArrayElements(env, right_java, NULL);
	jfloat* result = (*env)->GetFloatArrayElements(env, result_java, NULL);

	matrixMult(left, right, result);

	(*env)->ReleaseFloatArrayElements(env, result_java, result, 0);
	(*env)->ReleaseFloatArrayElements(env, left_java, left, JNI_ABORT);
	(*env)->ReleaseFloatArrayElements(env, right_java, right, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Matrix44_transformPoint(JNIEnv* env, jobject thiz, jfloatArray m16_java, jfloatArray vector_java)
{
	jfloat* m16		= (*env)->GetFloatArrayElements(env, m16_java, NULL);
	jfloat* vector	= (*env)->GetFloatArrayElements(env, vector_java, NULL);
	jsize dimension = (*env)->GetArrayLength(env, vector_java);

	jfloat vector4[4] = {0,0,0,1};

	memcpy(vector4, vector, dimension*sizeof(jfloat));

	transformVector(m16, vector4, vector4);

	if(dimension == 3)
		vector4Homogenize(vector4, vector4);

	memcpy(vector, vector4, dimension*sizeof(jfloat));

	(*env)->ReleaseFloatArrayElements(env, vector_java, vector, 0);
	(*env)->ReleaseFloatArrayElements(env, m16_java, m16, JNI_ABORT);
}

jfloat Java_nu_shacknet_poseidon_pn_signanzorbit_math_Matrix44_transformSphere(JNIEnv* env, jobject thiz, jfloatArray m16_java, jfloatArray center_java, jfloat radius)
{
	jfloat* center	= (*env)->GetFloatArrayElements(env, center_java, NULL);
	jfloat* m16		= (*env)->GetFloatArrayElements(env, m16_java, NULL);

	jfloat radiusVector[4] = {1,0,0,1};
	jfloat center4[4] = {0,0,0,1};
	memcpy(center4, center, 3*sizeof(jfloat));

	// make radiusVector a point on the hull
	vectorMultiplySkalar(3, radiusVector, radiusVector, radius);
	vectorAddVector(3, radiusVector, radiusVector, center4);

	// transform both points
	transformVector(m16, center4, center4);
	vector4Homogenize(center4, center4);
	transformVector(m16, radiusVector, radiusVector);
	vector4Homogenize(radiusVector, radiusVector);

	// make radiusVector a vector from center to hull again
	vectorSubtractVector(3, radiusVector, radiusVector, center4);

	memcpy(center, center4, 3*sizeof(jfloat));
	(*env)->ReleaseFloatArrayElements(env, center_java, center, 0);
	(*env)->ReleaseFloatArrayElements(env, m16_java, m16, JNI_ABORT);

	return vectorLength(3, radiusVector);
}

jboolean Java_nu_shacknet_poseidon_pn_signanzorbit_math_Sphere_isPointInside(JNIEnv* env, jobject thiz, jfloatArray center_java, jfloat radius, jfloatArray point_java)
{
	jfloat* center	= (*env)->GetFloatArrayElements(env, center_java, NULL);
	jfloat* point	= (*env)->GetFloatArrayElements(env, point_java, NULL);

	jfloat temp[3];
	vectorSubtractVector(3, temp, point, center);

	(*env)->ReleaseFloatArrayElements(env, center_java, center, JNI_ABORT);
	(*env)->ReleaseFloatArrayElements(env, point_java, point, JNI_ABORT);

	return (vectorLength(3, temp) <= radius);
}

jfloat Java_nu_shacknet_poseidon_pn_signanzorbit_math_Sphere_setPointSet(JNIEnv* env, jobject thiz, jfloatArray vertices_java, jfloatArray center_java)
{
	jfloat* vertices	= (*env)->GetFloatArrayElements(env, vertices_java, NULL);
	jfloat* center		= (*env)->GetFloatArrayElements(env, center_java, NULL);
	jsize size			= (*env)->GetArrayLength(env, vertices_java);

	jsize nrOfVertices = size/3;

	calcCentroid(3, center, nrOfVertices, vertices);

	jfloat temp[3];

	jfloat r = 0;
	int i;
	for(i=0; i<nrOfVertices; i++)
	{
		vectorSubtractVector(3, temp, &vertices[3*i], center);
		jfloat tempR = vectorLength(3, temp);
		if(tempR > r)
			r = tempR;
	}

	(*env)->ReleaseFloatArrayElements(env, center_java, center, 0);
	(*env)->ReleaseFloatArrayElements(env, vertices_java, vertices, JNI_ABORT);

	return r;
}

jfloat Java_nu_shacknet_poseidon_pn_signanzorbit_math_Sphere_setSphereSet(JNIEnv* env, jobject thiz, jfloatArray centers_java, jfloatArray radii_java, jfloatArray center_java)
{
	jfloat* centers	= (*env)->GetFloatArrayElements(env, centers_java, NULL);
	jfloat* radii	= (*env)->GetFloatArrayElements(env, radii_java, NULL);
	jfloat* center	= (*env)->GetFloatArrayElements(env, center_java, NULL);

	jsize nrOfCenters = ((*env)->GetArrayLength(env, centers_java))/3;

	calcCentroid(3, center, nrOfCenters, centers);

	jfloat temp[3];
	jfloat r = 0;
	int i;
	for(i=0; i<nrOfCenters; i++)
	{
		vectorSubtractVector(3, temp, &centers[3*i], center);
		jfloat tempR = vectorLength(3, temp) + radii[i];
		if(tempR > r)
			r = tempR;
	}

	(*env)->ReleaseFloatArrayElements(env, center_java, center, 0);
	(*env)->ReleaseFloatArrayElements(env, centers_java, centers, JNI_ABORT);
	(*env)->ReleaseFloatArrayElements(env, radii_java, radii, JNI_ABORT);

	return r;
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector2_add___3F_3F(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloatArray otherV_java)
{
	jfloat* v		= (*env)->GetFloatArrayElements(env, v_java, NULL);
	jfloat* otherV	= (*env)->GetFloatArrayElements(env, otherV_java, NULL);

	vectorAddVector(2, v, v, otherV);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
	(*env)->ReleaseFloatArrayElements(env, otherV_java, otherV, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector2_add___3FF(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloat skalar)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vectorAddSkalar(2, v, v, skalar);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector2_subtract___3F_3F(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloatArray otherV_java)
{
	jfloat* v		= (*env)->GetFloatArrayElements(env, v_java, NULL);
	jfloat* otherV	= (*env)->GetFloatArrayElements(env, otherV_java, NULL);

	vectorSubtractVector(2, v, v, otherV);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
	(*env)->ReleaseFloatArrayElements(env, otherV_java, otherV, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector2_subtract___3FF(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloat skalar)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vectorSubtractSkalar(2, v, v, skalar);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector2_multiply___3F_3F(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloatArray otherV_java)
{
	jfloat* v		= (*env)->GetFloatArrayElements(env, v_java, NULL);
	jfloat* otherV	= (*env)->GetFloatArrayElements(env, otherV_java, NULL);

	vectorMultiplyVector(2, v, v, otherV);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
	(*env)->ReleaseFloatArrayElements(env, otherV_java, otherV, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector2_multiply___3FF(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloat skalar)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vectorMultiplySkalar(2, v, v, skalar);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector2_divide___3F_3F(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloatArray otherV_java)
{
	jfloat* v		= (*env)->GetFloatArrayElements(env, v_java, NULL);
	jfloat* otherV	= (*env)->GetFloatArrayElements(env, otherV_java, NULL);

	vectorDivideVector(2, v, v, otherV);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
	(*env)->ReleaseFloatArrayElements(env, otherV_java, otherV, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector2_divide___3FF(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloat skalar)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vectorDivideSkalar(2, v, v, skalar);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

jfloat Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector2_dotProduct(JNIEnv* env, jclass clazz, jfloatArray a_java, jfloatArray b_java)
{
	jfloat* a = (*env)->GetFloatArrayElements(env, a_java, NULL);
	jfloat* b = (*env)->GetFloatArrayElements(env, b_java, NULL);

	jfloat dot = dotProduct(2, a, b);

	(*env)->ReleaseFloatArrayElements(env, a_java, a, JNI_ABORT);
	(*env)->ReleaseFloatArrayElements(env, b_java, b, JNI_ABORT);

	return dot;
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector2_normalize(JNIEnv* env, jobject thiz, jfloatArray v_java)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vectorNormalize(2, v, v);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector2_invert(JNIEnv* env, jobject thiz, jfloatArray v_java)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vectorInvert(2, v);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

jfloat Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector2_length(JNIEnv* env, jobject thiz, jfloatArray v_java)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	jfloat length = vectorLength(2, v);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, JNI_ABORT);

	return length;
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector3_add___3F_3F(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloatArray otherV_java)
{
	jfloat* v		= (*env)->GetFloatArrayElements(env, v_java, NULL);
	jfloat* otherV	= (*env)->GetFloatArrayElements(env, otherV_java, NULL);

	vectorAddVector(3, v, v, otherV);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
	(*env)->ReleaseFloatArrayElements(env, otherV_java, otherV, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector3_add___3FF(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloat skalar)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vectorAddSkalar(3, v, v, skalar);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector3_subtract___3F_3F(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloatArray otherV_java)
{
	jfloat* v		= (*env)->GetFloatArrayElements(env, v_java, NULL);
	jfloat* otherV	= (*env)->GetFloatArrayElements(env, otherV_java, NULL);

	vectorSubtractVector(3, v, v, otherV);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
	(*env)->ReleaseFloatArrayElements(env, otherV_java, otherV, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector3_subtract___3FF(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloat skalar)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vectorSubtractSkalar(3, v, v, skalar);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector3_multiply___3F_3F(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloatArray otherV_java)
{
	jfloat* v		= (*env)->GetFloatArrayElements(env, v_java, NULL);
	jfloat* otherV	= (*env)->GetFloatArrayElements(env, otherV_java, NULL);

	vectorMultiplyVector(3, v, v, otherV);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
	(*env)->ReleaseFloatArrayElements(env, otherV_java, otherV, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector3_multiply___3FF(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloat skalar)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vectorMultiplySkalar(3, v, v, skalar);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector3_divide___3F_3F(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloatArray otherV_java)
{
	jfloat* v		= (*env)->GetFloatArrayElements(env, v_java, NULL);
	jfloat* otherV	= (*env)->GetFloatArrayElements(env, otherV_java, NULL);

	vectorDivideVector(3, v, v, otherV);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
	(*env)->ReleaseFloatArrayElements(env, otherV_java, otherV, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector3_divide___3FF(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloat skalar)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vectorDivideSkalar(3, v, v, skalar);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

jfloat Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector3_dotProduct(JNIEnv* env, jclass clazz, jfloatArray a_java, jfloatArray b_java)
{
	jfloat* a = (*env)->GetFloatArrayElements(env, a_java, NULL);
	jfloat* b = (*env)->GetFloatArrayElements(env, b_java, NULL);

	jfloat dot = dotProduct(3, a, b);

	(*env)->ReleaseFloatArrayElements(env, a_java, a, JNI_ABORT);
	(*env)->ReleaseFloatArrayElements(env, b_java, b, JNI_ABORT);

	return dot;
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector3_crossProduct(JNIEnv* env, jclass clazz, jfloatArray a_java, jfloatArray b_java, jfloatArray result_java)
{
	jfloat* a		= (*env)->GetFloatArrayElements(env, a_java, NULL);
	jfloat* b		= (*env)->GetFloatArrayElements(env, b_java, NULL);
	jfloat* result	= (*env)->GetFloatArrayElements(env, result_java, NULL);

	vector3CrossProduct(result, a, b);

	(*env)->ReleaseFloatArrayElements(env, result_java, result, 0);
	(*env)->ReleaseFloatArrayElements(env, a_java, a, JNI_ABORT);
	(*env)->ReleaseFloatArrayElements(env, b_java, b, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector3_normalize(JNIEnv* env, jobject thiz, jfloatArray v_java)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vectorNormalize(3, v, v);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

jfloat Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector3_getAngle(JNIEnv* env, jobject thiz, jfloatArray a_java, jfloatArray b_java)
{
	jfloat* a = (*env)->GetFloatArrayElements(env, a_java, NULL);
	jfloat* b = (*env)->GetFloatArrayElements(env, b_java, NULL);

	float angle = vectorAngle(3, a, b);

	(*env)->ReleaseFloatArrayElements(env, a_java, a, JNI_ABORT);
	(*env)->ReleaseFloatArrayElements(env, b_java, b, JNI_ABORT);

	return angle;
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector3_invert(JNIEnv* env, jobject thiz, jfloatArray v_java)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vectorInvert(3, v);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

jfloat Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector3_length(JNIEnv* env, jobject thiz, jfloatArray v_java)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	jfloat length = vectorLength(3, v);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, JNI_ABORT);

	return length;
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector4_add___3F_3F(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloatArray otherV_java)
{
	jfloat* v		= (*env)->GetFloatArrayElements(env, v_java, NULL);
	jfloat* otherV	= (*env)->GetFloatArrayElements(env, otherV_java, NULL);

	vector4Homogenize(v,v);
	vector4Homogenize(otherV, otherV);
	vectorAddVector(3, v, v, otherV);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
	(*env)->ReleaseFloatArrayElements(env, otherV_java, otherV, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector4_add3(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloatArray otherV_java)
{
	jfloat* v		= (*env)->GetFloatArrayElements(env, v_java, NULL);
	jfloat* otherV	= (*env)->GetFloatArrayElements(env, otherV_java, NULL);

	vector4Homogenize(v,v);
	vectorAddVector(3, v, v, otherV);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
	(*env)->ReleaseFloatArrayElements(env, otherV_java, otherV, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector4_add___3FF(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloat skalar)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vector4Homogenize(v,v);
	vectorAddSkalar(3, v, v, skalar);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector4_subtract___3F_3F(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloatArray otherV_java)
{
	jfloat* v		= (*env)->GetFloatArrayElements(env, v_java, NULL);
	jfloat* otherV	= (*env)->GetFloatArrayElements(env, otherV_java, NULL);

	vector4Homogenize(v,v);
	vector4Homogenize(otherV, otherV);
	vectorSubtractVector(3, v, v, otherV);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
	(*env)->ReleaseFloatArrayElements(env, otherV_java, otherV, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector4_subtract3(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloatArray otherV_java)
{
	jfloat* v		= (*env)->GetFloatArrayElements(env, v_java, NULL);
	jfloat* otherV	= (*env)->GetFloatArrayElements(env, otherV_java, NULL);

	vector4Homogenize(v,v);
	vectorSubtractVector(3, v, v, otherV);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
	(*env)->ReleaseFloatArrayElements(env, otherV_java, otherV, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector4_subtract___3FF(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloat skalar)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vector4Homogenize(v,v);
	vectorSubtractSkalar(3, v, v, skalar);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector4_multiply___3F_3F(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloatArray otherV_java)
{
	jfloat* v		= (*env)->GetFloatArrayElements(env, v_java, NULL);
	jfloat* otherV	= (*env)->GetFloatArrayElements(env, otherV_java, NULL);

	vector4Homogenize(v,v);
	vector4Homogenize(otherV, otherV);
	vectorMultiplyVector(3, v, v, otherV);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
	(*env)->ReleaseFloatArrayElements(env, otherV_java, otherV, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector4_multiply3(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloatArray otherV_java)
{
	jfloat* v		= (*env)->GetFloatArrayElements(env, v_java, NULL);
	jfloat* otherV	= (*env)->GetFloatArrayElements(env, otherV_java, NULL);

	vector4Homogenize(v,v);
	vectorMultiplyVector(3, v, v, otherV);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
	(*env)->ReleaseFloatArrayElements(env, otherV_java, otherV, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector4_multiply___3FF(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloat skalar)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vector4Homogenize(v,v);
	vectorMultiplySkalar(3, v, v, skalar);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector4_divide___3F_3F(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloatArray otherV_java)
{
	jfloat* v		= (*env)->GetFloatArrayElements(env, v_java, NULL);
	jfloat* otherV	= (*env)->GetFloatArrayElements(env, otherV_java, NULL);

	vector4Homogenize(v,v);
	vector4Homogenize(otherV, otherV);
	vectorDivideVector(3, v, v, otherV);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
	(*env)->ReleaseFloatArrayElements(env, otherV_java, otherV, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector4_divide3(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloatArray otherV_java)
{
	jfloat* v		= (*env)->GetFloatArrayElements(env, v_java, NULL);
	jfloat* otherV	= (*env)->GetFloatArrayElements(env, otherV_java, NULL);

	vector4Homogenize(v,v);
	vectorDivideVector(3, v, v, otherV);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
	(*env)->ReleaseFloatArrayElements(env, otherV_java, otherV, JNI_ABORT);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector4_divide___3FF(JNIEnv* env, jobject thiz, jfloatArray v_java, jfloat skalar)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vector4Homogenize(v,v);
	vectorDivideSkalar(3, v, v, skalar);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

jfloat Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector4_dotProduct(JNIEnv* env, jclass clazz, jfloatArray a_java, jfloatArray b_java)
{
	jfloat* a = (*env)->GetFloatArrayElements(env, a_java, NULL);
	jfloat* b = (*env)->GetFloatArrayElements(env, b_java, NULL);

	vector4Homogenize(a,a);
	vector4Homogenize(b,b);
	jfloat dot = dotProduct(3, a, b);

	(*env)->ReleaseFloatArrayElements(env, a_java, a, JNI_ABORT);
	(*env)->ReleaseFloatArrayElements(env, b_java, b, JNI_ABORT);

	return dot;
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector4_normalize(JNIEnv* env, jobject thiz, jfloatArray v_java)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vector4Homogenize(v,v);
	vectorNormalize(3, v, v);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector4_homogenize(JNIEnv* env, jobject thiz, jfloatArray v_java)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vector4Homogenize(v,v);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector4_invert(JNIEnv* env, jobject thiz, jfloatArray v_java)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vector4Homogenize(v,v);
	vectorInvert(3, v);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, 0);
}

jfloat Java_nu_shacknet_poseidon_pn_signanzorbit_math_Vector4_length(JNIEnv* env, jobject thiz, jfloatArray v_java)
{
	jfloat* v = (*env)->GetFloatArrayElements(env, v_java, NULL);

	vector4Homogenize(v,v);
	jfloat length = vectorLength(3, v);

	(*env)->ReleaseFloatArrayElements(env, v_java, v, JNI_ABORT);

	return length;
}

float Java_nu_shacknet_poseidon_pn_signanzorbit_util_MathUtil_forcePulseFunktion(JNIEnv* env, jclass clazz, jfloat t)
{
	return (jfloat)(sinf(sinf(t*PIHALF)*PI-PIHALF)/2.0f+0.5f);
}

void Java_nu_shacknet_poseidon_pn_signanzorbit_util_managers_OGLManager_convertPixelFormatABGRtoARGB(JNIEnv* env, jclass clazz, int w, int h, jintArray bitmap_ogl_java, jintArray bitmap_android_java)
{
	jint* bitmap_ogl = (*env)->GetIntArrayElements(env, bitmap_ogl_java, NULL);
	jint* bitmap_android = (*env)->GetIntArrayElements(env, bitmap_android_java, NULL);

	convertPixelFormatABGRtoARGB(w, h, bitmap_ogl, bitmap_android);

	(*env)->ReleaseIntArrayElements(env, bitmap_android_java, bitmap_android, 0);
	(*env)->ReleaseIntArrayElements(env, bitmap_ogl_java, bitmap_ogl, JNI_ABORT);
}

jintArray Java_nu_shacknet_poseidon_pn_signanzorbit_util_managers_OGLManager_getPixelsNative(JNIEnv* env, jclass clazz, jint x, jint y, jint w, jint h)
{
	jintArray result_java = (*env)->NewIntArray(env, w*h);
	jint* result_c = (*env)->GetIntArrayElements(env, result_java, NULL);

	jint* result_opengl = malloc(w*h*sizeof(jint));

	glReadPixels(x, y, w, h, GL_RGBA, GL_UNSIGNED_BYTE, result_opengl);

	convertPixelFormatABGRtoARGB(w, h, result_opengl, result_c);

	free(result_opengl);

	(*env)->ReleaseIntArrayElements(env, result_java, result_c, 0);
	return result_java;
}
