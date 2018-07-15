package com.marcowong.motoman.track;

import java.util.Hashtable;
import java.util.Map;

public class TrackSegment {
	public TrackSegment prev;
	public TrackSegment next;
	public float x1;
	public float y1;
	public float l1;
	public float r1;
	public float w1;
	public float x2;
	public float y2;
	public float l2;
	public float r2;
	public float w2;
	public Map<String, Object> attributes = new Hashtable<String, Object>();
}
