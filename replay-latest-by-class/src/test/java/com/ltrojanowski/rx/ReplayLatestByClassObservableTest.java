package com.ltrojanowski.rx;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class ReplayLatestByClassObservableTest {

    @Test
    public void noInitialValue() {
        PublishSubject<SealAB> subject = PublishSubject.create();

        Observable<SealAB> observable = subject.compose(ReplayLatestByClass.<SealAB>instance());

        TestObserver<SealAB> observer = new TestObserver<>();
        observable.subscribe(observer);
        observer.assertNoValues();
    }

    @Test
    public void initialValueToNewSubscriber() {
        PublishSubject<SealAB> subject = PublishSubject.create();
        Observable<SealAB> observable = subject.compose(ReplayLatestByClass.<SealAB>instance());

        TestObserver<SealAB> observer1 = new TestObserver<>();
        observable.subscribe(observer1);
        observer1.assertNoValues();

        A a1 = new A(1);
        B b1 = new B("1");
        Set<SealAB> expectedValueSet = new HashSet<SealAB>();
        expectedValueSet.add(a1);
        expectedValueSet.add(b1);
        subject.onNext(a1);
        subject.onNext(b1);
        observer1.assertValueSet(expectedValueSet);

        TestObserver<SealAB> observer2 = new TestObserver<>();
        observable.subscribe(observer2);
        observer2.assertValueSet(expectedValueSet);
    }

    @Test
    public void correctReplacementOfSubtype() {
        PublishSubject<SealAB> subject = PublishSubject.create();
        Observable<SealAB> observable = subject.compose(ReplayLatestByClass.<SealAB>instance());

        TestObserver<SealAB> observer1 = new TestObserver<>();
        observable.subscribe(observer1);
        observer1.assertNoValues();

        A a1 = new A(1);
        subject.onNext(a1);
        observer1.assertValue(a1);
        B b1 = new B("1");
        subject.onNext(b1);
        observer1.onNext(b1);
        A a2 = new A(2);
        subject.onNext(a2);
        observer1.onNext(a2);
        Set<SealAB> expectedValueSet = new HashSet<SealAB>();
        expectedValueSet.add(b1);
        expectedValueSet.add(a2);

        TestObserver<SealAB> observer2 = new TestObserver<>();
        observable.subscribe(observer2);
        observer2.assertValueSet(expectedValueSet);
    }

    @Test
    public void checkNoSubscribersBehaviour() {
        PublishSubject<SealAB> subject = PublishSubject.create();
        Observable<SealAB> observable = subject.compose(ReplayLatestByClass.<SealAB>instance());

        TestObserver<SealAB> observer1 = new TestObserver<>();
        observable.subscribe(observer1);
        observer1.assertNoValues();

        observer1.dispose();
        A a1 = new A(1);
        subject.onNext(a1);
        A a2 = new A(2);
        subject.onNext(a2);

        TestObserver<SealAB> observer2 = new TestObserver<>();
        observable.subscribe(observer2);
        observer2.assertValue(a2);
    }
}
