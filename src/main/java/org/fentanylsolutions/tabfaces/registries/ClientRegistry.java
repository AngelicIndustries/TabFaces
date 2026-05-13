package org.fentanylsolutions.tabfaces.registries;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import org.fentanylsolutions.tabfaces.Config;
import org.fentanylsolutions.tabfaces.TabFaces;
import org.fentanylsolutions.tabfaces.compat.LoadedMods;
import org.fentanylsolutions.tabfaces.compat.skinport.SkinPortCompat;
import org.fentanylsolutions.tabfaces.compat.wawelauth.WawelAuthCompat;
import org.fentanylsolutions.tabfaces.util.ISkinCallbackMarker;
import org.fentanylsolutions.tabfaces.util.PingUtil;
import org.fentanylsolutions.tabfaces.util.Util;
import org.fentanylsolutions.tabfaces.varinstances.VarInstanceClient;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

public class ClientRegistry {

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
        1,
        2,
        60L,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(),
        new java.util.concurrent.ThreadFactory() {

            private final AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "TabFaces-Worker-" + counter.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        });

    private final Map<String, Data> byName = new ConcurrentHashMap<>();
    private final Map<UUID, Data> byUuid = new ConcurrentHashMap<>();
    private final AtomicBoolean pingInFlight = new AtomicBoolean(false);
    private volatile long lastPingTimestamp = 0;
    private float tickCounter;

    public Data getByDisplayName(String displayName) {
        return byName.get(nameKey(displayName));
    }

    public void insertOrUpdateFromPing(String displayName, UUID id, boolean removeAfterTTL, int ttl) {
        Data data = getOrCreate(displayName, id, removeAfterTTL, ttl);
        if (id == null) {
            markPlaceholder(data);
        }
    }

    public void insertProfile(GameProfile profile, boolean removeAfterTTL, int ttl) {
        if (profile == null || profile.getName() == null) {
            return;
        }
        if (isMetadataProfile(profile)) {
            TabFaces.debug("Skipping formatted server status profile: " + profile.getName());
            return;
        }

        Data data = getOrCreate(profile.getName(), profile.getId(), removeAfterTTL, ttl);
        synchronized (data) {
            data.displayName = profile.getName();
            data.profile = preferFullProfile(data.profile, profile);
            if (hasTextures(data.profile)) {
                data.skinResourceLocation = null;
                data.realSkin = false;
            }
        }
    }

    public ResourceLocation getTabMenuResourceLocation(String displayName, boolean removeAfterTTL, int ttl) {
        if (displayName == null || displayName.trim()
            .isEmpty()) {
            return getDefaultSkin();
        }

        if (VarInstanceClient.minecraftRef.thePlayer != null
            && VarInstanceClient.minecraftRef.thePlayer.getDisplayName()
                .equals(displayName)) {
            return VarInstanceClient.minecraftRef.thePlayer.getLocationSkin();
        }

        Data data = getOrCreate(displayName, null, removeAfterTTL, ttl);
        resolve(data, false);
        return data.skinResourceLocation != null ? data.skinResourceLocation : getDefaultSkin();
    }

    public ResourceLocation getServerMenuResourceLocation(String displayName, ServerData serverData,
        boolean removeAfterTTL, int ttl) {
        if (isMetadataProfileName(displayName)) {
            return null;
        }
        if (serverData == null) {
            return getTabMenuResourceLocation(displayName, removeAfterTTL, ttl);
        }

        Data data = getByDisplayName(displayName);
        if (data == null) {
            return null;
        }
        data.touch(removeAfterTTL, ttl);

        if (LoadedMods.wawelAuthLoaded) {
            if (data.id == null) {
                return null;
            }
            ResourceLocation rl = WawelAuthCompat.getSkinForServerTooltip(data.id, data.displayName, serverData);
            if (rl == null) {
                return null;
            }
            markResolved(data, rl, !WawelAuthCompat.isPlaceholder(rl));
            return rl;
        }

        resolve(data, true);
        return data.skinResourceLocation != null ? data.skinResourceLocation : getDefaultSkin();
    }

    private void resolve(Data data, boolean serverMenu) {
        data.lastAccessTimestamp = System.currentTimeMillis();
        clearStalePlaceholder(data);

        if (LoadedMods.wawelAuthLoaded && !serverMenu && data.id != null) {
            ResourceLocation rl = WawelAuthCompat.getSkin(data.id, data.displayName);
            markResolved(
                data,
                WawelAuthCompat.isPlaceholder(rl) ? getDefaultSkin() : rl,
                !WawelAuthCompat.isPlaceholder(rl));
            return;
        }

        if (tryUseLivePlayer(data)) {
            return;
        }
        if (data.id == null) {
            maybeSubmitPingTask(data.displayName);
            return;
        }
        if (LoadedMods.skinPortLoaded && data.profile != null) {
            ResourceLocation skinPortSkin = SkinPortCompat.getSkin(data.profile);
            if (skinPortSkin != null) {
                markResolved(data, skinPortSkin, true);
            } else if (data.skinResourceLocation == null) {
                data.skinResourceLocation = getDefaultSkin();
            }
            return;
        }
        if (data.skinLoading) {
            if (shouldRetry(data)) {
                markPlaceholder(data);
            }
            return;
        }
        if (!hasTextures(data.profile)) {
            maybeResolveProfile(data);
            if (LoadedMods.customSkinLoaderLoaded && data.profile != null && data.skinResourceLocation == null) {
                maybeRequestVanillaSkin(data);
            }
            return;
        }
        if (data.skinResourceLocation == null) {
            maybeRequestVanillaSkin(data);
        }
    }

    private boolean tryUseLivePlayer(Data data) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            return false;
        }

        List players = mc.theWorld.playerEntities;
        for (Object object : players) {
            if (!(object instanceof AbstractClientPlayer)) {
                continue;
            }

            AbstractClientPlayer player = (AbstractClientPlayer) object;
            GameProfile profile = ((EntityPlayer) player).getGameProfile();
            if (!matches(data, profile)) {
                continue;
            }

            insertProfile(profile, data.removeAfterTTL, data.ttl);
            if (player.func_152123_o()) {
                markResolved(data, player.getLocationSkin(), true);
            }
            return true;
        }
        return false;
    }

    private void maybeResolveProfile(Data data) {
        synchronized (data) {
            if (data.profileLoading || !shouldRetry(data)) {
                return;
            }
            data.profileLoading = true;
            data.lastAttemptTimestamp = System.currentTimeMillis();
        }

        EXECUTOR.submit(() -> {
            GameProfile profile = Util.getFullProfile(data.id, data.displayName);
            synchronized (data) {
                data.profileLoading = false;
                data.profile = preferFullProfile(data.profile, profile);
                if (hasTextures(profile)) {
                    data.skinLoading = false;
                    data.skinResourceLocation = null;
                }
            }

            if (hasTextures(profile) || LoadedMods.customSkinLoaderLoaded) {
                maybeRequestVanillaSkin(data);
            } else {
                markPlaceholder(data);
            }
        });
    }

    private void maybeRequestVanillaSkin(Data data) {
        final GameProfile profile;
        final int requestId;
        synchronized (data) {
            if (data.skinLoading || data.profile == null) {
                return;
            }
            if (!hasTextures(data.profile) && !LoadedMods.customSkinLoaderLoaded) {
                return;
            }
            data.skinLoading = true;
            data.lastAttemptTimestamp = System.currentTimeMillis();
            requestId = ++data.requestId;
            profile = data.profile;
        }

        Minecraft.getMinecraft()
            .func_152342_ad()
            .func_152790_a(profile, new RegistrySkinCallback(data, requestId), true);
    }

    private void maybeSubmitPingTask(String targetDisplayName) {
        ServerData currentServer = Minecraft.getMinecraft()
            .func_147104_D();
        if (currentServer == null || currentServer.serverIP == null
            || currentServer.serverIP.trim()
                .isEmpty()) {
            TabFaces.debug("Skipping server ping because no current server is available.");
            return;
        }
        if (System.currentTimeMillis() - lastPingTimestamp <= 5000 || !pingInFlight.compareAndSet(false, true)) {
            return;
        }

        EXECUTOR.submit(() -> {
            try {
                lastPingTimestamp = System.currentTimeMillis();
                PingUtil.pingServer(new PingUtil.ServerStatusCallbackClientRegistry(), targetDisplayName);
            } catch (Exception e) {
                TabFaces.error("Server ping failed: " + e.getMessage());
            } finally {
                pingInFlight.set(false);
            }
        });
    }

    private Data getOrCreate(String displayName, UUID id, boolean removeAfterTTL, int ttl) {
        Data data = id == null ? null : byUuid.get(id);
        if (data == null) {
            data = byName.get(nameKey(displayName));
        }
        if (data == null) {
            Data created = new Data(displayName, removeAfterTTL, ttl);
            Data raced = byName.putIfAbsent(nameKey(displayName), created);
            data = raced == null ? created : raced;
        }

        data.touch(removeAfterTTL, ttl);
        if (id != null) {
            linkUuid(data, id);
        }
        byName.put(nameKey(displayName), data);
        return data;
    }

    private void linkUuid(Data data, UUID id) {
        synchronized (data) {
            if (data.id != null && !data.id.equals(id)) {
                byUuid.remove(data.id, data);
            }
            data.id = id;
            if (data.profile == null) {
                data.profile = new GameProfile(id, data.displayName);
            }
            byUuid.put(id, data);
        }
    }

    private void markResolved(Data data, ResourceLocation resourceLocation, boolean realSkin) {
        synchronized (data) {
            data.skinResourceLocation = resourceLocation;
            data.realSkin = realSkin;
            data.skinLoading = false;
            data.lastAttemptTimestamp = System.currentTimeMillis();
            data.lastResolvedTimestamp = data.lastAttemptTimestamp;
        }
    }

    private void markPlaceholder(Data data) {
        markResolved(data, getDefaultSkin(), false);
    }

    private void clearStalePlaceholder(Data data) {
        if (!data.realSkin && data.skinResourceLocation != null && shouldRetry(data)) {
            synchronized (data) {
                data.skinResourceLocation = null;
                data.skinLoading = false;
            }
        }
    }

    private boolean shouldRetry(Data data) {
        return System.currentTimeMillis() - data.lastAttemptTimestamp >= Config.failedRetrySeconds * 1000L;
    }

    private boolean matches(Data data, GameProfile profile) {
        if (profile == null) {
            return false;
        }
        if (data.id != null && data.id.equals(profile.getId())) {
            return true;
        }
        return nameKey(data.displayName).equals(nameKey(profile.getName()));
    }

    private static GameProfile preferFullProfile(GameProfile current, GameProfile incoming) {
        if (incoming == null) {
            return current;
        }
        return !hasTextures(current) && hasTextures(incoming) ? incoming : current;
    }

    private static boolean hasTextures(GameProfile profile) {
        return profile != null && profile.getProperties() != null
            && profile.getProperties()
                .containsKey("textures");
    }

    private static boolean isMetadataProfile(GameProfile profile) {
        return isMetadataProfileName(profile.getName());
    }

    private static boolean isMetadataProfileName(String name) {
        return name != null && name.indexOf('\u00A7') >= 0;
    }

    private ResourceLocation getDefaultSkin() {
        return Config.showQuestionMarkIfUnknown ? TabFaces.varInstanceClient.defaultResourceLocation
            : AbstractClientPlayer.locationStevePng;
    }

    private static String nameKey(String displayName) {
        return displayName == null ? ""
            : StringUtils.stripControlCodes(displayName)
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    public void clear() {
        byName.clear();
        byUuid.clear();
    }

    public void tick() {
        if (++tickCounter / 20.0 < Config.skinTtlInterval) {
            return;
        }
        tickCounter = 0;

        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Data>> iterator = byName.entrySet()
            .iterator();
        while (iterator.hasNext()) {
            Data data = iterator.next()
                .getValue();
            long timestamp = data.removeAfterTTL ? data.lastAccessTimestamp : data.lastResolvedTimestamp;
            if ((now - timestamp) / 1000 <= data.ttl) {
                continue;
            }

            if (data.removeAfterTTL) {
                iterator.remove();
                if (data.id != null) {
                    byUuid.remove(data.id, data);
                }
            } else {
                synchronized (data) {
                    data.skinResourceLocation = null;
                    data.realSkin = false;
                    data.skinLoading = false;
                }
            }
        }
    }

    private static class RegistrySkinCallback implements SkinManager.SkinAvailableCallback, ISkinCallbackMarker {

        private final Data data;
        private final int requestId;

        private RegistrySkinCallback(Data data, int requestId) {
            this.data = data;
            this.requestId = requestId;
        }

        @Override
        public void func_152121_a(MinecraftProfileTexture.Type skinPart, ResourceLocation skinLoc) {
            if (skinPart != MinecraftProfileTexture.Type.SKIN || skinLoc == null) {
                return;
            }
            synchronized (data) {
                if (requestId == data.requestId) {
                    data.skinResourceLocation = skinLoc;
                    data.realSkin = true;
                    data.skinLoading = false;
                    data.lastResolvedTimestamp = System.currentTimeMillis();
                }
            }
        }
    }

    public static class Data {

        public volatile String displayName;
        public volatile UUID id;
        public volatile ResourceLocation skinResourceLocation;
        volatile GameProfile profile;
        volatile boolean realSkin;
        volatile boolean profileLoading;
        volatile boolean skinLoading;
        volatile int requestId;
        volatile long lastAttemptTimestamp;
        volatile long lastAccessTimestamp;
        volatile long lastResolvedTimestamp;
        boolean removeAfterTTL;
        int ttl;

        Data(String displayName, boolean removeAfterTTL, int ttl) {
            long now = System.currentTimeMillis();
            this.displayName = displayName;
            this.lastAccessTimestamp = now;
            this.lastResolvedTimestamp = now;
            this.removeAfterTTL = removeAfterTTL;
            this.ttl = ttl == -1 ? Config.skinTtl : ttl;
        }

        void touch(boolean removeAfterTTL, int ttl) {
            this.lastAccessTimestamp = System.currentTimeMillis();
            if (!removeAfterTTL) {
                this.removeAfterTTL = false;
                this.ttl = ttl == -1 ? Config.skinTtl : ttl;
            } else if (this.removeAfterTTL && ttl != -1) {
                this.ttl = ttl;
            }
        }

        public boolean hasRealSkin() {
            return realSkin && skinResourceLocation != null;
        }
    }
}
