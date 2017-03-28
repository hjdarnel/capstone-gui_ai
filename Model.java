import java.awt.Graphics;
import java.io.File;
import java.util.Random;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import javax.imageio.ImageIO;



class Model {
	public static final float EPSILON = 0.0001f; // A small number
	public static final float XMAX = 1200.0f - EPSILON; // The maximum horizontal screen position. (The minimum is 0.)
	public static final float YMAX = 600.0f - EPSILON; // The maximum vertical screen position. (The minimum is 0.)

	private Controller controller;
	private byte[] terrain;
	private ArrayList<Sprite> sprites;

	public float maxTravelSpeed;
	
	public static List<Integer> obstacles = new ArrayList<Integer>();
	
	Model(Controller c) {
		this.controller = c;
	}

	void initGame() throws Exception {
		BufferedImage bufferedImage = ImageIO.read(new File("terrain.png"));
		if(bufferedImage.getWidth() != 60 || bufferedImage.getHeight() != 60)
			throw new Exception("Expected the terrain image to have dimensions of 60-by-60");
		terrain = ((DataBufferByte)bufferedImage.getRaster().getDataBuffer()).getData();
		sprites = new ArrayList<Sprite>();
		sprites.add(new Sprite(100, 100));
		maxTravelSpeed = getMaxTravelSpeed();
		getObstacles();
	}

	// These methods are used internally. They are not useful to the agents.
	byte[] getTerrain() { return this.terrain; }
	ArrayList<Sprite> getSprites() { return this.sprites; }

	void update() {
		// Update the agents
		for(int i = 0; i < sprites.size(); i++)
			sprites.get(i).update();
	}

	// 0 <= x < MAP_WIDTH.
	// 0 <= y < MAP_HEIGHT.
	float getTravelSpeed(float x, float y) {
			int xx = (int)(x * 0.1f);
			int yy = (int)(y * 0.1f);
			if(xx >= 60)
			{
				xx = 119 - xx;
				yy = 59 - yy;
			}
			int pos = 4 * (60 * yy + xx);
			//return Math.max(0.2f, Math.min(3.5f, -0.01f * (terrain[pos + 1] & 0xff) + 0.02f * (terrain[pos + 3] & 0xff)));
			return Math.max(0.2f, Math.min(3.5f, + 0.02f * (terrain[pos + 1] & 0xff) - 0.02f * (terrain[pos + 2] & 0xff)));
	}

	int getGreenColor(float x, float y) {
		int xx = (int)(x * 0.1f);
		int yy = (int)(y * 0.1f);
		if(xx >= 60)
		{
			xx = 119 - xx;
			yy = 59 - yy;
		}
		int pos = 4 * (60 * yy + xx);
		//return Math.max(0.2f, Math.min(3.5f, -0.01f * (terrain[pos + 1] & 0xff) + 0.02f * (terrain[pos + 3] & 0xff)));
		return terrain[pos + 2] & 0xff;
}

	float getMaxTravelSpeed(){
		float maxTravelSpeed = 0;
		float travelSpeed;
		for(int i =0; i < 1200; i++){
			for(int j=0; j < 600; j++){
				travelSpeed = getTravelSpeed(i,j);
				if(travelSpeed > maxTravelSpeed) maxTravelSpeed = travelSpeed;
			}
		}
		return maxTravelSpeed;
	}
	
	void getObstacles(){
		for(int s =0; s < 7200; s++){
			if(isObstacle(s)) obstacles.add(s);
		}
	}
	
	boolean isObstacle(int state){
		int x = controller.agent.stateX(state);
		int y = controller.agent.stateY(state);
		for(int i =x; i < x+10; i++){
			for(int j=y; j < y+10; j++){
				if(getGreenColor(i,j) > 150) return true;;
			}
		}
		return false;
	}
	
	Controller getController() { return controller; }
	float getX() { return sprites.get(0).x; }
	float getY() { return sprites.get(0).y; }
	float getDestinationX() { return sprites.get(0).xDestination; }
	float getDestinationY() { return sprites.get(0).yDestination; }
	
	void setDestination(float x, float y) {
		Sprite s = sprites.get(0);
		s.xDestination = x;
		s.yDestination = y;
	}

	void setPosition(float x, float y) {
		Sprite s = sprites.get(0);
		s.x = x;
		s.y = y;
	}

	
	void setAngle() {
		Sprite s = sprites.get(0);
		s.angle = Math.atan2(s.yDestination - s.y, s.xDestination - s.x);
	}

	/*void setStart(float x, float y) {
		Sprite s = sprites.get(0);
		s.x = x;
		s.y = y;
	}*/
	
	double getDistanceToDestination(int sprite) {
		Sprite s = sprites.get(sprite);
		return Math.sqrt((s.x - s.xDestination) * (s.x - s.xDestination) + (s.y - s.yDestination) * (s.y - s.yDestination));
	}

	class Sprite {
		float x;
		float y;
		float xDestination;
		float yDestination;
		double angle;

		Sprite(float x, float y) {
			this.x = x;
			this.y = y;
			this.xDestination = x;
			this.yDestination = y;
		}

		void update() {
			
			
			//float speed = Model.this.getTravelSpeed(this.x, this.y);
			//int state = controller.agent.XYtoState((int)this.x,(int)this.y);
			//float speed = Model.this.getTravelSpeed(controller.agent.stateX(state), controller.agent.stateY(state));
			float speed = Model.this.getTravelSpeed((int)this.xDestination, (int)this.yDestination);
			float dx = this.xDestination - this.x;
			float dy = this.yDestination - this.y;
			float dist = (float)Math.sqrt(dx * dx + dy * dy);
			float t = speed / Math.max(speed, dist);
			dx *= t;
			dy *= t;
			this.x += dx;
			this.y += dy;
			this.x = Math.max(0.0f, Math.min(XMAX, this.x));
			this.y = Math.max(0.0f, Math.min(YMAX, this.y));
			

			if(Math.abs(this.x - this.xDestination) <=1 && Math.abs(this.y - this.yDestination) <=1 ){
				int curState =  controller.agent.XYtoState((int)this.xDestination,(int)this.yDestination);			
				int nextState;
				//System.out.println(controller.agent.states.size());
				if(controller.agent.states.contains(curState)){
					this.x = (float)controller.agent.stateX(curState);
					this.y = (float)controller.agent.stateY(curState);				
					int idx = controller.agent.states.indexOf(curState);				
					//System.out.println(idx);
					if(idx > 0){
						nextState = controller.agent.states.get(idx-1);
						setDestination((float)controller.agent.stateX(nextState), (float)controller.agent.stateY(nextState));
						setAngle();
					}
				}				
			}			
			
		}
	}
}
