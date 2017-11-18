package com.ltrojanowski.rx;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.HashMap;
import java.util.Map;

public class ReplayLatestByClass<T> implements ObservableTransformer<T, T>, FlowableTransformer<T, T> {

    private static final ReplayLatestByClass<Object> INSTANCE = new ReplayLatestByClass<>();

    /** The singleton instance of this transformer **/
    @SuppressWarnings("unchecked")
    public static <T> ReplayLatestByClass<T> instance() {
        return (ReplayLatestByClass<T>) INSTANCE;
    }

    private ReplayLatestByClass() {
    }

    @Override
    public Observable<T> apply(Observable<T> upstream) {
        LastSeen<T> lastSeen = new LastSeen<>();
        return new LastSeenObservable<>(upstream.doOnNext(lastSeen).publish().autoConnect(), lastSeen);
    }

    @Override
    public Flowable<T> apply(Flowable<T> upstream) {
        LastSeen<T> lastSeen = new LastSeen<>();
        return new LastSeenFlowable<T>(upstream.doOnNext(lastSeen).publish().autoConnect(), lastSeen);
    }

    static final class LastSeen<T> implements Consumer<T> {
        Map<Class, T> values = new HashMap<>();

        @Override public void accept(T latest) {
            values.put(latest.getClass(), latest);
        }
    }

    static final class LastSeenObservable<T> extends Observable<T> {
        private final Observable<T> upstream;
        private final LastSeen<T> lastSeen;

        LastSeenObservable(Observable<T> upstream, LastSeen<T> lastSeen) {
            this.upstream = upstream;
            this.lastSeen = lastSeen;
        }

        @Override
        protected void subscribeActual(Observer<? super T> observer) {
            upstream.subscribe(new LastSeenObserver<T>(observer, lastSeen));
        }
    }

    static final class LastSeenObserver<T> implements Observer<T> {
        private final Observer<? super T> downstream;
        private final LastSeen<T> lastSeen;

        LastSeenObserver(Observer<? super T> downstream, LastSeen<T> lastSeen) {
            this.downstream = downstream;
            this.lastSeen = lastSeen;
        }

        @Override public void onSubscribe(Disposable d) {
            downstream.onSubscribe(d);

            Map<Class, T> values = lastSeen.values;
            for (T t: values.values()) {
                downstream.onNext(t);
            }
        }

        @Override public void onNext(T value) {
            downstream.onNext(value);
        }

        @Override public void onComplete() {
            downstream.onComplete();
        }

        @Override public void onError(Throwable e) {
            downstream.onError(e);
        }
    }

    static final class LastSeenFlowable<T> extends Flowable<T> {
        private final Flowable<T> upstream;
        private final LastSeen<T> lastSeen;

        LastSeenFlowable(Flowable<T> upstream, LastSeen<T> lastSeen) {
            this.upstream = upstream;
            this.lastSeen = lastSeen;
        }

        @Override
        protected void subscribeActual(Subscriber<? super T> subscriber) {
            upstream.subscribe(new LastSeenSubscriber<T>(subscriber, lastSeen));
        }
    }

    static final class LastSeenSubscriber<T> implements Subscriber<T>, Subscription {
        private final Subscriber<? super T> downstream;
        private final LastSeen<T> lastSeen;

        private Subscription subscription;
        private boolean first = true;

        LastSeenSubscriber(Subscriber<? super T> downstream, LastSeen<T> lastSeen) {
            this.downstream = downstream;
            this.lastSeen = lastSeen;
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
            downstream.onSubscribe(this);
        }

        @Override
        public void onNext(T t) {
            downstream.onNext(t);
        }

        @Override
        public void onError(Throwable throwable) {
            downstream.onError(throwable);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }

        @Override
        public void request(long amount) {
            if (amount < lastSeen.values.size())
                return;

            if (first) {
                first = false;
                Map<Class, T> values = lastSeen.values;
                for (T t: values.values()) {
                    downstream.onNext(t);
                }
            }
            subscription.request(amount);
        }

        @Override
        public void cancel() {
            subscription.cancel();
        }
    }

}
