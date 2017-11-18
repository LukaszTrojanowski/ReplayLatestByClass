package com.ltrojanowski.rx;

import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class ReplayLatestByClassFlowableTest {

    @Test
    public void noInitialValue() {
        PublishProcessor<SealAB> subject = PublishProcessor.create();
        Flowable<SealAB> flowable = subject.compose(ReplayLatestByClass.<SealAB>instance());

        TestSubscriber<SealAB> subscriber = new TestSubscriber<>();
        flowable.subscribe(subscriber);
        subscriber.assertNoValues();
    }

    @Test
    public void initialValueToNewSubscriber() {
        PublishProcessor<SealAB> subject = PublishProcessor.create();
        Flowable<SealAB> flowable = subject.compose(ReplayLatestByClass.<SealAB>instance());

        TestSubscriber<SealAB> subscriber1 = new TestSubscriber<>();
        flowable.subscribe(subscriber1);
        subscriber1.assertNoValues();

        A a1 = new A(1);
        B b1 = new B("1");
        Set<SealAB> expectedValueSet = new HashSet<SealAB>();
        expectedValueSet.add(a1);
        expectedValueSet.add(b1);
        subject.onNext(a1);
        subject.onNext(b1);
        subscriber1.assertValueSet(expectedValueSet);

        TestSubscriber<SealAB> subscriber2 = new TestSubscriber<>();
        flowable.subscribe(subscriber2);
        subscriber2.assertValueSet(expectedValueSet);
    }

    @Test
    public void correctReplacementOfSubtype() {
        PublishProcessor<SealAB> subject = PublishProcessor.create();
        Flowable<SealAB> flowable = subject.compose(ReplayLatestByClass.<SealAB>instance());

        TestSubscriber<SealAB> subscriber1 = new TestSubscriber<>();
        flowable.subscribe(subscriber1);
        subscriber1.assertNoValues();

        A a1 = new A(1);
        subject.onNext(a1);
        subscriber1.assertValue(a1);
        B b1 = new B("1");
        subject.onNext(b1);
        subscriber1.onNext(b1);
        A a2 = new A(2);
        subject.onNext(a2);
        subscriber1.onNext(a2);
        Set<SealAB> expectedValueSet = new HashSet<SealAB>();
        expectedValueSet.add(b1);
        expectedValueSet.add(a2);

        TestSubscriber<SealAB> subscriber2 = new TestSubscriber<>();
        flowable.subscribe(subscriber2);
        subscriber2.assertValueSet(expectedValueSet);
    }

    @Test
    public void checkNoSubscribersBehaviour() {
        PublishProcessor<SealAB> subject = PublishProcessor.create();
        Flowable<SealAB> flowable = subject.compose(ReplayLatestByClass.<SealAB>instance());

        TestSubscriber<SealAB> subscriber1 = new TestSubscriber<>();
        flowable.subscribe(subscriber1);
        subscriber1.assertNoValues();

        subscriber1.dispose();
        A a1 = new A(1);
        subject.onNext(a1);
        A a2 = new A(2);
        subject.onNext(a2);

        TestSubscriber<SealAB> subscriber2 = new TestSubscriber<>();
        flowable.subscribe(subscriber2);
        subscriber2.assertValue(a2);
    }

}
