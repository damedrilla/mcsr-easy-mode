package me.dota1g.mcsreasymode.mixin;

import me.dota1g.mcsreasymode.Mcsreasymode;
import me.dota1g.mcsreasymode.client.AsyncWorldListStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@Mixin(WorldListWidget.class)
public abstract class WorldListWidgetMixin extends AlwaysSelectedEntryListWidget<WorldListWidget.Entry>
        implements AsyncWorldListStatus {
    @Unique
    private static final ExecutorService MCSR_WORLD_LIST_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "MCSR Easy Mode World List");
        thread.setDaemon(true);
        return thread;
    });

    @Shadow
    @Final
    private SelectWorldScreen parent;

    @Shadow
    private List<LevelSummary> levels;

    @Shadow
    public abstract void filter(Supplier<String> searchFilter, boolean load);

    @Unique
    private boolean mcsreasymode$loadingWorlds;

    @Unique
    private int mcsreasymode$loadRequest;

    @Unique
    private Supplier<String> mcsreasymode$latestSearchFilter;

    protected WorldListWidgetMixin(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
    }

    @Inject(method = "filter", at = @At("HEAD"), cancellable = true)
    private void mcsreasymode$loadWorldsAsync(Supplier<String> searchFilter, boolean load, CallbackInfo ci) {
        if (!Mcsreasymode.shouldLoadWorldListAsync()) {
            return;
        }

        this.mcsreasymode$latestSearchFilter = searchFilter;
        boolean hadLoadedLevels = this.levels != null;
        if (hadLoadedLevels && !load) {
            return;
        }

        ci.cancel();
        if (this.mcsreasymode$loadingWorlds) {
            return;
        }

        this.clearEntries();
        if (hadLoadedLevels) {
            this.setSelected(null);
        }
        this.levels = null;
        this.mcsreasymode$loadingWorlds = true;
        int request = ++this.mcsreasymode$loadRequest;

        MinecraftClient client = MinecraftClient.getInstance();
        LevelStorage storage = client.getLevelStorage();
        CompletableFuture
                .supplyAsync(() -> mcsreasymode$readLevelList(storage), MCSR_WORLD_LIST_EXECUTOR)
                .whenComplete((loadedLevels, throwable) ->
                        client.execute(() -> this.mcsreasymode$finishWorldLoad(client, request, loadedLevels, throwable)));
    }

    @Unique
    private static List<LevelSummary> mcsreasymode$readLevelList(LevelStorage storage) {
        try {
            List<LevelSummary> loadedLevels = new ArrayList<>(storage.getLevelList());
            Collections.sort(loadedLevels);
            return loadedLevels;
        } catch (LevelStorageException exception) {
            throw new CompletionException(exception);
        }
    }

    @Unique
    private void mcsreasymode$finishWorldLoad(
            MinecraftClient client,
            int request,
            List<LevelSummary> loadedLevels,
            Throwable throwable
    ) {
        if (request != this.mcsreasymode$loadRequest) {
            return;
        }

        this.mcsreasymode$loadingWorlds = false;
        if (client.currentScreen != this.parent) {
            return;
        }

        if (throwable != null) {
            Throwable cause = throwable instanceof CompletionException && throwable.getCause() != null
                    ? throwable.getCause()
                    : throwable;
            Mcsreasymode.LOGGER.error("Couldn't load level list asynchronously", cause);
            String message = cause.getMessage() == null ? cause.toString() : cause.getMessage();
            client.openScreen(new FatalErrorScreen(
                    new TranslatableText("selectWorld.unable_to_load"),
                    new LiteralText(message)
            ));
            return;
        }

        this.levels = loadedLevels;
        Supplier<String> searchFilter = this.mcsreasymode$latestSearchFilter;
        if (searchFilter != null) {
            this.filter(searchFilter, false);
        }
    }

    @Override
    public boolean mcsreasymode$isLoadingWorlds() {
        return this.mcsreasymode$loadingWorlds;
    }
}
