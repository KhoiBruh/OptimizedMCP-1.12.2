package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;

public class ChunkRenderDispatcher {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final ThreadFactory THREAD_FACTORY = (new ThreadFactoryBuilder()).setNameFormat("Chunk Batcher %d").setDaemon(true).build();
	private final int countRenderBuilders;
	private final List<Thread> listWorkerThreads = Lists.newArrayList();
	private final List<ChunkRenderWorker> listThreadedWorkers = Lists.newArrayList();
	private final PriorityBlockingQueue<ChunkCompileTaskGenerator> queueChunkUpdates = Queues.newPriorityBlockingQueue();
	private final BlockingQueue<RegionRenderCacheBuilder> queueFreeRenderBuilders;
	private final WorldVertexBufferUploader worldVertexUploader = new WorldVertexBufferUploader();
	private final VertexBufferUploader vertexUploader = new VertexBufferUploader();
	private final Queue<ChunkRenderDispatcher.PendingUpload> queueChunkUploads = Queues.newPriorityQueue();
	private final ChunkRenderWorker renderWorker;

	public ChunkRenderDispatcher() {

		int i = Math.max(1, (int) ((double) Runtime.getRuntime().maxMemory() * 0.3D) / 10485760);
		int j = Math.max(1, MathHelper.clamp(Runtime.getRuntime().availableProcessors(), 1, i / 5));
		countRenderBuilders = MathHelper.clamp(j * 10, 1, i);

		if (j > 1) {
			for (int k = 0; k < j; ++k) {
				ChunkRenderWorker chunkrenderworker = new ChunkRenderWorker(this);
				Thread thread = THREAD_FACTORY.newThread(chunkrenderworker);
				thread.start();
				listThreadedWorkers.add(chunkrenderworker);
				listWorkerThreads.add(thread);
			}
		}

		queueFreeRenderBuilders = Queues.newArrayBlockingQueue(countRenderBuilders);

		for (int l = 0; l < countRenderBuilders; ++l) {
			queueFreeRenderBuilders.add(new RegionRenderCacheBuilder());
		}

		renderWorker = new ChunkRenderWorker(this, new RegionRenderCacheBuilder());
	}

	public String getDebugInfo() {

		return listWorkerThreads.isEmpty() ? String.format("pC: %03d, single-threaded", queueChunkUpdates.size()) : String.format("pC: %03d, pU: %1d, aB: %1d", queueChunkUpdates.size(), queueChunkUploads.size(), queueFreeRenderBuilders.size());
	}

	public boolean runChunkUploads(long finishTimeNano) {

		boolean flag = false;

		while (true) {
			boolean flag1 = false;

			if (listWorkerThreads.isEmpty()) {
				ChunkCompileTaskGenerator chunkcompiletaskgenerator = queueChunkUpdates.poll();

				if (chunkcompiletaskgenerator != null) {
					try {
						renderWorker.processTask(chunkcompiletaskgenerator);
						flag1 = true;
					} catch (InterruptedException var8) {
						LOGGER.warn("Skipped task due to interrupt");
					}
				}
			}

			synchronized (queueChunkUploads) {
				if (!queueChunkUploads.isEmpty()) {
					(queueChunkUploads.poll()).uploadTask.run();
					flag1 = true;
					flag = true;
				}
			}

			if (finishTimeNano == 0L || !flag1 || finishTimeNano < System.nanoTime()) {
				break;
			}
		}

		return flag;
	}

	public boolean updateChunkLater(RenderChunk chunkRenderer) {

		chunkRenderer.getLockCompileTask().lock();
		boolean flag1;

		try {
			final ChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskChunk();
			chunkcompiletaskgenerator.addFinishRunnable(() -> queueChunkUpdates.remove(chunkcompiletaskgenerator));
			boolean flag = queueChunkUpdates.offer(chunkcompiletaskgenerator);

			if (!flag) {
				chunkcompiletaskgenerator.finish();
			}

			flag1 = flag;
		} finally {
			chunkRenderer.getLockCompileTask().unlock();
		}

		return flag1;
	}

	public boolean updateChunkNow(RenderChunk chunkRenderer) {

		chunkRenderer.getLockCompileTask().lock();
		boolean flag;

		try {
			ChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskChunk();

			try {
				renderWorker.processTask(chunkcompiletaskgenerator);
			} catch (InterruptedException var7) {
			}

			flag = true;
		} finally {
			chunkRenderer.getLockCompileTask().unlock();
		}

		return flag;
	}

	public void stopChunkUpdates() {

		clearChunkUpdates();
		List<RegionRenderCacheBuilder> list = Lists.newArrayList();

		while (list.size() != countRenderBuilders) {
			runChunkUploads(Long.MAX_VALUE);

			try {
				list.add(allocateRenderBuilder());
			} catch (InterruptedException var3) {
			}
		}

		queueFreeRenderBuilders.addAll(list);
	}

