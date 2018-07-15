package com.marcowong.motoman;

import com.badlogic.gdx.scenes.scene2d.ui.Slider;

public class SliderEx extends Slider {
	private boolean vertical;
	private SliderStyle style;
	private float bgMin;
	private float kMin;
	private float akMin;
	private float bkMin;
	
	public SliderEx (float min, float max, float stepSize, boolean vertical, SliderStyle style) {
		super(min, max, stepSize, vertical, style);
		this.vertical = vertical;
		this.style = style;
		if (vertical) {
			if (style.background != null) bgMin = style.background.getMinWidth();
			if (style.knob != null) kMin = style.knob.getMinWidth();
			if (style.knobAfter != null) akMin = style.knobAfter.getMinWidth();
			if (style.knobBefore != null) bkMin = style.knobBefore.getMinWidth();
		} else {
			if (style.background != null) bgMin = style.background.getMinHeight();
			if (style.knob != null) kMin = style.knob.getMinHeight();
			if (style.knobAfter != null) akMin = style.knobAfter.getMinHeight();
			if (style.knobBefore != null) bkMin = style.knobBefore.getMinHeight();
		}
	}
	
	@Override
	public void setBounds(float x, float y, float w, float h) {
		super.setBounds(x, y, w, h);
		if (vertical) {
			if (style.background != null) style.background.setMinWidth(w);
			if (style.knob != null) style.knob.setMinWidth(w * (kMin / bgMin));
			if (style.knobAfter != null) style.knobAfter.setMinWidth(w * (akMin / bgMin));
			if (style.knobBefore != null) style.knobBefore.setMinWidth(w * (bkMin / bgMin));
		} else {
			if (style.background != null) style.background.setMinHeight(h);
			if (style.knob != null) style.knob.setMinHeight(h * (kMin / bgMin));
			if (style.knobAfter != null) style.knobAfter.setMinHeight(h * (akMin / bgMin));
			if (style.knobBefore != null) style.knobBefore.setMinHeight(h * (bkMin / bgMin));
		}
	}
}
