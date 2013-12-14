package jkmau5.modjam.radiomod.tile;

import jkmau5.modjam.radiomod.network.RadioNetwork;
import jkmau5.modjam.radiomod.util.CableConnections;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

/**
 * Author: Lordmau5
 * Date: 14.12.13
 * Time: 14:53
 * You are allowed to change this code,
 * however, not to publish it without my permission.
 */
public class TileEntityCable extends TileEntity {

    private CableConnections connections;
    private RadioNetwork network;

    public TileEntityCable() {
        this.connections = new CableConnections();
    }

    public RadioNetwork getNetwork() {
        return this.network;
    }

    public void setNetwork(RadioNetwork network) {
        System.out.println("New network: " + network.toString());
        this.network = network;
    }

    public CableConnections getConnections() {
        return this.connections;
    }

    public void validate() {
        super.validate();
        this.network = new RadioNetwork(this);
    }

    public void onNeighborTileChange() {
        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tile = worldObj.getBlockTileEntity(this.xCoord + dir.offsetX, this.yCoord + dir.offsetY, this.zCoord + dir.offsetZ);

            boolean connect = false;
            if(isValidTile(tile))
                connect = true;

            connections.setConnected(dir, connect);
        }
    }

    public void tryMergeWithNeighbors() {
        if(worldObj.isRemote)
            return;

        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tile = worldObj.getBlockTileEntity(this.xCoord + dir.offsetX, this.yCoord + dir.offsetY, this.zCoord + dir.offsetZ);

            if(tile != null && tile instanceof TileEntityCable) {
                ((TileEntityCable)tile).getNetwork().mergeWithNetwork(getNetwork());
            }
        }
    }

    private boolean isValidTile(TileEntity tile) {
        if(tile == null)
            return false;
        if(tile instanceof TileEntityCable || tile instanceof TileEntityRadio)
            return true;
        return false;
    }
}