	public void freeRenderBuilder(RegionRenderCacheBuilder p_178512_1_) {

		queueFreeRenderBuilders.add(p_178512_1_);
	}

	public RegionRenderCacheBuilder allocateRenderBuilder() throws InterruptedException {

		return queueFreeRenderBuilders.take();
	}

	public ChunkCompileTaskGenerator getNextChunkUpdate() throws InterruptedException {

		return queueChunkUpdates.take();
	}

	public boolean updateTransparencyLater(RenderChunk chunkRenderer) {

		chunkRenderer.getLockCompileTask().lock();
		boolean flag;

		try {
			final ChunkCompileTaskGenerator chunkcompiletaskgenerator = chunkRenderer.makeCompileTaskTransparency();

			if (chunkcompiletaskgenerator == null) {
				flag = true;
				return flag;
			}

			chunkcompiletaskgenerator.addFinishRunnable(() -> queueChunkUpdates.remove(chunkcompiletaskgenerator));
			flag = queueChunkUpdates.offer(chunkcompiletaskgenerator);
		} finally {
			chunkRenderer.getLockCompileTask().unlock();
		}

		return flag;
	}

	public ListenableFuture<Object> uploadChunk(final BlockRenderLayer p_188245_1_, final BufferBuilder p_188245_2_, final RenderChunk p_188245_3_, final CompiledChunk p_188245_4_, final double p_188245_5_) {

		if (Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
			if (OpenGlHelper.useVbo()) {
				uploadVertexBuffer(p_188245_2_, p_188245_3_.getVertexBufferByLayer(p_188245_1_.ordinal()));
			} else {
				uploadDisplayList(p_188245_2_, ((ListedRenderChunk) p_188245_3_).getDisplayList(p_188245_1_, p_188245_4_), p_188245_3_);
			}

			p_188245_2_.setTranslation(0.0D, 0.0D, 0.0D);
			return Futures.immediateFuture(null);
		} else {
			ListenableFutureTask<Object> listenablefuturetask = ListenableFutureTask.create(() -> uploadChunk(p_188245_1_, p_188245_2_, p_188245_3_, p_188245_4_, p_188245_5_), null);

			synchronized (queueChunkUploads) {
				queueChunkUploads.add(new ChunkRenderDispatcher.PendingUpload(listenablefuturetask, p_188245_5_));
				return listenablefuturetask;
			}
		}
	}

	private void uploadDisplayList(BufferBuilder bufferBuilderIn, int list, RenderChunk chunkRenderer) {

		GlStateManager.glNewList(list, 4864);
		GlStateManager.pushMatrix();
		chunkRenderer.multModelviewMatrix();
		worldVertexUploader.draw(bufferBuilderIn);
		GlStateManager.popMatrix();
		GlStateManager.glEndList();
	}

	private void uploadVertexBuffer(BufferBuilder p_178506_1_, VertexBuffer vertexBufferIn) {

		vertexUploader.setVertexBuffer(vertexBufferIn);
		vertexUploader.draw(p_178506_1_);
	}

	public void clearChunkUpdates() {

		while (!queueChunkUpdates.isEmpty()) {
			ChunkCompileTaskGenerator chunkcompiletaskgenerator = queueChunkUpdates.poll();

			if (chunkcompiletaskgenerator != null) {
				chunkcompiletaskgenerator.finish();
			}
		}
	}

	public boolean hasChunkUpdates() {

		return queueChunkUpdates.isEmpty() && queueChunkUploads.isEmpty();
	}

	public void stopWorkerThreads() {

		clearChunkUpdates();

		for (ChunkRenderWorker chunkrenderworker : listThreadedWorkers) {
			chunkrenderworker.notifyToStop();
		}

		for (Thread thread : listWorkerThreads) {
			try {
				thread.interrupt();
				thread.join();
			} catch (InterruptedException interruptedexception) {
				LOGGER.warn("Interrupted whilst waiting for worker to die", interruptedexception);
			}
		}

		queueFreeRenderBuilders.clear();
	}

	public boolean hasNoFreeRenderBuilders() {

		return queueFreeRenderBuilders.isEmpty();
	}

	class PendingUpload implements Comparable<ChunkRenderDispatcher.PendingUpload> {

		private final ListenableFutureTask<Object> uploadTask;
		private final double distanceSq;

		public PendingUpload(ListenableFutureTask<Object> uploadTaskIn, double distanceSqIn) {

			uploadTask = uploadTaskIn;
			distanceSq = distanceSqIn;
		}

		public int compareTo(ChunkRenderDispatcher.PendingUpload p_compareTo_1_) {

			return Doubles.compare(distanceSq, p_compareTo_1_.distanceSq);
		}

	}

}
