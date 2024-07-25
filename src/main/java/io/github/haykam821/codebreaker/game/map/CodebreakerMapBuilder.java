package io.github.haykam821.codebreaker.game.map;

import io.github.haykam821.codebreaker.Main;
import io.github.haykam821.codebreaker.block.CodeControlBlock;
import io.github.haykam821.codebreaker.block.CodeControlBlockEntity;
import io.github.haykam821.codebreaker.game.CodebreakerConfig;
import io.github.haykam821.codebreaker.game.code.Code;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;

public class CodebreakerMapBuilder {
	private static final BlockPos ORIGIN = new BlockPos(0, 64, 0);

	private static final int FLOOR_PADDING_X = 1;
	private static final int FLOOR_PADDING_Z = 1;

	private static final int BOARD_PADDING_X = 1;
	private static final int BOARD_PADDING_TOP = 1;
	private static final int BOARD_PADDING_BOTTOM = 0;

	private static final int CODE_CONTROL_BOARD_PADDING_Z = 4;
	private static final int CODE_CONTROL_END_PADDING_Z = 11;

	private static final int FLOOR_HEIGHT = 1;
	private static final int FLOOR_WIDTH_Z = FLOOR_PADDING_Z + CODE_CONTROL_BOARD_PADDING_Z + CODE_CONTROL_END_PADDING_Z + 2;

	private static final BlockState CODE_ORIGIN = Blocks.EMERALD_BLOCK.getDefaultState();

	private static final BlockState CODE_CONTROL = Main.CODE_CONTROL.getDefaultState().with(CodeControlBlock.FACING, Direction.SOUTH);
	private static final BlockState BEDROCK = Blocks.BEDROCK.getDefaultState();

	private final CodebreakerConfig config;

	public CodebreakerMapBuilder(CodebreakerConfig config) {
		this.config = config;
	}

	public CodebreakerMap create(Random random, Code correctCode, RegistryEntryList<Block> codePegs) {
		MapTemplate template = MapTemplate.createEmpty();

		CodebreakerMapConfig mapConfig = this.config.getMapConfig();

		int spaces = correctCode.getLength();
		int codeControls = codePegs.size() + 1;

		int boardWidth = this.config.getChances() + (BOARD_PADDING_X * 2);
		int floorWidthX = Math.max(boardWidth, codeControls) + (FLOOR_PADDING_X * 2);

		// Floor
		BlockPos floorOrigin = ORIGIN;
		BlockBounds floorBounds = createBounds(floorOrigin, floorWidthX, -FLOOR_HEIGHT + 2, FLOOR_WIDTH_Z);

		fillBounds(template, random, floorBounds, mapConfig.getFloorProvider());

		// Board
		int boardStartX = (floorWidthX - boardWidth) / 2;
		int boardHeight = spaces * 2 + BOARD_PADDING_TOP + BOARD_PADDING_BOTTOM;

		BlockPos boardOrigin = floorOrigin.add(boardStartX, 1, FLOOR_PADDING_Z);
		BlockBounds boardBounds = createBounds(boardOrigin, boardWidth, boardHeight, 1);

		fillBounds(template, random, boardBounds, mapConfig.getBoardProvider());

		int codeStartY = spaces * 2 + BOARD_PADDING_BOTTOM - 1;
		BlockPos codeOrigin = boardOrigin.add(BOARD_PADDING_X, codeStartY, 0);

		template.setBlockState(codeOrigin, CODE_ORIGIN);

		// Code controls
		int codeControlsStartX = (floorWidthX - codeControls) / 2;

		BlockPos codeControlOrigin = floorOrigin.add(codeControlsStartX, 1, FLOOR_PADDING_Z + 1 + CODE_CONTROL_BOARD_PADDING_Z);
		BlockBounds codeControlBounds = createBounds(codeControlOrigin, codeControls, 1, 1);

		int codeControlIndex = 0;

		for (BlockPos pos : codeControlBounds) {
			CodeControlBlockEntity blockEntity = new CodeControlBlockEntity(pos, CODE_CONTROL);
			blockEntity.setBlock(getCodeControlBlock(codePegs, codeControlIndex));

			template.setBlockState(pos, CODE_CONTROL);
			template.setBlockEntity(pos, blockEntity);

			codeControlIndex += 1;
		}

		return new CodebreakerMap(template, codeOrigin, floorBounds);
	}

	private static BlockBounds createBounds(BlockPos origin, int widthX, int height, int widthZ) {
		return BlockBounds.of(origin, origin.add(widthX - 1, height - 1, widthZ - 1));
	}

	private static void fillBounds(MapTemplate template, Random random, BlockBounds bounds, BlockStateProvider provider) {
		for (BlockPos pos : bounds) {
			template.setBlockState(pos, provider.get(random, pos));
		}
	}

	private static BlockState getCodeControlBlock(RegistryEntryList<Block> codePegs, int index) {
		if (index < codePegs.size()) {
			return codePegs.get(index).value().getDefaultState();
		}

		return BEDROCK;
	}
}