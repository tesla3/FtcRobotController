package org.firstinspires.ftc.teamcode;


import android.speech.tts.TextToSpeech;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcontroller.external.samples.SensorColor;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.mechanisms.TestBenchColor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


class BoundedFifoBuffer<T> {
    private final ArrayDeque<T> deque;
    private final int maxSize;

    public BoundedFifoBuffer(int maxSize) {
        this.deque = new ArrayDeque<>(maxSize);
        this.maxSize = maxSize;
    }

    public void clear(){
        deque.clear();
    }

    public T peekLast(){
        return deque.peekLast();
    }
    public T peekFirst() { return deque.peekFirst(); }

    public Iterator<T> iterator() {
        return deque.iterator();  // Iterates oldest to newest
    }

    public void add(T element) {
        if (deque.size() >= maxSize) {
            deque.removeFirst();  // Auto-free oldest
        }
        deque.addLast(element);
    }

    // Majority vote on last N elements
    public T getMajorityVote(int n) {
        if (deque.isEmpty()) return null;

        Map<T, Integer> counts = new HashMap<>();
        Iterator<T> it = deque.descendingIterator();

        int count = 0;
        while (it.hasNext() && count < n) {
            T value = it.next();
            counts.put(value, counts.getOrDefault(value, 0) + 1);
            count++;
        }

        // Find element with max count
        T majority = null;
        int maxCount = 0;
        for (Map.Entry<T, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                majority = entry.getKey();
            }
        }

        return majority;
    }

    // Optional: reverse iteration (newest to oldest)
    public Iterator<T> descendingIterator() {
        return deque.descendingIterator();
    }


}


@TeleOp(name="TestBenchColor", group="Concept")
public class ColorSensorTest extends OpMode {
    TestBenchColor bench = new TestBenchColor();
    ElapsedTime colorSensorTimer = new ElapsedTime();
    private static final double POLL_INTERVAL_MS = 15; //ms

    private long counter = 0;
    private BoundedFifoBuffer<TestBenchColor.DetectedColor> buffer = new BoundedFifoBuffer<TestBenchColor.DetectedColor>(20);

    private BoundedFifoBuffer<TestBenchColor.DetectedColor> m_buffer = new BoundedFifoBuffer<>(20);

    private void updateTelemetry(){
        telemetry.clearAll();
        Iterator<TestBenchColor.DetectedColor> it = buffer.iterator();
        Iterator<TestBenchColor.DetectedColor> it_m = m_buffer.iterator();

        while (it.hasNext()){
            TestBenchColor.DetectedColor d = it.next();
            TestBenchColor.DetectedColor d_m = it_m.next();
            telemetry.addData("detection", d.toString()  + ":" + d_m.toString());
        }

    }

    public void init(){
        bench.init(hardwareMap);
        colorSensorTimer.reset();
        buffer.clear();
        m_buffer.clear();

        TestBenchColor.DetectedColor _init =TestBenchColor.DetectedColor.UNKNOWN;

        buffer.add(_init);
        TestBenchColor.DetectedColor majority = buffer.getMajorityVote(7);
        m_buffer.add(majority);
    }

    public void loop(){

        if (colorSensorTimer.milliseconds() > POLL_INTERVAL_MS) {
            TestBenchColor.DetectedColor detected = bench.getDetectedColor(telemetry);

            boolean d_changed = detected != buffer.peekLast();
            buffer.add(detected);
            TestBenchColor.DetectedColor majority = buffer.getMajorityVote(7);
            m_buffer.add(majority);

            if (d_changed) {updateTelemetry(); }

        }
    }
}

