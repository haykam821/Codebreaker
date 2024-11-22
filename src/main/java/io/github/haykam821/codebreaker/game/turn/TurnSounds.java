package io.github.haykam821.codebreaker.game.turn;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public final class TurnSounds {
	private static final SoundEvent TURN_SOUND = SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value();
	private static final SoundCategory TURN_SOUND_CATEGORY = SoundCategory.PLAYERS;

	private static final float TURN_SOUND_VOLUME = 0.3f;

	private static final float TURN_SOUND_BASE_PITCH = 1.2f;
	private static final float TURN_SOUND_PITCH_STEP = 0.3f;

	private TurnSounds() {
		return;
	}

	private static void playTurnSound(ServerPlayerEntity player, float index) {
		float pitch = TURN_SOUND_BASE_PITCH + TURN_SOUND_PITCH_STEP * index;
		player.playSoundToPlayer(TURN_SOUND, TURN_SOUND_CATEGORY, TURN_SOUND_VOLUME, pitch);
	}

	protected static void playTurnSounds(ServerPlayerEntity player) {
		if (player == null) {
			return;
		}

		TurnSounds.playTurnSound(player, 0);
		TurnSounds.playTurnSound(player, 1);
		TurnSounds.playTurnSound(player, 2);
	}
}
