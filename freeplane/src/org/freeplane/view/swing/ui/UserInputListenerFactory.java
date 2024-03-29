/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.view.swing.ui;

import java.awt.Component;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelListener;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.IMouseWheelEventHandler;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.UIBuilder;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;
import org.freeplane.view.swing.map.MapView;

public class UserInputListenerFactory implements IUserInputListenerFactory {
	public static final String NODE_POPUP = "/node_popup";
// // 	final private Controller controller;
	private IMouseListener mapMouseListener;
	private MouseWheelListener mapMouseWheelListener;
	final private ActionListener mapsMenuActionListener;
	private JPopupMenu mapsPopupMenu;
	private FreeplaneMenuBar menuBar;
	private final MenuBuilder menuBuilder;
	final private HashSet<IMouseWheelEventHandler> mRegisteredMouseWheelEventHandler = new HashSet<IMouseWheelEventHandler>();
	private DragGestureListener nodeDragListener;
	private DropTargetListener nodeDropTargetListener;
	private KeyListener nodeKeyListener;
	private IMouseListener nodeMotionListener;
	private IMouseListener nodeMouseMotionListener;
	private JPopupMenu nodePopupMenu;
	private final Map<String, JComponent> toolBars;
	private final List<JComponent>[] toolbarLists;

	public UserInputListenerFactory(final ModeController modeController) {
		Controller controller = Controller.getCurrentController();
		mapsMenuActionListener = new MapsMenuActionListener(controller);
		menuBuilder = new MenuBuilder(modeController);
		controller.getMapViewManager().addMapSelectionListener(new IMapSelectionListener() {
			public void afterMapChange(final MapModel oldMap, final MapModel newMap) {
				menuBuilder.afterMapChange(newMap);
			}

			public void beforeMapChange(final MapModel oldMap, final MapModel newMap) {
			}
		});
		toolBars = new LinkedHashMap<String, JComponent>();
		toolbarLists = newListArray();
		for (int j = 0; j < 4; j++) {
			toolbarLists[j] = new LinkedList<JComponent>();
		}
	}

	// isolate unchecked stuff in this method
	@SuppressWarnings("unchecked")
	private List<JComponent>[] newListArray() {
		return new List[4];
	}

	public void addToolBar(final String name, final int position, final JComponent toolBar) {
		toolBars.put(name, toolBar);
		toolbarLists[position].add(toolBar);
	}

	public void addMouseWheelEventHandler(final IMouseWheelEventHandler handler) {
		mRegisteredMouseWheelEventHandler.add(handler);
	}

	public IMouseListener getMapMouseListener() {
		if (mapMouseListener == null) {
			mapMouseListener = new DefaultMapMouseListener();
		}
		return mapMouseListener;
	}

	public MouseWheelListener getMapMouseWheelListener() {
		if (mapMouseWheelListener == null) {
			mapMouseWheelListener = new DefaultMouseWheelListener();
		}
		return mapMouseWheelListener;
	}

	public JPopupMenu getMapPopup() {
		return mapsPopupMenu;
	}

	public FreeplaneMenuBar getMenuBar() {
		if (menuBar == null) {
			menuBar = new FreeplaneMenuBar();
		}
		return menuBar;
	}

	public MenuBuilder getMenuBuilder() {
		return menuBuilder;
	}

	public Set<IMouseWheelEventHandler> getMouseWheelEventHandlers() {
		return Collections.unmodifiableSet(mRegisteredMouseWheelEventHandler);
	}

	public DragGestureListener getNodeDragListener() {
		return nodeDragListener;
	}

	public DropTargetListener getNodeDropTargetListener() {
		return nodeDropTargetListener;
	}

	public KeyListener getNodeKeyListener() {
		if (nodeKeyListener == null) {
			nodeKeyListener = new DefaultNodeKeyListener(null);
		}
		return nodeKeyListener;
	}

	public IMouseListener getNodeMotionListener() {
		return nodeMotionListener;
	}

	public IMouseListener getNodeMouseMotionListener() {
		if (nodeMouseMotionListener == null) {
			nodeMouseMotionListener = new DefaultNodeMouseMotionListener();
		}
		return nodeMouseMotionListener;
	}

	public JPopupMenu getNodePopupMenu() {
		return nodePopupMenu;
	}

	public JComponent getToolBar(final String name) {
		return toolBars.get(name);
	}

	public Iterable<JComponent> getToolBars(final int position) {
		return toolbarLists[position];
	}

	public void removeMouseWheelEventHandler(final IMouseWheelEventHandler handler) {
		mRegisteredMouseWheelEventHandler.remove(handler);
	}

	public void setMapMouseListener(final IMouseListener mapMouseMotionListener) {
		if (mapMouseListener != null) {
			throw new RuntimeException("already set");
		}
		mapMouseListener = mapMouseMotionListener;
	}

	public void setMapMouseWheelListener(final MouseWheelListener mouseWheelListener) {
		if (mapMouseWheelListener != null) {
			throw new RuntimeException("already set");
		}
		mapMouseWheelListener = mouseWheelListener;
	}

