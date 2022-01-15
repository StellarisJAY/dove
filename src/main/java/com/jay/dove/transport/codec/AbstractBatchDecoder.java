package com.jay.dove.transport.codec;

import com.jay.dove.exception.DecoderException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.RecyclableArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  Batch decoder
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 13:26
 */
public abstract class AbstractBatchDecoder extends ChannelInboundHandlerAdapter {

    /**
     * accumulated bytes from the last channelRead
     */
    private ByteBuf accumulation;

    private static final Accumulator MERGE_ACCUMULATOR = ((allocator, in, original) -> {
        ByteBuf buffer;
        if(original.writableBytes() < in.readableBytes()){
            // expand buffer to new size
            buffer = expandByteBuf(allocator, original, in.readableBytes());
        }else{
            buffer = original;
        }
        // write new data
        buffer.writeBytes(in);
        in.release();
        return buffer;
    });

    /**
     * read from channel
     * do batch decode here
     * @param ctx ctx
     * @param msg msg
     * @throws Exception e
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof ByteBuf){
            RecyclableArrayList out = RecyclableArrayList.newInstance();
            try{
                ByteBuf data = (ByteBuf) msg;
                if(accumulation == null){
                    accumulation = data;
                }else{
                    accumulation = MERGE_ACCUMULATOR.accumulate(ctx.alloc(), data, accumulation);
                }
                decodeBatch(ctx, accumulation, out);
            }catch (DecoderException e){
                throw e;
            }catch (Throwable e){
                throw new DecoderException(e);
            }finally {
                // release empty accumulation
                if(accumulation != null && !accumulation.isReadable()){
                    accumulation.release();
                    accumulation = null;
                }

                int size = out.size();
                if(size == 1){
                    ctx.fireChannelRead(out.get(0));
                }else if(size > 0){
                    ArrayList<Object> res = new ArrayList<>(size);
                    res.addAll(out);
                    ctx.fireChannelRead(res);
                }
                out.recycle();
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        RecyclableArrayList out = RecyclableArrayList.newInstance();
        try{
            if(accumulation != null){
                decodeBatch(ctx, accumulation, out);
                decode(ctx, accumulation, out);
            }
        }catch (DecoderException e){
            throw e;
        } catch (Exception e){
            throw new DecoderException(e);
        }finally {
            if(accumulation != null){
                accumulation.release();
            }
            int size = out.size();
            for (Object o : out) {
                ctx.fireChannelRead(o);
            }
            if(size > 0){
                ctx.fireChannelReadComplete();
            }
            ctx.fireChannelInactive();
            out.recycle();
        }
    }

    /**
     * decode all readable bytes from input buffer
     * this calls {@link #decode(ChannelHandlerContext, ByteBuf, List)}
     * @param ctx context
     * @param in input byteBuf
     * @param out output list
     * @throws DecoderException Exception
     */
    public void decodeBatch(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws DecoderException {
        try{
            while(in.isReadable()){
                // original size of List and readable bytes before decode
                int originalSize = out.size();
                int originalReaderIndex = in.readerIndex();

                // decode once
                decode(ctx, in, out);

                // nothing decoded
                if(originalSize == out.size()){
                    // nothing read
                    if(originalReaderIndex == in.readerIndex()){
                        // break to prevent endless loop
                        break;
                    }
                    else{
                        continue;
                    }
                }

                // read nothing but decode() return a result
                if(originalReaderIndex == in.readerIndex()){
                    throw new DecoderException("decode failed , weird ");
                }
            }
        }catch (DecoderException e){
            throw e;
        } catch (Throwable e){
            throw new DecoderException(e);
        }
    }

    /**
     * decode once from the input buffer
     * @param ctx context
     * @param in input byteBuf
     * @param out output list
     * @throws Exception e
     */
    public abstract void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception;

    interface Accumulator {
        /**
         * accumulate bytes from input to accumulation
         * @param allocator allocator
         * @param in input bytes
         * @param accumulation accumulations from last {@link #channelRead(ChannelHandlerContext, Object)}
         * @return ByteBuf
         */
        ByteBuf accumulate(ByteBufAllocator allocator, ByteBuf in, ByteBuf accumulation);
    }

    /**
     * expand the byteBuf
     * @param allocator {@link ByteBufAllocator}
     * @param buf buffer to expand
     * @param readable expansion size
     * @return expanded buffer
     */
    static ByteBuf expandByteBuf(ByteBufAllocator allocator, ByteBuf buf, int readable){
        ByteBuf oldBuf = buf;
        buf = allocator.buffer(oldBuf.readableBytes() + readable);
        buf.writeBytes(oldBuf);
        oldBuf.release();
        return buf;
    }
}
