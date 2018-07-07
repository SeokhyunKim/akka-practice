package io.akka_practice.cluster;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class SimpleClusterListener extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final Cluster cluster = Cluster.get(getContext().system());

    public static Props props() {
        return Props.create(SimpleClusterListener.class, () -> new SimpleClusterListener());
    }

    @Override
    public void preStart() {
        cluster.subscribe(self(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class);
    }

    @Override
    public void postStop() {
        cluster.unsubscribe(self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                       .match(MemberUp.class, mUp -> log.info("Member is up: {}", mUp.member()))
                       .match(UnreachableMember.class,
                              mUnreachable -> log.info("Member detected as unreachable: {}", mUnreachable.member()))
                       .match(MemberRemoved.class, mRemoved -> log.info("Member is removed: {}", mRemoved.member()))
                       .match(MemberEvent.class, msg -> {}) // ignore message
                       .build();
    }


}
