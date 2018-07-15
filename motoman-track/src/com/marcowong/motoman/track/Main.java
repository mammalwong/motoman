package com.marcowong.motoman.track;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class Main {
	
	private static TrackGenerator trackGenerator;
	private static List<TrackSegment> tss;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.load("C:\\Downloads\\motoman\\motoman-track\\native\\gdx64.dll");
		
		trackGenerator = new TrackGenerator();
		trackGenerator.random = new BasicRandom(999940);
		trackGenerator.trackLen = 300;
		trackGenerator.turnAngleZeroFactor = 0.3f;
		trackGenerator.segLen = 2;
		trackGenerator.segWidth = 1;
		trackGenerator.segPad = 0.5f;
		tss = trackGenerator.generate();
		new TrackGuideGenerator().generate(tss);
		
		Frame frame = new Frame("motoman-track");
		Canvas canvas = new Canvas() {
			private static final long serialVersionUID = 4101216358115592367L;
			private int ox = 0;
			private int oy = 0;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				g.translate(getWidth() / 2 + ox, getHeight() / 2 + oy);
				
				for (TrackSegment ts : tss) {
					TrackSegLines tsl = (TrackSegLines)ts.attributes.get("lines");
					TrackDirection dir = (TrackDirection)ts.attributes.get("directionNotice");
					
					final float scale = 8f;
					
					//g.drawLine((int)Math.round(ts.x1 * scale), (int)Math.round(ts.y1 * scale), (int)Math.round(ts.x2 * scale), (int)Math.round(ts.y2 * scale));
					//System.out.println((int)ts.x1 + " " + (int)ts.y1 + " " + (int)ts.x2 + " " + (int)ts.y2);
					
					g.drawLine((int)Math.round(tsl.h.x1 * scale), (int)Math.round(tsl.h.y1 * scale), (int)Math.round(tsl.h.x2 * scale), (int)Math.round(tsl.h.y2 * scale));
					g.drawLine((int)Math.round(tsl.t.x1 * scale), (int)Math.round(tsl.t.y1 * scale), (int)Math.round(tsl.t.x2 * scale), (int)Math.round(tsl.t.y2 * scale));
					g.drawLine((int)Math.round(tsl.l.x1 * scale), (int)Math.round(tsl.l.y1 * scale), (int)Math.round(tsl.l.x2 * scale), (int)Math.round(tsl.l.y2 * scale));
					g.drawLine((int)Math.round(tsl.r.x1 * scale), (int)Math.round(tsl.r.y1 * scale), (int)Math.round(tsl.r.x2 * scale), (int)Math.round(tsl.r.y2 * scale));
					
					//g.drawLine((int)Math.round(tsl.hp.x1 * scale), (int)Math.round(tsl.hp.y1 * scale), (int)Math.round(tsl.hp.x2 * scale), (int)Math.round(tsl.hp.y2 * scale));
					//g.drawLine((int)Math.round(tsl.tp.x1 * scale), (int)Math.round(tsl.tp.y1 * scale), (int)Math.round(tsl.tp.x2 * scale), (int)Math.round(tsl.tp.y2 * scale));
					//g.drawLine((int)Math.round(tsl.lp.x1 * scale), (int)Math.round(tsl.lp.y1 * scale), (int)Math.round(tsl.lp.x2 * scale), (int)Math.round(tsl.lp.y2 * scale));
					//g.drawLine((int)Math.round(tsl.rp.x1 * scale), (int)Math.round(tsl.rp.y1 * scale), (int)Math.round(tsl.rp.x2 * scale), (int)Math.round(tsl.rp.y2 * scale));
					
					if (dir != null)
						g.drawString(dir.name(), (int)Math.round(ts.x1 * scale), (int)Math.round(ts.y1 * scale));
				}
			}
			
			@Override
			public boolean keyUp(Event arg0, int arg1) {
				switch ((char)arg1) {
				case 'a':
					ox += 20;
					break;
				case 'd':
					ox -= 20;
					break;
				case 'w':
					oy += 20;
					break;
				case 's':
					oy -= 20;
					break;
				}
				this.repaint();
				return super.keyUp(arg0, arg1);
			}
		};
		frame.add(canvas);
		frame.setSize(640, 480);
		canvas.setSize(640, 480);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		frame.setVisible(true);
		
		canvas.invalidate();
		
		//TrackGenerator tg = new TrackGenerator();
	}

}
