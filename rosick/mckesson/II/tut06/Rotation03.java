package rosick.mckesson.II.tut06;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import static rosick.glm.Vec.*;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import rosick.GLWindow;
import rosick.framework.Framework;
import rosick.glm.Glm;
import rosick.glm.Mat3;
import rosick.glm.Mat4;
import rosick.glm.Vec3;
import rosick.glm.Vec4;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * II. Positioning
 * 6. Objects in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2006.html
 * @author integeruser
 */
public class Rotation03 extends GLWindow {
	
	public static void main(String[] args) {		
		new Rotation03().start();
	}
	
	
	private static final String BASEPATH = "/rosick/mckesson/II/tut06/data/";

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		
	private int theProgram;
	private int modelToCameraMatrixUnif, cameraToClipMatrixUnif;
	private int vertexBufferObject, indexBufferObject;
	private int vao;
	
	private final float vertexData[] = {
		+1.0f, +1.0f, +1.0f,
		-1.0f, -1.0f, +1.0f,
		-1.0f, +1.0f, -1.0f,
		+1.0f, -1.0f, -1.0f,

		-1.0f, -1.0f, -1.0f,
		+1.0f, +1.0f, -1.0f,
		+1.0f, -1.0f, +1.0f,
		-1.0f, +1.0f, +1.0f,

		0.0f, 1.0f, 0.0f, 1.0f,
		0.0f, 0.0f, 1.0f, 1.0f,
		1.0f, 0.0f, 0.0f, 1.0f,
		0.5f, 0.5f, 0.0f, 1.0f,

		0.0f, 1.0f, 0.0f, 1.0f,
		0.0f, 0.0f, 1.0f, 1.0f,
		1.0f, 0.0f, 0.0f, 1.0f,
		0.5f, 0.5f, 0.0f, 1.0f,
	};
	
	private final short indexData[] = {
		0, 1, 2,
		1, 0, 3,
		2, 3, 0,
		3, 2, 1,

		5, 4, 6,
		4, 5, 7,
		7, 6, 4,
		6, 7, 5,
	};
	
	private final int numberOfVertices = 8;
	
	private Mat4 cameraToClipMatrix = new Mat4();
	private FloatBuffer tempSharedBuffer16 = BufferUtils.createFloatBuffer(16);

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
		
