import java.util.ArrayList;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;


class Agent {

	void drawPlan(Graphics g, Model m) {
		g.setColor(Color.red);
		g.drawLine((int)m.getX(), (int)m.getY(), (int)m.getDestinationX(), (int)m.getDestinationY());
		
		// draw the optimal path 
		ListIterator<Integer> li = states.listIterator(states.size());
		// Iterate in reverse.
		int startState = 0;
		boolean initalState = true;
		int theState;
		float travelSpeed;
		while(li.hasPrevious()) {
		  theState = li.previous();
		  if(initalState){
		  startState = theState;
		  initalState = false;
		  }
		  g.drawLine(stateX(startState),stateY(startState),stateX(theState),stateY(theState));
		  startState = theState;		  		  		  
		}
/*		
		// draw the frontier points when goal found
		ListIterator<Integer> liF = frontStates.listIterator(frontStates.size());
		// Iterate in reverse
		int frontPoint;
		while(liF.hasPrevious()){
			frontPoint = liF.previous();
			g.drawOval(stateX(frontPoint), stateY(frontPoint), 10, 10);
		}*/
	}

	void update(Model m)
	{
		Controller c = m.getController();
		while(true)
		{
			MouseEvent e = c.nextMouseEvent();
			if(e == null || e.getButton() == MouseEvent.BUTTON2 || m.getGreenColor(e.getX(), e.getY()) > 150)
				break;
			boolean heuristic = false;
			if(e.getButton() == MouseEvent.BUTTON1) heuristic = false;
			if(e.getButton() == MouseEvent.BUTTON3) heuristic = true;
			// draw flag
			//c.view.panel.drawFlag(Graphics g, e.getX(), e.getY() - 80);
			//m.setDestination(e.getX(), e.getY());	
			
			MyPlaner myPlaner = new MyPlaner(m);
			MyState startState = new MyState(0,null);
			startState.state = XYtoState((int)m.getX(),(int)m.getY());
			MyState goalState = new MyState(0,null);
			goalState.state = XYtoState(e.getX(), e.getY());
			myPlaner.uniformCostSearch(startState, goalState, heuristic);
					
		    //m.setDestination(e.getX(), e.getY());		    
		    //m.update();
					
		}
	}

	public static void main(String[] args) throws Exception
	{
		Controller.playGame();
	}
	class MyState{
		public double cost;
		MyState parent;
		int state;
		MyState(double cost, MyState parent){
			this.cost = cost;
			this.parent = parent;
			this.state = 0;
		}
		public MyState transition(int action, float travelSpeed){
			MyState newState = new MyState(this.cost, this);
			newState.state = this.state + action;
			newState.cost = this.cost + transitionCost(this.state, newState.state, travelSpeed);
			return newState;
		}
	}
	class MyPlaner{
		Model m;
		MyPlaner(Model m){
			this.m = m;
		}
		//Comparator anonymous class implementation
		class CostStateComparator implements Comparator<MyState>
		{
			public int compare(MyState a, MyState b)
			{
				if(a.cost < b.cost) return -1;
				if(a.cost > b.cost) return 1;
				return 0;
			}
		}
		class StateComparator implements Comparator<MyState>
		{
			public int compare(MyState a, MyState b)
			{
				if(a.state < b.state) return -1;
				if(a.state > b.state) return 1;
				return 0;
			}
		}
		public MyState uniformCostSearch(MyState startState, MyState goalState, boolean heuristic){
			
			/*
			 * left click  -> heuristic = false: UCS (Uniform Cost Search)
			 * right click -> heuristic = true: A* Search (Generalization of UCS)
			 */

			int[] actions = new int[]{
					1 - 120,
					1,
					1 + 120,
					120,
					-120,
					-1 - 120,
					-1,
					-1 + 120					
			};		
			PriorityQueue<MyState> frontier = new PriorityQueue<MyState>(7200,new CostStateComparator());
			TreeSet<MyState> beenThere = new TreeSet<MyState>(new StateComparator());
			startState.cost = 0.0;
			startState.parent = null;
			beenThere.add(startState);
			frontier.add(startState);
			while(frontier.size()>0){
				MyState s = frontier.poll(); // get lowest-cost state
				if(s.state == goalState.state) {
					states.clear();
					savePath(s);
					// mark all points at the frontier when goal found
					frontStates.clear();
					while(frontier.size()>0){
						MyState front = frontier.poll(); 
						frontStates.add(front.state);	
					}
					// make sure the sprite located in the start state position
					//m.setPosition(stateX(startState.state), stateY(startState.state));
					m.setDestination(stateX(startState.state), stateY(startState.state));
					return s;
				}
				float travelSpeed = m.getTravelSpeed(stateX(s.state),stateY(s.state));
				for(int i=0; i < 8; i++){  // at most 8 actions 
					if(isValidState(s.state+actions[i]) && !m.obstacles.contains(s.state+actions[i])){ // valid move only						
						MyState child = s.transition(actions[i], travelSpeed);
						if(heuristic) child.cost += (double)(distance(child.state,goalState.state)/m.maxTravelSpeed); 
						if(beenThere.contains(child)){
							MyState oldState = beenThere.floor(child);
							if(child.cost < oldState.cost){
								oldState.cost = child.cost;
								oldState.parent = s;
							}
						}
						else {
							frontier.add(child);
							beenThere.add(child);
						}

					}
				}
			}
			return null;
		} 

	}
	
	static boolean isValidState(int state){
		return (state >=0 && state < 7200);
	}
	
	public static int XYtoState(int x, int y){
		return (120 * (int)(y/10) + (int)(x/10));
	}
	
	public static int stateX (int state){
		return (state % 120)*10;
	}
	
	public static int stateY (int state){
		return (state / 120)*10;
	}
	
	static float distance(int state1, int state2){
		int deltaX = stateX(state2)-stateX(state1);
		int deltaY = stateY(state2)-stateY(state1);
		return (float)Math.sqrt(deltaX*deltaX + deltaY*deltaY);
	}
	
	static double transitionCost(int state1, int state2, float travelSpeed){
		return (double)(distance(state1,state2)/travelSpeed);
	}
	
	public static List<Integer> states = new ArrayList<Integer>();

	public static List<Integer> frontStates = new ArrayList<Integer>();
	
	static void savePath(MyState myState)
	{
		states.add(myState.state);
		if(myState.parent != null)
			savePath(myState.parent);
	}		 
}
