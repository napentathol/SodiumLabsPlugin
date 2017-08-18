package us.sodiumlabs.plugins.villajerks;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.item.merchant.VillagerRegistry;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VillaJerksTest {

    @Mock
    private Game game;

    @Mock
    private Logger logger;

    @Mock
    private GameRegistry gameRegistry;

    @Mock
    private Career nitwit;

    @Mock
    private Random random;

    @Mock
    private AssetManager assetManager;

    @Mock
    private VillagerRegistry villagerRegistry;

    @InjectMocks
    private VillaJerks villaJerks;

    @Before
    public void initialize() throws Exception {
        initializeAssetManager();
        initializeGame();
    }

    private void initializeGame() {
        when(game.getRegistry()).thenReturn(gameRegistry);
        when(gameRegistry.getType(Career.class, VillaJerks.NITWIT_ID)).thenReturn(Optional.of(nitwit));
        when(gameRegistry.getVillagerRegistry()).thenReturn(villagerRegistry);
    }

    @Test
    public void buildVillagerName_zeroeth() throws Exception {
        villaJerks.onGamePreInitializationEvent(null);

        final UUID uuid = new UUID(0L, 0L);
        final Villager villager = mock(Villager.class);
        when(villager.getUniqueId()).thenReturn(uuid);

        final Text text = villaJerks.buildVillagerName(villager);

        assertEquals("Text{Ada Adams}", text.toString());
    }

    @Test
    public void buildVillagerName_first() throws Exception {
        villaJerks.onGamePreInitializationEvent(null);

        final UUID uuid = new UUID(1L, 1L);
        final Villager villager = mock(Villager.class);
        when(villager.getUniqueId()).thenReturn(uuid);

        final Text text = villaJerks.buildVillagerName(villager);

        assertEquals("Text{Agnes Alexander}", text.toString());
    }

    @Test
    public void buildVillagerName_last() throws Exception {
        villaJerks.onGamePreInitializationEvent(null);

        final UUID uuid = new UUID(317L, 99L);
        final Villager villager = mock(Villager.class);
        when(villager.getUniqueId()).thenReturn(uuid);

        final Text text = villaJerks.buildVillagerName(villager);

        assertEquals("Text{Willie Young}", text.toString());
    }

    @Test
    public void buildVillagerName_zeroeth_repeat() throws Exception {
        villaJerks.onGamePreInitializationEvent(null);

        final UUID uuid = new UUID(318L, 100L);
        final Villager villager = mock(Villager.class);
        when(villager.getUniqueId()).thenReturn(uuid);

        final Text text = villaJerks.buildVillagerName(villager);

        assertEquals("Text{Ada Adams}", text.toString());
    }

    @Test
    public void buildVillagerName_last_neg() throws Exception {
        villaJerks.onGamePreInitializationEvent(null);

        final UUID uuid = new UUID(-1L, -1L);
        final Villager villager = mock(Villager.class);
        when(villager.getUniqueId()).thenReturn(uuid);

        final Text text = villaJerks.buildVillagerName(villager);

        assertEquals("Text{Willie Young}", text.toString());
    }

    private void initializeAssetManager() throws IOException, URISyntaxException {
        final Optional<Asset> firstAsset = initializeAsset("assets/villajerks/first.txt");
        when(assetManager.getAsset(any(), eq("first.txt"))).thenReturn(firstAsset);
        final Optional<Asset> lastAsset = initializeAsset("assets/villajerks/last.txt");
        when(assetManager.getAsset(any(), eq("last.txt"))).thenReturn(lastAsset);
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