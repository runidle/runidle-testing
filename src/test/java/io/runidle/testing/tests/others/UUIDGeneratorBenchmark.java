package io.runidle.testing.tests.others;

import io.runidle.testing.benchmark.AbstractMicrobenchmark;
import com.fasterxml.uuid.NoArgGenerator;
import com.fasterxml.uuid.impl.RandomBasedGenerator;
import com.fasterxml.uuid.impl.UUIDUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.AppendableCharSequence;
import org.openjdk.jmh.annotations.*;

import java.nio.charset.Charset;
import java.util.Random;
import java.util.UUID;

@Threads(4)
@Warmup(iterations = 0)
@Measurement(iterations = 5)
public class UUIDGeneratorBenchmark extends AbstractMicrobenchmark {
    @State(Scope.Benchmark)
    public static class GeneratorState {
        NoArgGenerator randomUuidGenerator = new RandomBasedGenerator(new Random());
        Charset charset = Charset.forName("UTF-8");
    }

    @Benchmark
    public void benchmarkJDKUUID1(GeneratorState generatorState) {
        UUID.randomUUID();
    }

    @Benchmark
    public void benchmarkJDKUUID2(GeneratorState generatorState) {
        UUID.randomUUID().toString();
    }

    @Benchmark
    public void benchmarkRandomUUID1(GeneratorState generatorState) {
        generatorState.randomUuidGenerator.generate();
    }

    @Benchmark
    public void benchmarkRandomUUID2(GeneratorState generatorState) {
        generatorState.randomUuidGenerator.generate().toString().getBytes();
    }

    @Benchmark
    public void benchmarkEaioUUID1(GeneratorState generatorState) {
        new com.eaio.uuid.UUID();
    }

    @Benchmark
    public void benchmarkEaioUUID2(GeneratorState generatorState) {
        new com.eaio.uuid.UUID().toAppendable(null).toString().getBytes();
    }

    @Benchmark
    public void benchmarkEaioUUID3(final GeneratorState generatorState) {
        com.eaio.uuid.UUID uuid = new com.eaio.uuid.UUID();
        AppendableCharSequence appendableCharSequence = new AppendableCharSequence(36);
        uuid.toAppendable(appendableCharSequence);
        ByteBuf byteBuf = Unpooled.buffer(36);
        byteBuf.writeCharSequence(appendableCharSequence, generatorState.charset);
        byte[] data = new byte[36];
        byteBuf.readBytes(data);
    }

    @Benchmark
    public void benchmarkFastXMLUUID1(GeneratorState generatorState) {
        com.eaio.uuid.UUID uuid = new com.eaio.uuid.UUID();
        AppendableCharSequence appendableCharSequence = new AppendableCharSequence(36);
        uuid.toAppendable(appendableCharSequence);
        UUIDUtil.uuid(appendableCharSequence.toString());
    }
}
