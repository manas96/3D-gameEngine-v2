package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import fontRendering.TextMaster;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.RawModel;
import models.TexturedModel;
import normalMappingObjConverter.NormalMappedObjLoader;
import objConverter.ModelData;
import objConverter.OBJFileLoader;
import particles.ParticleMaster;
import particles.ParticleSystem;
import particles.ParticleTexture;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.HeightMapTerrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.MousePicker;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;

public class MainGameLoop {
	 
    public static void main(String[] args) {
 
        DisplayManager.createDisplay();
        Loader loader = new Loader();
        TextMaster.init(loader); 
        MasterRenderer renderer = new MasterRenderer(loader);
        ParticleMaster.init(loader, renderer.getProjectionMatrix());
        
//        FontType font = new FontType(loader.loadFontTextureAtlas("candara"),
//        		new File("res/candara.fnt"));
//        GUIText text = new GUIText("This is a test text!", 3, font, new Vector2f(0.0f,0.4f), 1f, true);
//        
        
        //---------terrain texture-----------------
        TerrainTexture backgroundTexture = new TerrainTexture(loader.loadGameTexture("grassy"));
        TerrainTexture rTexture = new TerrainTexture(loader.loadGameTexture("dirt"));
        TerrainTexture gTexture = new TerrainTexture(loader.loadGameTexture("pinkFlowers"));
        TerrainTexture bTexture = new TerrainTexture(loader.loadGameTexture("path"));

        TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture,
        		rTexture,gTexture,bTexture);
        TerrainTexture blendMap = new TerrainTexture(loader.loadGameTexture("blendMap"));
        
        HeightMapTerrain terrain = new HeightMapTerrain(0,-1,loader, texturePack, blendMap, "heightmap");
        //    Terrain terrain2 = new Terrain(-1,-1,loader,texturePack,blendMap, "heightmap");
              
        //---------------------------------------------
        List<HeightMapTerrain> terrains = new ArrayList<HeightMapTerrain>();
        terrains.add(terrain);
        
        
        ModelData data = OBJFileLoader.loadOBJ("tree"); 
        
        RawModel treeModel = loader.loadToVAO(data.getVertices(), 
        		data.getTextureCoords(), data.getNormals(), data.getIndices());
         
        TexturedModel staticModelTree = new TexturedModel(treeModel,
        		new ModelTexture(loader.loadGameTexture("tree")));
        
        TexturedModel box = new TexturedModel(OBJLoader.loadObjModel("lowPolyTree", loader),
        		new ModelTexture(loader.loadGameTexture("lowPolyTree")));
        
        TexturedModel flower = new TexturedModel(OBJLoader.loadObjModel("fern", loader),
        		new ModelTexture(loader.loadGameTexture("flower")));
     
        
        ModelTexture fernTextureAtlas = new ModelTexture(loader.loadGameTexture("fern"));
        fernTextureAtlas.setNumberOfRows(2);
        
        TexturedModel fern = new TexturedModel(OBJLoader.loadObjModel("fern", loader),
        		fernTextureAtlas);
       
        TexturedModel lamp = new TexturedModel(OBJLoader.loadObjModel("lamp", loader),
        		new ModelTexture(loader.loadGameTexture("lamp")));
        lamp.getTexture().setUseFakeLighting(true);
        
        flower.getTexture().setHasTransparency(true);
        flower.getTexture().setUseFakeLighting(true);
        fern.getTexture().setHasTransparency(true);
        
        List<Entity> entities = new ArrayList<Entity>();
        List<Entity> normalMapEntities = new ArrayList<Entity>();
        