	public void setMenuBar(final FreeplaneMenuBar menuBar) {
		if (mapMouseWheelListener != null) {
			throw new RuntimeException("already set");
		}
		this.menuBar = menuBar;
	}


	public void setNodeDragListener(DragGestureListener nodeDragListener) {
		if (this.nodeDragListener != null) {
			throw new RuntimeException("already set");
		}
    	this.nodeDragListener = nodeDragListener;
    }

	public void setNodeDropTargetListener(final DropTargetListener nodeDropTargetListener) {
		if (this.nodeDropTargetListener != null) {
			throw new RuntimeException("already set");
		}
		this.nodeDropTargetListener = nodeDropTargetListener;
	}

	public void setNodeKeyListener(final KeyListener nodeKeyListener) {
		if (this.nodeKeyListener != null) {
			throw new RuntimeException("already set");
		}
		this.nodeKeyListener = nodeKeyListener;
	}

	public void setNodeMotionListener(final IMouseListener nodeMotionListener) {
		if (this.nodeMotionListener != null) {
			throw new RuntimeException("already set");
		}
		this.nodeMotionListener = nodeMotionListener;
	}

	public void setNodeMouseMotionListener(final IMouseListener nodeMouseMotionListener) {
		if (this.nodeMouseMotionListener != null) {
			throw new RuntimeException("already set");
		}
		this.nodeMouseMotionListener = nodeMouseMotionListener;
	}

	public void setNodePopupMenu(final JPopupMenu nodePopupMenu) {
		if (this.nodePopupMenu != null) {
			throw new RuntimeException("already set");
		}
		this.nodePopupMenu = nodePopupMenu;
	}

	public void updateMapList() {
		updateModeMenu();
		updateMapList("main_menu_mindmaps");
	}

	private void updateMapList(final String mapsMenuPosition) {
		menuBuilder.removeChildElements(mapsMenuPosition);
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		final List<? extends Component> mapViewVector = mapViewManager.getMapViewVector();
		if (mapViewVector == null) {
			return;
		}
		final ButtonGroup group = new ButtonGroup();
		int i = 0;
		for (final Component mapView : mapViewVector) {
			final String displayName = mapView.getName();
			final JRadioButtonMenuItem newItem = new JRadioButtonMenuItem(displayName);
			newItem.setSelected(false);
			group.add(newItem);
			newItem.addActionListener(mapsMenuActionListener);
			if (displayName.length() > 0) {
				newItem.setMnemonic(displayName.charAt(0));
			}
			final MapView currentMapView = (MapView) mapViewManager.getMapViewComponent();
			if (currentMapView != null) {
				if (mapView == currentMapView) {
					newItem.setSelected(true);
				}
			}
			menuBuilder.addMenuItem(mapsMenuPosition, newItem, mapsMenuPosition + '-' + i++, UIBuilder.AS_CHILD);
		}
	}

	public void updateMenus(String menuStructureResource, Set<String> plugins) {
		final FreeplaneMenuBar menuBar = getMenuBar();
		menuBuilder.addMenuBar(menuBar, FreeplaneMenuBar.MENU_BAR_PREFIX);
		mapsPopupMenu = new JPopupMenu();
		menuBuilder.addPopupMenu(mapsPopupMenu, FreeplaneMenuBar.MAP_POPUP_MENU);
		menuBuilder.addPopupMenu(getNodePopupMenu(), UserInputListenerFactory.NODE_POPUP);
		menuBuilder.addToolbar((JToolBar) getToolBar("/main_toolbar"), "/main_toolbar");
		mapsPopupMenu.setName(TextUtils.getText("mindmaps"));
		final URL menuStructure = ResourceController.getResourceController().getResource(menuStructureResource);
		if (menuStructure != null) {
			final boolean isUserDefined = menuStructure.getProtocol().equalsIgnoreCase("file");
			try{
			menuBuilder.processMenuCategory(menuStructure, plugins);
			}
			catch (RuntimeException e){
				if(isUserDefined){
					LogUtils.warn(e);
					String myMessage = TextUtils.format("menu_error", menuStructure.getPath(), e.getMessage());
					UITools.backOtherWindows();
					JOptionPane.showMessageDialog(UITools.getFrame(), myMessage, "Freeplane", JOptionPane.ERROR_MESSAGE);
					System.exit(-1);
				}
				throw e;
			}
		}
		final ViewController viewController = Controller.getCurrentController().getViewController();
		viewController.updateMenus(menuBuilder);
	}

	private void updateModeMenu() {
		menuBuilder.removeChildElements(FreeplaneMenuBar.MODES_MENU);
		Controller controller = Controller.getCurrentController();
		for (final String key : new LinkedList<String>(controller.getModes())) {
			final AFreeplaneAction modesMenuActionListener = new ModesMenuActionListener(key, controller);
			final ModeController modeController = controller.getModeController();
			final boolean isSelected;
			if (modeController != null) {
				isSelected = modeController.getModeName().equals(key);
			}
			else {
				isSelected = false;
			}
			menuBuilder.addRadioItem(FreeplaneMenuBar.MODES_MENU, modesMenuActionListener, isSelected);
			ResourceController.getResourceController().getProperty(("keystroke_mode_" + key));
		}
	}
}
