package rosick.mckesson.tut07;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import rosick.GLWindow;
import rosick.framework.Framework;
import rosick.framework.Mesh;
import rosick.glm.Glm;
import rosick.glm.Mat4;
import rosick.glm.Vec3;
import rosick.glm.Vec4;
import rosick.glutil.MatrixStack;


/**
 * II. Positioning
 * 7. World in Motion
 * http://www.arcsynthesis.org/gltut/Positioning/Tutorial%2007.html
 * @author integeruser
 */
public class WorldScene01 extends GLWindow {

	public static void main(String[] args) {		
		new WorldScene01().start(1024, 768);
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private static class ProgramData {
		int theProgram;
		int modelToWorldMatrixUnif;
		int worldToCameraMatrixUnif;
		int cameraToClipMatrixUnif;
		int baseColorUnif;
	}
	
	private float g_fzNear = 1.0f;
	private float g_fzFar = 1000.0f;

	private ProgramData uniformColor;
	private ProgramData objectColor;
	private ProgramData uniformColorTint;
	
	private Mesh g_pConeMesh;
	private Mesh g_pCylinderMesh;
	private Mesh g_pCubeTintMesh;
	private Mesh g_pCubeColorMesh;
	private Mesh g_pPlaneMesh;

	private MatrixStack camMatrix = new MatrixStack(); 
	private MatrixStack	modelMatrix = new MatrixStack();
	private MatrixStack	persMatrix = new MatrixStack();
	private FloatBuffer tempSharedBuffer = BufferUtils.createFloatBuffer(16);



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	@Override
	protected void init() {
		initializeProgram();

		try {
			g_pConeMesh 		= new Mesh("/rosick/mckesson/data/tut07/UnitConeTint.xml");
			g_pCylinderMesh 	= new Mesh("/rosick/mckesson/data/tut07/UnitCylinderTint.xml");
			g_pCubeTintMesh 	= new Mesh("/rosick/mckesson/data/tut07/UnitCubeTint.xml");
			g_pCubeColorMesh 	= new Mesh("/rosick/mckesson/data/tut07/UnitCubeColor.xml");
			g_pPlaneMesh 		= new Mesh("/rosick/mckesson/data/tut07/UnitPlane.xml");
		} catch (Exception exception) {
			System.err.println(exception.getMessage());
			System.exit(0);
		}

		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);

		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(0.0f, 1.0f);
		glEnable(GL_DEPTH_CLAMP);
	}

	private ProgramData loadProgram(String strVertexShader, String strFragmentShader) {		
		int vertexShader =	 	Framework.loadShader(GL_VERTEX_SHADER, 		strVertexShader);
		int fragmentShader = 	Framework.loadShader(GL_FRAGMENT_SHADER,	strFragmentShader);

		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add(vertexShader);
		shaderList.add(fragmentShader);

		ProgramData data = new ProgramData();
		data.theProgram = Framework.createProgram(shaderList);
		data.modelToWorldMatrixUnif = glGetUniformLocation(data.theProgram, "modelToWorldMatrix");
		data.worldToCameraMatrixUnif = glGetUniformLocation(data.theProgram, "worldToCameraMatrix");
		data.cameraToClipMatrixUnif = glGetUniformLocation(data.theProgram, "cameraToClipMatrix");
		data.baseColorUnif = glGetUniformLocation(data.theProgram, "baseColor");

		return data;
	}

	private void initializeProgram() {
		uniformColor = 		loadProgram("/rosick/mckesson/data/tut07/posOnlyWorldTransform.vert", 	"/rosick/mckesson/data/tut07/colorUniform.frag");
		objectColor = 		loadProgram("/rosick/mckesson/data/tut07/posColorWorldTransform.vert", 	"/rosick/mckesson/data/tut07/colorPassthrough.frag");
		uniformColorTint = 	loadProgram("/rosick/mckesson/data/tut07/posColorWorldTransform.vert", 	"/rosick/mckesson/data/tut07/colorMultUniform.frag");
	}


	@Override
	protected void update() {
		lastFrameDuration *= 5;
		
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			g_camTarget.x -= 11.25f * lastFrameDuration;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			g_camTarget.x += 11.25f * lastFrameDuration;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			g_camTarget.z -= 11.25f * lastFrameDuration;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			g_camTarget.z += 11.25f * lastFrameDuration;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			g_camTarget.y += 11.25f * lastFrameDuration;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			g_camTarget.y -= 11.25f * lastFrameDuration;
		}


