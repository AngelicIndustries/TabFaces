package org.fentanylsolutions.tabfaces.compat.wawelauth;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.ResourceLocation;

import org.fentanylsolutions.tabfaces.TabFaces;
import org.fentanylsolutions.wawelauth.api.WawelFaceRendererClient;
import org.fentanylsolutions.wawelauth.api.WawelTextureResolver;
import org.fentanylsolutions.wawelauth.wawelclient.IServerDataExt;
import org.fentanylsolutions.wawelauth.wawelclient.WawelClient;
import org.fentanylsolutions.wawelauth.wawelclient.data.ClientAccount;
import org.fentanylsolutions.wawelauth.wawelclient.data.ClientProvider;

public class WawelAuthCompat {

    private static final Set<UUID> CLEARED_EXACT_LOOKUPS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Resolve a skin for a player while in-game.
     * Uses WawelClient.resolvePlayerProvider to find the right provider.
     */
    public static ResourceLocation getSkin(UUID uuid, String displayName) {
        WawelClient client = WawelClient.instance();
        if (client == null) {
            return null;
        }

        try {
            ClientProvider provider = client.resolvePlayerProvider(uuid);
            if (provider == null) {
                return null;
            }
            return client.getTextureResolver()
                .getSkin(uuid, displayName, effectiveProvider(client, provider), false);
        } catch (Exception e) {
            TabFaces.debug("WawelAuth skin lookup failed for " + displayName + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Resolve a skin for a player shown in the server selection tooltip.
     *
     * <ul>
     * <li>Local WawelAuth account: use its exact account provider.</li>
     * <li>Bound server account: use that exact account provider.</li>
     * <li>Unknown account/provider: don't fetch (returns null).</li>
     * </ul>
     */
    public static ResourceLocation getSkinForServerTooltip(UUID uuid, String displayName, ServerData serverData) {
        WawelClient client = WawelClient.instance();
        if (client == null || uuid == null) {
            return null;
        }

        try {
            ClientProvider accountProvider = getLocalAccountProvider(client, uuid);
            if (accountProvider != null) {
                return getExactSkin(client, uuid, displayName, accountProvider);
            }

            ClientProvider boundProvider = getBoundAccountProvider(client, serverData, uuid);
            if (boundProvider != null) {
                return getExactSkin(client, uuid, displayName, boundProvider);
            }

            return null;
        } catch (Exception e) {
            TabFaces.debug("WawelAuth server tooltip skin lookup failed for " + displayName + ": " + e.getMessage());
            return null;
        }
    }

    public static boolean isPlaceholder(ResourceLocation resourceLocation) {
        return resourceLocation == null || WawelTextureResolver.getDefaultSkin()
            .equals(resourceLocation);
    }

    public static void drawFace(ResourceLocation resourceLocation, float xPos, float yPos, float alpha) {
        WawelFaceRendererClient.drawFace(resourceLocation, xPos, yPos, alpha);
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    private static ClientProvider getLocalAccountProvider(WawelClient client, UUID uuid) {
        for (ClientAccount account : client.getAccountManager()
            .listAccounts()) {
            if (account != null && uuid.equals(account.getProfileUuid())) {
                return resolveProviderByName(client, account.getProviderName());
            }
        }
        return null;
    }

    private static ResourceLocation getExactSkin(WawelClient client, UUID uuid, String displayName,
        ClientProvider provider) {
        if (CLEARED_EXACT_LOOKUPS.add(uuid)) {
            client.getTextureResolver()
                .invalidate(uuid);
        }
        return client.getTextureResolver()
            .getSkin(uuid, displayName, provider, false);
    }

    private static String normalizeString(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Get the provider for the account bound to this server entry, or null.
     */
    private static ClientProvider getBoundAccountProvider(WawelClient client, ServerData serverData, UUID uuid) {
        if (!(serverData instanceof IServerDataExt)) {
            return null;
        }

        IServerDataExt ext = (IServerDataExt) serverData;
        long accountId = ext.getWawelAccountId();
        if (accountId < 0) {
            return null;
        }

        ClientAccount account = client.getAccountManager()
            .getAccount(accountId);
        if (account == null) {
            return null;
        }
        if (uuid != null && !uuid.equals(account.getProfileUuid())) {
            return null;
        }

        return resolveProviderByName(client, account.getProviderName());
    }

    private static ClientProvider resolveProviderByName(WawelClient client, String providerName) {
        String name = normalizeString(providerName);
        if (name == null) {
            return null;
        }

        ClientProvider exact = client.resolveProviderByName(name);
        if (exact != null) {
            return effectiveProvider(client, exact);
        }

        for (ClientProvider provider : client.getProviderRegistry()
            .listProviders()) {
            if (provider != null && name.equalsIgnoreCase(normalizeString(provider.getName()))) {
                return effectiveProvider(client, provider);
            }
        }
        return null;
    }

    private static ClientProvider effectiveProvider(WawelClient client, ClientProvider provider) {
        return client.getProviderRegistry()
            .applyRuntimeOverrides(provider);
    }
}
