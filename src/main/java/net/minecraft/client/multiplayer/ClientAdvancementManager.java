package net.minecraft.client.multiplayer;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.AdvancementToast;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.CPacketSeenAdvancements;
import net.minecraft.network.play.server.SPacketAdvancementInfo;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientAdvancementManager
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft mc;
    private final AdvancementList advancementList = new AdvancementList();
    private final Map<Advancement, AdvancementProgress> advancementToProgress = Maps.<Advancement, AdvancementProgress>newHashMap();
    @Nullable
    private ClientAdvancementManager.IListener listener;
    @Nullable
    private Advancement selectedTab;

    public ClientAdvancementManager(Minecraft p_i47380_1_)
    {
        mc = p_i47380_1_;
    }

    public void read(SPacketAdvancementInfo p_192799_1_)
    {
        if (p_192799_1_.isFirstSync())
        {
            advancementList.clear();
            advancementToProgress.clear();
        }

        advancementList.removeAll(p_192799_1_.getAdvancementsToRemove());
        advancementList.loadAdvancements(p_192799_1_.getAdvancementsToAdd());

        for (Entry<ResourceLocation, AdvancementProgress> entry : p_192799_1_.getProgressUpdates().entrySet())
        {
            Advancement advancement = advancementList.getAdvancement(entry.getKey());

            if (advancement != null)
            {
                AdvancementProgress advancementprogress = entry.getValue();
                advancementprogress.update(advancement.getCriteria(), advancement.getRequirements());
                advancementToProgress.put(advancement, advancementprogress);

                if (listener != null)
                {
                    listener.onUpdateAdvancementProgress(advancement, advancementprogress);
                }

                if (!p_192799_1_.isFirstSync() && advancementprogress.isDone() && advancement.getDisplay() != null && advancement.getDisplay().shouldShowToast())
                {
                    mc.getToastGui().add(new AdvancementToast(advancement));
                }
            }
            else
            {
                LOGGER.warn("Server informed client about progress for unknown advancement " + entry.getKey());
            }
        }
    }

    public AdvancementList getAdvancementList()
    {
        return advancementList;
    }

    public void setSelectedTab(@Nullable Advancement p_194230_1_, boolean tellServer)
    {
        NetHandlerPlayClient nethandlerplayclient = mc.getConnection();

        if (nethandlerplayclient != null && p_194230_1_ != null && tellServer)
        {
            nethandlerplayclient.sendPacket(CPacketSeenAdvancements.openedTab(p_194230_1_));
        }

        if (selectedTab != p_194230_1_)
        {
            selectedTab = p_194230_1_;

            if (listener != null)
            {
                listener.setSelectedTab(p_194230_1_);
            }
        }
    }

    public void setListener(@Nullable ClientAdvancementManager.IListener p_192798_1_)
    {
        listener = p_192798_1_;
        advancementList.setListener(p_192798_1_);

        if (p_192798_1_ != null)
        {
            for (Entry<Advancement, AdvancementProgress> entry : advancementToProgress.entrySet())
            {
                p_192798_1_.onUpdateAdvancementProgress(entry.getKey(), entry.getValue());
            }

            p_192798_1_.setSelectedTab(selectedTab);
        }
    }

    public interface IListener extends AdvancementList.Listener
    {
        void onUpdateAdvancementProgress(Advancement p_191933_1_, AdvancementProgress p_191933_2_);

        void setSelectedTab(Advancement p_193982_1_);
    }
}