	private void initializeProgram() {	
		int vertexShader =		Framework.loadShader(GL_VERTEX_SHADER, 		BASEPATH + "PosColorLocalTransform.vert");
		int fragmentShader = 	Framework.loadShader(GL_FRAGMENT_SHADER, 	BASEPATH + "ColorPassthrough.frag");
        
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(vertexShader);
		shaderList.add(fragmentShader);

		theProgram = Framework.createProgram(shaderList);
		
	    modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix");
		cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix");
		
		float fzNear = 1.0f; float fzFar = 45.0f;

		cameraToClipMatrix.set(0, 	fFrustumScale);
		cameraToClipMatrix.set(5, 	fFrustumScale);
		cameraToClipMatrix.set(10, 	(fzFar + fzNear) / (fzNear - fzFar));
		cameraToClipMatrix.set(11, 	-1.0f);
		cameraToClipMatrix.set(14, 	(2 * fzFar * fzNear) / (fzNear - fzFar));
		
		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillBuffer(tempSharedBuffer16));
		glUseProgram(0);
	}
	
	private void initializeVertexBuffer() {
		FloatBuffer tempVertexBuffer = BufferUtils.createFloatBuffer(vertexData.length);
		tempVertexBuffer.put(vertexData);
		tempVertexBuffer.flip();
		
        vertexBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
	    glBufferData(GL_ARRAY_BUFFER, tempVertexBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		
		ShortBuffer tempIndexBuffer = BufferUtils.createShortBuffer(indexData.length);
		tempIndexBuffer.put(indexData);
		tempIndexBuffer.flip();
		
        indexBufferObject = glGenBuffers();	       
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);
	    glBufferData(GL_ELEMENT_ARRAY_BUFFER, tempIndexBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	
	@Override
	protected void init() {
		initializeProgram();
		initializeVertexBuffer(); 

		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		int colorDataOffset = 4 * 3 * numberOfVertices;
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, colorDataOffset);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferObject);

		glBindVertexArray(0);
		
	    glEnable(GL_CULL_FACE);
	    glCullFace(GL_BACK);
	    glFrontFace(GL_CW);
	    
	    glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(0.0f, 1.0f);
	}
	
	
	@Override
	protected void display() {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		glUseProgram(theProgram);
		
		glBindVertexArray(vao);
		
		float fElapsedTime = (float) (getElapsedTime() / 1000.0);
		for (Instance currInst : g_instanceList) {
			final Mat4 transformMatrix = currInst.constructMatrix(fElapsedTime);
			
			glUniformMatrix4(modelToCameraMatrixUnif, false, transformMatrix.fillBuffer(tempSharedBuffer16));
			glDrawElements(GL_TRIANGLES, indexData.length, GL_UNSIGNED_SHORT, 0);
		}

		glBindVertexArray(0);
		glUseProgram(0);
	}

	
	@Override
	protected void reshape(int width, int height) {
		cameraToClipMatrix.set(0, fFrustumScale / (width / (float) height));
		cameraToClipMatrix.set(5, fFrustumScale);

		glUseProgram(theProgram);
		glUniformMatrix4(cameraToClipMatrixUnif, false, cameraToClipMatrix.fillBuffer(tempSharedBuffer16));
		glUseProgram(0);

		glViewport(0, 0, width, height);
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private Instance g_instanceList[] = {
			new NullRotation	(new Vec3(0.0f, 0.0f, -25.0f)),
			new RotateX			(new Vec3(-5.0f, -5.0f, -25.0f)),
			new RotateY			(new Vec3(-5.0f, 5.0f, -25.0f)),
			new RotateZ			(new Vec3(5.0f, 5.0f, -25.0f)),
			new RotateAxis		(new Vec3(5.0f, -5.0f, -25.0f))
	};
	
	
	private abstract class Instance {
		
		Vec3 offset;
		
		
		abstract Mat3 calcRotation(float fElapsedTime);
		
		Mat4 constructMatrix(float fElapsedTime) {
			final Mat3 rotMatrix = calcRotation(fElapsedTime);
			Mat4 theMat = new Mat4(rotMatrix);
			theMat.setColumn(3, new Vec4(offset, 1.0f));
			
			return theMat;
		}
	}
	
	
	private class NullRotation extends Instance {

		public NullRotation(Vec3 vec) {
			offset = vec;
		}


		@Override
		Mat3 calcRotation(float fElapsedTime) {
			return new Mat3(1.0f);
		}
	}
	
	private class RotateX extends Instance {

		public RotateX(Vec3 vec) {
			offset = vec;
		}


		@Override
		Mat3 calcRotation(float fElapsedTime) {
			float fAngRad = computeAngleRad(fElapsedTime, 3.0f);
			float fCos = (float) Math.cos(fAngRad);
			float fSin = (float) Math.sin(fAngRad);

			Mat3 theMat = new Mat3(1.0f);
			theMat.set(4, fCos); theMat.set(7, -fSin); 
			theMat.set(5, fSin); theMat.set(8, fCos); 
			return theMat;
		}
	}
	
	private class RotateY extends Instance {

		public RotateY(Vec3 vec) {
			offset = vec;
		}


		@Override
		Mat3 calcRotation(float fElapsedTime) {
			float fAngRad = computeAngleRad(fElapsedTime, 2.0f);
			float fCos = (float) Math.cos(fAngRad);
			float fSin = (float) Math.sin(fAngRad);

			Mat3 theMat = new Mat3(1.0f);
			theMat.set(0, fCos); theMat.set(6, fSin); 
			theMat.set(2, -fSin); theMat.set(8, fCos);
			return theMat;
		}
	}
	
	private class RotateZ extends Instance {

		public RotateZ(Vec3 vec) {
			offset = vec;
		}


		@Override
		Mat3 calcRotation(float fElapsedTime) {
			float fAngRad = computeAngleRad(fElapsedTime, 2.0f);
			float fCos = (float) Math.cos(fAngRad);
			float fSin = (float) Math.sin(fAngRad);

			Mat3 theMat = new Mat3(1.0f);
			theMat.set(0, fCos); theMat.set(3, -fSin); 
			theMat.set(1, fSin); theMat.set(4, fCos);
			return theMat;
		}
	}
	
	private class RotateAxis extends Instance {

		public RotateAxis(Vec3 vec) {
			offset = vec;
		}


		@Override
		Mat3 calcRotation(float fElapsedTime) {
			float fAngRad = computeAngleRad(fElapsedTime, 2.0f);
			float fCos = (float) Math.cos(fAngRad);
			float fInvCos = 1.0f - fCos;
			float fSin = (float) Math.sin(fAngRad);

			Vec3 axis = new Vec3(1.0f, 1.0f, 1.0f);
			axis = Glm.normalize(axis);

			Mat3 theMat = new Mat3(1.0f);
			theMat.set(0, (axis.get(X) * axis.get(X)) + ((1 - axis.get(X) * axis.get(X)) * fCos));
			theMat.set(3, axis.get(X) * axis.get(Y) * (fInvCos) - (axis.get(Z) * fSin));
			theMat.set(6, axis.get(X) * axis.get(Z) * (fInvCos) + (axis.get(Y) * fSin));

			theMat.set(1, axis.get(X) * axis.get(Y) * (fInvCos) + (axis.get(Z) * fSin));
			theMat.set(4, (axis.get(Y) * axis.get(Y)) + ((1 - axis.get(Y) * axis.get(Y)) * fCos));
			theMat.set(7, axis.get(Y) * axis.get(Z) * (fInvCos) - (axis.get(X) * fSin));

			theMat.set(2, axis.get(X) * axis.get(Z) * (fInvCos) - (axis.get(Y) * fSin));
			theMat.set(5, axis.get(Y) * axis.get(Z) * (fInvCos) + (axis.get(X) * fSin));
			theMat.set(8, (axis.get(Z) * axis.get(Z)) + ((1 - axis.get(Z) * axis.get(Z)) * fCos));
			return theMat;
		}
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private final float fFrustumScale = calcFrustumScale(45.0f);

	
	private float calcFrustumScale(float fFovDeg) {
		final float degToRad = 3.14159f * 2.0f / 360.0f;
		float fFovRad = fFovDeg * degToRad;
		
		return 1.0f / (float) (Math.tan(fFovRad / 2.0f));
	}
	
	
	private float computeAngleRad(float fElapsedTime, float fLoopDuration) {
		final float fScale = 3.14159f * 2.0f / fLoopDuration;
		float fCurrTimeThroughLoop = fElapsedTime % fLoopDuration;
		
		return fCurrTimeThroughLoop * fScale;
	}
}