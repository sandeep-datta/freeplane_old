/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.nodestyle;

import java.awt.Color;
import java.awt.Font;
import java.util.Collection;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.CombinedPropertyChain;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ExclusivePropertyChain;
import org.freeplane.features.mode.IPropertyHandler;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyleModel;

/**
 * @author Dimitry Polivaev
 */
public class NodeStyleController implements IExtension {
	public static Color standardNodeTextColor = Color.BLACK;

	public static NodeStyleController getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return getController(modeController);
	}

	public static NodeStyleController getController(ModeController modeController) {
		return (NodeStyleController) modeController.getExtension(NodeStyleController.class);
	}
	public static void install( final NodeStyleController styleController) {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addExtension(NodeStyleController.class, styleController);
	}

	final private ExclusivePropertyChain<Color, NodeModel> backgroundColorHandlers;
// // //	final private Controller controller;
	final private CombinedPropertyChain<Font, NodeModel> fontHandlers;
 	final private ModeController modeController;
	final private ExclusivePropertyChain<String, NodeModel> shapeHandlers;
	final private ExclusivePropertyChain<Color, NodeModel> textColorHandlers;
	public static final String NODE_NUMBERING = "NodeNumbering";

	public NodeStyleController(final ModeController modeController) {
		this.modeController = modeController;
//		controller = modeController.getController();
		fontHandlers = new CombinedPropertyChain<Font, NodeModel>(true);
		textColorHandlers = new ExclusivePropertyChain<Color, NodeModel>();
		backgroundColorHandlers = new ExclusivePropertyChain<Color, NodeModel>();
		shapeHandlers = new ExclusivePropertyChain<String, NodeModel>();
		addFontGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<Font, NodeModel>() {
			public Font getProperty(final NodeModel node, final Font currentValue) {
				final Font defaultFont = NodeStyleController.getDefaultFont();
				return defaultFont;
			}
		});
		addFontGetter(IPropertyHandler.STYLE, new IPropertyHandler<Font, NodeModel>() {
			public Font getProperty(final NodeModel node, final Font currentValue) {
				final Font defaultFont = getStyleFont(currentValue, node.getMap(), LogicalStyleController.getController(modeController).getStyles(node));
				return defaultFont;
			}
		});
		addColorGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return standardNodeTextColor;
			}
		});
		addColorGetter(IPropertyHandler.STYLE, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return getStyleTextColor(node.getMap(), LogicalStyleController.getController(modeController).getStyles(node));
			}
		});
		addBackgroundColorGetter(IPropertyHandler.STYLE, new IPropertyHandler<Color, NodeModel>() {
			public Color getProperty(final NodeModel node, final Color currentValue) {
				return getStyleBackgroundColor(node.getMap(), LogicalStyleController.getController(modeController).getStyles(node));
			}
		});
		addShapeGetter(IPropertyHandler.STYLE, new IPropertyHandler<String, NodeModel>() {
			public String getProperty(final NodeModel node, final String currentValue) {
				final MapModel map = node.getMap();
				final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
				final Collection<IStyle> style = styleController.getStyles(node);
				final String returnedString = getStyleShape(map, style);
				return returnedString;
			}
		});
		addShapeGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<String, NodeModel>() {
			public String getProperty(final NodeModel node, final String currentValue) {
				return getShape(node);
			}

			private String getShape(final NodeModel node) {
				return NodeStyleModel.SHAPE_AS_PARENT;
			}
		});
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final NodeStyleBuilder styleBuilder = new NodeStyleBuilder(this);
		styleBuilder.registerBy(readManager, writeManager);
	}

	public IPropertyHandler<Color, NodeModel> addBackgroundColorGetter(final Integer key,
	                                                                   final IPropertyHandler<Color, NodeModel> getter) {
		return backgroundColorHandlers.addGetter(key, getter);
	}

	public IPropertyHandler<Color, NodeModel> addColorGetter(final Integer key,
	                                                         final IPropertyHandler<Color, NodeModel> getter) {
		return textColorHandlers.addGetter(key, getter);
	}

	public IPropertyHandler<Font, NodeModel> addFontGetter(final Integer key,
	                                                       final IPropertyHandler<Font, NodeModel> getter) {
		return fontHandlers.addGetter(key, getter);
	}

	public IPropertyHandler<String, NodeModel> addShapeGetter(final Integer key,
	                                                          final IPropertyHandler<String, NodeModel> getter) {
		return shapeHandlers.addGetter(key, getter);
	}

	public Color getBackgroundColor(final NodeModel node) {
		return backgroundColorHandlers.getProperty(node);
	}

	public Color getColor(final NodeModel node) {
		return textColorHandlers.getProperty(node);
	}

	private Color getStyleBackgroundColor(final MapModel map, final Collection<IStyle> styleKeys) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : styleKeys){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeStyleModel styleModel = NodeStyleModel.getModel(styleNode);
			if (styleModel == null) {
				continue;
			}
			final Color styleColor =styleModel.getBackgroundColor();
			if (styleColor == null) {
				continue;
			}
			return styleColor;
		}
		return null;
	}

	private int getStyleMaxNodeWidth(final MapModel map, final Collection<IStyle> styleKeys) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : styleKeys){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeSizeModel sizeModel = NodeSizeModel.getModel(styleNode);
			if (sizeModel == null) {
				continue;
			}
			final int maxTextWidth = sizeModel.getMaxNodeWidth();
			if (maxTextWidth == NodeSizeModel.NOT_SET) {
				continue;
			}
			return maxTextWidth;
		}
		return getDefaultMaxNodeWidth();
	}
	
	private int getStyleMinWidth(final MapModel map, final Collection<IStyle> styleKeys) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : styleKeys){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeSizeModel sizeModel = NodeSizeModel.getModel(styleNode);
			if (sizeModel == null) {
				continue;
			}
			final int minWidth = sizeModel.getMinNodeWidth();
			if (minWidth == NodeSizeModel.NOT_SET) {
				continue;
			}
			return minWidth;
		}
		return getDefaultMinNodeWidth();
	}
	
	public static Font getDefaultFont() {
		final int fontSize = NodeStyleController.getDefaultFontSize();
		final int fontStyle = NodeStyleController.getDefaultFontStyle();
		final String fontFamily = NodeStyleController.getDefaultFontFamilyName();
		return new Font(fontFamily, fontStyle, fontSize);
	}

	/**
	*/
	private static String getDefaultFontFamilyName() {
		return ResourceController.getResourceController().getProperty("defaultfont");
	}

	private static int getDefaultFontStyle() {
		return ResourceController.getResourceController().getIntProperty("defaultfontstyle", 0);
	}

	private static int getDefaultFontSize() {
		return ResourceController.getResourceController().getIntProperty("defaultfontsize", 10);
	}

	public Font getDefaultFont(final MapModel map, final IStyle style) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		final NodeModel styleNode = model.getStyleNodeSafe(style);
		return getFont(styleNode);
	}

	private Font getStyleFont(final Font baseFont, final MapModel map, final Collection<IStyle> collection) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		Boolean bold = null;
        Boolean italic = null;
        String fontFamilyName = null;
        Integer fontSize = null;
		for(IStyle styleKey : collection){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeStyleModel styleModel = NodeStyleModel.getModel(styleNode);
			if (styleModel == null) {
				continue;
			}
			if (bold == null) bold = styleModel.isBold();
			if (italic == null) italic = styleModel.isItalic();
			if (fontFamilyName == null) fontFamilyName = styleModel.getFontFamilyName();
			if (fontSize == null) fontSize = styleModel.getFontSize();
			if(bold != null && italic != null && fontFamilyName != null && fontSize != null) break;
		}
		return createFont(baseFont, fontFamilyName, fontSize, bold, italic);
	}

	private Font createFont(final Font baseFont, String family, Integer size, Boolean bold, Boolean italic) {
		if (family == null && size == null && bold == null && italic == null) {
			return baseFont;
		}
		if (family == null) {
			family = baseFont.getFamily();
		}
		if (size == null) {
			size = baseFont.getSize();
		}
		if (bold == null) {
			bold = baseFont.isBold();
		}
		if (italic == null) {
			italic = baseFont.isItalic();
		}
		int style = 0;
		if (bold) {
			style += Font.BOLD;
		}
		if (italic) {
			style += Font.ITALIC;
		}
		return new Font(family, style, size);
	}

	private String getStyleShape(final MapModel map, final Collection<IStyle> style) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : style){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeStyleModel styleModel = NodeStyleModel.getModel(styleNode);
			if (styleModel == null) {
				continue;
			}
			final String shape = styleModel.getShape();
			if (shape == null) {
				continue;
			}
			return shape;
		}
		return null;
	}

	private Color getStyleTextColor(final MapModel map, final Collection<IStyle> collection) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : collection){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final NodeStyleModel styleModel = NodeStyleModel.getModel(styleNode);
			if (styleModel == null) {
				continue;
			}
			final Color styleColor = styleModel == null ? null : styleModel.getColor();
			if (styleColor == null) {
				continue;
			}
			return styleColor;
		}
		return null;
	}

	public Font getFont(final NodeModel node) {
		final Font font = fontHandlers.getProperty(node, null);
		return font;
	}

	public String getFontFamilyName(final NodeModel node) {
		final Font font = getFont(node);
		return font.getFamily();
	}

	public int getFontSize(final NodeModel node) {
		final Font font = getFont(node);
		return font.getSize();
	}

	public String getShape(final NodeModel node) {
		final String returnedString = shapeHandlers.getProperty(node);
		return returnedString;
	}

	public boolean isBold(final NodeModel node) {
		return getFont(node).isBold();
	}

	public boolean isItalic(final NodeModel node) {
		return getFont(node).isItalic();
	}

	public IPropertyHandler<Color, NodeModel> removeBackgroundColorGetter(final Integer key) {
		return backgroundColorHandlers.removeGetter(key);
	}

	public IPropertyHandler<Color, NodeModel> removeColorGetter(final Integer key) {
		return textColorHandlers.removeGetter(key);
	}

	public IPropertyHandler<Font, NodeModel> removeFontGetter(final Integer key) {
		return fontHandlers.removeGetter(key);
	}

	public IPropertyHandler<String, NodeModel> removeShapeGetter(final Integer key) {
		return shapeHandlers.removeGetter(key);
	}

	public Boolean getNodeNumbering(NodeModel node) {
		final NodeStyleModel style = (NodeStyleModel) node.getExtension(NodeStyleModel.class);
		if (style == null)
			return false;
		final Boolean nodeNumbering = style.getNodeNumbering();
		return nodeNumbering == null ? false : nodeNumbering.booleanValue();
	}

	public String getNodeFormat(NodeModel node) {
		final NodeStyleModel style = (NodeStyleModel) node.getExtension(NodeStyleModel.class);
		return style == null ? null : style.getNodeFormat();
	}

	public int getMaxWidth(NodeModel node) {
		final MapModel map = node.getMap();
		final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
		final Collection<IStyle> style = styleController.getStyles(node);
		final int maxTextWidth = getStyleMaxNodeWidth(map, style);
		return maxTextWidth;
    }

	public int getMinWidth(NodeModel node) {
		final MapModel map = node.getMap();
		final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
		final Collection<IStyle> style = styleController.getStyles(node);
		final int minWidth = getStyleMinWidth(map, style);
		return minWidth;
    }

	public ModeController getModeController() {
	    return modeController;
    }

	public static int getDefaultMinNodeWidth() {
    		return ResourceController.getResourceController().getIntProperty("min_node_width", 1);
    }

	public static int getDefaultMaxNodeWidth() {
    		final int w = ResourceController.getResourceController().getIntProperty("max_node_width", -1);
			if(w != -1)
				return w;
    		return ResourceController.getResourceController().getIntProperty("el__max_default_window_width", 600) * 2 / 3;
    }
}