        TexturedModel barrelModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("barrel", loader),
        		new ModelTexture(loader.loadGameTexture("barrel")));
        barrelModel.getTexture().setNormalMap(loader.loadGameTexture("barrelNormal"));
        barrelModel.getTexture().setShineDamper(10);
        barrelModel.getTexture().setReflectivity(0.5f);
        normalMapEntities.add(new Entity(barrelModel,
        		new Vector3f(190, 5, -185),0 ,0 ,0 ,1f));
        
        Random random = new Random(21);
        for(int i=0;i<500;i++){
        	
        if(i%2 == 0)
        {
        	float x = random.nextFloat()*800 ;
        	float z = random.nextFloat() * -600;
        	float y = terrain.getHeightOfTerrain(x, z);
        	
        	entities.add(new Entity(fern, random.nextInt(4), new Vector3f(x,y,z),
        			0,random.nextFloat() * 360, 0 , 0.6f));
        }	
        else{
        	float x = random.nextFloat()*800 ;
        	float z = random.nextFloat() * -600;
        	float y = terrain.getHeightOfTerrain(x, z);
            entities.add(new Entity(staticModelTree, new Vector3f(x,y,z),
            		0,0,0,3));
            x = random.nextFloat()*800;
        	z = random.nextFloat() * -600;
        	y = terrain.getHeightOfTerrain(x, z);
            entities.add(new Entity(box, new Vector3f(x,y,z),
            		0,0,0,1f));
            x = random.nextFloat()*800;
        	z = random.nextFloat() * -600;
        	y = terrain.getHeightOfTerrain(x, z);
            entities.add(new Entity(flower, new Vector3f(x,y,z),
            		0,0,0,0.6f));
        }
        }
         
        List<Light> lights = new ArrayList<Light>();
        lights.add(new Light(new Vector3f(0,1000,-7000),new Vector3f(0.4f,0.4f,0.4f)));
       // lights.add(new Light(new Vector3f(185,terrain.getHeightOfTerrain(185, -293)+16,-293),new Vector3f(2,0,0),new Vector3f(1, 0.01f, 0.002f)));
        lights.add(new Light(new Vector3f(370,17,-300),new Vector3f(0,2,2),new Vector3f(1, 0.01f, 0.002f)));
        lights.add(new Light(new Vector3f(293,7,-305),new Vector3f(2,2,0),new Vector3f(1, 0.01f, 0.002f)));

        entities.add(new Entity(lamp,new Vector3f(185,terrain.getHeightOfTerrain(185, -293),-293),0,0,0,1 ));
        entities.add(new Entity(lamp,new Vector3f(370,terrain.getHeightOfTerrain(370, -300),-300),0,0,0,1 ));
        entities.add(new Entity(lamp,new Vector3f(293,terrain.getHeightOfTerrain(293, -305),-305),0,0,0,1 ));
         
        RawModel bunnyModel = OBJLoader.loadObjModel("person", loader);
        TexturedModel stanfordBunny = new TexturedModel(bunnyModel, new ModelTexture(
        		loader.loadGameTexture("playerTexture")));
        
        Player player = new Player(stanfordBunny, new Vector3f(153,5,-274),0,0,0,1);
        player.setScale(0.2f);
        entities.add(player);
        
        Camera camera = new Camera(player);
              
        MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);
        
        Entity lampEntity = new Entity(lamp, new Vector3f(0,0,0),0,0,0,1);
        entities.add(lampEntity);
        Light light = new Light(new Vector3f(0,0,0),
        		new Vector3f(1,1,1), new Vector3f(1,0.01f, 0.002f));
        lights.add(light);
//       max allowed lights = 4 
        
        WaterFrameBuffers buffers = new WaterFrameBuffers();
        WaterShader waterShader = new WaterShader();
        WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(),buffers);
        WaterTile water = new WaterTile(170, -180, 0);
        List<WaterTile> waters = new ArrayList<WaterTile>();
        waters.add(water);
        
      
        List<GuiTexture> guiTextures = new ArrayList<GuiTexture>();
        GuiRenderer guiRenderer = new GuiRenderer(loader);
//        GuiTexture refraction = new GuiTexture(buffers.getRefractionTexture(),new Vector2f(0.5f,0.5f),
//				 new Vector2f(0.25f, 0.25f));
//        GuiTexture reflection = new GuiTexture(buffers.getReflectionTexture(),new Vector2f(-0.5f,0.5f),
//				 new Vector2f(0.25f, 0.25f));
//        guiTextures.add(refraction);
//        guiTextures.add(reflection);
    
        
        ParticleTexture particleTexture = new ParticleTexture(loader.loadGameTexture("particleAtlas"), 4, true);
        ParticleSystem system = new ParticleSystem(particleTexture,
        		1000, 10, 0.1f, 4, 1);
        system.randomizeRotation();
        system.setDirection(new Vector3f(0,1,0), 0.1f);
//        system.setLifeError(0.1f);
//        system.setSpeedError(0.4f);
//        system.setScaleError(0.8f);
        
        while(!Display.isCloseRequested()){
            
        	player.move(terrain);
        	camera.move();
        	picker.update();
      
        	system.generateParticles(new Vector3f(153,5,-285) );
        	ParticleMaster.update(camera);
//        	Vector3f terrainPoint = picker.getCurrentTerrainPoint();
//        	if(terrainPoint != null){
//        		lampEntity.setPosition(terrainPoint);
//        		light.setPosition(new Vector3f(terrainPoint.x,terrainPoint.y+15,terrainPoint.z));
//        	}
//        	
        	GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
        	
        	//render reflection texture
        	buffers.bindReflectionFrameBuffer();
        	float distance = 2 * (camera.getPosition().y - water.getHeight());
        	camera.getPosition().y -= distance;
        	camera.invertPitch();
        	camera.invertRoll();
        	renderer.renderScene(entities,normalMapEntities, terrains, lights, camera, new Vector4f(0,1,0,-water.getHeight()+ 1f));
        	camera.getPosition().y += distance;
        	camera.invertPitch();
        	camera.invertRoll();
        	
        	//render refraction texture
        	buffers.bindRefractionFrameBuffer();
        	renderer.renderScene(entities,normalMapEntities, terrains, lights, camera, new Vector4f(0,-1,0,water.getHeight()));

        	//render to screen
        	GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
        	buffers.unbindCurrentFrameBuffer();
        	renderer.renderScene(entities,normalMapEntities, terrains, lights, camera, new Vector4f(0,-1,0,10000));
            waterRenderer.render(waters, camera, light);
            
            ParticleMaster.renderParticles(camera);
            
            guiRenderer.render(guiTextures);
            TextMaster.render();
            
            DisplayManager.updateDisplay();
        }
 
        //CleanUP :
        ParticleMaster.cleanUp();
        TextMaster.cleanUp();
        buffers.cleanUp();
        waterShader.cleanUp();
        guiRenderer.cleanUp();
        renderer.cleanUp();
        loader.cleanUp();
        DisplayManager.closeDisplay();
 
    }
 
}
