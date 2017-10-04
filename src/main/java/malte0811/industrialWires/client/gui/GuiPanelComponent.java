package malte0811.industrialWires.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonCheckbox;
import blusunrize.immersiveengineering.client.gui.elements.GuiSliderIE;
import com.google.common.collect.ImmutableList;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.client.gui.elements.GuiChannelPicker;
import malte0811.industrialWires.client.gui.elements.GuiChannelPickerSmall;
import malte0811.industrialWires.client.gui.elements.GuiIntChooser;
import malte0811.industrialWires.containers.ContainerPanelComponent;
import malte0811.industrialWires.controlpanel.IConfigurableComponent;
import malte0811.industrialWires.controlpanel.PanelComponent;
import malte0811.industrialWires.network.MessageItemSync;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static malte0811.industrialWires.util.NBTKeys.*;

public class GuiPanelComponent extends GuiContainer {
	private PanelComponent component;
	private IConfigurableComponent confComp;
	private ContainerPanelComponent container;
	private List<GuiButtonCheckbox> boolButtons = new ArrayList<>();
	private List<GuiTextField> stringTexts = new ArrayList<>();
	private List<GuiChannelPicker> rsChannelChoosers = new ArrayList<>();
	private List<GuiIntChooser> intChoosers = new ArrayList<>();
	private List<GuiSliderIE> floatSliders = new ArrayList<>();

	public GuiPanelComponent(EnumHand h, PanelComponent pc) {
		super(new ContainerPanelComponent(h));
		container = (ContainerPanelComponent) inventorySlots;
		component = pc;
	}

	@Override
	public void initGui() {
		super.initGui();
		xSize = 150;
		ySize = 150;
		Keyboard.enableRepeatEvents(true);
		if (component instanceof IConfigurableComponent) {
			confComp = (IConfigurableComponent) component;
			IConfigurableComponent.BoolConfig[] bools = confComp.getBooleanOptions();
			boolButtons.clear();
			int componentLeft = this.guiLeft + 5;
			int componentTop = this.guiTop + 5;
			for (int i = 0; i < bools.length; i++) {
				IConfigurableComponent.BoolConfig bc = bools[i];
				boolButtons.add(new GuiButtonCheckbox(0, componentLeft + bc.x, componentTop + bc.y, confComp.fomatConfigName(IConfigurableComponent.ConfigType.BOOL, i), bc.value));
			}
			IConfigurableComponent.StringConfig[] strings = confComp.getStringOptions();
			stringTexts.clear();
			for (IConfigurableComponent.StringConfig sc : strings) {
				GuiTextField toAdd = new GuiTextField(0, mc.fontRenderer, componentLeft + sc.x, componentTop + sc.y, 58, 12);
				toAdd.setText(sc.value);
				stringTexts.add(toAdd);
			}
			IConfigurableComponent.RSChannelConfig[] rs = confComp.getRSChannelOptions();
			rsChannelChoosers.clear();
			for (IConfigurableComponent.RSChannelConfig rc : rs) {
				if (rc.small) {
					rsChannelChoosers.add(new GuiChannelPickerSmall(0, componentLeft + rc.x, componentTop + rc.y, 10, 40, rc.value));
				} else {
					rsChannelChoosers.add(new GuiChannelPicker(0, componentLeft + rc.x, componentTop + rc.y, 40, rc.value));
				}
			}
			intChoosers.clear();
			IConfigurableComponent.IntConfig[] is = confComp.getIntegerOptions();
			for (IConfigurableComponent.IntConfig ic : is) {
				intChoosers.add(new GuiIntChooser(componentLeft + ic.x, componentTop + ic.y, ic.allowNegative, ic.value, ic.digits));
			}
			floatSliders.clear();
			IConfigurableComponent.FloatConfig[] fs = confComp.getFloatOptions();
			for (int i = 0; i < fs.length; i++) {
				IConfigurableComponent.FloatConfig fc = fs[i];
				floatSliders.add(new GuiSliderIE(0, componentLeft + fc.x, componentTop + fc.y, fc.width,
						confComp.fomatConfigName(IConfigurableComponent.ConfigType.FLOAT, i), fc.value));
			}
		}
	}

