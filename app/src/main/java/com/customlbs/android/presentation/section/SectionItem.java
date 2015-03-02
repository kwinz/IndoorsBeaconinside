package com.customlbs.android.presentation.section;

/**
 * 
 * @author Dominic Bartl - http://bartinger.at/listview-with-sectionsseparators/
 * 
 */
public class SectionItem implements Item {

    private final String title;

    public SectionItem(String title) {
	this.title = title;
    }

    @Override
    public boolean isSection() {
	return true;
    }

    public String getTitle() {
	return title;
    }
}
