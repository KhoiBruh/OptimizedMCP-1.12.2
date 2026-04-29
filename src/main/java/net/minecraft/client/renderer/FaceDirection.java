package net.minecraft.client.renderer;

import net.minecraft.util.Facing;

public enum FaceDirection {
	DOWN(new FaceDirection.VertexInformation[]{new FaceDirection.VertexInformation(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.SOUTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.NORTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.NORTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.SOUTH_INDEX)}),
	UP(new FaceDirection.VertexInformation[]{new FaceDirection.VertexInformation(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.NORTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.SOUTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.SOUTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.NORTH_INDEX)}),
	NORTH(new FaceDirection.VertexInformation[]{new FaceDirection.VertexInformation(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.NORTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.NORTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.NORTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.NORTH_INDEX)}),
	SOUTH(new FaceDirection.VertexInformation[]{new FaceDirection.VertexInformation(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.SOUTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.SOUTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.SOUTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.SOUTH_INDEX)}),
	WEST(new FaceDirection.VertexInformation[]{new FaceDirection.VertexInformation(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.NORTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.NORTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.SOUTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.WEST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.SOUTH_INDEX)}),
	EAST(new FaceDirection.VertexInformation[]{new FaceDirection.VertexInformation(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.SOUTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.SOUTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.DOWN_INDEX, FaceDirection.Constants.NORTH_INDEX), new FaceDirection.VertexInformation(FaceDirection.Constants.EAST_INDEX, FaceDirection.Constants.UP_INDEX, FaceDirection.Constants.NORTH_INDEX)});

	private static final FaceDirection[] FACINGS = new FaceDirection[6];

	static {
		FACINGS[FaceDirection.Constants.DOWN_INDEX] = DOWN;
		FACINGS[FaceDirection.Constants.UP_INDEX] = UP;
		FACINGS[FaceDirection.Constants.NORTH_INDEX] = NORTH;
		FACINGS[FaceDirection.Constants.SOUTH_INDEX] = SOUTH;
		FACINGS[FaceDirection.Constants.WEST_INDEX] = WEST;
		FACINGS[FaceDirection.Constants.EAST_INDEX] = EAST;
	}

	private final FaceDirection.VertexInformation[] vertexInfos;

	FaceDirection(FaceDirection.VertexInformation[] vertexInfosIn) {

		vertexInfos = vertexInfosIn;
	}

	public static FaceDirection getFacing(Facing facing) {

		return FACINGS[facing.getIndex()];
	}

	public FaceDirection.VertexInformation getVertexInformation(int index) {

		return vertexInfos[index];
	}

	public static final class Constants {

		public static final int SOUTH_INDEX = Facing.SOUTH.getIndex();
		public static final int UP_INDEX = Facing.UP.getIndex();
		public static final int EAST_INDEX = Facing.EAST.getIndex();
		public static final int NORTH_INDEX = Facing.NORTH.getIndex();
		public static final int DOWN_INDEX = Facing.DOWN.getIndex();
		public static final int WEST_INDEX = Facing.WEST.getIndex();

	}

	public static class VertexInformation {

		public final int xIndex;
		public final int yIndex;
		public final int zIndex;

		private VertexInformation(int xIndexIn, int yIndexIn, int zIndexIn) {

			xIndex = xIndexIn;
			yIndex = yIndexIn;
			zIndex = zIndexIn;
		}

	}
}
