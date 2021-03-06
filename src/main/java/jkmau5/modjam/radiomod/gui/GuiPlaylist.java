package jkmau5.modjam.radiomod.gui;

import cpw.mods.fml.common.network.PacketDispatcher;
import jkmau5.modjam.radiomod.Constants;
import jkmau5.modjam.radiomod.network.PacketPlayBroadcastedSound;
import jkmau5.modjam.radiomod.network.PacketRemovePlaylistTitle;
import jkmau5.modjam.radiomod.tile.TileEntityPlaylist;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiPlaylist extends GuiScreen {

    private static int actualHeight = 0;
    private int xSize = 176;
    private int ySize = 166;
    private int scrollY = 0;
    private String selectedIndex = "";
    private static List<String> titleList = new ArrayList<String>();
    private static Map<String, Integer> titleCoordinates = new HashMap<String, Integer>();

    public static TileEntityPlaylist playlist;

    public GuiPlaylist(TileEntityPlaylist playlist){
        this.playlist = playlist;

        titleList = new ArrayList<String>();
        titleCoordinates = new HashMap<String, Integer>();
    }

    @Override
    public void initGui(){
        super.initGui();

        buttonList.add(new GuiButton(0, this.width / 2, this.height / 8, 100, 20, "Play"));

        actualHeight = height;
        initTitles();
    }

    @Override
    protected void actionPerformed(GuiButton button){
        if(button.id == 0 && !this.selectedIndex.isEmpty()){
            PacketDispatcher.sendPacketToServer(new PacketPlayBroadcastedSound(this.selectedIndex, playlist.xCoord, playlist.yCoord, playlist.zCoord, playlist.worldObj.provider.dimensionId).getPacket());
        }
    }

    public static void initTitles(){
        if(playlist.getTitles() != null && !playlist.getTitles().isEmpty()){
            List<String> titles = playlist.getTitles();
            for(int i = 0; i < titles.size(); i++){
                String realTitle = Constants.getRealRecordTitle(titles.get(i));

                int fromPos = 2 + ((actualHeight - 116) / 2) + (i * 10);
                titleList.add(realTitle);
                titleCoordinates.put(realTitle, fromPos);
            }
        }
    }

    public static void updateTitles(){
        titleList = new ArrayList<String>();
        titleCoordinates = new HashMap<String, Integer>();

        initTitles();
    }

    public String getIndexId(int yClick){
        for(String title : titleList){
            int titlePos = titleCoordinates.get(title);

            if(yClick >= titlePos && yClick < titlePos + 10)
                return title;
        }
        return "";
    }

    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick){
        int x = (this.width - this.xSize) / 2;
        int y = (actualHeight - this.ySize) / 2;
        if(playlist != null && playlist.getTitles() != null){
            this.scrollY = Math.min(this.scrollY, 0);
            this.scrollY = Math.max(this.scrollY, 47 - (playlist.getTitles().size() * 10 + 2));
        }

        this.drawDefaultBackground();

        ScaledResolution res = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);

        this.ySize = 116;
        if(playlist != null && playlist.getTitles() != null && !playlist.getTitles().isEmpty()){
            GL11.glPushMatrix();
            int yS = (actualHeight / 5);
            Gui.drawRect(x - 1, y - 1, x + this.xSize + 1, y + yS + 1, 0xFFAAAAAA);
            Gui.drawRect(x, y, x + this.xSize, y + yS, 0xFF000000);

            GL11.glScissor(x * res.getScaleFactor(), this.mc.displayHeight - yS * res.getScaleFactor() - y * res.getScaleFactor(), (this.xSize - 5) * res.getScaleFactor(), yS * res.getScaleFactor());
            GL11.glTranslated(0, this.scrollY, 0);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            int index = 0;
            for(String title : playlist.getTitles()){
                String realRecord = Constants.getRealRecordTitle(title);
                if(selectedIndex.equals(realRecord)){
                    Gui.drawRect(x + 2, y + 2 + index * 10, x + this.xSize - 2, y + 2 + (index + 1) * 10, 0xFF00AA00);
                    Gui.drawRect(x + this.xSize - 16, y + 3 + index * 10, x + this.xSize - 6, y + 1 + (index + 1) * 10, 0xFFDD0000);
                }
                this.fontRenderer.drawString(realRecord, x + 3, 3 + y + index * 10, 0xFFFFFFFF);
                index++;
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GL11.glPopMatrix();
        }else{
            this.fontRenderer.drawString("No songs available", x, y, 0xFFFFFFFF);
        }

        super.drawScreen(mouseX, mouseY, partialTick);
    }

    private int getYStartForTitle(){
        if(!titleCoordinates.containsKey(selectedIndex))
            return -1;

        return titleCoordinates.get(selectedIndex);
    }

    private void removeIfClickingRemove(int xMouse, int yMouse){
        if(selectedIndex.isEmpty()) return;
        int realX = (this.width - this.xSize) / 2;
        if(xMouse >= realX + this.xSize - 16 && xMouse < realX + this.xSize - 6){
            int y = getYStartForTitle();
            if(yMouse >= y + 2 && yMouse <= y + 8){
                PacketDispatcher.sendPacketToServer(new PacketRemovePlaylistTitle(playlist.worldObj.provider.dimensionId, playlist.xCoord, playlist.yCoord, playlist.zCoord, selectedIndex).getPacket());
            }
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button){
        super.mouseClicked(x, y, button);
        if(button == 0){
            int realX = (this.width - this.xSize) / 2;
            int realY = (actualHeight - this.ySize) / 2;
            int yS = actualHeight / 5;
            if(x > realX && x < realX + this.xSize && y > realY && y < realY + yS){
                removeIfClickingRemove(x, y - scrollY);
                selectedIndex = getIndexId(y - scrollY);
            }
        }
    }

    @Override
    public void handleMouseInput(){
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if(scroll < 0) scroll = -5;
        if(scroll > 0) scroll = 5;
        this.scrollY += scroll;
        if(playlist != null && playlist.getTitles() != null && !playlist.getTitles().isEmpty()){
            this.scrollY = Math.min(this.scrollY, 0);
            this.scrollY = Math.max(this.scrollY, 47 - (playlist.getTitles().size() * 10 + 2));
        }
    }
}