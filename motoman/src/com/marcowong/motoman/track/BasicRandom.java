package com.marcowong.motoman.track;

import java.util.Random;

public class BasicRandom implements IRandom {
	private Random random;
	
	public BasicRandom(int seed) {
		random = new Random(seed);
	}
	
	public float next() {
		return random.nextFloat();
	}
}
