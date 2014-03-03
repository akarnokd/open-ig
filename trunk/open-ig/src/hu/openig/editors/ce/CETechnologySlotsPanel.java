/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Action1;
import hu.openig.model.ResearchSubCategory;
import hu.openig.utils.GUIUtils;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * The technology slots panel.
 * @author akarnokd, 2012.11.03.
 */
public class CETechnologySlotsPanel extends CESlavePanel {
	/** */
	private static final long serialVersionUID = -4055368744794508986L;
	/** The slot editor. */
	CESlotEdit slotEdit;
	/** The normal slot image. */
	ImageIcon slotNormal;
	/** The fixed slot image. */
	ImageIcon slotFixed;
	/** The slots model. */
	GenericTableModel<XElement> slotsModel;
	/** The slots table. */
	JTable slots;
	/** The current selected slot. */
	XElement selectedSlot;
	/** Slot property. */
	CEValueBox<JTextField> slotId;
	/** Slot property. */
	CEValueBox<JTextField> slotX;
	/** Slot property. */
	CEValueBox<JTextField> slotY;
	/** Slot property. */
	CEValueBox<JTextField> slotWidth;
	/** Slot property. */
	CEValueBox<JTextField> slotHeight;
	/** Slot property. */
	CEValueBox<JComboBox<String>> slotType;
	/** Slot property. */
	CEValueBox<JTextField> slotCount;
	/** The slot item combobox. */
	JComboBox<String> slotItems;
	/** The slot item value box. */
	CEValueBox<JComboBox<String>> slotItemField;
	/**
	 * Constructor.
	 * @param context the context
	 */
	public CETechnologySlotsPanel(CEContext context) {
		super(context);
		initGUI();
	}
	/**
	 * Initializes the GUI.
	 */
	protected void initGUI() {
		GroupLayout gl = new GroupLayout(this);
		this.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);

		slotId = CEValueBox.of(get("slot.id"), new JTextField());
		slotX = CEValueBox.of(get("slot.x"), new JTextField());
		slotY = CEValueBox.of(get("slot.y"), new JTextField());
		slotWidth = CEValueBox.of(get("slot.width"), new JTextField());
		slotHeight = CEValueBox.of(get("slot.height"), new JTextField());
		JComboBox<String> slotTypeBox = new JComboBox<>(new String[] {
				get("slot.type.normal"), get("slot.type.fixed")
		});
		slotType = CEValueBox.of(get("slot.type"), slotTypeBox);
		slotCount = CEValueBox.of(get("slot.count"), numberField());
		
		slotNormal = GUIUtils.createColorImageIcon(16, 16, 0xFF00FF00);
		slotFixed = GUIUtils.createColorImageIcon(16, 16, 0xFF808080);
		
		slotsModel = new GenericTableModel<XElement>() {
			/** */
			private static final long serialVersionUID = 2557373261832556243L;

			@Override
			public Object getValueFor(XElement item, int rowIndex, int columnIndex) {
				switch (columnIndex) {
				case 0: return item.name.equals("slot-fixed") ? slotFixed : slotNormal;
				case 1: return item.get("id", "");
				case 2: return item.name.equals("slot-fixed") ? item.get("count", "") : item.get("max", "");
				case 3: return item.name.equals("slot-fixed") ? item.get("item", "") : item.get("items", "");
				case 4: return validSlot(item);
				default:
					return null;
				}
			}
		};
		slotsModel.setColumnNames(
			"", get("slot.id"), get("slot.count_max"), get("slot.items"), ""
		);
		slotsModel.setColumnTypes(
			ImageIcon.class, String.class, Integer.class, String.class, ImageIcon.class
		);
		
		JButton addSlot = new JButton(icon("/hu/openig/gfx/plus16.png"));
		JButton removeSlot = new JButton(icon("/hu/openig/gfx/minus16.png"));

