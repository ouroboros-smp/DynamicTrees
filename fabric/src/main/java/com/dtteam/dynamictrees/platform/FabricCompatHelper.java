package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.DynamicTrees;
import net.fabricmc.loader.api.FabricLoader;
import com.dtteam.dynamictrees.platform.services.ICompatHelper;

public class FabricCompatHelper implements ICompatHelper {

    @Override
    public void registerSeasonProvider(String modId) {
        if (FabricLoader.getInstance().isModLoaded(modId)) {
            DynamicTrees.LOG.warn("Season integration for {} is disabled on the experimental Fabric 26.2 port.", modId);
        }
    }

}
