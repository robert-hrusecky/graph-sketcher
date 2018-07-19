package com.sketcher.movement;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.Pin;

public class Plotter extends StepperGroup {

	private static final double WHEEL_RADIUS = 0.75;
	private static final double STEPS_PER_CM =  Stepper.STEPS_PER_REVOLUTION / (2 * Math.PI * WHEEL_RADIUS);
	
	private double x, y;
	
	public Plotter(Pin[] gpiosX, Pin[] gpiosY, GpioController gpio) {
		super(new Stepper(gpiosX, gpio), new Stepper(gpiosY, gpio));
		x = 0.0; y = 0.0;
	}
	
	public void moveTo(double newX, double newY) {
		double dx = newX - x;
		double dy = newY - y;
		step(dx * STEPS_PER_CM, dy * STEPS_PER_CM);
	}
	
	@Override
	public void step(double stepsX, double stepsY) {
		super.step(stepsX, stepsY);
		x += stepsX / STEPS_PER_CM;
		y += stepsY / STEPS_PER_CM;
	}

	@Override
	public void zero() {
		super.zero();
		x = 0.0;
		y = 0.0;
	}
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
}
