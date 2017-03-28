// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

import javax.swing.JFrame;
import java.awt.Graphics;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.io.File;
import java.awt.Image;
import java.util.ArrayList;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.event.MouseListener;

public class View extends JFrame implements ActionListener {
	Controller controller;
	Model model;
	//private MyPanel panel;
	public MyPanel panel;
	private ArrayList<Controller> replayPoints;
	private int slomo;

	public View(Controller c, Model m) throws Exception {
		this.controller = c;
		this.model = m;

		// Make the game window
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Moving Robot");
		this.setSize(1203, 636);
		this.panel = new MyPanel();
		this.panel.addMouseListener(controller);
		this.getContentPane().add(this.panel);
		this.setVisible(true);

		this.replayPoints = new ArrayList<Controller>();
	}

	public void actionPerformed(ActionEvent evt) {
		repaint(); // indirectly calls MyPanel.paintComponent
	}

	class MyPanel extends JPanel {
		public static final int FLAG_IMAGE_HEIGHT = 25;

		BufferedImage image_robot;
		
		MyPanel() throws Exception {
			this.image_robot = ImageIO.read(new File("robot_80x22.png"));
		}

		void drawTerrain(Graphics g) {
			byte[] terrain = model.getTerrain();
			int posBlue = 0;
			int posRed = (60 * 60 - 1) * 4;
			for(int y = 0; y < 60; y++) {
				for(int x = 0; x < 60; x++) {
					int bb = terrain[posBlue + 1] & 0xff;
					int gg = terrain[posBlue + 2] & 0xff;
					int rr = terrain[posBlue + 3] & 0xff;
					g.setColor(new Color(rr, gg, bb));
					g.fillRect(10 * x, 10 * y, 10, 10);
					posBlue += 4;
				}
				for(int x = 60; x < 120; x++) {
					int bb = terrain[posRed + 1] & 0xff;
					int gg = terrain[posRed + 2] & 0xff;
					int rr = terrain[posRed + 3] & 0xff;
					g.setColor(new Color(rr, gg, bb));
					g.fillRect(10 * x, 10 * y, 10, 10);
					posRed -= 4;
				}
			}
		}

		void drawSprites(Graphics g) {
			ArrayList<Model.Sprite> sprites = model.getSprites();
			for(int i = 0; i < sprites.size(); i++) {

				// Draw the robot image with angle
				Model.Sprite s = sprites.get(i);
				
				double rotationRequired = s.angle;
				double locationX = 40;
				double locationY = 11;
				AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
				AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

				// Drawing the rotated image at the required drawing locations
				g.drawImage(op.filter(image_robot, null), (int)s.x - 40, (int)s.y - 11, null);
		
				//g.drawImage(image_robot, (int)s.x - 20, (int)s.y - 5, null);
			}
		}
		

		public void paintComponent(Graphics g) {

			// Give the agents a chance to make decisions
			if(!controller.update()) {
				View.this.dispatchEvent(new WindowEvent(View.this, WindowEvent.WINDOW_CLOSING)); // Close this window
			}

			// Draw the view
			drawTerrain(g);
			drawSprites(g);
			controller.agent.drawPlan(g, model);
		}
	}
}
