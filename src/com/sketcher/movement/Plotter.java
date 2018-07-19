package com.sketcher.movement;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.Pin;

/**
 * Convinece wrapper for using a {@link StepperGroup} as a mechanical plotter.
 * 
 * @author robert
 *
 */
public class Plotter extends StepperGroup {

	private static final double WHEEL_RADIUS = 0.75;
	private static final double STEPS_PER_CM = Stepper.STEPS_PER_REVOLUTION / (2 * Math.PI * WHEEL_RADIUS);

	private double x, y;

	/**
	 * Sets up the plotter and initializes the steppers.
	 * 
	 * @param gpiosX
	 *            The pins for the X-axis stepper
	 * @param gpiosY
	 *            The pins for the Y-axis stepper
	 * @param gpio
	 *            The {@link GpioController}
	 */
	public Plotter(Pin[] gpiosX, Pin[] gpiosY, GpioController gpio) {
		super(new Stepper(gpiosX, gpio), new Stepper(gpiosY, gpio));
		x = 0.0;
		y = 0.0;
	}

	/**
	 * Moves the plotter to the specified coordinates.
	 * 
	 * @param newX
	 *            the x-coordinate
	 * @param newY
	 *            the y-coordinate
	 */
	public void moveTo(double newX, double newY) {
		double dx = newX - x;
		double dy = newY - y;
		step(dx * STEPS_PER_CM, dy * STEPS_PER_CM);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param stepsX
	 *            steps for x-axis motor
	 * @param stepsY
	 *            steps for y-axis motor
	 */
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

	/**
	 * Gives the x-coordinate
	 * 
	 * @return the x-coordinate
	 */
	public double getX() {
		return x;
	}

	/**
	 * Gives the y-coordinate
	 * 
	 * @return the y-coordinate
	 */
	public double getY() {
		return y;
	}
}
