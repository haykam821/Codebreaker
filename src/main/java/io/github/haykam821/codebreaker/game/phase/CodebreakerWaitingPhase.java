package io.github.haykam821.codebreaker.game.phase;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import io.github.haykam821.codebreaker.game.code.Code;
import io.github.haykam821.codebreaker.game.map.CodebreakerMap;
import io.github.haykam821.codebreaker.game.map.CodebreakerMapBuilder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class CodebreakerWaitingPhase {
	private static final Formatting GUIDE_FORMATTING = Formatting.GOLD;
	private static final Text GUIDE_TEXT = Text.empty()
		.append(Text.translatable("gameType.codebreaker.codebreaker").formatted(Formatting.BOLD))
		.append(ScreenTexts.LINE_BREAK)
		.append(Text.translatable("text.codebreaker.guide"))
		.formatted(GUIDE_FORMATTING);

	private final GameSpace gameSpace;
	private final ServerWorld world;
	private final CodebreakerMap map;
	private final CodebreakerConfig config;

	private final Code correctCode;
	private final boolean duplicatePegs;

	private HolderAttachment guideText;

	public CodebreakerWaitingPhase(GameSpace gameSpace, ServerWorld world, CodebreakerMap map, CodebreakerConfig config, Code correctCode, boolean duplicatePegs) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.map = map;
		this.config = config;

		this.correctCode = correctCode;
		this.duplicatePegs = duplicatePegs;
	}

	public static GameOpenProcedure open(GameOpenContext<CodebreakerConfig> context) {
		Random random = Random.createLocal();
		CodebreakerConfig config = context.config();

		Code correctCode = config.getCodeProvider().generate(random, config);
		boolean duplicatePegs = config.getCodeProvider().hasDuplicatePegs(config);

		CodebreakerMapBuilder mapBuilder = new CodebreakerMapBuilder(config);
		CodebreakerMap map = mapBuilder.create(random, correctCode, context.server().getRegistryManager(), config.getCodePegs());

		RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
			.setGenerator(map.createGenerator(context.server()));

		return context.openWithWorld(worldConfig, (activity, world) -> {
			CodebreakerWaitingPhase waiting = new CodebreakerWaitingPhase(activity.getGameSpace(), world, map, config, correctCode, duplicatePegs);

			GameWaitingLobby.addTo(activity, config.getPlayerConfig());
			CodebreakerActivePhase.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.TICK, waiting::tick);
			activity.listen(GameActivityEvents.ENABLE, waiting::open);
			activity.listen(GamePlayerEvents.ADD, waiting::addPlayer);
			activity.listen(PlayerDeathEvent.EVENT, waiting::onPlayerDeath);
			activity.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
			activity.listen(GamePlayerEvents.ACCEPT, waiting::onAcceptPlayers);
			activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
		});
	}

	public JoinAcceptorResult onAcceptPlayers(JoinAcceptor acceptor) {
		return acceptor.teleport(this.world, this.map.getSpawnPos()).thenRunForEach(player -> {
			player.changeGameMode(GameMode.ADVENTURE);
		});
	}

	public GameResult requestStart() {
		CodebreakerActivePhase.open(this.gameSpace, this.world, this.map, this.config, this.guideText, this.correctCode, this.duplicatePegs);
		return GameResult.ok();
	}

	public void addPlayer(ServerPlayerEntity player) {
		CodebreakerActivePhase.spawn(this.world, this.map, player);
	}

	public EventResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		CodebreakerActivePhase.spawn(this.world, this.map, player);
		return EventResult.ALLOW;
	}

	public void tick() {
		for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
			if (this.map.isBelowPlatform(player)) {
				CodebreakerActivePhase.spawn(this.world, this.map, player);
			}
		}
	}

	private void open() {
		TextDisplayElement element = new TextDisplayElement(GUIDE_TEXT);

		element.setBillboardMode(BillboardMode.CENTER);
		element.setLineWidth(450);

		ElementHolder holder = new ElementHolder();
		holder.addElement(element);

		// Spawn guide text
		Vec3d center = new Vec3d(this.map.getBounds().center().getX(), this.map.getBounds().min().getY() + 2, this.map.getBounds().max().getZ());
		this.guideText = ChunkAttachment.of(holder, world, center);
	}
}