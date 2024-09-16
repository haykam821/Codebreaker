package io.github.haykam821.codebreaker.game.code;

import io.github.haykam821.codebreaker.game.map.CodebreakerMapConfig;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class Code {
	private final BlockState[] pegs;

	public Code(int spaces) {
		this.pegs = new BlockState[spaces];
	}

	public Code(BlockState[] pegs) {
		this.pegs = pegs;
	}

	public BlockState[] getPegs() {
		return this.pegs;
	}

	public void setPeg(int index, BlockState state) {
		this.pegs[index] = state;
	}

	public int getLength() {
		return this.pegs.length;
	}

	/**
	 * @return whether the inserted peg would be a duplicate and duplicate pegs are not allowed
	 */
	public boolean setNext(BlockState state, boolean allowDuplicatePegs) {
		for (int index = 0; index < this.getLength(); index++) {
			BlockState peg = this.pegs[index];

			if (peg == null) {
				this.pegs[index] = state;
				return true;
			} else if (peg == state && !allowDuplicatePegs) {
				return false;
			}
		}

		return true;
	}

	public boolean isCompletelyFilled() {
		for (int index = 0; index < this.getLength(); index++) {
			if (this.pegs[index] == null) {
				return false;
			}
		}
		return true;
	}

	public void build(WorldAccess world, BlockPos originPos, CodebreakerMapConfig mapConfig) {
		BlockPos.Mutable pos = originPos.mutableCopy();
		for (int index = 0; index < this.getLength(); index++) {
			BlockState state = this.pegs[index];
			world.setBlockState(pos, state == null ? mapConfig.getBoardProvider().get(world.getRandom(), pos) : state, 3);

			pos.move(Direction.DOWN);
		}
	}

	@Override
	public String toString() {
		String string = "Code{pegs=";
		for (int index = 0; index < this.getLength(); index++) {
			BlockState state = this.pegs[index];
			string += state == null ? "<empty>" : state.toString();

			if (index + 1 < this.getLength()) {
				string += ", ";
			}
		}
		return string + "}";
	}
}