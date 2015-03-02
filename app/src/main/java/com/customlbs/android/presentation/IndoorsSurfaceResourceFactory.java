package com.customlbs.android.presentation;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;

import com.customlbs.android.R;
import com.customlbs.library.util.UnitConverter;
import com.customlbs.surface.library.IndoorsSurfaceFactory;
import com.customlbs.surface.library.SurfacePainterConfiguration;
import com.customlbs.surface.library.SurfacePainterConfiguration.PaintConfiguration;

public class IndoorsSurfaceResourceFactory extends IndoorsSurfaceFactory {

    public static IndoorsSurfaceFactory.Builder fromXml(Resources res) {
	SurfacePainterConfiguration configuration = new SurfacePainterConfiguration();

	configuration.setNavigationArrow(BitmapFactory.decodeResource(res,
		R.drawable.maps_arrow_green_100_centered));
	configuration.setNavigationPoint(BitmapFactory.decodeResource(res,
		R.drawable.maps_circle_green_100));
	configuration.setRoutingStart(BitmapFactory.decodeResource(res, R.drawable.routing_start));
	configuration.setRoutingEnd(BitmapFactory.decodeResource(res, R.drawable.routing_stop));

	PaintConfiguration routingPathPaintConfiguration = new PaintConfiguration();
	routingPathPaintConfiguration.setColor(res.getColor(R.color.routing_path_color));
	routingPathPaintConfiguration.setStyle(Style.valueOf(res
		.getString(R.string.routing_path_style)));
	routingPathPaintConfiguration.setAntiAlias(res
		.getBoolean(R.bool.routing_path_anti_aliasing));
	routingPathPaintConfiguration.setStrokeWidth(UnitConverter.convertDpToPixel(4));

	configuration.setRoutingPathPaintConfiguration(routingPathPaintConfiguration);

	PaintConfiguration noTilePaintConfiguration = new PaintConfiguration();
	noTilePaintConfiguration.setColor(res.getColor(R.color.no_tile_color));
	noTilePaintConfiguration.setStyle(Style.valueOf(res.getString(R.string.no_tile_style)));
	noTilePaintConfiguration.setAntiAlias(res.getBoolean(R.bool.no_tile_anti_aliasing));

	configuration.setNoTilePaintConfiguration(noTilePaintConfiguration);

	PaintConfiguration solidRedFillPaintConfiguration = new PaintConfiguration();
	solidRedFillPaintConfiguration.setColor(res.getColor(R.color.solide_red_fill_color));
	solidRedFillPaintConfiguration.setStyle(Style.valueOf(res
		.getString(R.string.solide_red_fill_style)));
	solidRedFillPaintConfiguration.setAntiAlias(res
		.getBoolean(R.bool.solide_red_fill_anti_aliasing));

	configuration.setSolidRedFillPaintConfiguration(solidRedFillPaintConfiguration);

	PaintConfiguration solidFillPaintConfiguration = new PaintConfiguration();
	solidFillPaintConfiguration.setColor(res.getColor(R.color.solid_fill_color));
	solidFillPaintConfiguration
		.setStyle(Style.valueOf(res.getString(R.string.solid_fill_style)));
	solidFillPaintConfiguration.setAntiAlias(res.getBoolean(R.bool.solid_fill_anti_aliasing));

	configuration.setSolidFillPaintConfiguration(solidFillPaintConfiguration);

	PaintConfiguration userPositionCircleInlinePaintConfiguration = new PaintConfiguration();
	userPositionCircleInlinePaintConfiguration.setColor(res
		.getColor(R.color.user_position_circle_inline_color));
	userPositionCircleInlinePaintConfiguration.setStyle(Style.valueOf(res
		.getString(R.string.user_position_circle_inline_style)));
	userPositionCircleInlinePaintConfiguration.setAntiAlias(res
		.getBoolean(R.bool.user_position_circle_inline_anti_aliasing));

	configuration
		.setUserPositionCircleInlinePaintConfiguration(userPositionCircleInlinePaintConfiguration);

	PaintConfiguration largeCircleOutlinePaintConfiguration = new PaintConfiguration();
	largeCircleOutlinePaintConfiguration.setColor(res
		.getColor(R.color.large_circle_outline_color));
	largeCircleOutlinePaintConfiguration.setStyle(Style.valueOf(res
		.getString(R.string.large_circle_outline_style)));
	largeCircleOutlinePaintConfiguration.setAntiAlias(res
		.getBoolean(R.bool.large_circle_outline_aliasing));

	configuration.setLargeCircleOutlinePaintConfiguration(largeCircleOutlinePaintConfiguration);

	PaintConfiguration blackFillPaintConfiguration = new PaintConfiguration();
	blackFillPaintConfiguration.setColor(res.getColor(R.color.black_fill_color));
	blackFillPaintConfiguration
		.setStyle(Style.valueOf(res.getString(R.string.black_fill_style)));
	blackFillPaintConfiguration.setAntiAlias(res.getBoolean(R.bool.black_fill_anti_aliasing));

	configuration.setBlackFillPaintConfiguration(blackFillPaintConfiguration);

	PaintConfiguration debugTextPaintConfiguration = new PaintConfiguration();
	debugTextPaintConfiguration.setColor(res.getColor(R.color.debug_text_color));
	debugTextPaintConfiguration.setAntiAlias(res.getBoolean(R.bool.debug_text_anti_aliasing));
	debugTextPaintConfiguration.setTextSize(UnitConverter.convertDpToPixel(16));
	debugTextPaintConfiguration
		.setStyle(Style.valueOf(res.getString(R.string.debug_text_style)));

	configuration.setDebugTextPaintConfiguration(debugTextPaintConfiguration);

	PaintConfiguration zonePaintInnerPaintConfiguration = new PaintConfiguration();
	zonePaintInnerPaintConfiguration.setColor(res.getColor(R.color.zone_color));
	zonePaintInnerPaintConfiguration
		.setStyle(Style.valueOf(res.getString(R.string.zone_style)));
	zonePaintInnerPaintConfiguration.setAntiAlias(res.getBoolean((R.bool.zone_anti_aliasing)));

	configuration.setZonePaintInnerPaintConfiguration(zonePaintInnerPaintConfiguration);

	PaintConfiguration zonePaintFramePaintConfiguration = new PaintConfiguration();
	zonePaintFramePaintConfiguration.setColor(res.getColor(R.color.zone_frame_color));
	zonePaintFramePaintConfiguration.setStyle(Style.valueOf(res
		.getString(R.string.zone_frame_style)));
	zonePaintFramePaintConfiguration.setAntiAlias(res
		.getBoolean((R.bool.zone_frame_anti_aliasing)));
	zonePaintFramePaintConfiguration.setStrokeCap(Cap.valueOf(res
		.getString(R.string.zone_frame_cap)));
	zonePaintFramePaintConfiguration.setStrokeJoin(Join.valueOf(res
		.getString(R.string.zone_frame_join)));
	zonePaintFramePaintConfiguration.setDimensionPixelSize(res
		.getDimensionPixelSize(R.dimen.zone_frame_width_base));

	configuration.setZonePaintFramePaintConfiguration(zonePaintFramePaintConfiguration);

	PaintConfiguration zoneNamePaintConfiguration = new PaintConfiguration();
	zoneNamePaintConfiguration.setColor(res.getColor(R.color.zone_name_color));
	zoneNamePaintConfiguration.setStyle(Style.valueOf(res.getString(R.string.zone_name_style)));
	zoneNamePaintConfiguration.setAntiAlias(res.getBoolean((R.bool.zone_anti_aliasing)));
	zoneNamePaintConfiguration.setDimensionPixelSize(res
		.getDimensionPixelSize(R.dimen.zone_name_size_base));
	zoneNamePaintConfiguration.setTextSize((float) res
		.getDimensionPixelSize(R.dimen.zone_name_size_base));

	configuration.setZoneNamePaintConfiguration(zoneNamePaintConfiguration);

	return new IndoorsSurfaceFactory.Builder().setSurfacePainterConfiguration(configuration);
    }
}
