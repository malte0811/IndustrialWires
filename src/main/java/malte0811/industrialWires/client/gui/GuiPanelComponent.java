package malte0811.industrialWires.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonCheckbox;
import blusunrize.immersiveengineering.client.gui.elements.GuiSliderIE;
import com.google.common.collect.ImmutableList;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.client.gui.elements.GuiChannelPicker;
import malte0811.industrialWires.client.gui.elements.GuiIntChooser;
import malte0811.industrialWires.containers.ContainerPanelComponent;
import malte0811.industrialWires.controlpanel.PanelComponent;
import malte0811.industrialWires.controlpanel.properties.IConfigurableComponent;
import malte0811.industrialWires.network.MessageComponentSync;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiPanelComponent extends GuiContainer {
	private PanelComponent component;
	private IConfigurableComponent confComp;
	private ContainerPanelComponent container;
	private List<GuiButtonCheckbox> boolButtons = new ArrayList<>();
	private List<GuiTextField> stringTexts = new ArrayList<>();
	private List<GuiChannelPicker> rsChannelChoosers = new ArrayList<>();
	private List<GuiIntChooser> intChoosers = new ArrayList<>();
	private List<GuiSliderIE> floatSliders = new ArrayList<>();

	//TODO int, float
	public GuiPanelComponent(EnumHand h, PanelComponent pc) {
		super(new ContainerPanelComponent(h));
		container = (ContainerPanelComponent) inventorySlots;
		component = pc;
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		if (component instanceof IConfigurableComponent) {
			confComp = (IConfigurableComponent) component;
			IConfigurableComponent.BoolConfig[] bools = confComp.getBooleanOptions();
			boolButtons.clear();
			for (int i = 0;i<bools.length;i++) {
				IConfigurableComponent.BoolConfig bc = bools[i];
				//TODO check whether ID==0 is a bad thing when using custom button lists
				boolButtons.add(new GuiButtonCheckbox(0, guiLeft + bc.x, guiTop + bc.y, confComp.fomatConfigName(IConfigurableComponent.ConfigType.BOOL, i), bc.value));
			}
			IConfigurableComponent.StringConfig[] strings = confComp.getStringOptions();
			stringTexts.clear();
			for (IConfigurableComponent.StringConfig sc : strings) {
				stringTexts.add(new GuiTextField(0, mc.fontRendererObj, guiLeft + sc.x, guiTop + sc.y, 58, 12));
			}
			IConfigurableComponent.RSChannelConfig[] rs = confComp.getRSChannelOptions();
			rsChannelChoosers.clear();
			for (IConfigurableComponent.RSChannelConfig rc : rs) {
				rsChannelChoosers.add(new GuiChannelPicker(0, guiLeft + rc.x, guiTop + rc.y, 40, rc.value));
			}
			intChoosers.clear();
			IConfigurableComponent.IntConfig[] is = confComp.getIntegerOptions();
			for (IConfigurableComponent.IntConfig ic : is) {
				intChoosers.add(new GuiIntChooser(guiLeft+ic.x, guiTop+ic.y, ic.allowNegative, ic.value, ic.digits));
			}
			floatSliders.clear();
			IConfigurableComponent.FloatConfig[] fs = confComp.getFloatOptions();
			for (int i = 0;i<fs.length;i++) {
				IConfigurableComponent.FloatConfig fc = fs[i];
				floatSliders.add(new GuiSliderIE(0, guiLeft+fc.x, guiTop+fc.y, fc.width,
						confComp.fomatConfigName(IConfigurableComponent.ConfigType.FLOAT, i), fc.value));
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		//TODO background
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
		for (int i = 0;i<stringTexts.size();i++) {
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
		for (int i = 0;i<stringTexts.size();i++) {
			GuiTextField field = stringTexts.get(i);
			boolean focus = field.isFocused();
			field.mouseClicked(mouseX, mouseY, mouseButton);
			if (focus&&!field.isFocused()) {
				sync(i, field.getText());
			}
		}
		for (int i = 0;i<rsChannelChoosers.size();i++) {
			GuiChannelPicker picker = rsChannelChoosers.get(i);
			int mXRel = mouseX-picker.xPosition;
			int mYRel = mouseY-picker.yPosition;
			if (mXRel>=0&&mXRel<picker.width&&mYRel>=0&&mYRel<picker.height) {
				int old = picker.getSelected();
				picker.select();
				if (old != picker.getSelected()) {
					sync(i, picker.getSelected());
				}
			}
		}
		for (int i = 0;i<boolButtons.size();i++) {
			GuiButtonCheckbox box = boolButtons.get(i);
			boolean on = box.state;
			box.mousePressed(mc, mouseX, mouseY);
			if (on!=box.state) {
				sync(i, box.state);
			}
		}
		for (int i = 0;i<intChoosers.size();i++) {
			GuiIntChooser chooser = intChoosers.get(i);
			int oldV = chooser.getValue();
			chooser.click(mouseX, mouseY);
			if (oldV!=chooser.getValue()) {
				sync(i, chooser.getValue());
			}
		}
		for (int i = 0;i<floatSliders.size();i++) {
			GuiSliderIE slider = floatSliders.get(i);
			double oldV = slider.getValue();
			slider.mousePressed(mc, mouseX, mouseY);
			slider.mouseReleased(mouseX, mouseY);
			if (oldV!=slider.getValue()) {
				sync(i, (float) slider.getValue());
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		GlStateManager.color(1, 1, 1, 1);
		for (int i = 0;i<rsChannelChoosers.size();i++) {
			GuiChannelPicker pick = rsChannelChoosers.get(i);
			pick.drawButton(mc, mouseX, mouseY);
			String tooltip = confComp.fomatConfigDescription(IConfigurableComponent.ConfigType.RS_CHANNEL, i);
			if (tooltip!=null&&pick.isHovered()) {
				ClientUtils.drawHoveringText(ImmutableList.of(tooltip), mouseX, mouseY, mc.fontRendererObj);
			}
		}
		for (int i = 0;i<boolButtons.size();i++) {
			GuiButtonCheckbox box = boolButtons.get(i);
			box.drawButton(mc, mouseX, mouseY);
			String tooltip = confComp.fomatConfigDescription(IConfigurableComponent.ConfigType.BOOL, i);
			if (tooltip!=null&&box.isMouseOver()) {
				ClientUtils.drawHoveringText(ImmutableList.of(tooltip), mouseX, mouseY, mc.fontRendererObj);
			}
		}
		for (int i = 0;i<stringTexts.size();i++) {
			GuiTextField field = stringTexts.get(i);
			field.drawTextBox();
			String tooltip = confComp.fomatConfigDescription(IConfigurableComponent.ConfigType.STRING, i);
			if (tooltip!=null&&mouseX>=field.xPosition&&mouseX<field.xPosition+field.width&&
					mouseY>=field.yPosition&&mouseY<field.yPosition+field.height) {
				ClientUtils.drawHoveringText(ImmutableList.of(tooltip), mouseX, mouseY, mc.fontRendererObj);
			}
		}
		for (int i = 0;i<intChoosers.size();i++) {
			GuiIntChooser choose = intChoosers.get(i);
			choose.drawChooser();
			String tooltip = confComp.fomatConfigDescription(IConfigurableComponent.ConfigType.INT, i);
			if (tooltip!=null&&choose.isMouseOver(mouseX, mouseY)) {
				ClientUtils.drawHoveringText(ImmutableList.of(tooltip), mouseX, mouseY, mc.fontRendererObj);
			}
		}
		for (int i = 0;i<floatSliders.size();i++) {
			GuiSliderIE choose = floatSliders.get(i);
			choose.drawButton(mc, mouseX, mouseY);
			String tooltip = confComp.fomatConfigDescription(IConfigurableComponent.ConfigType.INT, i);
			if (tooltip!=null&&choose.isMouseOver()) {
				ClientUtils.drawHoveringText(ImmutableList.of(tooltip), mouseX, mouseY, mc.fontRendererObj);
			}
		}
	}

	private void sync(int id, String value) {
		NBTTagCompound update = new NBTTagCompound();
		update.setInteger(MessageComponentSync.TYPE, IConfigurableComponent.ConfigType.STRING.ordinal());
		update.setInteger(MessageComponentSync.ID, id);
		update.setString(MessageComponentSync.VALUE, value);
		syncSingle(update);
	}
	private void sync(int id, boolean value) {
		NBTTagCompound update = new NBTTagCompound();
		update.setInteger(MessageComponentSync.TYPE, IConfigurableComponent.ConfigType.BOOL.ordinal());
		update.setInteger(MessageComponentSync.ID, id);
		update.setBoolean(MessageComponentSync.VALUE, value);
		syncSingle(update);
	}
	private void sync(int id, byte value) {
		NBTTagCompound update = new NBTTagCompound();
		update.setInteger(MessageComponentSync.TYPE, IConfigurableComponent.ConfigType.RS_CHANNEL.ordinal());
		update.setInteger(MessageComponentSync.ID, id);
		update.setByte(MessageComponentSync.VALUE, value);
		syncSingle(update);
	}
	private void sync(int id, int value) {
		NBTTagCompound update = new NBTTagCompound();
		update.setInteger(MessageComponentSync.TYPE, IConfigurableComponent.ConfigType.INT.ordinal());
		update.setInteger(MessageComponentSync.ID, id);
		update.setInteger(MessageComponentSync.VALUE, value);
		syncSingle(update);
	}
	private void sync(int id, float value) {
		NBTTagCompound update = new NBTTagCompound();
		update.setInteger(MessageComponentSync.TYPE, IConfigurableComponent.ConfigType.FLOAT.ordinal());
		update.setInteger(MessageComponentSync.ID, id);
		update.setFloat(MessageComponentSync.VALUE, value);
		syncSingle(update);
	}
	private void syncAll() {
		NBTTagList list = new NBTTagList();
		for (int i = 0;i<stringTexts.size();i++) {
			NBTTagCompound update = new NBTTagCompound();
			update.setInteger(MessageComponentSync.TYPE, IConfigurableComponent.ConfigType.STRING.ordinal());
			update.setInteger(MessageComponentSync.ID, i);
			update.setString(MessageComponentSync.VALUE, stringTexts.get(i).getText());
			list.appendTag(update);
		}
		for (int i = 0;i<boolButtons.size();i++) {
			NBTTagCompound update = new NBTTagCompound();
			update.setInteger(MessageComponentSync.TYPE, IConfigurableComponent.ConfigType.BOOL.ordinal());
			update.setInteger(MessageComponentSync.ID, i);
			update.setBoolean(MessageComponentSync.VALUE, boolButtons.get(i).state);
			list.appendTag(update);
		}
		for (int i = 0;i<rsChannelChoosers.size();i++) {
			NBTTagCompound update = new NBTTagCompound();
			update.setInteger(MessageComponentSync.TYPE, IConfigurableComponent.ConfigType.RS_CHANNEL.ordinal());
			update.setInteger(MessageComponentSync.ID, i);
			update.setByte(MessageComponentSync.VALUE, rsChannelChoosers.get(i).getSelected());
			list.appendTag(update);
		}
		for (int i = 0;i<intChoosers.size();i++) {
			NBTTagCompound update = new NBTTagCompound();
			update.setInteger(MessageComponentSync.TYPE, IConfigurableComponent.ConfigType.INT.ordinal());
			update.setInteger(MessageComponentSync.ID, i);
			update.setInteger(MessageComponentSync.VALUE, intChoosers.get(i).getValue());
			list.appendTag(update);
		}
		for (int i = 0;i<floatSliders.size();i++) {
			NBTTagCompound update = new NBTTagCompound();
			update.setInteger(MessageComponentSync.TYPE, IConfigurableComponent.ConfigType.FLOAT.ordinal());
			update.setInteger(MessageComponentSync.ID, i);
			update.setFloat(MessageComponentSync.VALUE, (float) floatSliders.get(i).getValue());
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
		IndustrialWires.packetHandler.sendToServer(new MessageComponentSync(container.hand, nbt));
	}
}