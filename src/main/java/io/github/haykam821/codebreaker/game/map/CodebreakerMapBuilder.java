package io.github.haykam821.codebreaker.game.map;

import java.util.Random;

import io.github.haykam821.codebreaker.Main;
import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class CodebreakerMapBuilder {
	private static final BlockPos ORIGIN = new BlockPos(0, 64, 0);
	private static final int FLOOR_WIDTH = 18;

	private final CodebreakerConfig config;

	public CodebreakerMapBuilder(CodebreakerConfig config) {
		this.config = config;
	}

	public CodebreakerMap create() {
		MapTemplate template = MapTemplate.createEmpty();

		CodebreakerMapConfig mapConfig = this.config.getMapConfig();
		Random random = new Random();

		// Board
		BlockPos boardOrigin = ORIGIN.add(1, 1, 1);
		BlockBounds boardBounds = new BlockBounds(boardOrigin, boardOrigin.add(this.config.getChances() + 1, this.config.getSpaces() * 2, 0));
		for (BlockPos pos : boardBounds) {
			template.setBlockState(pos, mapConfig.getBoardProvider().getBlockState(random, pos));
		}

		BlockPos codeOrigin = boardOrigin.add(1, this.config.getSpaces() * 2 - 1, 0);
		template.setBlockState(codeOrigin, Blocks.EMERALD_BLOCK.getDefaultState());

		// Floor
		BlockPos floorOrigin = ORIGIN.add(0, 0, 0);
		BlockBounds floorBounds = new BlockBounds(floorOrigin, floorOrigin.add(this.config.getChances() + 3, 0, FLOOR_WIDTH - 1));
		for (BlockPos pos : floorBounds) {
			template.setBlockState(pos, mapConfig.getFloorProvider().getBlockState(random, pos));
		}

		// Pegs
		int pegs = Main.CODE_PEGS.values().size();
		BlockPos pegOrigin = ORIGIN.add(1, 0, 6);
		BlockBounds pegBounds = new BlockBounds(pegOrigin, pegOrigin.add(pegs, 0, 0));
		int pegIndex = 0;
		for (BlockPos pos : pegBounds) {
			if (pegIndex < pegs) {
				template.setBlockState(pos, Main.CODE_PEGS.values().get(pegIndex).getDefaultState());
			} else {
				template.setBlockState(pos, Blocks.BEDROCK.getDefaultState());
			}
			pegIndex += 1;
		}

		return new CodebreakerMap(template, codeOrigin, floorBounds);
	}
}