		addSlot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doAddSlot();
			}
		});
		removeSlot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doRemoveSlot();
			}
		});

		slots = new JTable(slotsModel);
		slots.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int idx = slots.getSelectedRow();
					if (idx >= 0) {
						idx = slots.convertRowIndexToModel(idx);
						doSelectSlot(slotsModel.get(idx));
					}
				}
			}
		});
		
		JScrollPane slotScroll = new JScrollPane(slots);
		
		slotEdit = new CESlotEdit();
		slotEdit.onSlotAdded = new Action1<XElement>() {
			@Override
			public void invoke(XElement value) {
				doAddSlot(value);
			}
		};
		slotEdit.onSlotRemoved = new Action1<XElement>() {
			@Override
			public void invoke(XElement value) {
				doRemoveSlot(value);
			}
		};
		slotEdit.onSlotSelected = new Action1<XElement>() {
			@Override
			public void invoke(XElement value) {
				doSelectSlotInTable(value);
				doSelectSlot(value);
			}
		};
		
		addValidator(slotId, new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				slotId.clearInvalid();
				if (selectedSlot != null) {
					selectedSlot.set("id", slotId.component.getText());
					if (selectedSlot.isNullOrEmpty("id")) {
						slotId.setInvalid(errorIcon, get("invalid_empty"));
					} else
					if (selectedSlot.name.equals("slot") && context.dataManager().label("inventoryslot." + selectedSlot.get("id")) == null) {
						slotId.setInvalid(warningIcon, format("missing_label_of", "inventoryslot." + selectedSlot.get("id")));
					}
					slotsModel.update(selectedSlot);
				}
			}
		});

		addValidator(slotCount, new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				slotCount.valid.setIcon(null);
				if (selectedSlot == null) {
					return;
				}
                if (selectedSlot.name.equals("slot")) {
                    selectedSlot.set("max", slotCount.component.getText());
                    if (!selectedSlot.hasPositiveInt("max")) {
                        slotCount.setInvalid(errorIcon, get("invalid_number"));
                    }
                } else {
                    selectedSlot.set("count", slotCount.component.getText());
                    if (!selectedSlot.hasPositiveInt("count")) {
                        slotCount.setInvalid(errorIcon, get("invalid_number"));
                    }
                }
                slotsModel.update(selectedSlot);
			}
		});
		slotType.component.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedSlot == null) {
					return;
				}
				int idx = slotType.component.getSelectedIndex();
				if (idx == 0 && !selectedSlot.name.equals("slot")) {
					XElement newslot = new XElement("slot");
					newslot.set("id", selectedSlot.get("id", null));
					newslot.set("max", selectedSlot.get("count", null));
					newslot.set("items", selectedSlot.get("item", null));
					
					master.replace(selectedSlot, newslot);
					slotsModel.replace(selectedSlot, newslot);
					
					selectedSlot = newslot;
					doSelectSlot(selectedSlot);
				} else
				if (idx == 1 && !selectedSlot.name.equals("slot-fixed")) {
					XElement newslot = new XElement("slot-fixed");
					newslot.set("id", selectedSlot.get("id", null));
					newslot.set("count", selectedSlot.get("max", null));
					String items = selectedSlot.get("items", null);
					if (items != null) {
						int iidx = items.indexOf(',');
						if (iidx >= 0) {
							newslot.set("item", items.substring(0, iidx).trim());
						} else {
							newslot.set("item", items.trim());
						}
					}
					
					master.replace(selectedSlot, newslot);
					slotsModel.replace(selectedSlot, newslot);
					
					selectedSlot = newslot;
					doSelectSlot(selectedSlot);
				}
			}
		});
		addNumberChanged(slotX, "x", true);
		addNumberChanged(slotY, "y", true);
		addNumberChanged(slotWidth, "width", true);
		addNumberChanged(slotHeight, "height", true);

		slotItems = new JComboBox<>();
		slotItemField = CEValueBox.of(get("slot.items"), slotItems);

		GenericTableModel<String> slotItemModel = new GenericTableModel<String>() {
			/** */
			private static final long serialVersionUID = 8789251342194906642L;

			@Override
			public Object getValueFor(String item, int rowIndex, int columnIndex) {
				switch (columnIndex) {
				case 0: return item;
				case 1: return getTechName(item);
				case 2: return getTechValue(item);
				case 3: return isTechValid(item);
				default:
					return null;
				}
			}
		};
		slotItemModel.setColumnNames(get("tech.id"), get("tech.name"), get("tech.production_cost"), "");
		slotItemModel.setColumnTypes(String.class, String.class, Integer.class, ImageIcon.class);
		
		JTable slotItemTable = new JTable(slotItemModel);
		
		JScrollPane slotItemScroll = new JScrollPane(slotItemTable);
		
		JButton addSlotItem = new JButton(icon("/hu/openig/gfx/plus16.png"));
		JButton removeSlotItem = new JButton(icon("/hu/openig/gfx/minus16.png"));
		
		// -----------------------------------------------------------------------------------
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(slotEdit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(slotScroll)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(addSlot)
					.addComponent(removeSlot)
				)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(slotId)
						.addComponent(slotType)
						.addComponent(slotCount)
					)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(slotX)
						.addComponent(slotY)
						.addComponent(slotWidth)
						.addComponent(slotHeight)
					)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(slotItemField)
					.addComponent(slotItemScroll)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(addSlotItem)
					.addComponent(removeSlotItem)
				)
			)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup()
				.addComponent(slotEdit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(slotScroll, 0, 100, Short.MAX_VALUE)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(addSlot)
					.addComponent(removeSlot)
				)
			)
			.addGroup(
				gl.createParallelGroup()
				.addGroup(
					gl.createSequentialGroup()
					.addGroup(
						gl.createParallelGroup(Alignment.BASELINE)
						.addComponent(slotId)
						.addComponent(slotType)
						.addComponent(slotCount)
					)
					.addGroup(
						gl.createParallelGroup(Alignment.BASELINE)
						.addComponent(slotX)
						.addComponent(slotY)
						.addComponent(slotWidth)
						.addComponent(slotHeight)
					)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addGroup(
						gl.createParallelGroup()
						.addComponent(slotItemField)
						.addComponent(addSlotItem)
					)
					.addGroup(
						gl.createParallelGroup()
						.addComponent(slotItemScroll)
						.addComponent(removeSlotItem)
					)
				)
			)
		);
		
		gl.linkSize(SwingConstants.VERTICAL, slotScroll, slotEdit);

		doSelectSlot(null);
	}
	/**
	 * Add a change event handler which writes and validates the specified selected slot attribute.
	 * @param field the field
	 * @param attribute the attribute
	 * @param allowNegative allow negative values?
	 */
	void addNumberChanged(
			final CEValueBox<? extends JTextField> field, 
			final String attribute, 
			final boolean allowNegative) {
		addValidator(field, new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				field.valid.setIcon(null);
				if (selectedSlot != null) {
					if (selectedSlot.name.equals("slot")) {
						selectedSlot.set(attribute, field.component.getText());
						if (allowNegative) {
							if (!selectedSlot.hasInt(attribute)) {
								field.setInvalid(errorIcon, get("invalid_number"));
							}
						} else {
							if (!selectedSlot.hasPositiveInt(attribute)) {
								field.setInvalid(errorIcon, get("invalid_number_positive"));
							}
						}
						slotsModel.update(selectedSlot);
						slotEdit.repaint();
					}
				}
			}
		});
	}
	/** Add a new slot. */
	void doAddSlot() {
		if (master != null) {
			XElement xslot = master.add("slot");
			int idx = slotsModel.getRowCount();
			slotsModel.add(xslot);

			idx = slots.convertRowIndexToView(idx);
			slots.getSelectionModel().clearSelection();
			slots.getSelectionModel().addSelectionInterval(idx, idx);
			
			slotEdit.repaint();

			slots.scrollRectToVisible(slots.getCellRect(idx, 0, true));
		}
	}
	/**
	 * Add the existing slot to the table.
	 * @param xslot the new slot
	 */
	void doAddSlot(XElement xslot) {
		int idx = slotsModel.getRowCount();
		slotsModel.add(xslot);

		idx = slots.convertRowIndexToView(idx);
		slots.getSelectionModel().clearSelection();
		slots.getSelectionModel().addSelectionInterval(idx, idx);

		slots.scrollRectToVisible(slots.getCellRect(idx, 0, true));
	}
	/**
	 * Remove the slots.
	 */
	void doRemoveSlot() {
		if (master != null) {
			int[] idxs = GUIUtils.convertSelectionToModel(slots);
            for (int idx : idxs) {
                master.remove(slotsModel.get(idx));
            }
			slotsModel.delete(idxs);
			slotEdit.repaint();
			doSelectSlot(null);
		}
	}
	/**
	 * Remove the specified slot from the table.
	 * @param slot the slot to remove
	 */
	void doRemoveSlot(XElement slot) {
		slotsModel.delete(slot);
	}
	/**
	 * Select a specific slot.
	 * @param slot the slot
	 */
	void doSelectSlot(XElement slot) {
		selectedSlot = slot;
		slotEdit.selectedSlot = slot;
		if (selectedSlot != null) {
			setTextAndEnabled(slotId, slot, "id", true);
			slotType.label.setEnabled(true);
			slotType.component.setEnabled(true);
            switch (slot.name) {
                case "slot":

                    setTextAndEnabled(slotCount, slot, "max", true);
                    setTextAndEnabled(slotX, slot, "x", true);
                    setTextAndEnabled(slotY, slot, "y", true);
                    setTextAndEnabled(slotWidth, slot, "width", true);
                    setTextAndEnabled(slotHeight, slot, "height", true);

                    slotType.component.setSelectedIndex(0);

                    slotItems.setSelectedIndex(-1);

                    break;
                case "slot-fixed":
                    setTextAndEnabled(slotCount, slot, "count", true);
                    setTextAndEnabled(slotX, null, "", false);
                    setTextAndEnabled(slotY, null, "", false);
                    setTextAndEnabled(slotWidth, null, "", false);
                    setTextAndEnabled(slotHeight, null, "", false);
                    slotType.component.setSelectedIndex(1);
                    slotItems.setSelectedItem(slot.get("item", ""));
                    break;
                default:
                    setTextAndEnabled(slotCount, null, "", false);
                    setTextAndEnabled(slotX, null, "", false);
                    setTextAndEnabled(slotY, null, "", false);
                    setTextAndEnabled(slotWidth, null, "", false);
                    setTextAndEnabled(slotHeight, null, "", false);
                    slotType.component.setSelectedIndex(-1);
                    break;
            }
			
		} else {
			setTextAndEnabled(slotId, null, "", false);
			setTextAndEnabled(slotCount, null, "", false);
			setTextAndEnabled(slotX, null, "", false);
			setTextAndEnabled(slotY, null, "", false);
			setTextAndEnabled(slotWidth, null, "", false);
			setTextAndEnabled(slotHeight, null, "", false);
			slotType.label.setEnabled(false);
			slotType.component.setEnabled(false);
			slotType.component.setSelectedIndex(-1);
			
			slotItems.setSelectedIndex(-1);
		}
		slotEdit.repaint();
	}
	/**
	 * Check if the slot is valid.
	 * @param slot the slot XML
	 * @return the error indicator
	 */
	ImageIcon validSlot(XElement slot) {
		ImageIcon result = okIcon;
		
		if (!slot.has("id") || slot.get("id").isEmpty()) {
			result = max(result, errorIcon);
		} else
		if (slot.name.equals("slot") &&  context.dataManager().label("inventoryslot." + slot.get("id")) == null) {
			result = max(result, warningIcon);
		}
		if (slot.name.equals("slot")) {
			if (!slot.hasPositiveInt("max")) {
				result = max(result, errorIcon);
			}
			if (!slot.hasInt("x")) {
				result = max(result, errorIcon);
			}
			if (!slot.hasInt("y")) {
				result = max(result, errorIcon);
			}
			if (!slot.hasInt("width")) {
				result = max(result, errorIcon);
			}
			if (!slot.hasInt("height")) {
				result = max(result, errorIcon);
			}
			if (!slot.has("items") || slot.get("items").isEmpty()) {
				result = max(result, errorIcon);
			}
		}
		if (slot.name.equals("slot-fixed")) {
			if (!slot.hasPositiveInt("count")) {
				result = max(result, errorIcon);
			}
			if (!slot.has("item") || slot.get("item").isEmpty()) {
				result = max(result, errorIcon);
			}
		}
		
		return result;
	}
	/**
	 * Select a specific slot in the table.
	 * @param slot the slot
	 */
	void doSelectSlotInTable(XElement slot) {
		slots.getSelectionModel().clearSelection();
		int idx = slotsModel.items.indexOf(slot);
		if (idx >= 0) {
			idx = slots.convertRowIndexToView(idx);
			slots.getSelectionModel().setSelectionInterval(idx, idx);
		}
	}
	/**
	 * Returns the technology name in the current locale.
	 * @param id the identifier
	 * @return the technology name
	 */
	String getTechName(String id) {
		return null;
	}
	/**
	 * Returns the technology value.
	 * @param id the identifier
	 * @return the value
	 */
	Integer getTechValue(String id) {
		return null;
	}
	/**
	 * Check if the given technology is valid for the slot.
	 * @param id the id
	 * @return the error indicator
	 */
	ImageIcon isTechValid(String id) {
		return null;
	}
	@Override
	public void onMasterChanged() {
		slotEdit.setSlotParent(master);
		slotEdit.clearImage();
		slotsModel.clear();
		if (master != null) {
			for (XElement xe : master.children()) {
				if (xe.name.startsWith("slot")) {
					slotsModel.add(xe);
				}
			}
			slotEdit.setImage(master.get("image") + "_small.png", context);
			GUIUtils.autoResizeColWidth(slots, slotsModel);
			doSelectSlot(null);
		}
	}
	/**
	 * Set the equipments from the available technologies list.
	 * @param techs the technologies list
	 */
	public void setAllTechnologies(List<XElement> techs) {
		Set<String> filter = U.newSet(
			ResearchSubCategory.EQUIPMENT_HYPERDRIVES.toString(),
			ResearchSubCategory.EQUIPMENT_RADARS.toString(),
			ResearchSubCategory.EQUIPMENT_SHIELDS.toString(),
			ResearchSubCategory.EQUIPMENT_MODULES.toString(),
			ResearchSubCategory.WEAPONS_CANNONS.toString(),
			ResearchSubCategory.WEAPONS_LASERS.toString(),
			ResearchSubCategory.WEAPONS_PROJECTILES.toString()
		);
		List<String> items = new ArrayList<>();
		slotItems.removeAllItems();
		for (XElement e : techs) {
			if (e.has("id") && filter.contains(e.get("category", ""))) {
				items.add(e.get("id"));
			}
		}
		Collections.sort(items);
		
		for (String s : items) {
			slotItems.addItem(s);
		}
		
		slotItems.setSelectedIndex(-1);
	}
}
