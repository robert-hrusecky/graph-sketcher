package com.sketcher;

import java.util.List;

import com.sketcher.graph.CPPGraph;
import com.sketcher.movement.Plotter;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

class Tester {


	public static void main(String[] args) {
		class Point {
			double x, y;
			public Point(double x, double y) {
				this.x = x;
				this.y = y;
			}
		}
		
		Point[] vertices = {
			new Point(0, 0),
			new Point(12, 0),
			new Point(4.732 * 4, 4),
			new Point(0, 12),
			new Point(12, 12),
			new Point(4.732 * 4, 16),
			new Point(1.732 * 4, 16)
		};
		int[][] edges = {
			{ 0, 1 },
			{ 0, 3 },
			{ 1, 2 },
			{ 1, 4 },
			{ 2, 5 },
			{ 3, 4 },
			{ 3, 6 },
			{ 4, 5 },
			{ 5, 6 }
		};
		CPPGraph<Point> graph = new CPPGraph<>(vertices, edges, (a, b) -> {
			double dx = Math.abs(a.x - b.x);
			double dy = Math.abs(a.y - b.y);
			return Math.max(dx, dy);
		});
		List<Point> path = graph.getPath();

		GpioController gpio = GpioFactory.getInstance();
		Plotter plotter = new Plotter(new Pin[] { RaspiPin.GPIO_22, RaspiPin.GPIO_23, RaspiPin.GPIO_24, RaspiPin.GPIO_25 }, new Pin[] { RaspiPin.GPIO_29, RaspiPin.GPIO_28, RaspiPin.GPIO_27, RaspiPin.GPIO_26 }, gpio);
		
		for (Point p : path) {
			plotter.moveTo(p.x, p.y);
		}
	}

}