	private ResourceLocation textureLoc = new ResourceLocation(IndustrialWires.MODID, "textures/gui/panel_component.png");

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1, 1, 1, 1);
		mc.getTextureManager().bindTexture(textureLoc);
		Gui.drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, xSize, ySize, 150, 150);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
		syncAll();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		boolean superCall = true;
		for (int i = 0; i < stringTexts.size(); i++) {
			GuiTextField field = stringTexts.get(i);
			if (field.isFocused() && keyCode == 28) {
				sync(i, field.getText());
				superCall = false;
			} else if (field.textboxKeyTyped(typedChar, keyCode)) {
				superCall = false;
			}
		}
		if (superCall) {
			super.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		for (int i = 0; i < rsChannelChoosers.size(); i++) {
			GuiChannelPicker picker = rsChannelChoosers.get(i);
			int old = picker.getSelected();
			boolean stopNow = picker.click(mouseX, mouseY);
			if (old != picker.getSelected()) {
				sync(i, picker.getSelected());
			}
			if (stopNow) {
				for (GuiChannelPicker picker2:rsChannelChoosers) {
					if (picker!=picker2&&picker2 instanceof GuiChannelPickerSmall) {
						((GuiChannelPickerSmall) picker2).close();
					}
				}
				return;
			}
		}
		for (int i = 0; i < stringTexts.size(); i++) {
			GuiTextField field = stringTexts.get(i);
			boolean focus = field.isFocused();
			field.mouseClicked(mouseX, mouseY, mouseButton);
			if (focus && !field.isFocused()) {
				sync(i, field.getText());
			}
		}
		for (int i = 0; i < boolButtons.size(); i++) {
			GuiButtonCheckbox box = boolButtons.get(i);
			boolean on = box.state;
			box.mousePressed(mc, mouseX, mouseY);
			if (on != box.state) {
				sync(i, box.state);
			}
		}
		for (int i = 0; i < intChoosers.size(); i++) {
			GuiIntChooser chooser = intChoosers.get(i);
			int oldV = chooser.getValue();
			chooser.click(mouseX, mouseY);
			if (oldV != chooser.getValue()) {
				sync(i, chooser.getValue());
			}
		}
		for (int i = 0; i < floatSliders.size(); i++) {
			GuiSliderIE slider = floatSliders.get(i);
			double oldV = slider.getValue();
			slider.mousePressed(mc, mouseX, mouseY);
			if (oldV != slider.getValue()) {
				sync(i, (float) slider.getValue());
			}
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		for (int i = 0; i < floatSliders.size(); i++) {
			GuiSliderIE slider = floatSliders.get(i);
			double oldV = slider.getValue();
			slider.mouseReleased(mouseX, mouseY);
			if (oldV != slider.getValue()) {
				sync(i, (float) slider.getValue());
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
		GlStateManager.color(1, 1, 1, 1);
		RenderHelper.disableStandardItemLighting();
		for (GuiButtonCheckbox box : boolButtons) {
			box.drawButton(mc, mouseX, mouseY, partialTicks);
		}
		for (GuiTextField field : stringTexts) {
			field.drawTextBox();
		}
		for (GuiIntChooser choose : intChoosers) {
			choose.drawChooser();
		}
		for (GuiSliderIE choose : floatSliders) {
			choose.drawButton(mc, mouseX, mouseY, partialTicks);
		}
		GuiChannelPickerSmall openPicker = null;
		for (GuiChannelPicker pick : rsChannelChoosers) {
			if (pick instanceof GuiChannelPickerSmall&&((GuiChannelPickerSmall) pick).open) {
				openPicker = (GuiChannelPickerSmall) pick;
			} else {
				pick.drawButton(mc, mouseX, mouseY, partialTicks);
			}
		}
		if (openPicker != null) {
			openPicker.drawButton(mc, mouseX, mouseY, partialTicks);
		}
		//TOOLTIPS
		for (int i = 0; i < rsChannelChoosers.size(); i++) {
			GuiChannelPicker pick = rsChannelChoosers.get(i);
			String tooltip = confComp.fomatConfigDescription(IConfigurableComponent.ConfigType.RS_CHANNEL, i);
			if (tooltip != null && pick.isHovered(mouseX, mouseY)) {
				ClientUtils.drawHoveringText(ImmutableList.of(tooltip), mouseX, mouseY, mc.fontRenderer);
				return;
			}
		}
		for (int i = 0; i < boolButtons.size(); i++) {
			GuiButtonCheckbox box = boolButtons.get(i);
			String tooltip = confComp.fomatConfigDescription(IConfigurableComponent.ConfigType.BOOL, i);
			if (tooltip != null && box.isMouseOver()) {
				ClientUtils.drawHoveringText(ImmutableList.of(tooltip), mouseX, mouseY, mc.fontRenderer);
				return;
			}
		}
		for (int i = 0; i < stringTexts.size(); i++) {
			GuiTextField field = stringTexts.get(i);
			String tooltip = confComp.fomatConfigDescription(IConfigurableComponent.ConfigType.STRING, i);
			if (tooltip != null && mouseX >= field.x && mouseX < field.x + field.width &&
					mouseY >= field.y && mouseY < field.y + field.height) {
				ClientUtils.drawHoveringText(ImmutableList.of(tooltip), mouseX, mouseY, mc.fontRenderer);
				return;
			}
		}
		for (int i = 0; i < intChoosers.size(); i++) {
			GuiIntChooser choose = intChoosers.get(i);
			String tooltip = confComp.fomatConfigDescription(IConfigurableComponent.ConfigType.INT, i);
			if (tooltip != null && choose.isMouseOver(mouseX, mouseY)) {
				ClientUtils.drawHoveringText(ImmutableList.of(tooltip), mouseX, mouseY, mc.fontRenderer);
				return;
			}
		}
		for (int i = 0; i < floatSliders.size(); i++) {
			GuiSliderIE choose = floatSliders.get(i);
			String tooltip = confComp.fomatConfigDescription(IConfigurableComponent.ConfigType.FLOAT, i);
			if (tooltip != null && choose.isMouseOver()) {
				ClientUtils.drawHoveringText(ImmutableList.of(tooltip), mouseX, mouseY, mc.fontRenderer);
				return;
			}
		}
	}

	private void sync(int id, String value) {
		NBTTagCompound update = new NBTTagCompound();
		update.setInteger(TYPE, IConfigurableComponent.ConfigType.STRING.ordinal());
		update.setInteger(ID, id);
		update.setString(VALUE, value);
		syncSingle(update);
	}

	private void sync(int id, boolean value) {
		NBTTagCompound update = new NBTTagCompound();
		update.setInteger(TYPE, IConfigurableComponent.ConfigType.BOOL.ordinal());
		update.setInteger(ID, id);
		update.setBoolean(VALUE, value);
		syncSingle(update);
	}

	private void sync(int id, byte value) {
		NBTTagCompound update = new NBTTagCompound();
		update.setInteger(TYPE, IConfigurableComponent.ConfigType.RS_CHANNEL.ordinal());
		update.setInteger(ID, id);
		update.setByte(VALUE, value);
		syncSingle(update);
	}

	private void sync(int id, int value) {
		NBTTagCompound update = new NBTTagCompound();
		update.setInteger(TYPE, IConfigurableComponent.ConfigType.INT.ordinal());
		update.setInteger(ID, id);
		update.setInteger(VALUE, value);
		syncSingle(update);
	}

	private void sync(int id, float value) {
		NBTTagCompound update = new NBTTagCompound();
		update.setInteger(TYPE, IConfigurableComponent.ConfigType.FLOAT.ordinal());
		update.setInteger(ID, id);
		update.setFloat(VALUE, value);
		syncSingle(update);
	}

	private void syncAll() {
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < stringTexts.size(); i++) {
			NBTTagCompound update = new NBTTagCompound();
			update.setInteger(TYPE, IConfigurableComponent.ConfigType.STRING.ordinal());
			update.setInteger(ID, i);
			update.setString(VALUE, stringTexts.get(i).getText());
			list.appendTag(update);
		}
		for (int i = 0; i < boolButtons.size(); i++) {
			NBTTagCompound update = new NBTTagCompound();
			update.setInteger(TYPE, IConfigurableComponent.ConfigType.BOOL.ordinal());
			update.setInteger(ID, i);
			update.setBoolean(VALUE, boolButtons.get(i).state);
			list.appendTag(update);
		}
		for (int i = 0; i < rsChannelChoosers.size(); i++) {
			NBTTagCompound update = new NBTTagCompound();
			update.setInteger(TYPE, IConfigurableComponent.ConfigType.RS_CHANNEL.ordinal());
			update.setInteger(ID, i);
			update.setByte(VALUE, rsChannelChoosers.get(i).getSelected());
			list.appendTag(update);
		}
		for (int i = 0; i < intChoosers.size(); i++) {
			NBTTagCompound update = new NBTTagCompound();
			update.setInteger(TYPE, IConfigurableComponent.ConfigType.INT.ordinal());
			update.setInteger(ID, i);
			update.setInteger(VALUE, intChoosers.get(i).getValue());
			list.appendTag(update);
		}
		for (int i = 0; i < floatSliders.size(); i++) {
			NBTTagCompound update = new NBTTagCompound();
			update.setInteger(TYPE, IConfigurableComponent.ConfigType.FLOAT.ordinal());
			update.setInteger(ID, i);
			update.setFloat(VALUE, (float) floatSliders.get(i).getValue());
			list.appendTag(update);
		}
		sync(list);
	}

	private void syncSingle(NBTTagCompound nbt) {
		NBTTagList list = new NBTTagList();
		list.appendTag(nbt);
		sync(list);
	}

	private void sync(NBTTagList list) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("data", list);
		IndustrialWires.packetHandler.sendToServer(new MessageItemSync(container.hand, nbt));
	}
}