		if (Keyboard.isKeyDown(Keyboard.KEY_J)) {
			g_sphereCamRelPos.x -= 11.25f * lastFrameDuration;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
			g_sphereCamRelPos.x += 11.25f * lastFrameDuration;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
			g_sphereCamRelPos.y -= 11.25f * lastFrameDuration;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
			g_sphereCamRelPos.y += 11.25f * lastFrameDuration;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_U)) {
			g_sphereCamRelPos.z += 11.25f * lastFrameDuration;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
			g_sphereCamRelPos.z -= 11.25f * lastFrameDuration;
		}

		
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			leaveMainLoop();
		}
		
		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
					g_bDrawLookatPoint = !g_bDrawLookatPoint;
				}
			}
		}
		
		
		g_sphereCamRelPos.y = Glm.clamp(g_sphereCamRelPos.y, -78.75f, -1.0f);
		g_camTarget.y = g_camTarget.y > 0.0f ? g_camTarget.y : 0.0f;
		g_sphereCamRelPos.z = g_sphereCamRelPos.z > 5.0f ? g_sphereCamRelPos.z : 5.0f;
	}


	@Override
	protected void render() {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		{
			Vec3 camPos = resolveCamPosition();
	
			camMatrix.clear(calcLookAtMatrix(camPos, g_camTarget, new Vec3(0.0f, 1.0f, 0.0f)));
			
			glUseProgram(uniformColor.theProgram);
			glUniformMatrix4(uniformColor.worldToCameraMatrixUnif, false, camMatrix.top().fillBuffer(tempSharedBuffer));
			glUseProgram(objectColor.theProgram);
			glUniformMatrix4(objectColor.worldToCameraMatrixUnif, false, camMatrix.top().fillBuffer(tempSharedBuffer));
			glUseProgram(uniformColorTint.theProgram);
			glUniformMatrix4(uniformColorTint.worldToCameraMatrixUnif, false, camMatrix.top().fillBuffer(tempSharedBuffer));
			glUseProgram(0);
	
			modelMatrix.clear();

			// Render the ground plane.
			{
				modelMatrix.push();

				modelMatrix.scale(100.0f, 1.0f, 100.0f);
			
				glUseProgram(uniformColor.theProgram);
				glUniformMatrix4(uniformColor.modelToWorldMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedBuffer));
				glUniform4f(uniformColor.baseColorUnif, 0.302f, 0.416f, 0.0589f, 1.0f);
				g_pPlaneMesh.render();
				glUseProgram(0);
				
				modelMatrix.pop();
			}

			// Draw the trees
			drawForest(modelMatrix);

			// Draw the building.
			{
				modelMatrix.push();
				
				modelMatrix.translate(20.0f, 0.0f, -10.0f);

				drawParthenon(modelMatrix);
				
				modelMatrix.pop();
			}
					
			if (g_bDrawLookatPoint) {
				glDisable(GL_DEPTH_TEST);
				
				Mat4 identity = new Mat4(1.0f);

				{
					modelMatrix.push();
	
					Vec3 cameraAimVec = Vec3.sub(g_camTarget, camPos);
					modelMatrix.translate(0.0f, 0.0f, - Glm.length(cameraAimVec));
					modelMatrix.scale(1.0f, 1.0f, 1.0f);
					
					glUseProgram(objectColor.theProgram);
					glUniformMatrix4(objectColor.modelToWorldMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedBuffer));
					glUniformMatrix4(objectColor.worldToCameraMatrixUnif, false, identity.fillBuffer(tempSharedBuffer));
					g_pCubeColorMesh.render();
					glUseProgram(0);

					modelMatrix.pop();
				}
				
				glEnable(GL_DEPTH_TEST);
			}
		}
	}


	@Override
	protected void reshape(int width, int height) {
		persMatrix.clear();
		persMatrix.perspective(45.0f, (width / (float) height), g_fzNear, g_fzFar);

		glUseProgram(uniformColor.theProgram);
		glUniformMatrix4(uniformColor.cameraToClipMatrixUnif, false, persMatrix.top().fillBuffer(tempSharedBuffer));
		glUseProgram(objectColor.theProgram);
		glUniformMatrix4(objectColor.cameraToClipMatrixUnif, false, persMatrix.top().fillBuffer(tempSharedBuffer));
		glUseProgram(uniformColorTint.theProgram);
		glUniformMatrix4(uniformColorTint.cameraToClipMatrixUnif, false, persMatrix.top().fillBuffer(tempSharedBuffer));
		glUseProgram(0);
	}

	

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */		

	// Trees are 3x3 in X/Z, and fTrunkHeight + fConeHeight in the Y.
	private void drawTree(MatrixStack modelMatrix, float fTrunkHeight, float fConeHeight) {
		// Draw trunk.
		{
			modelMatrix.push();
			
			modelMatrix.scale(1.0f, fTrunkHeight, 1.0f);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			glUseProgram(uniformColorTint.theProgram);
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedBuffer));
			glUniform4f(uniformColorTint.baseColorUnif, 0.694f, 0.4f, 0.106f, 1.0f);
			g_pCylinderMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}

		// Draw the treetop
		{
			modelMatrix.push();
			
			modelMatrix.translate(0.0f, fTrunkHeight, 0.0f);
			modelMatrix.scale(3.0f, fConeHeight, 3.0f);

			glUseProgram(uniformColorTint.theProgram);
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedBuffer));
			glUniform4f(uniformColorTint.baseColorUnif, 0.0f, 1.0f, 0.0f, 1.0f);
			g_pConeMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	

	private final float g_fColumnBaseHeight = 0.25f;


	// Columns are 1x1 in the X/Z, and fHieght units in the Y.
	private void drawColumn(MatrixStack modelMatrix, float fHeight) {
		// Draw the bottom of the column.
		{
			modelMatrix.push();
			
			modelMatrix.scale(1.0f, g_fColumnBaseHeight, 1.0f);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			glUseProgram(uniformColorTint.theProgram);
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedBuffer));
			glUniform4f(uniformColorTint.baseColorUnif, 1.0f, 1.0f, 1.0f, 1.0f);
			g_pCubeTintMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}

		// Draw the top of the column.
		{
			modelMatrix.push();
			
			modelMatrix.translate(0.0f, fHeight - g_fColumnBaseHeight, 0.0f);
			modelMatrix.scale(1.0f, g_fColumnBaseHeight, 1.0f);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			glUseProgram(uniformColorTint.theProgram);
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedBuffer));
			glUniform4f(uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f);
			g_pCubeTintMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}

		// Draw the main column.
		{
			modelMatrix.push();
			
			modelMatrix.translate(0.0f, g_fColumnBaseHeight, 0.0f);
			modelMatrix.scale(0.8f, fHeight - (g_fColumnBaseHeight * 2.0f), 0.8f);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			glUseProgram(uniformColorTint.theProgram);
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedBuffer));
			glUniform4f(uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f);
			g_pCylinderMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	private final float g_fParthenonWidth = 14.0f;
	private final float g_fParthenonLength = 20.0f;
	private final float g_fParthenonColumnHeight = 5.0f;
	private final float g_fParthenonBaseHeight = 1.0f;
	private final float g_fParthenonTopHeight = 2.0f;

	private final float fFrontZVal = (g_fParthenonLength / 2.0f) - 1.0f;
	private final float fRightXVal = (g_fParthenonWidth / 2.0f) - 1.0f;
	private final int max1 = (int) (g_fParthenonWidth / 2.0f);
	private final int max2 = (int) ((g_fParthenonLength - 2.0f) / 2.0f);
	

	private void drawParthenon(MatrixStack modelMatrix) {
		// Draw base.
		{
			modelMatrix.push();

			modelMatrix.scale(g_fParthenonWidth, g_fParthenonBaseHeight, g_fParthenonLength);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			glUseProgram(uniformColorTint.theProgram);
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedBuffer));
			glUniform4f(uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f);
			g_pCubeTintMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}

		// Draw top.
		{
			modelMatrix.push();

			modelMatrix.translate(0.0f, g_fParthenonColumnHeight + g_fParthenonBaseHeight, 0.0f);
			modelMatrix.scale(g_fParthenonWidth, g_fParthenonTopHeight, g_fParthenonLength);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			glUseProgram(uniformColorTint.theProgram);
			glUniformMatrix4(uniformColorTint.modelToWorldMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedBuffer));
			glUniform4f(uniformColorTint.baseColorUnif, 0.9f, 0.9f, 0.9f, 0.9f);
			g_pCubeTintMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}

		// Draw columns.
		for (int iColumnNum = 0; iColumnNum < max1; iColumnNum++) {
			{
				modelMatrix.push();
				
				modelMatrix.translate((2.0f * iColumnNum) - (g_fParthenonWidth / 2.0f) + 1.0f, g_fParthenonBaseHeight, fFrontZVal);

				drawColumn(modelMatrix, g_fParthenonColumnHeight);
				
				modelMatrix.pop();
			}
			{
				modelMatrix.push();
				
				modelMatrix.translate((2.0f * iColumnNum) - (g_fParthenonWidth / 2.0f) + 1.0f, g_fParthenonBaseHeight, -fFrontZVal);

				drawColumn(modelMatrix, g_fParthenonColumnHeight);
				
				modelMatrix.pop();
			}
		}

		// Don't draw the first or last columns, since they've been drawn already.
		for (int iColumnNum = 1; iColumnNum < max2; iColumnNum++) {
			{
				modelMatrix.push();
				
				modelMatrix.translate(fRightXVal, g_fParthenonBaseHeight, (2.0f * iColumnNum) - (g_fParthenonLength / 2.0f) + 1.0f);

				drawColumn(modelMatrix, g_fParthenonColumnHeight);
				
				modelMatrix.pop();
			}
			{
				modelMatrix.push();
				
				modelMatrix.translate(-fRightXVal, g_fParthenonBaseHeight, (2.0f * iColumnNum) - (g_fParthenonLength / 2.0f) + 1.0f);

				drawColumn(modelMatrix, g_fParthenonColumnHeight);
				
				modelMatrix.pop();
			}
		}

		// Draw interior.
		{
			modelMatrix.push();

			modelMatrix.translate(0.0f, 1.0f, 0.0f);
			modelMatrix.scale(g_fParthenonWidth - 6.0f, g_fParthenonColumnHeight, g_fParthenonLength - 6.0f);
			modelMatrix.translate(0.0f, 0.5f, 0.0f);

			glUseProgram(objectColor.theProgram);
			glUniformMatrix4(objectColor.modelToWorldMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedBuffer));
			g_pCubeColorMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}

		// Draw headpiece.
		{
			modelMatrix.push();

			modelMatrix.translate(0.0f, g_fParthenonColumnHeight + g_fParthenonBaseHeight + (g_fParthenonTopHeight / 2.0f),	g_fParthenonLength / 2.0f);
			modelMatrix.rotateX(-135.0f);
			modelMatrix.rotateY(45.0f);

			glUseProgram(objectColor.theProgram);
			glUniformMatrix4(objectColor.modelToWorldMatrixUnif, false, modelMatrix.top().fillBuffer(tempSharedBuffer));
			g_pCubeColorMesh.render();
			glUseProgram(0);
			
			modelMatrix.pop();
		}
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	

	private static class TreeData {
		float fXPos;
		float fZPos;
		float fTrunkHeight;
		float fConeHeight;

		public TreeData(float fXPos, float fZPos, float fTrunkHeight, float fConeHeight) {
			this.fXPos = fXPos;
			this.fZPos = fZPos;
			this.fTrunkHeight = fTrunkHeight;
			this.fConeHeight = fConeHeight;
		}
	}


	private static final TreeData g_forest[] = {
		new TreeData (-45.0f, -40.0f, 2.0f, 3.0f),
		new TreeData (-42.0f, -35.0f, 2.0f, 3.0f),
		new TreeData (-39.0f, -29.0f, 2.0f, 4.0f),
		new TreeData (-44.0f, -26.0f, 3.0f, 3.0f),
		new TreeData (-40.0f, -22.0f, 2.0f, 4.0f),
		new TreeData (-36.0f, -15.0f, 3.0f, 3.0f),
		new TreeData (-41.0f, -11.0f, 2.0f, 3.0f),
		new TreeData (-37.0f, -6.0f, 3.0f, 3.0f),
		new TreeData (-45.0f, 0.0f, 2.0f, 3.0f),
		new TreeData (-39.0f, 4.0f, 3.0f, 4.0f),
		new TreeData (-36.0f, 8.0f, 2.0f, 3.0f),
		new TreeData (-44.0f, 13.0f, 3.0f, 3.0f),
		new TreeData (-42.0f, 17.0f, 2.0f, 3.0f),
		new TreeData (-38.0f, 23.0f, 3.0f, 4.0f),
		new TreeData (-41.0f, 27.0f, 2.0f, 3.0f),
		new TreeData (-39.0f, 32.0f, 3.0f, 3.0f),
		new TreeData (-44.0f, 37.0f, 3.0f, 4.0f),
		new TreeData (-36.0f, 42.0f, 2.0f, 3.0f),

		new TreeData (-32.0f, -45.0f, 2.0f, 3.0f),
		new TreeData (-30.0f, -42.0f, 2.0f, 4.0f),
		new TreeData (-34.0f, -38.0f, 3.0f, 5.0f),
		new TreeData (-33.0f, -35.0f, 3.0f, 4.0f),
		new TreeData (-29.0f, -28.0f, 2.0f, 3.0f),
		new TreeData (-26.0f, -25.0f, 3.0f, 5.0f),
		new TreeData (-35.0f, -21.0f, 3.0f, 4.0f),
		new TreeData (-31.0f, -17.0f, 3.0f, 3.0f),
		new TreeData (-28.0f, -12.0f, 2.0f, 4.0f),
		new TreeData (-29.0f, -7.0f, 3.0f, 3.0f),
		new TreeData (-26.0f, -1.0f, 2.0f, 4.0f),
		new TreeData (-32.0f, 6.0f, 2.0f, 3.0f),
		new TreeData (-30.0f, 10.0f, 3.0f, 5.0f),
		new TreeData (-33.0f, 14.0f, 2.0f, 4.0f),
		new TreeData (-35.0f, 19.0f, 3.0f, 4.0f),
		new TreeData (-28.0f, 22.0f, 2.0f, 3.0f),
		new TreeData (-33.0f, 26.0f, 3.0f, 3.0f),
		new TreeData (-29.0f, 31.0f, 3.0f, 4.0f),
		new TreeData (-32.0f, 38.0f, 2.0f, 3.0f),
		new TreeData (-27.0f, 41.0f, 3.0f, 4.0f),
		new TreeData (-31.0f, 45.0f, 2.0f, 4.0f),
		new TreeData (-28.0f, 48.0f, 3.0f, 5.0f),

		new TreeData (-25.0f, -48.0f, 2.0f, 3.0f),
		new TreeData (-20.0f, -42.0f, 3.0f, 4.0f),
		new TreeData (-22.0f, -39.0f, 2.0f, 3.0f),
		new TreeData (-19.0f, -34.0f, 2.0f, 3.0f),
		new TreeData (-23.0f, -30.0f, 3.0f, 4.0f),
		new TreeData (-24.0f, -24.0f, 2.0f, 3.0f),
		new TreeData (-16.0f, -21.0f, 2.0f, 3.0f),
		new TreeData (-17.0f, -17.0f, 3.0f, 3.0f),
		new TreeData (-25.0f, -13.0f, 2.0f, 4.0f),
		new TreeData (-23.0f, -8.0f, 2.0f, 3.0f),
		new TreeData (-17.0f, -2.0f, 3.0f, 3.0f),
		new TreeData (-16.0f, 1.0f, 2.0f, 3.0f),
		new TreeData (-19.0f, 4.0f, 3.0f, 3.0f),
		new TreeData (-22.0f, 8.0f, 2.0f, 4.0f),
		new TreeData (-21.0f, 14.0f, 2.0f, 3.0f),
		new TreeData (-16.0f, 19.0f, 2.0f, 3.0f),
		new TreeData (-23.0f, 24.0f, 3.0f, 3.0f),
		new TreeData (-18.0f, 28.0f, 2.0f, 4.0f),
		new TreeData (-24.0f, 31.0f, 2.0f, 3.0f),
		new TreeData (-20.0f, 36.0f, 2.0f, 3.0f),
		new TreeData (-22.0f, 41.0f, 3.0f, 3.0f),
		new TreeData (-21.0f, 45.0f, 2.0f, 3.0f),

		new TreeData (-12.0f, -40.0f, 2.0f, 4.0f),
		new TreeData (-11.0f, -35.0f, 3.0f, 3.0f),
		new TreeData (-10.0f, -29.0f, 1.0f, 3.0f),
		new TreeData (-9.0f, -26.0f, 2.0f, 2.0f),
		new TreeData (-6.0f, -22.0f, 2.0f, 3.0f),
		new TreeData (-15.0f, -15.0f, 1.0f, 3.0f),
		new TreeData (-8.0f, -11.0f, 2.0f, 3.0f),
		new TreeData (-14.0f, -6.0f, 2.0f, 4.0f),
		new TreeData (-12.0f, 0.0f, 2.0f, 3.0f),
		new TreeData (-7.0f, 4.0f, 2.0f, 2.0f),
		new TreeData (-13.0f, 8.0f, 2.0f, 2.0f),
		new TreeData (-9.0f, 13.0f, 1.0f, 3.0f),
		new TreeData (-13.0f, 17.0f, 3.0f, 4.0f),
		new TreeData (-6.0f, 23.0f, 2.0f, 3.0f),
		new TreeData (-12.0f, 27.0f, 1.0f, 2.0f),
		new TreeData (-8.0f, 32.0f, 2.0f, 3.0f),
		new TreeData (-10.0f, 37.0f, 3.0f, 3.0f),
		new TreeData (-11.0f, 42.0f, 2.0f, 2.0f),


		new TreeData (15.0f, 5.0f, 2.0f, 3.0f),
		new TreeData (15.0f, 10.0f, 2.0f, 3.0f),
		new TreeData (15.0f, 15.0f, 2.0f, 3.0f),
		new TreeData (15.0f, 20.0f, 2.0f, 3.0f),
		new TreeData (15.0f, 25.0f, 2.0f, 3.0f),
		new TreeData (15.0f, 30.0f, 2.0f, 3.0f),
		new TreeData (15.0f, 35.0f, 2.0f, 3.0f),
		new TreeData (15.0f, 40.0f, 2.0f, 3.0f),
		new TreeData (15.0f, 45.0f, 2.0f, 3.0f),

		new TreeData (25.0f, 5.0f, 2.0f, 3.0f),
		new TreeData (25.0f, 10.0f, 2.0f, 3.0f),
		new TreeData (25.0f, 15.0f, 2.0f, 3.0f),
		new TreeData (25.0f, 20.0f, 2.0f, 3.0f),
		new TreeData (25.0f, 25.0f, 2.0f, 3.0f),
		new TreeData (25.0f, 30.0f, 2.0f, 3.0f),
		new TreeData (25.0f, 35.0f, 2.0f, 3.0f),
		new TreeData (25.0f, 40.0f, 2.0f, 3.0f),
		new TreeData (25.0f, 45.0f, 2.0f, 3.0f),
	};


	private void drawForest(MatrixStack modelMatrix) {
		for (TreeData currTree : g_forest) {
			modelMatrix.push();

			modelMatrix.translate(currTree.fXPos, 0.0f, currTree.fZPos);
			
			drawTree(modelMatrix, currTree.fTrunkHeight, currTree.fConeHeight);
			
			modelMatrix.pop();
		}
	}



	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	

	private static boolean g_bDrawLookatPoint = false;
	private static Vec3 g_camTarget = new Vec3(0.0f, 0.4f, 0.0f);

	// In spherical coordinates.
	private static Vec3 g_sphereCamRelPos = new Vec3(67.5f, -46.0f, 150.0f);
	
	
	private Vec3 resolveCamPosition() {
		float phi = Framework.degToRad(g_sphereCamRelPos.x);
		float theta = Framework.degToRad(g_sphereCamRelPos.y + 90.0f);

		float fSinTheta = (float) Math.sin(theta);
		float fCosTheta = (float) Math.cos(theta);
		float fCosPhi = (float) Math.cos(phi);
		float fSinPhi = (float) Math.sin(phi);

		Vec3 dirToCamera = new Vec3(fSinTheta * fCosPhi, fCosTheta, fSinTheta * fSinPhi);
		
		return (dirToCamera.scale(g_sphereCamRelPos.z)).add(g_camTarget);
	}
	
	
	private Mat4 calcLookAtMatrix(Vec3 cameraPt, Vec3 lookPt, Vec3 upPt) {
		Vec3 lookDir = Glm.normalize(Vec3.sub(lookPt, cameraPt));
		Vec3 upDir = Glm.normalize(upPt);

		Vec3 rightDir = Glm.normalize(Glm.cross(lookDir, upDir));
		Vec3 perpUpDir = Glm.cross(rightDir, lookDir);

		Mat4 rotMat = new Mat4(1.0f);
		rotMat.putColumn(0, new Vec4(rightDir, 0.0f));
		rotMat.putColumn(1, new Vec4(perpUpDir, 0.0f));
		rotMat.putColumn(2, new Vec4(Vec3.negate(lookDir), 0.0f));

		rotMat = Glm.transpose(rotMat);

		Mat4 transMat = new Mat4(1.0f);
		transMat.putColumn(3, new Vec4(Vec3.negate(cameraPt), 1.0f));

		rotMat.mul(transMat);
		
		return rotMat;
	}
}