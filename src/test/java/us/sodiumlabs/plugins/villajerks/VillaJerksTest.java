package us.sodiumlabs.plugins.villajerks;

import org.junit.Test;

import org.slf4j.Logger;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VillaJerksTest {
    @Test
    public void buildVillagerName_zeroeth() throws Exception {
        final Logger logger = mock(Logger.class);
        final AssetManager assetManager = initializeAssetManager();

        final VillaJerks villaJerks = new VillaJerks(logger, assetManager);
        villaJerks.onGamePreInitializationEvent(null);

        final UUID uuid = new UUID(0L, 0L);
        final Villager villager = mock(Villager.class);
        when(villager.getUniqueId()).thenReturn(uuid);

        final Text text = villaJerks.buildVillagerName(villager);

        assertEquals("Text{Ada Adams}", text.toString());
    }

    @Test
    public void buildVillagerName_first() throws Exception {
        final Logger logger = mock(Logger.class);
        final AssetManager assetManager = initializeAssetManager();

        final VillaJerks villaJerks = new VillaJerks(logger, assetManager);
        villaJerks.onGamePreInitializationEvent(null);

        final UUID uuid = new UUID(1L, 1L);
        final Villager villager = mock(Villager.class);
        when(villager.getUniqueId()).thenReturn(uuid);

        final Text text = villaJerks.buildVillagerName(villager);

        assertEquals("Text{Agnes Alexander}", text.toString());
    }

    @Test
    public void buildVillagerName_last() throws Exception {
        final Logger logger = mock(Logger.class);
        final AssetManager assetManager = initializeAssetManager();

        final VillaJerks villaJerks = new VillaJerks(logger, assetManager);
        villaJerks.onGamePreInitializationEvent(null);

        final UUID uuid = new UUID(317L, 99L);
        final Villager villager = mock(Villager.class);
        when(villager.getUniqueId()).thenReturn(uuid);

        final Text text = villaJerks.buildVillagerName(villager);

        assertEquals("Text{Willie Young}", text.toString());
    }

    @Test
    public void buildVillagerName_zeroeth_repeat() throws Exception {
        final Logger logger = mock(Logger.class);
        final AssetManager assetManager = initializeAssetManager();

        final VillaJerks villaJerks = new VillaJerks(logger, assetManager);
        villaJerks.onGamePreInitializationEvent(null);

        final UUID uuid = new UUID(318L, 100L);
        final Villager villager = mock(Villager.class);
        when(villager.getUniqueId()).thenReturn(uuid);

        final Text text = villaJerks.buildVillagerName(villager);

        assertEquals("Text{Ada Adams}", text.toString());
    }

    @Test
    public void buildVillagerName_last_neg() throws Exception {
        final Logger logger = mock(Logger.class);
        final AssetManager assetManager = initializeAssetManager();

        final VillaJerks villaJerks = new VillaJerks(logger, assetManager);
        villaJerks.onGamePreInitializationEvent(null);

        final UUID uuid = new UUID(-1L, -1L);
        final Villager villager = mock(Villager.class);
        when(villager.getUniqueId()).thenReturn(uuid);

        final Text text = villaJerks.buildVillagerName(villager);

        assertEquals("Text{Willie Young}", text.toString());
    }

    private AssetManager initializeAssetManager() throws IOException, URISyntaxException {
        final AssetManager assetManager = mock(AssetManager.class);

        final Optional<Asset> firstAsset = initializeAsset("assets/villajerks/first.txt");
        when(assetManager.getAsset(any(), eq("first.txt"))).thenReturn(firstAsset);
        final Optional<Asset> lastAsset = initializeAsset("assets/villajerks/last.txt");
        when(assetManager.getAsset(any(), eq("last.txt"))).thenReturn(lastAsset);

        return assetManager;
    }

    private Optional<Asset> initializeAsset(final String fileName) throws URISyntaxException, IOException {
        final Asset asset = mock(Asset.class);

        final URL url = ClassLoader.getSystemResource(fileName);
        final List<String> allLines = Files.readAllLines(Paths.get(url.toURI()));
        when(asset.getUrl()).thenReturn(url);
        when(asset.readLines()).thenReturn(allLines);

        return Optional.of(asset);
    }
}