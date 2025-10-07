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

class SensorDetected{
    long counter;
    TestBenchColor.DetectedColor detected;

    public SensorDetected(long _counter, TestBenchColor.DetectedColor _detected){
        counter = _counter;
        detected = _detected;
    }
    @Override
    public String toString(){
        return String.format("%05d:", counter) + detected + ",";
    }

    @Override
    public boolean equals(Object o){
        SensorDetected _o = (SensorDetected) o;
        return detected == _o.detected;
    }

    @Override
    public int hashCode(){
        return detected.ordinal();
    }
}

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
    public void add(T element) {
        if (deque.size() >= maxSize) {
            deque.removeFirst();  // Auto-free oldest
        }
        deque.addLast(element);
    }

    public T peekLast(){
        return deque.peekLast();
    }
    public T getOldest() { return deque.peekFirst(); }
    public T getNewest() { return deque.peekLast(); }
    public int size() { return deque.size(); }


    public Iterator<T> iterator() {
        return deque.iterator();  // Iterates oldest to newest
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
    private BoundedFifoBuffer<SensorDetected> buffer = new BoundedFifoBuffer<SensorDetected>(20);

    private BoundedFifoBuffer<SensorDetected> m_buffer = new BoundedFifoBuffer<SensorDetected>(20);

    private void updateTelemetry(){
        telemetry.clearAll();
        Iterator<SensorDetected> it = buffer.iterator();
        Iterator<SensorDetected> it_m = m_buffer.iterator();

        while (it.hasNext()){
            SensorDetected d = it.next();
            SensorDetected d_m = it_m.next();
            telemetry.addData("detection", d.toString()  + ":" + d_m.toString());
        }

    }

    public void init(){
        bench.init(hardwareMap);
        colorSensorTimer.reset();
        counter = 0;
        buffer.clear();
        m_buffer.clear();

        SensorDetected _init = new SensorDetected(0, TestBenchColor.DetectedColor.UNKNOWN);

        buffer.add(_init);
        SensorDetected majority = buffer.getMajorityVote(5);
        m_buffer.add(majority);
    }

    public void loop(){

        if (colorSensorTimer.milliseconds() > POLL_INTERVAL_MS) {
            TestBenchColor.DetectedColor detected = bench.getDetectedColor(telemetry);

            SensorDetected _new = new SensorDetected(counter, detected);
            boolean d_changed = _new.detected != buffer.peekLast().detected;
            buffer.add(_new);

            SensorDetected majority = buffer.getMajorityVote(7);
            boolean d_m_changed = majority.detected != m_buffer.peekLast().detected;

            m_buffer.add(majority);
            if (d_changed || d_m_changed) {
                updateTelemetry();
            }
            counter++;
        }
    }
}

