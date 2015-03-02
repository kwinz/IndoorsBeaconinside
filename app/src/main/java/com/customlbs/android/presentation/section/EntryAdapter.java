package com.customlbs.android.presentation.section;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.customlbs.android.R;

/**
 * 
 * @author Dominic Bartl - http://bartinger.at/listview-with-sectionsseparators/
 * 
 */
public class EntryAdapter extends ArrayAdapter<Item> {

    private final int LAYOUT_ENTRY_ITEM;

    private ArrayList<Item> items;
    private LayoutInflater inflater;

    public EntryAdapter(Context context, ArrayList<Item> items) {
	super(context, 0, items);
	this.items = items;
	inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	// TODO: uncomment if zones do have descriptions
//	LAYOUT_ENTRY_ITEM = R.layout.custom_list_row_2_item;;
	LAYOUT_ENTRY_ITEM = R.layout.custom_list_row_1_item;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	View view = convertView;
	Item item = items.get(position);
	if (item != null) {
	    if (item.isSection()) {
		SectionItem section = (SectionItem) item;
		view = inflater.inflate(R.layout.section_item_section, null);

		view.setOnClickListener(null);
		view.setOnLongClickListener(null);
		view.setLongClickable(false);

		TextView sectionView = (TextView) view.findViewById(android.R.id.text1);
		sectionView.setText(section.getTitle());
	    } else {
		ZoneItem entry = (ZoneItem) item;
		view = inflater.inflate(LAYOUT_ENTRY_ITEM, null);
		TextView title = (TextView) view.findViewById(android.R.id.text1);
		// TODO: uncomment if zones do have descriptions
//		TextView subtitle = (TextView) view
//			.findViewById(android.R.id.text2);

		if (title != null)
		    title.setText(entry.getTitle());
//		if (subtitle != null)
//		    subtitle.setText(entry.getSubtitle());
	    }
	}

	return view;
    }

}
