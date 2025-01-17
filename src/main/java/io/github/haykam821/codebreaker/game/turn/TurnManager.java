package io.github.haykam821.codebreaker.game.turn;

import io.github.haykam821.codebreaker.game.phase.CodebreakerActivePhase;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public abstract class TurnManager {
	protected final CodebreakerActivePhase phase;

	public TurnManager(CodebreakerActivePhase phase) {
		this.phase = phase;
	}

	public abstract ServerPlayerEntity getTurn();

	public boolean isTurn(ServerPlayerEntity player) {
		return player == this.getTurn();
	}

	public final void switchTurnAndPlayEffects() {
		if (this.switchTurn()) {
			this.playNextTurnEffects();
		}
	}

	public final void playNextTurnEffects() {
		Text nextTurnMessage = this.getNextTurnMessage();
		if (nextTurnMessage != null) {
			this.phase.getGameSpace().getPlayers().sendMessage(nextTurnMessage);
		}

		ServerPlayerEntity turn = this.getTurn();
		if (turn != null) {
			TurnSounds.playTurnSounds(turn);
		}
	}

	/**
	 * @return whether the new turn is different from the old turn
	 */
	public abstract boolean switchTurn();

	public final Text getNextTurnMessage() {
		ServerPlayerEntity turn = this.getTurn();
		if (turn == null) return null;

		return Text.translatable("text.codebreaker.next_turn", turn.getDisplayName()).formatted(Formatting.GOLD);
	}

	public final Text getOtherTurnMessage() {
		ServerPlayerEntity turn = this.getTurn();
		if (turn == null) {
			return Text.translatable("text.codebreaker.no_turn").formatted(Formatting.RED);
		}

		return Text.translatable("text.codebreaker.other_turn", turn.getDisplayName()).formatted(Formatting.RED);
	}
}
