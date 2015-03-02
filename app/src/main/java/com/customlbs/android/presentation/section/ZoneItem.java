package com.customlbs.android.presentation.section;

import com.customlbs.library.model.Zone;

public class ZoneItem implements Item {

    private final Zone zone;

    public ZoneItem(Zone zone) {
	this.zone = zone;
    }

    @Override
    public boolean isSection() {
	return false;
    }

    public String getTitle() {
	return zone.getName();
    }

    public String getSubtitle() {
	return zone.getDescription();
    }

    public Zone getZone() {
	return zone;
    }
}
